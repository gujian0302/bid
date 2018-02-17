package com.bid.car.client.robot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

public class CaptureImageUtils {

    public static BufferedImage capture(Robot robot, Integer capture_x, Integer capture_y, Integer capture_w, Integer capture_h){
        Rectangle screenRect = new Rectangle(capture_x, capture_y, capture_w, capture_h);
        return robot.createScreenCapture(screenRect);
    }

    public static BufferedImage capture(Robot robot, Rectangle rectangle){
        return  robot.createScreenCapture(rectangle);
    }

    public static void writeToStream(OutputStream outputStream, BufferedImage bufferedImage) throws IOException {
        ImageIO.write(bufferedImage,"JPG", outputStream);
    }
}
