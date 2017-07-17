package com.wiseclient.script;

import javax.swing.JTextArea;

/**
 * Created by kuanghaochuan on 2017/7/11.
 */

public class UserAction implements UserActionInterface {
    private ScriptInterface mDisplayScript;
    private JTextArea mJTextArea;

    public UserAction(JTextArea jTextArea) {
        this.mJTextArea = jTextArea;
        mDisplayScript = new DisplayScript(this.mJTextArea);
    }

    @Override
    public void actionViewClick(int x, int y) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("TOUCH|{'x':" + x + ",'y':" + y + ",'type':'downAndUp',}");
        stringBuilder.append("\n");
        stringBuilder.append("WAIT|{'seconds':4.0,}");
        stringBuilder.append("\n");

        System.out.println("actionViewClick x is " + x + " y is " + y);

        mDisplayScript.handleScript(stringBuilder);
    }

    @Override
    public void actionViewMove(int oldX, int oldY, int newX, int newY) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DRAG|{'start':(" + oldX + "," + oldY + "),'end':(" + newX + "," + newY + "),'duration':1.0,'steps':10,}");
        stringBuilder.append("\n");

        System.out.println("actionViewMove");

        mDisplayScript.handleScript(stringBuilder);
    }

    @Override
    public void actionBackPress() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PRESS|{'name':'BACK','type':'downAndUp',}");
        stringBuilder.append("\n");
        stringBuilder.append("WAIT|{'seconds':1.0,}");
        stringBuilder.append("\n");

        System.out.println("actionBackPress");

        mDisplayScript.handleScript(stringBuilder);
    }

    @Override
    public void actionMenuPress() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PRESS|{'name':'MENU','type':'downAndUp',}");
        stringBuilder.append("\n");

        System.out.println("actionMenuPress");

        mDisplayScript.handleScript(stringBuilder);
    }

    @Override
    public void actionHomePress() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PRESS|{'name':'HOME','type':'downAndUp',}");
        stringBuilder.append("\n");

        System.out.println("actionHomePress");

        mDisplayScript.handleScript(stringBuilder);
    }
}
