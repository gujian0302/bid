package com.bid.car;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.internal.ConcurrentSet;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

public class Main {

    private static Logger logger = LoggerFactory.getLogger(com.bid.car.Main.class);

    private static LocalTime bidTime = LocalTime.of(11, 29, 30);
    private static Base64 base64 = new Base64();

    private static ScheduledExecutorService exectorService = Executors.newScheduledThreadPool(1);
    private static Map<SocketAddress, SocketIOClient> serverMap = new ConcurrentHashMap<>();
    private static Map<SocketAddress, SocketIOClient> clientMap = new ConcurrentHashMap<>();
    private static Set<UUID> idleClientSet = new ConcurrentSet<>();
    private static Queue<ImageData> queue = new ConcurrentLinkedDeque<>();

    public static void main(String[] argv) throws IOException {
        Configuration conf = new Configuration();
        conf.setPort(8080);
        conf.setMaxFramePayloadLength(Integer.MAX_VALUE);
        SocketIOServer server = new SocketIOServer(conf);

        //连接监听器
        server.addConnectListener(client -> logger.info("connection created by :{}", client.getRemoteAddress()));
        //断连监听器
        server.addDisconnectListener(client -> {
            logger.info("disconnect remote address:{}", client.getRemoteAddress());

            //从服务器列表中找出并且删除
            if (serverMap.containsKey(client.getRemoteAddress())) {
                serverMap.remove(client.getRemoteAddress());
            }

            //从客户端列表中找出并且删除
            if (clientMap.containsKey(client.getRemoteAddress())) {
                clientMap.remove(client.getRemoteAddress());
                idleClientSet.remove(client.getSessionId());
            }
            logger.info("serverMap:{} clientMap:{}" , serverMap, clientMap);
        });

        server.addEventListener("CODE", CodeData.class, (socketIOClient, data, ackRequest) ->
                //TODO Input Register
        {
            logger.info("RECEIVE CODE:{} FROM WEB CLIENT:{}", data, socketIOClient.getRemoteAddress());
            String code = data.getCode();
            String sessionId = data.getSessionId();
            SocketIOClient client = server.getClient(UUID.fromString(sessionId));
            client.sendEvent("CODE", code);

            checkQueueAndSendImage(socketIOClient);
        });


        server.addEventListener("SERVER", String.class, (client, data, ackSender) -> {
            logger.info("REGISTER CLIENT AS SERVER:{} , {}", client.getRemoteAddress(), data);
            serverMap.put(client.getRemoteAddress(), client);
        });

        server.addEventListener("CLIENT", String.class, (client, data, ackSender) -> {
            logger.info("REGISTER WEB CLIENT AS CLIENT:{}, {}", client.getRemoteAddress(), data);
            clientMap.put(client.getRemoteAddress(), client);

            checkQueueAndSendImage(client);
        });

        server.addEventListener("IMAGE", String.class, (client, data, ackSender) -> {
            String sessionId = client.getSessionId().toString();
            ImageData imageData = new ImageData();
            imageData.setSessionId(sessionId);
            imageData.setBase64Image(data);
            queue.add(imageData);

            if(!idleClientSet.isEmpty()){
                Optional<UUID> targetSessionId =  idleClientSet.stream().findAny();
                if ( targetSessionId.isPresent() ) {
                    sendImageToWeb(server.getClient(targetSessionId.get()),queue);
                    idleClientSet.remove(targetSessionId.get());
                }
            }

        });

        server.start();
        logger.info("server start");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            logger.info("shutdown server");
        }));

//        File file = new File("/Users/gwen/Desktop/1.png");
//        FileInputStream fileInputStream = new FileInputStream(file);
//        String base64Image = base64.encodeAsString(StreamUtils.read(fileInputStream));
//
//        exectorService.scheduleAtFixedRate(
//                () -> {
//                    server.getAllClients().forEach(s -> {
//                        s.sendEvent("image", base64Image);
//                    });
//
//                }, 0, 5, TimeUnit.SECONDS
//        );
//        ClockTask clockTask = new ClockTask();
//        clockTask.execute(() -> {
//            server.getAllClients().forEach(s -> s.sendEvent("server message" , "fuck"));
//        }, LocalTime.of(15,40,0));

    }

    private static void checkQueueAndSendImage(SocketIOClient socketIOClient) {
        if(!queue.isEmpty()) {
            ImageData imageData = queue.poll();
            logger.debug("imageData:{}", imageData);
            if( imageData != null ) {
                socketIOClient.sendEvent("IMAGE", imageData);
            }else {
                idleClientSet.add(socketIOClient.getSessionId());
            }
        }else{
            idleClientSet.add(socketIOClient.getSessionId());
        }
    }

    public static void sendImageToWeb(SocketIOClient client, Queue<ImageData> queue) {
        ImageData imageData =  queue.poll();
        if( imageData != null ) {
            logger.info("SEND IMAGE TO WEB:{}",client.getRemoteAddress());
            client.sendEvent("IMAGE", imageData);
        }
    }

    public void sendImageToWeb(SocketIOClient client, ImageData imageData) {
        client.sendEvent("IMAGE", imageData);
    }

    public void send(SocketIOServer server, String event, String data, UUID targetSessionId) {
        SocketIOClient client = server.getClient(targetSessionId);
        client.sendEvent(event, data);
    }

    public void send(SocketIOServer server, String event, String data, String targetSessionId) {
        SocketIOClient client = server.getClient(UUID.fromString(targetSessionId));
        client.sendEvent(event, data);
    }

}
