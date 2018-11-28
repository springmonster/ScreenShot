package com.client.script

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

/**
 * Created by kuanghaochuan on 2017/7/16.
 */

object SaveScript {
    fun saveFile(file: File, scriptContent: String) {
        val writer = OutputStreamWriter(FileOutputStream(file, true), "utf-8")
        writer.use { writer.write(scriptContent) }
    }
}
