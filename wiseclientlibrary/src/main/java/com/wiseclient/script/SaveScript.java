package com.wiseclient.script;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by kuanghaochuan on 2017/7/16.
 */

public class SaveScript implements ScriptInterface {
    @Override
    public void handleScript(StringBuilder stringBuilder) {
        saveFile(stringBuilder);
    }

    private static void saveFile(StringBuilder builder) {
        try {
            File file = new File("/Users/kuanghaochuan/Library/Android/sdk/tools/bin/wise.mr");
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file, true), "utf-8");
            writer.write(builder.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
