package com.ksyun.start.camp;

import lombok.Data;

import java.util.Objects;

@Data
public class LogData {
    private String logId; // 日志ID，需要保证有序且无重复
    private String serviceName;
    private String serviceId;
    private String datetime;
    private String level;
    private String message;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogData logData = (LogData) o;
        return Objects.equals(serviceName, logData.serviceName) &&
                Objects.equals(serviceId, logData.serviceId) &&
                Objects.equals(datetime, logData.datetime) &&
                Objects.equals(level, logData.level) &&
                Objects.equals(message, logData.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, serviceId, datetime, level, message);
    }

}
