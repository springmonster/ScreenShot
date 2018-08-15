package com.wiseclient.script;

import javax.swing.JTextArea;

/**
 * Created by kuanghaochuan on 2017/7/17.
 */

public class DisplayScript implements ScriptInterface {
    private JTextArea mJTextArea;

    DisplayScript(JTextArea jTextArea) {
        this.mJTextArea = jTextArea;
    }

    @Override
    public void handleScript(StringBuilder stringBuilder) {
        this.mJTextArea.append(stringBuilder.toString());
    }
}
