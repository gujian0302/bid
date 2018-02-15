package com.bid.car;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;

public class StreamUtilsTest {

    @Test
    public void test() throws IOException {
        FileInputStream fileInputStream = new FileInputStream("/Users/gwen/Desktop/1.png");
        System.out.println(new String(Base64.getEncoder().encode(StreamUtils.read(fileInputStream))));
    }
}
