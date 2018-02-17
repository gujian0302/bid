package com.bid.car.client.property;

import com.bid.car.client.Server;

import java.awt.*;
import java.io.*;
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

    public static Rectangle readLowestPricePosition() throws IOException {
        File file = new File("lowest-price.properties");
        InputStream inputStream = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(inputStream);
        Integer x = Integer.valueOf(properties.getProperty("x"));
        Integer y = Integer.valueOf(properties.getProperty("y"));
        Integer width = Integer.valueOf(properties.getProperty("width"));
        Integer height = Integer.valueOf(properties.getProperty("height"));


        inputStream.close();
        return new Rectangle(x,y ,width, height);
    }

    public static Rectangle readCodePosition() throws IOException {
        File file = new File("code.properties");
        InputStream inputStream = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(inputStream);
        Integer x = Integer.valueOf(properties.getProperty("x"));
        Integer y = Integer.valueOf(properties.getProperty("y"));
        Integer width = Integer.valueOf(properties.getProperty("width"));
        Integer height = Integer.valueOf(properties.getProperty("height"));

        inputStream.close();
        return new Rectangle(x,y ,width, height);
    }

    public static Point readInputPricePosition() throws IOException {
        File file = new File("input-price.properties");
        InputStream inputStream = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(inputStream);
        Integer x = Integer.valueOf(properties.getProperty("x"));
        Integer y = Integer.valueOf(properties.getProperty("y"));
        Integer width = Integer.valueOf(properties.getProperty("width"));
        Integer height = Integer.valueOf(properties.getProperty("height"));

        inputStream.close();
        return new Point(x+width/2,y + height/2);
    }

    public static Point readBidButton() throws IOException {
        File file = new File("bid.properties");
        InputStream inputStream = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(inputStream);
        Integer x = Integer.valueOf(properties.getProperty("x"));
        Integer y = Integer.valueOf(properties.getProperty("y"));
        Integer width = Integer.valueOf(properties.getProperty("width"));
        Integer height = Integer.valueOf(properties.getProperty("height"));

        inputStream.close();
        return new Point(x+width/2,y + height/2);
    }

    public static Point readSubmitButton() throws IOException {
        File file = new File("submit.properties");
        InputStream inputStream = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(inputStream);
        Integer x = Integer.valueOf(properties.getProperty("x"));
        Integer y = Integer.valueOf(properties.getProperty("y"));
        Integer width = Integer.valueOf(properties.getProperty("width"));
        Integer height = Integer.valueOf(properties.getProperty("height"));

        inputStream.close();
        return new Point(x+width/2,y + height/2);
    }

}
