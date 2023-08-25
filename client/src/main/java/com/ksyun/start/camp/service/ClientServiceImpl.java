package com.ksyun.start.camp.service;

import com.ksyun.start.camp.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sun.security.timestamp.TSRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 客户端服务实现
 */

@Component
public class ClientServiceImpl implements ClientService {

    @Autowired
    TimeServiceImpl timeService;
    @Value("${spring.application.name}")
    String serviceName;
    @Value("${spring.application.serviceid}")
    String serviceId;
    @Autowired
    ApiResponse apiResponse;

    @Override
    public ApiResponse getInfo(String style) {
        Map<String, Object> infoMap = new LinkedHashMap<>();
        String time = null;
        time = timeService.getDateTime(style);
        if (time.equals("failed")) {
            infoMap.put("error","时间服务器出现了故障，请您重试一下");
            infoMap.put("result","null");
            apiResponse.setCode(404);
            apiResponse.setData(infoMap);
        } else {
            if(style.equals("full")){
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(time, formatter);
                LocalDateTime updatedDateTime = dateTime.plusHours(8);
                String resultTime = updatedDateTime.format(formatter);
                infoMap.put("error","null");
                String resultString = String.format("Hello Kingsoft Clound Star Camp - %s - %s",serviceId,resultTime);
                infoMap.put("result",resultString);
                apiResponse.setCode(200);
                apiResponse.setData(infoMap);
            }else{
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                LocalDateTime dateTime = LocalDateTime.parse(time, formatter);
                //LocalDateTime updatedDateTime = dateTime.plusHours(8);
               // String resultTime = updatedDateTime.format(formatter);
                String resultTime = dateTime.format(formatter);
                infoMap.put("error","null");
                infoMap.put("result",resultTime);
                apiResponse.setCode(200);
                apiResponse.setData(infoMap);
            }

        }
        // 开始编写你的逻辑，下面是提示
        // 1. 调用 TimeService 获取远端服务返回的时间
        // 2. 获取到自身的 serviceId 信息
        // 3. 组合相关信息返回
        return apiResponse;
    }
}
