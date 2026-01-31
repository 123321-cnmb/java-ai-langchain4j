/*
 * 聊天表单数据传输对象
 * 用于封装前端发送的聊天请求数据
 */
package com.atguigu.java.ai.langchain4j.bean;

import lombok.Data;

/**
 * 聊天表单数据传输对象
 * 封装与AI助手对话所需的参数信息
 */
@Data
public class ChatForm {
    private Long memoryId; // 对话会话ID，用于区分不同的对话会话
    private String message; // 用户发送的消息内容
}