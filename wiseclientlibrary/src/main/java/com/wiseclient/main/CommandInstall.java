package com.wiseclient.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by kuanghaochuan on 2017/7/13.
 */

public class CommandInstall {

    public static void main(String[] args) {
        installDex();
    }

    public static void installDex() {
        execAdbForwardCommand();
        execAppProcessCommand();
    }

    private static void execAdbForwardCommand() {
        System.out.println("-----> adb forward command start <------");
        try {
            Process process = Runtime
                    .getRuntime()
                    .exec("sh");

            final BufferedWriter outputStream = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream()));

            outputStream.write("adb forward tcp:9999 localabstract:wisescreenshot");
            outputStream.write("\n");
            outputStream.write("exit\n");
            outputStream.flush();

            process.waitFor();
            readExecCommandResult(process.getInputStream());
            System.out.println("-----> adb forward command end <------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void execAppProcessCommand() {
        String findApkCmd = "export CLASSPATH=/data/app/com.wise.wisescreenshot-1/base.apk";
        String startApkCmd = "exec app_process /system/bin com.wise.wisescreenshot.PhoneClient";
        String[] commands = new String[]{findApkCmd, startApkCmd};
        try {
            Process process = Runtime
                    .getRuntime()
                    .exec("adb shell");

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
}
