package com.server.script

import javax.swing.JTextArea

/**
 * Created by kuanghaochuan on 2017/7/17.
 */

class DisplayScript internal constructor(private val mJTextArea: JTextArea) : ScriptInterface {

    override fun handleScript(value: String) {
        this.mJTextArea.append(value)
    }
}
