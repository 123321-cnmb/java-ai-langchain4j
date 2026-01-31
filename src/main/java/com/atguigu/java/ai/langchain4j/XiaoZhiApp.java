/*
 * 小智AI助手应用程序主类
 * 该类是Spring Boot应用程序的入口点
 */
package com.atguigu.java.ai.langchain4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 小智AI助手应用程序主类
 * 作为Spring Boot应用程序的启动类，负责初始化整个应用
 */
@SpringBootApplication
public class XiaoZhiApp {
    /**
     * 应用程序入口方法
     * 启动Spring Boot应用程序并初始化所有配置的组件
     *
     * @param args 启动应用程序时传入的命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(XiaoZhiApp.class, args);
    }
}