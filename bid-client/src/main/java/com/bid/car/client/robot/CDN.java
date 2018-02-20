package com.bid.car.client.robot;

import com.aliyun.oss.OSSClient;

import java.io.ByteArrayInputStream;
import java.util.UUID;

public class CDN {
    final static private String hostName="http://omni-img-test.oss-cn-shanghai.aliyuncs.com";

    public static String upload(byte[] imageData, OSSClient ossClient,String buckName){
        String key = UUID.randomUUID().toString() + ".jpg";
        ossClient.putObject(buckName,key, new ByteArrayInputStream(imageData) );
        return hostName + "/" + key;
    }
}
