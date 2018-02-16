package com.bid.car.client.robot;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Slf4j
public class RobotUtilsTest {

    @Test
    public void testCaptureImage() throws AWTException, IOException {
        Robot robot = new Robot();
        BufferedImage bufferedImage = CaptureImageUtils.capture(robot,0, 0, 100, 100) ;

        log.info("image:{}",bufferedImage);
        ImageIO.write(bufferedImage,"jpg", new File("1.jpg"));
        Assert.assertNotNull(bufferedImage);
    }
}
