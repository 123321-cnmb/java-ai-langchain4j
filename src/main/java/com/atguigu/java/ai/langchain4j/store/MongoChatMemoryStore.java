/*
 * MongoDB聊天记忆存储实现
 * 实现ChatMemoryStore接口，将聊天记忆存储到MongoDB中
 */
package com.atguigu.java.ai.langchain4j.store;

import com.atguigu.java.ai.langchain4j.bean.ChatMessages;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * MongoDB聊天记忆存储实现
 * 实现ChatMemoryStore接口，提供基于MongoDB的聊天记忆存储功能
 */
@Component
public class MongoChatMemoryStore implements ChatMemoryStore {
    @Autowired
    private MongoTemplate mongoTemplate;
    
    /**
     * 获取指定记忆ID的聊天消息列表
     * 从MongoDB中查询并反序列化聊天消息
     *
     * @param memoryId 记忆ID，用于区分不同的对话会话
     * @return 返回聊天消息列表，如果未找到则返回空列表
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        ChatMessages chatMessages = mongoTemplate.findOne(query, ChatMessages.class);
        if(chatMessages == null) return new LinkedList<>();
        return ChatMessageDeserializer.messagesFromJson(chatMessages.getContent());
    }
    
    /**
     * 更新指定记忆ID的聊天消息列表
     * 将聊天消息序列化后存储到MongoDB中，如果不存在则新增
     *
     * @param memoryId 记忆ID，用于区分不同的对话会话
     * @param messages 要更新的聊天消息列表
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("content", ChatMessageSerializer.messagesToJson(messages));
        // 根据query条件能查询出文档，则修改文档；否则新增文档
        mongoTemplate.upsert(query, update, ChatMessages.class);
    }
    
    /**
     * 删除指定记忆ID的聊天消息
     * 从MongoDB中删除对应记忆ID的聊天记录
     *
     * @param memoryId 记忆ID，用于区分不同的对话会话
     */
    @Override
    public void deleteMessages(Object memoryId) {
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        mongoTemplate.remove(query, ChatMessages.class);
    }
}