package com.client.script

import javax.swing.JTextArea

/**
 * Created by kuanghaochuan on 2017/7/17.
 */

class DisplayScript internal constructor(private val mJTextArea: JTextArea) : ScriptInterface {

    override fun handleScript(stringBuilder: StringBuilder) {
        this.mJTextArea.append(stringBuilder.toString())
    }
}
