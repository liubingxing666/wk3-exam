package com.ksyun.start.camp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksyun.start.camp.service.TimeService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 代表远端时间服务接口实现
 */
@Slf4j
@Component
public class TimeServiceImpl implements TimeService {

    List <String>serverListURLS =new ArrayList<>();
    int currentIndex = 0;
    @Override
    public String getDateTime(String style) {
        // 开始编写你的逻辑，下面是提示
        // 1. 连接到 registry 服务，获取远端服务列表
        // 2. 从远端服务列表中获取一个服务实例
        // 3. 执行远程调用，获取指定格式的时间
        String requestURL=null;
        requestURL= getNextServiceURL();
        log.info("远程调用的服务url为：{}",requestURL+style);
        if(requestURL.equals("failed")){
            return "failed";
        }
        String timeResult= executeRemoteCall(requestURL+style);
        return timeResult;
    }

    // 轮询获取服务实例URL
    @Scheduled(fixedRate = 5000)
    private String getNextServiceURL() {
        serverListURLS.clear();
        List<Map<String,String>>serverList=new ArrayList<Map<String,String>>();
        String registerURL="http://localhost:8180/api/discovery/lunxun?name=time-service";
        // Create OkHttpClient instance
        OkHttpClient client = new OkHttpClient();
        // Build the Request
        Request request = new Request.Builder().url(registerURL).get().build();
        try {
            Response response = client.newCall(request).execute();
            ObjectMapper objectMapper = new ObjectMapper();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
               // System.out.println("skahjhka:"+responseBody);
                // 将JSON字符串解析为JsonNode对象
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                // 获取data字段的值
                JsonNode dataNode = jsonNode.get("data");
                String secondBody=dataNode.toString();
                serverList = objectMapper.readValue(secondBody, List.class);
               log.info("远程服务有:{} ", secondBody);
                if(serverList.isEmpty()){
                    return "failed";
                }
                for (Map<String, String> serverInfo : serverList) {
                    serverListURLS.add("http://"+serverInfo.get("ipAddress")+":"+serverInfo.get("port")+"/api/getDateTime?style=");
                }
                int index = currentIndex % serverListURLS.size();
                // 根据索引获取服务实例URL
                String serviceURL = serverListURLS.get(index);
                // 增加索引，以便下次选择下一个服务实例
                currentIndex++;
                response.close();
                return serviceURL;
                //System.out.println("serverList:"+serverList);
               // System.out.println("serverListURLS"+serverListURLS);
            } else {
                System.out.println("Request failed! Response code: " + response.code());
                return "failed";
            }
        } catch (IOException e) {
            System.err.println("Error sending GET request: " + e.getMessage());
        }
        // 获取当前选择的服务实例索引
        return "failed";
    }

    // 执行远程调用，获取指定格式的时间
    private String executeRemoteCall(String serviceURL) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(serviceURL)
                .get()
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                log.info("lbx tag:"+responseBody);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(responseBody);
                // 提取result字段的值
                String resultValue = jsonNode.get("data").get("result").asText();
                //获取到的时间
                log.info("time:{}",resultValue);

                //转化为北京时间


                return resultValue;
            }
            response.close();
        } catch (IOException e) {
            System.err.println("错误信息:" + e.getMessage());
        }
        return null;
    }

}
