package com.ksyun.start.camp.service;

import com.ksyun.start.camp.ApiResponse;
import com.ksyun.start.camp.LogData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 日志服务的实现
 */
@Component
@Slf4j
public class LoggingServiceImpl implements LoggingService {

    private List<LogData> logStorage = new ArrayList<>();
    @Autowired
    ApiResponse apiResponse;
    Integer index=1;

    public ApiResponse loggingInfo(LogData logData) {

        if (!logStorage.contains(logData)) {
            logData.setLogId(index.toString());
            index++;
            logStorage.add(logData);
            log.info("请求写入的日志为" + logData);
            apiResponse.setCode(200);
            apiResponse.setData("日志写入成功！");
        }
        else{
            apiResponse.setCode(500);
            apiResponse.setData("日志写入失败！");
        }
        return apiResponse;
    }


    public ApiResponse loggingList(String service) {
        if (service == null) {
            List<LogData> reversedLogStorage = new ArrayList<>(logStorage);
            Collections.reverse(reversedLogStorage);
            apiResponse.setCode(200);
            apiResponse.setData(reversedLogStorage);
            return apiResponse;
        } else {
            List<LogData> filteredLogs = new ArrayList<>();
            // 对logStorage进行逆序处理
            List<LogData> reversedLogStorage = new ArrayList<>(logStorage);
            Collections.reverse(reversedLogStorage);
            for (LogData logData : reversedLogStorage) {
                if (logData.getServiceId().equals(service)) {
                    filteredLogs.add(logData);
                }
                if (filteredLogs.size() >= 5) {
                    break;
                }
            }
            apiResponse.setCode(200);
            apiResponse.setData(filteredLogs);
            return apiResponse;
        }
    }
}
