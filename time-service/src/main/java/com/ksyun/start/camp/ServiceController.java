package com.ksyun.start.camp;

import com.ksyun.start.camp.service.SimpleTimeService;
import com.ksyun.start.camp.service.SimpleTimeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Component
@RequestMapping("/api")
public class ServiceController {

    @Autowired
    SimpleTimeService simpleTimeService;
    @Autowired
    ApiResponse apiResponse;

    @Value("${spring.application.serviceid}")
    String serviceId;
    // 在此实现简单时间服务的接口逻辑
    // 1. 调用 SimpleTimeService
//    @GetMapping("/getDateTime")
//    public String getDataTime(String style) {
//        return simpleTimeService.getDateTime(style);
//    }
    @GetMapping("/getDateTime")
    public ApiResponse getDataTime(String style) {
        String time=null;
        Map<String,String>resutMap=new LinkedHashMap<>();
        time= simpleTimeService.getDateTime(style);
        if(time!=null){
            resutMap.put("result",time);
            resutMap.put("serviceId",serviceId);
            apiResponse.setCode(200);
            apiResponse.setData(resutMap);
        }
        return apiResponse;
    }


}
