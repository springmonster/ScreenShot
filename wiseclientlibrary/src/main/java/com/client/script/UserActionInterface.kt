package com.client.script

/**
 * Created by kuanghaochuan on 2017/7/16.
 */

interface UserActionInterface {
    fun actionViewClick(x: Int, y: Int)

    fun actionViewMove(oldX: Int, oldY: Int, newX: Int, newY: Int)

    fun actionBackPress()

    fun actionMenuPress()

    fun actionHomePress()

    fun actionKeyUpPress()

    fun actionKeyDownPress()

    fun actionKeyLeftPress()

    fun actionKeyRightPress()

    fun actionKeyEnterPress()

    fun actionKeyBackPress()
}
