package com.jay.cloud_board.bean;

import java.io.Serializable;

/**
 * Created by Jay on 2019/3/2.
 * desc:笔划的点
 */

public class Point implements Serializable {
    public float X;
    public float Y;

    public Point clone() {
        Point point = new Point();
        point.X = X;
        point.Y = Y;
        return point;
    }
}
