/*
 * 聊天消息实体类，用于MongoDB存储
 * 该实体类映射到MongoDB中的chat_messages集合，用于存储聊天记录
 */
package com.atguigu.java.ai.langchain4j.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 聊天消息实体类
 * 用于在MongoDB中存储聊天记录的JSON字符串
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("chat_messages")
public class ChatMessages {
    // 唯一标识，映射到 MongoDB 文档的 _id 字段
    @Id
    private ObjectId messageId;
    //private Long messageId;
    private String content; // 存储当前聊天记录列表的json字符串
}