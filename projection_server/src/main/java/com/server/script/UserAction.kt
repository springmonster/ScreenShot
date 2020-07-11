package com.server.script

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

        println("UserAction actionViewClick x is $x y is $y")

        mDisplayScript.handleScript(result)
    }

    override fun actionViewMove(oldX: Int, oldY: Int, newX: Int, newY: Int) {
        val result = appendString {
            append("DRAG|{'start':($oldX,$oldY),'end':($newX,$newY),'duration':1.0,'steps':10,}")
            append("\n")
        }

        println("UserAction actionViewMove")

        mDisplayScript.handleScript(result)
    }

    override fun actionBackPress() {
        val result = appendString {
            append("PRESS|{'name':'BACK','type':'downAndUp',}")
            append("\n")
            append("WAIT|{'seconds':1.0,}")
            append("\n")
        }

        println("UserAction actionBackPress")

        mDisplayScript.handleScript(result)
    }

    override fun actionMenuPress() {
        val result = appendString {
            append("PRESS|{'name':'MENU','type':'downAndUp',}")
            append("\n")
        }
        println("UserAction actionMenuPress")

        mDisplayScript.handleScript(result)
    }

    override fun actionHomePress() {
        val result = appendString {
            append("PRESS|{'name':'HOME','type':'downAndUp',}")
            append("\n")
        }

        println("UserAction actionHomePress")

        mDisplayScript.handleScript(result)
    }

    private inline fun appendString(appendFun: StringBuilder.() -> Unit): String {
        with(StringBuilder()) {
            appendFun()
            return toString()
        }
    }
}
