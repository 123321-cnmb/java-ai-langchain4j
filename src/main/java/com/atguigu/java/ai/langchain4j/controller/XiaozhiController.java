/*
 * 小智AI助手控制器，提供REST API接口
 * 该控制器处理前端发送的聊天请求，并调用AI助手进行响应
 */
package com.atguigu.java.ai.langchain4j.controller;

import com.atguigu.java.ai.langchain4j.assistant.XiaozhiAgent;
import com.atguigu.java.ai.langchain4j.bean.ChatForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 小智AI助手控制器
 * 提供与AI助手交互的REST API接口，支持流式响应
 */
@Tag(name = "硅谷小智")
@RestController
@RequestMapping("/xiaozhi")
public class XiaozhiController {
    @Autowired
    private XiaozhiAgent xiaozhiAgent;
    
    /**
     * 处理聊天请求
     * 接收前端发送的聊天表单数据，调用AI助手进行对话并返回流式响应
     *
     * @param chatForm 包含会话ID和用户消息的聊天表单
     * @return 返回字符串流，实现流式响应
     */
    @Operation(summary = "对话")
    @PostMapping(value = "/chat", produces = "text/stream;charset=utf-8")
    public Flux<String> chat(@RequestBody ChatForm chatForm) {
        return xiaozhiAgent.chat(chatForm.getMemoryId(), chatForm.getMessage());
    }
}