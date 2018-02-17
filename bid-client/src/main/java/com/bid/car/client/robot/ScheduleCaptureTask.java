package com.bid.car.client.robot;

import io.socket.client.Socket;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Data
@ToString
public class ScheduleCaptureTask {

    private String filePrefix = "";
    private String imageName = "";
    private String textName = "";
    private Integer lowestPrice;
    private String textPrefix;
    private Integer additionalPrice;
    private Integer bidPrice;
    private Rectangle lowestPricePosition;
    private Rectangle codePosition;
    private Point inputPricePosition;
    private Point clickPosition;
    private LocalTime lastBidTime;
    private LocalTime startTime;
    private String code;
    private Point addBidPosition;
    private Robot robot;

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public ScheduleCaptureTask(Rectangle lowestPricePosition, Point inputPricePosition, Point clickPosition, Integer additionalPrice, Point addBidPosition, Rectangle codePosition, LocalTime lastBidTime, LocalTime startTime) throws AWTException {
        this.lowestPricePosition = lowestPricePosition;
        this.inputPricePosition = inputPricePosition;
        this.clickPosition = clickPosition;
        this.additionalPrice = additionalPrice;
        this.codePosition = codePosition;
        this.addBidPosition = addBidPosition;
        this.lastBidTime = lastBidTime;
        this.startTime = startTime;
        this.robot = new Robot();
    }

    public void scanLowestPricePosition() throws IOException {
        BufferedImage capturedImage = CaptureImageUtils.capture(robot, this.lowestPricePosition);
        String dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.imageName = filePrefix + dateFormat + ".JPG";
        this.textName = filePrefix + dateFormat + ".text";
        this.textPrefix = filePrefix + dateFormat;
        File file = new File(imageName);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        CaptureImageUtils.writeToStream(fileOutputStream, capturedImage);
        fileOutputStream.close();
        log.info("截取图片存储:{}", this.imageName);
    }

    public void executeTesseractCommand() {
        try {
            Runtime.getRuntime().exec(String.format("tesseract %s %s", this.imageName, this.textPrefix));
        } catch (IOException e) {
            e.printStackTrace();
            log.error("执行tesseract：{},{}", this.imageName, this.textPrefix);
        }
    }


    public void inputCode(String code) {
        robot.mouseMove((int) this.inputPricePosition.getX(), (int) this.inputPricePosition.getY());
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(50);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(50);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        for (int j = 0; j < code.length(); j++) {
            char c = code.charAt(j);
            KeyStroke keycode = KeyStroke.getKeyStroke(c);
            robot.keyPress(keycode.getKeyCode());
            robot.delay(50);
            robot.keyRelease(keycode.getKeyCode());
        }
        robot.delay(50);
    }

    public void submit() {
        robot.mouseMove((int) this.clickPosition.getX(), (int) this.clickPosition.getY());
        doubleClick();
    }

    public void sendImage(Consumer<String> consumer) throws IOException {
        robot.delay(3000);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(CaptureImageUtils.capture(this.robot, this.codePosition), "JPG", byteArrayOutputStream);
        byte[] imageData = byteArrayOutputStream.toByteArray();

        String base64ImageData = Base64.getEncoder().encodeToString(imageData);
        consumer.accept(base64ImageData);
        log.info("加密图片发送");
    }

    //点击确认加价，
    public void clickAddBid() {
        robot.mouseMove((int) addBidPosition.getX(), (int) addBidPosition.getY());
        doubleClick();
    }

    public void doubleClick() {
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(50);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(50);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }


    public Boolean checkTimeToSubmit() {
        if (this.lowestPrice >= this.bidPrice && LocalTime.now().isAfter(this.lastBidTime)) {
            this.submit();
            log.info("点击确认进行提交: 在什么时候{},价格为:{}", LocalTime.now(), this.bidPrice);
            return Boolean.TRUE;
        } else {
            log.info("WAITING :{}", LocalTime.now());
            return Boolean.FALSE;
        }
    }

    public Integer readLowestPrice() throws IOException {
        String lowestPriceStr = new String(Files.readAllBytes(Paths.get(this.textName)));
        log.info("read lowest price:{}", this.lowestPrice);
        this.lowestPrice = Integer.valueOf(lowestPriceStr);
        return this.lowestPrice;
    }

    public void firstReadLowestPrice() throws IOException {
        Integer lowestPrice = this.readLowestPrice();
        this.bidPrice = lowestPrice + this.additionalPrice;
    }

    public void start(Socket socket) {
        socket.on("CODE", objects -> {
            log.info("Receive code:{}", objects);
            this.code = objects[0].toString();
            this.inputCode(this.code);
        });

        long delay = Duration.between(LocalTime.now(), this.startTime).getSeconds();
        scheduledExecutorService.schedule(() -> {
            try {
                scanLowestPricePosition();
                executeTesseractCommand();
                firstReadLowestPrice();

                log.info("task:{}", this);
                this.clickAddBid();
                this.sendImage((item) -> socket.emit("IMAGE", item));

            } catch (IOException e) {
                e.printStackTrace();
                log.error("遇到错误:{}", e);
            }
        }, delay, TimeUnit.SECONDS);

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                log.info("schdule before:{}", this);
                scanLowestPricePosition();
                executeTesseractCommand();
                readLowestPrice();

                log.info("schedule after:{}", this);

               if(checkTimeToSubmit()){
                   System.exit(0);
               }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, delay, 1, TimeUnit.SECONDS);
    }

}
