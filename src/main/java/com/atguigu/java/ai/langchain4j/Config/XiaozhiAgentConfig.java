/*
 * 小智AI助手配置类
 * 该配置类提供了聊天记忆提供者和内容检索器的配置
 */
package com.atguigu.java.ai.langchain4j.Config;

import com.atguigu.java.ai.langchain4j.store.MongoChatMemoryStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * 小智AI助手配置类
 * 配置聊天记忆提供者和内容检索器，用于支持AI助手的记忆功能和知识库检索功能
 */
@Configuration
public class XiaozhiAgentConfig {
    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;

    @Autowired
    private EmbeddingStore embeddingStore;
    @Autowired
    private EmbeddingModel embeddingModel;

    /**
     * 创建聊天记忆提供者
     * 为小智AI助手提供基于MongoDB的聊天记忆功能，支持最多保存20条消息
     *
     * @return 返回聊天记忆提供者实例
     */
    @Bean
    ChatMemoryProvider chatMemoryProviderXiaozhi() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .chatMemoryStore(mongoChatMemoryStore)
                .build();
    }

    /**
     * 创建内容检索器（基于本地知识库）
     * 从本地文件系统加载知识库文档并创建内容检索器（使用内存向量存储）
     * 注：此方法已被注释，在生产环境中使用contentRetrieverXiaozhiPincone
     *
     * @return 返回内容检索器实例
     */
    @Bean
    ContentRetriever contentRetrieverXiaozhi() {
        Document document1 = FileSystemDocumentLoader.loadDocument("D:/knowledge/医院信息.md");
        Document document2 = FileSystemDocumentLoader.loadDocument("D:/knowledge/科室信息.md");
        Document document3 = FileSystemDocumentLoader.loadDocument("D:/knowledge/神经内科.md");
        List<Document> documents = Arrays.asList(document1, document2, document3);
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor.ingest(documents, embeddingStore);
        return EmbeddingStoreContentRetriever.from(embeddingStore);
    }

    /**
     * 创建内容检索器（基于Pinecone向量数据库）
     * 使用Pinecone向量数据库创建内容检索器，支持精确匹配和相似度检索
     *
     * @return 返回基于Pinecone的内容检索器实例
     */
    @Bean
    ContentRetriever contentRetrieverXiaozhiPincone() {
        return EmbeddingStoreContentRetriever
                .builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(1)  // 最大返回结果数
                .minScore(0.8)  // 最小相似度分数
                .build();
    }
}