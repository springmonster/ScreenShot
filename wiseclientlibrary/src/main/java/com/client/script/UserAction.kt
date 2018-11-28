package com.client.script

import javax.swing.JTextArea

/**
 * Created by kuanghaochuan on 2017/7/11.
 */

class UserAction(private val mJTextArea: JTextArea) : UserActionInterface {
    private val mDisplayScript: ScriptInterface

    init {
        mDisplayScript = DisplayScript(this.mJTextArea)
    }

    override fun actionViewClick(x: Int, y: Int) {
        val stringBuilder = StringBuilder()
        stringBuilder.append("TOUCH|{'x':$x,'y':$y,'type':'downAndUp',}")
        stringBuilder.append("\n")
        stringBuilder.append("WAIT|{'seconds':4.0,}")
        stringBuilder.append("\n")

        println("actionViewClick x is $x y is $y")

        mDisplayScript.handleScript(stringBuilder)
    }

    override fun actionViewMove(oldX: Int, oldY: Int, newX: Int, newY: Int) {
        val stringBuilder = StringBuilder()
        stringBuilder.append("DRAG|{'start':($oldX,$oldY),'end':($newX,$newY),'duration':1.0,'steps':10,}")
        stringBuilder.append("\n")

        println("actionViewMove")

        mDisplayScript.handleScript(stringBuilder)
    }

    override fun actionBackPress() {
        val stringBuilder = StringBuilder()
        stringBuilder.append("PRESS|{'name':'BACK','type':'downAndUp',}")
        stringBuilder.append("\n")
        stringBuilder.append("WAIT|{'seconds':1.0,}")
        stringBuilder.append("\n")

        println("actionBackPress")

        mDisplayScript.handleScript(stringBuilder)
    }

    override fun actionMenuPress() {
        val stringBuilder = StringBuilder()
        stringBuilder.append("PRESS|{'name':'MENU','type':'downAndUp',}")
        stringBuilder.append("\n")

        println("actionMenuPress")

        mDisplayScript.handleScript(stringBuilder)
    }

    override fun actionHomePress() {
        val stringBuilder = StringBuilder()
        stringBuilder.append("PRESS|{'name':'HOME','type':'downAndUp',}")
        stringBuilder.append("\n")

        println("actionHomePress")

        mDisplayScript.handleScript(stringBuilder)
    }

    override fun actionKeyUpPress() {
        val stringBuilder = StringBuilder()
        stringBuilder.append("device.press('KEYCODE_DPAD_UP',MonkeyDevice.DOWN_AND_UP)")
        stringBuilder.append("\n")
        stringBuilder.append("MonkeyRunner.sleep(1.5)")
        stringBuilder.append("\n")

        println("actionKeyUpPress")

        mDisplayScript.handleScript(stringBuilder)
    }

    override fun actionKeyDownPress() {
        val stringBuilder = StringBuilder()
        stringBuilder.append("device.press('KEYCODE_DPAD_DOWN',MonkeyDevice.DOWN_AND_UP)")
        stringBuilder.append("\n")
        stringBuilder.append("MonkeyRunner.sleep(1.5)")
        stringBuilder.append("\n")

        println("actionKeyDownPress")

        mDisplayScript.handleScript(stringBuilder)
    }

    override fun actionKeyLeftPress() {
        val stringBuilder = StringBuilder()
        stringBuilder.append("device.press('KEYCODE_DPAD_LEFT',MonkeyDevice.DOWN_AND_UP)")
        stringBuilder.append("\n")
        stringBuilder.append("MonkeyRunner.sleep(1.5)")
        stringBuilder.append("\n")

        println("actionKeyLeftPress")

        mDisplayScript.handleScript(stringBuilder)
    }

    override fun actionKeyRightPress() {
        val stringBuilder = StringBuilder()
        stringBuilder.append("device.press('KEYCODE_DPAD_RIGHT',MonkeyDevice.DOWN_AND_UP)")
        stringBuilder.append("\n")
        stringBuilder.append("MonkeyRunner.sleep(1.5)")
        stringBuilder.append("\n")

        println("actionKeyRightPress")

        mDisplayScript.handleScript(stringBuilder)
    }

    override fun actionKeyEnterPress() {
        val stringBuilder = StringBuilder()
        stringBuilder.append("device.press('KEYCODE_DPAD_CENTER',MonkeyDevice.DOWN_AND_UP)")
        stringBuilder.append("\n")
        stringBuilder.append("MonkeyRunner.sleep(1.5)")
        stringBuilder.append("\n")

        println("actionKeyEnterPress")

        mDisplayScript.handleScript(stringBuilder)
    }

    override fun actionKeyBackPress() {
        val stringBuilder = StringBuilder()
        stringBuilder.append("device.press('KEYCODE_BACK',MonkeyDevice.DOWN_AND_UP)")
        stringBuilder.append("\n")
        stringBuilder.append("MonkeyRunner.sleep(1.5)")
        stringBuilder.append("\n")

        println("actionKeyBackPress")

        mDisplayScript.handleScript(stringBuilder)
    }
}
