package com.wiseclient.script;

/**
 * Created by kuanghaochuan on 2017/7/16.
 */

public interface UserActionInterface {
    void actionViewClick(int x, int y);

    void actionViewMove(int oldX, int oldY, int newX, int newY);

    void actionBackPress();

    void actionMenuPress();

    void actionHomePress();
}
