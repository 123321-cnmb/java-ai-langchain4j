package com.atguigu.java.ai.langchain4j.Config;

import com.atguigu.java.ai.langchain4j.assistant.XiaozhiAgent;
import com.atguigu.java.ai.langchain4j.handler.CallHandler;
import com.atguigu.java.ai.langchain4j.handler.VoiceHandler;
import com.atguigu.java.ai.langchain4j.service.VoiceService;
import com.atguigu.java.ai.langchain4j.utils.AliyunTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private VoiceService voiceService;

    @Autowired
    private AliyunTokenUtil aliyunTokenUtil;

    // 1. 添加注入 XiaozhiAgent
    @Autowired
    private XiaozhiAgent xiaozhiAgent;

    @Value("${aliyun.tts.appKey}")
    private String appKey;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 2. 注册普通语音识别
        registry.addHandler(new VoiceHandler(aliyunTokenUtil, appKey), "/voice-asr")
                .setAllowedOrigins("*");

        // 3. 【核心修复】在构造函数中补全 xiaozhiAgent 参数
        registry.addHandler(new CallHandler(voiceService, xiaozhiAgent, aliyunTokenUtil, appKey), "/voice-call")
                .setAllowedOrigins("*");
    }
}