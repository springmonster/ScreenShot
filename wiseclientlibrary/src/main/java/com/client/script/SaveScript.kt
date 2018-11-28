package com.client.script

import okio.buffer
import okio.sink
import java.io.File

/**
 * Created by kuanghaochuan on 2017/7/16.
 */

internal fun saveFile(file: File, scriptContent: String) {
    val sink = file.sink()
    val buffer = sink.buffer()
    sink.use {
        buffer.use {
            buffer.writeUtf8(scriptContent)
        }
    }
}
