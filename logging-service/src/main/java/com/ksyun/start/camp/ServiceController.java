package com.ksyun.start.camp;

import com.ksyun.start.camp.service.LoggingService;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.ServiceInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 实现日志服务 API
 */
@RestController
@Slf4j
@RequestMapping("/api")
public class ServiceController {
    ApiResponse apiResponse;
    public LoggingService loggingService;
    private List<LogData> logStorage = new ArrayList<>();
    Integer index = 1;

    public ServiceController(ApiResponse apiResponse, LoggingService loggingService) {
        this.apiResponse = apiResponse;
        this.loggingService = loggingService;
    }

    // TODO: 实现日志服务 API
    @PostMapping("/logging")
    public ApiResponse loggingInformation(@RequestBody LogData logData) {
       return loggingService.loggingInfo(logData);
    }

    @GetMapping("/list")
    public ApiResponse loggingList(@RequestParam(required = false) String service) {
        return loggingService.loggingList(service);
    }
}

