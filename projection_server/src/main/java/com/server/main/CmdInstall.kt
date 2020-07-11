package com.server.main

import java.io.*

/**
 * Created by kuanghaochuan on 2017/7/13.
 */

private var isUnix = true

private var ADB_PATH = "${getFilePath()}/linux/adb"

private val adbFileForLinux: String = "${getFilePath()}/linux/adb"

private val adbFileForWin: String = "${getFilePath()}\\windows\\adb.exe"

private val adbFileForMacOS: String = "${getFilePath()}/macos/adb"

private val apk: String = "${getFilePath()}/apk/PhoneClient.apk"

private val apkForWin: String = "${getFilePath()}\\apk\\PhoneClient.apk"

private fun getFilePath() = File("").absolutePath

fun installDex() {
    checkPlatform()
    execAdbPushCommand()
    execAdbForwardCommand()
    execAppProcessCommand()
}

private fun execAdbPushCommand() {
    val currentApk: String?
    println("-----> adb shell push command start <------ ")
    try {
        val process: Process
        if (isUnix) {
            currentApk = apk
            process = Runtime.getRuntime().exec("sh")
        } else {
            currentApk = apkForWin
            process = Runtime.getRuntime().exec("cmd")
        }

        val outputStream = BufferedWriter(OutputStreamWriter(process.outputStream))

        outputStream.write("$ADB_PATH push $currentApk /sdcard/PhoneClient.apk")
        outputStream.write("\n")
        outputStream.write("exit\n")
        outputStream.flush()

        process.waitFor()
        readExecCommandResult(process.inputStream)
        println("-----> adb shell push command end <------")
    } catch (e: Exception) {
        e.printStackTrace()
    }

}

private fun checkPlatform() {
    val os = System.getProperty("os.name")
    println("os system is $os")

    when {
        os.toLowerCase().contains("linux") -> ADB_PATH = adbFileForLinux
        os.toLowerCase().contains("win") -> {
            isUnix = false
            ADB_PATH = adbFileForWin
        }
        os.toLowerCase().contains("mac") -> ADB_PATH = adbFileForMacOS
    }
}

/**
 * 将电脑的3000端口进行转发
 * 与手机端的localabstract：wisescreenshot进行通信
 */
private fun execAdbForwardCommand() {
    println("-----> adb shell forward command start <------")
    try {
        val process: Process = if (isUnix) {
            Runtime.getRuntime().exec("sh")
        } else {
            Runtime.getRuntime().exec("cmd")
        }

        val outputStream = BufferedWriter(OutputStreamWriter(process.outputStream))

        outputStream.write("$ADB_PATH forward tcp:3000 localabstract:wisescreenshot")
        outputStream.write("\n")
        outputStream.write("exit\n")
        outputStream.flush()

        process.waitFor()
        readExecCommandResult(process.inputStream)
        println("-----> adb shell forward command end <------")
    } catch (e: Exception) {
        e.printStackTrace()
    }

}

/**
 * 将生成的apk推送到手机端并进行Main方法的启动
 */
private fun execAppProcessCommand() {
    val findApkCmd = "export CLASSPATH=/sdcard/PhoneClient.apk"
    val startApkCmd = "exec app_process /sdcard com.kuang.screenshot.PhoneClient"

    val commands: Array<String>

    commands = arrayOf(findApkCmd, startApkCmd)

    try {
        val process: Process = if (isUnix) {
            Runtime.getRuntime().exec("$ADB_PATH shell")
        } else {
            Runtime.getRuntime().exec("cmd /c $ADB_PATH shell")
        }

        val bufferedWriter = BufferedWriter(OutputStreamWriter(process.outputStream))

        for (cmd in commands) {
            bufferedWriter.write(cmd)
            bufferedWriter.write("\n")
        }
        bufferedWriter.flush()

        readError(process.errorStream)
        readExecCommandResult(process.inputStream)
    } catch (e: IOException) {
        e.printStackTrace()
    }

}

private fun readError(errorStream: InputStream) {
    object : Thread() {
        override fun run() {
            super.run()
            readExecCommandResult(errorStream)
        }
    }.start()
}

private fun readExecCommandResult(stream: InputStream) {
    try {
        var line: String?
        val reader = BufferedReader(InputStreamReader(stream))
        while (true) {
            line = reader.readLine()
            if (line != null) {
                println(line)
            } else {
                break
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        try {
            stream.close()
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
    }
}

fun execExitAppProcessCommand() {
    val pid = findAppProcessPid()
    if (pid != null) {
        println("-----> adb shell kill command start <------")
        try {
            val process: Process = if (isUnix) {
                Runtime.getRuntime().exec("sh")
            } else {
                Runtime.getRuntime().exec("cmd")
            }

            val outputStream = BufferedWriter(OutputStreamWriter(process.outputStream))

            outputStream.write("$ADB_PATH shell kill $pid")
            outputStream.write("\n")
            outputStream.write("exit\n")
            outputStream.flush()

            println("-----> adb shell kill command end <------")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun findAppProcessPid(): String? {
    var pid: String? = null
    println("-----> adb shell ps command start <------")
    try {
        val process = if (isUnix) {
            Runtime.getRuntime().exec("sh")
        } else {
            Runtime.getRuntime().exec("cmd")
        }

        val outputStream = BufferedWriter(OutputStreamWriter(process.outputStream))

        outputStream.write("$ADB_PATH shell ps")
        outputStream.write("\n")
        outputStream.write("exit\n")
        outputStream.flush()

        var line: String
        val reader = BufferedReader(InputStreamReader(process.inputStream))

        while (true) {
            line = reader.readLine()
            if (line != null) {
                println(line)
                if (line.contains("app_process")) {
                    val regex = "\\s+"
                    line = line.replace(regex.toRegex(), " ")
                    pid = line.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1]
                    println("pid is $pid")
                    println("-----> adb shell ps command end <------")
                    return pid
                }
            } else {
                break
            }
        }
        return pid
    } catch (e: Exception) {
        e.printStackTrace()
        return pid
    }
}