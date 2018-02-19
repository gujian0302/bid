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
    //输入价格的位置
    private Point inputPricePosition;
    //输入验证码的输入框的位置
    private Point inputCodePosition;
    private Point clickPosition;
    private LocalTime lastBidTime;
    private LocalTime startTime;
    private String code;
    private Point addBidPosition;
    private Robot robot;

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public ScheduleCaptureTask(Rectangle lowestPricePosition, Point inputPricePosition,Point inputCodePosition, Point clickPosition, Integer additionalPrice, Point addBidPosition, Rectangle codePosition, LocalTime lastBidTime, LocalTime startTime) throws AWTException {
        this.lowestPricePosition = lowestPricePosition;
        this.inputPricePosition = inputPricePosition;
        this.clickPosition = clickPosition;
        this.additionalPrice = additionalPrice;
        this.codePosition = codePosition;
        this.addBidPosition = addBidPosition;
        this.lastBidTime = lastBidTime;
        this.startTime = startTime;
        this.inputCodePosition = inputCodePosition;
        this.robot = new Robot();
    }

    public void scanLowestPricePosition() throws IOException {
        BufferedImage capturedImage = CaptureImageUtils.capture(robot, this.lowestPricePosition);
        String dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        this.imageName = filePrefix + dateFormat + ".JPG";
        this.textName = filePrefix + dateFormat + ".txt";
        this.textPrefix = filePrefix + dateFormat;
        File file = new File(imageName);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        CaptureImageUtils.writeToStream(fileOutputStream, capturedImage);
        fileOutputStream.close();
        log.info("截取图片存储:{}", this.imageName);
    }

    public void executeTesseractCommand() {
        try {
            Runtime.getRuntime().exec(String.format("tesseract %s %s", this.imageName, this.textPrefix)).waitFor();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("执行tesseract：{},{}", this.imageName, this.textPrefix);
        } catch (InterruptedException e) {
            log.error("超时执行tesseract");
            e.printStackTrace();
        }
    }


    public void inputCode(String code) {
        robot.mouseMove((int) this.inputCodePosition.getX(), (int) this.inputCodePosition.getY());
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(50);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(50);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        type(code);
        robot.delay(50);
    }

    private void type(String str) {
        for (int j = 0; j < str.length(); j++) {
            char c = str.charAt(j);
            KeyStroke keycode = KeyStroke.getKeyStroke(c, 0);
            log.debug("press key code:{}", keycode);
            robot.keyPress(keycode.getKeyCode());
            robot.delay(50);
            robot.keyRelease(keycode.getKeyCode());
        }
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

    //输入最低价格
    public void inputPrice(){
        Integer bidPrice = this.bidPrice;
        robot.mouseMove((int)this.inputPricePosition.getX(),(int)this.inputPricePosition.getY());

        doubleClick();
        type(bidPrice.toString());
        robot.delay(50);

        this.clickAddBid();
        robot.delay(50);

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
        if ( (this.lowestPrice >= this.bidPrice || LocalTime.now().isAfter(this.lastBidTime) ) && this.code != null) {
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
        log.info("read lowest price:{}", lowestPriceStr);
        this.lowestPrice =  this.formatNumber(lowestPriceStr);
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
                robot.delay(1000);
                firstReadLowestPrice();

                log.info("task:{}", this);
                this.inputPrice();
                this.sendImage((item) -> socket.emit("IMAGE", item));

            } catch (IOException e) {
                log.error("遇到错误:{}", e);
                e.printStackTrace();
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

    public Integer formatNumber(String number) {
        StringBuilder result = new StringBuilder();
        Boolean flag = false;
        for (int i = 0 ; i < number.length(); ++i) {
            if (flag) break;
            if (Character.isDigit(number.charAt(i))){
                result.append(number.charAt(i));
            }
        }
        return Integer.valueOf(result.toString());
    }

}
