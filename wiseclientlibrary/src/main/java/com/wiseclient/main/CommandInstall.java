package com.wiseclient.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by kuanghaochuan on 2017/7/13.
 */

public class CommandInstall {
    private static String ADB_PATH = getAdbFileForLinux();
    private static boolean isUnix = true;

    public static void main(String[] args) {
        installDex();
    }

    public static void installDex() {
        checkPlatform();
        execAdbPushCommand();
        execAdbForwardCommand();
        execAppProcessCommand();
    }

    private static void execAdbPushCommand() {
        String apk = getApk();
        System.out.println("-----> adb shell push command start <------");
        try {
            Process process;
            if (isUnix) {
                process = Runtime.getRuntime().exec("sh");
            } else {
                process = Runtime.getRuntime().exec("cmd /c ");
            }

            final BufferedWriter outputStream = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream()));

            outputStream.write(ADB_PATH + " push " + apk + " /sdcard/PhoneClient.apk");
            outputStream.write("\n");
            outputStream.write("exit\n");
            outputStream.flush();

            process.waitFor();
            readExecCommandResult(process.getInputStream());
            System.out.println("-----> adb push forward command end <------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkPlatform() {
        String os = System.getProperty("os.name");
        System.out.println("os system is " + os);

        if (os.toLowerCase().contains("linux")) {
            ADB_PATH = getAdbFileForLinux();
        } else if (os.toLowerCase().contains("win")) {
            isUnix = false;
            ADB_PATH = getAdbFileForWin();
        } else if (os.toLowerCase().contains("mac")) {
            ADB_PATH = getAdbFileForMacOS();
        }
    }

    private static void execAdbForwardCommand() {
        System.out.println("-----> adb shell forward command start <------");
        try {
            Process process;
            if (isUnix) {
                process = Runtime.getRuntime().exec("sh");
            } else {
                process = Runtime.getRuntime().exec("cmd /c ");
            }

            final BufferedWriter outputStream = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream()));

            outputStream.write(ADB_PATH + " forward tcp:9999 localabstract:wisescreenshot");
            outputStream.write("\n");
            outputStream.write("exit\n");
            outputStream.flush();

            process.waitFor();
            readExecCommandResult(process.getInputStream());
            System.out.println("-----> adb shell forward command end <------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void execAppProcessCommand() {
//        String findApkCmd = "export CLASSPATH=/data/app/com.wise.wisescreenshot-1/base.apk";
        String findApkCmd = "export CLASSPATH=/sdcard/PhoneClient.apk";
        String startApkCmd = "exec app_process /system/bin com.wise.wisescreenshot.PhoneClient";

        String[] commands;

        commands = new String[]{findApkCmd, startApkCmd};

        try {
            Process process;
            if (isUnix) {
                process = Runtime.getRuntime().exec(ADB_PATH + " shell");
            } else {
                process = Runtime.getRuntime().exec("cmd /c " + ADB_PATH + " shell");
            }

            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            for (String cmd : commands) {
                bufferedWriter.write(cmd);
                bufferedWriter.write("\n");
            }
            bufferedWriter.flush();

            readError(process.getErrorStream());
            readExecCommandResult(process.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readError(final InputStream errorStream) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                readExecCommandResult(errorStream);
            }
        }.start();
    }

    private static void readExecCommandResult(final InputStream stream) {
        try {
            String line;
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                stream.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private static String getAdbFileForLinux() {
        File file = new File("");
        String path = file.getAbsolutePath();
        return path + "/linux/adb";
    }

    private static String getAdbFileForWin() {
        File file = new File("");
        String path = file.getAbsolutePath();
        return path + "/windows/adb";
    }

    private static String getAdbFileForMacOS() {
        File file = new File("");
        String path = file.getAbsolutePath();
        return path + "/macos/adb";
    }

    private static String getApk() {
        File file = new File("");
        String path = file.getAbsolutePath();
        return path + "/apk/PhoneClient.apk";
    }

    public static void execExitAppProcessCommand() {
        String pid = findAppProcessPid();
        if (pid != null) {
            System.out.println("-----> adb shell kill command start <------");
            try {
                Process process;
                if (isUnix) {
                    process = Runtime.getRuntime().exec("sh");
                } else {
                    process = Runtime.getRuntime().exec("cmd /c ");
                }

                final BufferedWriter outputStream = new BufferedWriter(
                        new OutputStreamWriter(process.getOutputStream()));

                outputStream.write(ADB_PATH + " shell kill " + pid);
                outputStream.write("\n");
                outputStream.write("exit\n");
                outputStream.flush();

                System.out.println("-----> adb shell kill command end <------");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String findAppProcessPid() {
        String pid = null;
        System.out.println("-----> adb shell ps command start <------");
        try {
            Process process;
            if (isUnix) {
                process = Runtime.getRuntime().exec("sh");
            } else {
                process = Runtime.getRuntime().exec("cmd /c ");
            }

            final BufferedWriter outputStream = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream()));

            outputStream.write(ADB_PATH + " shell ps");
            outputStream.write("\n");
            outputStream.write("exit\n");
            outputStream.flush();

            String line;
            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (line.contains("app_process")) {
                    String regex = "\\s+";
                    line = line.replaceAll(regex, " ");
                    pid = line.split(" ")[1];
                    System.out.println("pid is " + pid);
                    System.out.println("-----> adb shell ps command end <------");
                    return pid;
                }
            }
            return pid;
        } catch (Exception e) {
            e.printStackTrace();
            return pid;
        }
    }
}
