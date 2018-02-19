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
        return getRectangle(inputStream, properties);
    }

    private static Rectangle getRectangle(InputStream inputStream, Properties properties) throws IOException {
        Integer x = new Double(properties.getProperty("x")).intValue();
        Integer y = new Double(properties.getProperty("y")).intValue();
        Integer width = new Double(properties.getProperty("width")).intValue();
        Integer height = new Double(properties.getProperty("height")).intValue();


        inputStream.close();
        return new Rectangle(x,y ,width, height);
    }

    public static Rectangle readCodePosition() throws IOException {
        File file = new File("code.properties");
        InputStream inputStream = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(inputStream);
        return getRectangle(inputStream, properties);
    }

    public static Point readInputCode() throws IOException{

        File file = new File("input-code.properties");
        InputStream inputStream = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(inputStream);
        return getPoint(inputStream, properties);
    }

    private static Point getPoint(InputStream inputStream, Properties properties) throws IOException {
        Integer x = new Double(properties.getProperty("x")).intValue();
        Integer y = new Double(properties.getProperty("y")).intValue();
        Integer width = new Double(properties.getProperty("width")).intValue();
        Integer height = new Double(properties.getProperty("height")).intValue();

        inputStream.close();
        return new Point(x + width/2 ,y +  height /2 );
    }

    public static Point readInputPricePosition() throws IOException {
        File file = new File("input-price.properties");
        InputStream inputStream = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(inputStream);
        return getPoint(inputStream, properties);
    }

    public static Point readBidButton() throws IOException {
        File file = new File("bid.properties");
        InputStream inputStream = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(inputStream);
        return getPoint(inputStream, properties);
    }

    public static Point readSubmitButton() throws IOException {
        File file = new File("submit.properties");
        InputStream inputStream = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(inputStream);
        return getPoint(inputStream, properties);
    }

}
