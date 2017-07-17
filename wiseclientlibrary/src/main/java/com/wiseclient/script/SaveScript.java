package com.wiseclient.script;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by kuanghaochuan on 2017/7/16.
 */

public class SaveScript {
    public static void saveFile(File file, String scriptContent) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, true), "utf-8");
            writer.write(scriptContent);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
