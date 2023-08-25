package com.ksyun.start.camp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 基于 Spring Boot 实现的微服务
 * 日志收集服务
 */
@EnableScheduling
@SpringBootApplication
public class LoggingServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(LoggingServiceApp.class, args);
    }
}
