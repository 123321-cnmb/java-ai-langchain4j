/*
 * 小智AI助手接口，提供智能客服功能
 * 该接口集成了流式聊天、记忆管理、工具调用和内容检索功能
 */
package com.atguigu.java.ai.langchain4j.assistant;

import dev.langchain4j.service.*;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;
import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * 小智AI助手接口
 * 提供医院智能客服功能，支持流式响应、会话记忆、工具调用和知识库检索
 */
@AiService(
        wiringMode = EXPLICIT,
        streamingChatModel = "qwenStreamingChatModel",
        chatMemoryProvider = "chatMemoryProviderXiaozhi",
        tools = "appointmentTools",
        contentRetriever = "contentRetrieverXiaozhiPincone"
)
public interface XiaozhiAgent {
    /**
     * 与AI助手进行对话
     * 使用流式响应返回AI的回复内容，支持会话记忆和系统提示词
     *
     * @param memoryId   会话记忆ID，用于区分不同的对话会话
     * @param userMessage 用户发送的消息内容
     * @return 返回字符串流，实现流式响应
     */
    @SystemMessage(fromResource = "zhaozhi-prompt-template.txt")
    Flux<String> chat(@MemoryId Long memoryId, @UserMessage String userMessage);
}