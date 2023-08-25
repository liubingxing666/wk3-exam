package com.ksyun.start.camp;

import lombok.Data;

import java.io.Serializable;

@Data
public class ServiceInformation implements Serializable {
    private String serviceId;
    private String serviceName;
    private String ipAddress;
    private Integer port;
    private transient long lastHeartbeatTime; // 标记为 transient，不进行序列化

    // 更新心跳时间
    public void updateHeartbeatTime() {
        this.lastHeartbeatTime = System.currentTimeMillis();
    }

    // 判断服务实例是否过期
    public boolean isExpired(long heartbeatInterval) {
        return System.currentTimeMillis() - this.lastHeartbeatTime > heartbeatInterval;
    }
}