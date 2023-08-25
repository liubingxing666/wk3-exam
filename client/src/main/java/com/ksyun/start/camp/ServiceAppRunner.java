package com.ksyun.start.camp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.start.camp.service.ClientService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 服务启动运行逻辑
 */
@Component
@Slf4j
public class ServiceAppRunner implements ApplicationRunner {

    @Value("${spring.application.name}")
    private String serviceName;
    @Value("${server.port}")
    Integer port;
    private String ipAddress;
    @Value("${spring.application.serviceid}")
    String serviceId;
    Map<String, String> serviceMap = new LinkedHashMap<>();
    @Autowired
    ClientService clientService;
    @Override
    public void run(ApplicationArguments args) throws Exception {

        // 此处代码会在 Boot 应用启动时执行
        // 开始编写你的逻辑，下面是提示
        // 1. 向 registry 服务注册当前服务
        // 2. 定期发送心跳逻辑

        registerService();

        // 注册钩子，在应用正常退出时发送注销请求
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                unregisterService();
            } catch (IOException e) {
                // 在这里处理IOException异常
                e.printStackTrace();
            }
        }));
    }

    //注册服务
    private void registerService() throws IOException {
       // serviceId= UUID.randomUUID().toString();
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
            String responseBody = response.body().string();
            System.out.println("Response Body: " + responseBody);
        } else {
            System.out.println("Request failed! Response code: " + response.code());
        }
        log.info("本服务信息为：{}",serviceMap);
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

    //心跳60s定时执行
    @Scheduled(fixedRate = 60000)
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

    //TODO
    @Scheduled(fixedRate = 1000) // 每1秒执行一次定时任务
    public void sendLogToServer() throws IOException {
        Map<String, Object> logData = new LinkedHashMap<>();
        // 获取当前时间，并格式化为"yyyy-MM-dd HH:mm:ss.SSS"的字符串形式
        ApiResponse info= clientService.getInfo("allfull");
        if(info.getCode()==200)
        {
            String jsonString = info.toString();
//        System.out.println("info:"+info );
//        ObjectMapper mapper = new ObjectMapper();
//        String jsonString = mapper.writeValueAsString(info);
            //带毫秒时间
            int resultIndex = jsonString.indexOf("result=");
            int endIndex = jsonString.indexOf(")", resultIndex);
            String resultTime = jsonString.substring(resultIndex + 7, endIndex-1);
            log.info("带时间戳的GMT时间：{}",resultTime);
            // 构建日志信息
            logData.put("serviceName", serviceName);
            logData.put("serviceId", serviceId);
            logData.put("datetime", resultTime);
            logData.put("level", "info");
            logData.put("message", "Client status is OK.");
            log.info("写入的日志信息为：{}",logData);
            // 使用 Jackson 将日志信息转换为 JSON 字符串
//        ObjectMapper objectMapper = new ObjectMapper();
//        String logJson = objectMapper.writeValueAsString(logData);
        }else{
            logData.put("serviceName", serviceName);
            logData.put("serviceId", serviceId);
            logData.put("datetime", null);
            logData.put("level", "info");
            logData.put("message", "时间服务故障,Client无法使用时间服务");
            log.info("写入的日志信息为：{}",logData);
        }
        try {
            // 创建OkHttpClient实例
            OkHttpClient client = new OkHttpClient();
            // 将logData映射转换为JSON字符串
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonLogData = objectMapper.writeValueAsString(logData);
            // 构建POST请求
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(mediaType, jsonLogData);
            String url = "http://localhost:8320/api/logging";
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                log.info("请求成功！响应内容：" + responseBody);
            } else {
                System.out.println("请求失败！响应状态码：" + response.code());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

