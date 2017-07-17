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
        String findApkCmd = "export CLASSPATH=/sdcard/PhoneClient.dex";
        String startApkCmd = "exec app_process /sdcard com.wise.wisescreenshot.PhoneClient";
        execCommand(new String[]{findApkCmd, startApkCmd});
    }

    private static void execCommand(String[] command) {
        try {
            Process process = Runtime
                    .getRuntime()
                    .exec("./adb shell "); // adb

            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            for (String cmd :
                    command) {
                bufferedWriter.write(cmd);
                bufferedWriter.write("\n");
            }

            bufferedWriter.flush();
            System.out.println("shell write finished...");
            readError(process.getErrorStream());
            adbCommand("forward tcp:9999 localabstract:wise-screen-shot");
            readResult(process.getInputStream());

            while (true) {
                Thread.sleep(Integer.MAX_VALUE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void readError(final InputStream errorStream) {
        new Thread(){
            @Override
            public void run() {
                super.run();
                readResult(errorStream);
            }
        }.start();
    }

    private static void adbCommand(String com) {
        System.out.println("adbCommand...." + com);
        command("sh", "./adb " + com);
    }

    private static void command(String c, String com) {
        System.out.println("---> " + c + com);
        try {
            Process process = Runtime
                    .getRuntime()
                    .exec(c); // adb
            final BufferedWriter outputStream = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream()));


            outputStream.write(com);
            outputStream.write("\n");
            outputStream.write("exit\n");
            outputStream.flush();

            int i = process.waitFor();
            readResult(process.getInputStream());


            System.out.println("------END-------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readResult(final InputStream stream) {

        System.out.println("read result.....");
        try {
            String line;
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream));

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("-------END------");
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
