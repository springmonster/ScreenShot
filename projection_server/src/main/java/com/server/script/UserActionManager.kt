package com.server.script

import javax.swing.JTextArea

/**
 * Created by kuanghaochuan on 2017/7/11.
 */

internal class UserActionManager(private val mJTextArea: JTextArea) {
    private val mDisplayScript: ScriptInterface
    val strList = ArrayList<String>()

    init {
        mDisplayScript = DisplayScript(this.mJTextArea)
    }

    fun saveAction(s: String) {
        strList.add(s)
        mDisplayScript.handleScript(s)
    }
}
