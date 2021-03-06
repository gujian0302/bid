package com.bid.car.client;

import com.aliyun.oss.OSSClient;
import com.bid.car.client.property.Config;
import com.bid.car.client.property.ConfigData;
import com.bid.car.client.robot.ScheduleCaptureTask;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalTime;
import java.time.temporal.TemporalUnit;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.time.temporal.ChronoUnit.SECONDS;

@Slf4j
public class Main {

    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    public static void main(String[] argv) throws IOException, URISyntaxException, AWTException {
        Config config = new Config();
        Server server = config.readConfig();

        ConfigData configData = config.readConfigData();

        log.info("start");
        Socket socket = IO.socket("http://"+server.getHost()+":" + server.getPort());
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... objects) {
                log.info("success connect to server");
                socket.emit("SERVER", "");
                log.info("emit to register as server");
            }
        });
        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... objects) {
                log.info("disconnect to server");
            }
        });

//        socket.on("CODE", new Emitter.Listener() {
//            @Override
//            public void call(Object... objects) {
//                log.info("RECEIVE CODE:{}", objects);
//            }
//        });
//
        socket.connect();
        String startTimeStr = configData.getStartTime();
        String lastBidTimeStr = configData.getLastBidTime() ;
        String addPriceStr = configData.getAddPrice() ;
        LocalTime startTime = LocalTime.parse(startTimeStr);
//        LocalTime now = LocalTime.now().plus(20, SECONDS);
        LocalTime lastBidTime = LocalTime.parse(lastBidTimeStr);
        Integer addPrice = Integer.valueOf(addPriceStr);


        OSSClient ossClient = new OSSClient("oss-cn-shanghai.aliyuncs.com", "",  "");
        ScheduleCaptureTask scheduleCaptureTask = new ScheduleCaptureTask(Config.readLowestPricePosition(),
                Config.readInputPricePosition(),Config.readInputCode(), Config.readSubmitButton(), addPrice ,  Config.readBidButton(),
                Config.readCodePosition(), lastBidTime, startTime, Config.readNewLowestPosition(), ossClient);
        scheduleCaptureTask.start(socket);
//        File file = new File("/Users/gwen/Desktop/1.png");
//        FileInputStream fileInputStream = new FileInputStream(file);
//        String base64Image = Base64.getEncoder().encodeToString(StreamUtils.read(fileInputStream));
//        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                socket.emit("IMAGE", base64Image);
//            }
//        }, 0, 5, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                ossClient.shutdown();
                super.run();
            }
        });
    }
}

