package com.ksyun.start.camp.service;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * 代表简单时间服务实现
 */
@Component
public class SimpleTimeServiceImpl implements SimpleTimeService {

    @Override
    public String getDateTime(String style) {
        //获取GMT时间
        Instant now = Instant.now();
        ZonedDateTime currentTime = now.atZone(ZoneId.of("GMT"));

        //带毫秒的时间格式
        DateTimeFormatter fullTimeWithMs = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String resultfullTimeWithMs=currentTime.format(fullTimeWithMs);
        //获得标准时间格式
        DateTimeFormatter fullTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String resultFullTime= currentTime.format(fullTime);
        //获取日期时间
        DateTimeFormatter dataTime = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String resultDataTime= currentTime.format(dataTime);
        //只包含时间部分
        DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm:ss");
        String resultTime= currentTime.format(time);
        //时间戳
        String unixTimestampInMillis =  String.valueOf(currentTime.toInstant().toEpochMilli()) ;
        // 开始编写简单时间服务的核心逻辑
        // 获取时间、格式化时间、返回
        switch (style) {
            case "full":
                return resultFullTime; // 完整格式，形如 2023-07-25T12:34:56
            case "date":
                return resultDataTime; // 只含有日期部分，如 2023-07-25
            case "time":
                return resultTime; // 只含有时间部分，如 12:34:56
            case "unix":
                return unixTimestampInMillis;
            case "allfull":
                return resultfullTimeWithMs;
            default:
                return "style输入类型有误"; // 参数错误，返回错误信息
        }
    }
}
