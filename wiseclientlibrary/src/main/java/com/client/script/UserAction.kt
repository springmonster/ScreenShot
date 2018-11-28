package com.client.script

import javax.swing.JTextArea

/**
 * Created by kuanghaochuan on 2017/7/11.
 */

internal class UserAction(private val mJTextArea: JTextArea) : UserActionInterface {
    private val mDisplayScript: ScriptInterface

    init {
        mDisplayScript = DisplayScript(this.mJTextArea)
    }

    override fun actionViewClick(x: Int, y: Int) {
        val result = appendString {
            append("TOUCH|{'x':$x,'y':$y,'type':'downAndUp',}")
            append("\n")
            append("WAIT|{'seconds':4.0,}")
            append("\n")
        }

        println("actionViewClick x is $x y is $y")

        mDisplayScript.handleScript(result)
    }

    override fun actionViewMove(oldX: Int, oldY: Int, newX: Int, newY: Int) {
        val result = appendString {
            append("DRAG|{'start':($oldX,$oldY),'end':($newX,$newY),'duration':1.0,'steps':10,}")
            append("\n")
        }

        println("actionViewMove")

        mDisplayScript.handleScript(result)
    }

    override fun actionBackPress() {
        val result = appendString {
            append("PRESS|{'name':'BACK','type':'downAndUp',}")
            append("\n")
            append("WAIT|{'seconds':1.0,}")
            append("\n")
        }

        println("actionBackPress")

        mDisplayScript.handleScript(result)
    }

    override fun actionMenuPress() {
        val result = appendString {
            append("PRESS|{'name':'MENU','type':'downAndUp',}")
            append("\n")
        }
        println("actionMenuPress")

        mDisplayScript.handleScript(result)
    }

    override fun actionHomePress() {
        val result = appendString {
            append("PRESS|{'name':'HOME','type':'downAndUp',}")
            append("\n")
        }

        println("actionHomePress")

        mDisplayScript.handleScript(result)
    }

    override fun actionKeyUpPress() {
        val result = appendString {
            append("device.press('KEYCODE_DPAD_UP',MonkeyDevice.DOWN_AND_UP)")
            append("\n")
            append("MonkeyRunner.sleep(1.5)")
            append("\n")
        }

        println("actionKeyUpPress")

        mDisplayScript.handleScript(result)
    }

    override fun actionKeyDownPress() {
        val result = appendString {
            append("device.press('KEYCODE_DPAD_DOWN',MonkeyDevice.DOWN_AND_UP)")
            append("\n")
            append("MonkeyRunner.sleep(1.5)")
            append("\n")
        }

        println("actionKeyDownPress")

        mDisplayScript.handleScript(result)
    }

    override fun actionKeyLeftPress() {
        val result = appendString {
            append("device.press('KEYCODE_DPAD_LEFT',MonkeyDevice.DOWN_AND_UP)")
            append("\n")
            append("MonkeyRunner.sleep(1.5)")
            append("\n")
        }

        println("actionKeyLeftPress")

        mDisplayScript.handleScript(result)
    }

    override fun actionKeyRightPress() {
        val result = appendString {
            append("device.press('KEYCODE_DPAD_RIGHT',MonkeyDevice.DOWN_AND_UP)")
            append("\n")
            append("MonkeyRunner.sleep(1.5)")
            append("\n")
        }

        println("actionKeyRightPress")

        mDisplayScript.handleScript(result)
    }

    override fun actionKeyEnterPress() {
        val result = appendString {
            append("device.press('KEYCODE_DPAD_CENTER',MonkeyDevice.DOWN_AND_UP)")
            append("\n")
            append("MonkeyRunner.sleep(1.5)")
            append("\n")
        }

        println("actionKeyEnterPress")

        mDisplayScript.handleScript(result)

    }

    override fun actionKeyBackPress() {
        val result = appendString {
            append("device.press('KEYCODE_BACK',MonkeyDevice.DOWN_AND_UP)")
            append("\n")
            append("MonkeyRunner.sleep(1.5)")
            append("\n")
        }

        println("actionKeyBackPress")

        mDisplayScript.handleScript(result)
    }

    private inline fun appendString(appendFun: StringBuilder.() -> Unit): String {
        with(StringBuilder()) {
            appendFun()
            return toString()
        }
    }
}
