package com.bid.car.client.robot;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CaptureImageUtils {

    public static BufferedImage capture(Robot robot, Integer capture_x, Integer capture_y, Integer capture_w, Integer capture_h){
        Rectangle screenRect = new Rectangle(capture_x, capture_y, capture_w, capture_h);
        return robot.createScreenCapture(screenRect);
    }
}
