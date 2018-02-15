package com.bid.car.client;

import com.bid.car.client.property.Config;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main {

    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    public static void main(String[] argv) throws IOException, URISyntaxException {
        Config config = new Config();
        Server server = config.readConfig();

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

        socket.on("CODE", new Emitter.Listener() {
            @Override
            public void call(Object... objects) {
                log.info("RECEIVE CODE:{}", objects);
                //TODO SAVE CODE TO STORE
            }
        });

        socket.connect();

        File file = new File("/Users/gwen/Desktop/1.png");
        FileInputStream fileInputStream = new FileInputStream(file);
        String base64Image = Base64.getEncoder().encodeToString(StreamUtils.read(fileInputStream));
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                socket.emit("IMAGE", base64Image);
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}

