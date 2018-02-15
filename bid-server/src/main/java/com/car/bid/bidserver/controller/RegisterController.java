package com.car.bid.bidserver.controller;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RestController
@Slf4j
public class RegisterController {

    private final Map<String, Socket> availableAddress = new ConcurrentHashMap<>();
    private final Map<String, State> state = new ConcurrentHashMap<>();

    private Executor executor = Executors.newFixedThreadPool(10);

    @RequestMapping(value = "/register")
    public String register(@RequestHeader("Remote_Addr") String remoteAddr){
        log.info("启动注册服务:{}", remoteAddr);
        executor.execute(()-> {
            try {
                Socket socket = IO.socket("http://" + remoteAddr + ":8080");
                socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... objects) {
                        availableAddress.put(remoteAddr, socket);
                        state.put(remoteAddr,State.IDLE);
                        log.info("注册服务成功:{}", socket);
                    }
                });
                socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener(){

                    @Override
                    public void call(Object... objects) {
                        availableAddress.remove(remoteAddr,socket);
                        state.remove(remoteAddr);
                        socket.close();
                        log.info("连接断开，注销服务:{}", socket);
                    }
                });
                socket.on("CONNECT_APPLICATION", new Emitter.Listener(){

                    @Override
                    public void call(Object... objects) {
                        state.put(remoteAddr, State.BUSY);
                    }
                });

                socket.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        });
        return "success";
    }

    @RequestMapping(value = "/getIp")
    public String getIp() {
        String foundIp = null;
        synchronized (this.state) {
            Optional<Map.Entry<String,State>> found = this.state.entrySet().stream().filter(stringStateEntry -> stringStateEntry.getValue().equals(State.IDLE)).findAny();
            if(found.isPresent()){
                foundIp = found.get().getKey();
//                this.state.put(foundIp,State.BUSY);
            }
        }
        return foundIp;
    }

    @RequestMapping(value = "/connect-to-busy")
    public String connectToServer(@RequestParam String ip){
        String newIp = null;
        synchronized (this.state) {
            Optional<Map.Entry<String,State>> found = this.state.entrySet().stream().filter(stringStateEntry -> stringStateEntry.getKey().equals(ip)).findAny();
            if(found.isPresent() && found.get().getValue().equals(State.IDLE)){
                this.state.put(ip,State.BUSY);
            } else {
                found = this.state.entrySet().stream().filter(stringStateEntry -> stringStateEntry.getValue().equals(State.IDLE)).findAny();
                if ( found.isPresent() ) {
                    newIp = found.get().getKey();
                }
            }
        }
        return newIp;
    }
}
