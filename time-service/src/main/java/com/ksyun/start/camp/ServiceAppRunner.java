package com.ksyun.start.camp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * 服务启动运行逻辑
 */

@Component
public class ServiceAppRunner implements ApplicationRunner {
    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${server.port}")
    Integer port;

    @Value("${spring.application.serviceid}")
    String serviceId;

    private String ipAddress;
    Map<String, String> serviceMap = new LinkedHashMap<>();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 此处代码会在 Boot 应用启动时执行
        // 开始编写你的逻辑，下面是提示
        // 1. 向 registry 服务注册当前服务
        // 2. 定期发送心跳逻辑
        registerService();

        //在应用正常退出时发送注销请求
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                unregisterService();
            } catch (IOException e) {
                // 在这里处理IOException异常
                e.printStackTrace();
            }
        }));
    }


    //TODO
    @Scheduled(fixedRate = 60000) // 每60秒执行一次定时任务
    public void sendHeartbeat() throws IOException {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, new ObjectMapper().writeValueAsString(serviceMap));
        // 构建 POST 请求
        String registerURL="http://localhost:8180/api/heartbeat";
        Request request = new Request.Builder()
                .url(registerURL)
                .post(requestBody)
                .build();
        // 发送请求
        Response response = client.newCall(request).execute();
        // 处理响应
        if (response.isSuccessful()) {
            System.out.println("Request succeeded!");
            String responseBody = response.body().string();
            System.out.println("Response Body: " + responseBody);
        } else {
            System.out.println("Request failed! Response code: " + response.code());
        }
        System.out.println(serviceMap);
    }
    //注册服务
    private void registerService() throws IOException {
//        serviceId= UUID.randomUUID().toString();
        ipAddress="127.0.0.1";
        serviceMap.put("serviceName", serviceName);
        serviceMap.put("serviceId", serviceId);
        serviceMap.put("ipAddress", ipAddress);
        serviceMap.put("port", port.toString());
        serviceMap.put("lastHeartbeatTime",String.valueOf(System.currentTimeMillis()));
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, new ObjectMapper().writeValueAsString(serviceMap));
        // 构建 POST 请求
        String registerURL="http://localhost:8180/api/register";
        Request request = new Request.Builder()
                .url(registerURL)
                .post(requestBody)
                .build();
        // 发送请求
        Response response = client.newCall(request).execute();
        // 处理响应
        if (response.isSuccessful()) {
            System.out.println("Request succeeded!");
            String responseBody = response.body().string();
            System.out.println("Response Body: " + responseBody);
        } else {
            System.out.println("Request failed! Response code: " + response.code());
        }
        System.out.println(serviceMap);
    }
    //注销服务
    private void unregisterService() throws IOException {
        Map<String, String> serviceMap = new LinkedHashMap<>();
        serviceMap.put("serviceName", serviceName);
        serviceMap.put("serviceId", serviceId);
        serviceMap.put("ipAddress", ipAddress);
        serviceMap.put("port", port.toString());
        // 发送注销请求
        // 使用 OkHttp 或其他方式发送 POST 请求到注册中心的 /api/unregister 接口，将服务信息从注册中心注销
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(mediaType, new ObjectMapper().writeValueAsString(serviceMap));
        // 构建 POST 请求
        String registerURL="http://localhost:8180/api/unregister";
        Request request = new Request.Builder()
                .url(registerURL)
                .post(requestBody)
                .build();
        // 发送请求
        Response response = client.newCall(request).execute();
        // 处理响应
        if (response.isSuccessful()) {
            System.out.println("Request succeeded!");
            String responseBody = response.body().string();
            System.out.println("Response Body: " + responseBody);
        } else {
            System.out.println("Request failed! Response code: " + response.code());
        }
        System.out.println(serviceMap);

    }






}
