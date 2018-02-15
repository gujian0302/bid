package com.bid.car.client.property;

import com.bid.car.client.Server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    public Server readConfig() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("/application.properties");
        Properties properties = new Properties();
        properties.load(inputStream);
        String host = properties.getProperty("host");
        String port = properties.getProperty("port");
        Server server = new Server();
        server.setHost(host);
        server.setPort(Integer.valueOf(port));
        return server;
    }
}
