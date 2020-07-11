package com.server.script

/**
 * Created by kuanghaochuan on 2017/7/16.
 */

internal interface UserActionInterface {
    fun actionViewClick(x: Int, y: Int)

    fun actionViewMove(oldX: Int, oldY: Int, newX: Int, newY: Int)

    fun actionBackPress()

    fun actionMenuPress()

    fun actionHomePress()
}
