package com.bid.car;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class StreamUtils {

    public static byte[] read(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] array = new byte[1024];
        int byteRead = -1;
        while ( (byteRead = inputStream.read(array) ) != -1){
            byteArrayOutputStream.write(array, 0 , byteRead);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
