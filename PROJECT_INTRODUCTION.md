# 小智AI助手项目介绍

## 目录
1. [项目概述](#项目概述)
2. [技术背景](#技术背景)
3. [核心功能](#核心功能)
4. [系统架构](#系统架构)
5. [技术实现细节](#技术实现细节)
6. [部署与运行](#部署与运行)
7. [开发指南](#开发指南)
8. [扩展与优化](#扩展与优化)

## 项目概述

小智AI助手是一个基于Spring Boot和LangChain4j框架构建的智能客服系统，专门针对医院场景设计。该项目结合了最新的大语言模型技术、语音识别与合成技术，以及向量数据库检索技术，为用户提供智能化的医院咨询服务。

### 主要特点
- **智能对话**: 支持自然语言交互，能够理解用户意图并给出准确回答
- **语音交互**: 集成阿里云语音服务，支持语音输入输出
- **预约管理**: 提供预约挂号、取消预约等功能
- **会话记忆**: 基于MongoDB的会话管理，支持上下文连续对话
- **知识库检索**: 结合医院知识库，提供专业的医疗咨询

## 技术背景

### LangChain4j框架
LangChain4j是Java平台上的大语言模型开发框架，提供了丰富的AI应用开发工具。本项目利用其以下特性：
- AI服务抽象：通过注解简化AI应用开发
- 记忆管理：提供对话历史管理功能
- 工具调用：支持AI调用外部工具执行特定任务
- RAG技术：检索增强生成，提升回答准确性

### 大语言模型
项目集成了多个大语言模型服务：
- **阿里云通义千问**: 主要对话模型，支持流式响应
- **DeepSeek推理模型**: 专门用于复杂逻辑推理

### 向量数据库
使用Pinecone向量数据库存储医院知识库，支持语义相似度检索，使AI能够基于专业知识回答用户问题。

## 核心功能

### 1. 智能对话系统
```java
@AiService(
    wiringMode = EXPLICIT,
    streamingChatModel = "qwenStreamingChatModel",
    chatMemoryProvider = "chatMemoryProviderXiaozhi",
    tools = "appointmentTools",
    contentRetriever = "contentRetrieverXiaozhiPincone"
)
public interface XiaozhiAgent {
    @SystemMessage(fromResource = "zhaozhi-prompt-template.txt")
    Flux<String> chat(@MemoryId Long memoryId, @UserMessage String userMessage);
}
```

- **流式响应**: 使用Reactor的Flux实现流式输出，提供类似打字机的实时响应效果
- **会话记忆**: 支持多轮对话，每个会话有独立的记忆ID
- **系统提示词**: 加载专业医院服务提示词模板，确保AI行为符合预期

### 2. 语音交互功能
项目集成了完整的语音交互功能：

#### 语音识别(ASR)
- 通过WebSocket实现低延迟实时语音转文字
- 使用阿里云智能语音服务
- 支持中间结果和最终结果的区分处理

#### 语音合成(TTS)
- 将AI的回答转换为语音输出
- 支持流式和非流式两种模式
- 优化了音频播放的流畅性

### 3. 预约管理系统
```java
@Component
public class AppointmentTools {
    // 预约挂号功能
    @Tool(name="预约挂号", value = "根据参数，先执行工具方法queryDepartment查询是否可预约...")
    public String bookAppointment(Appointment appointment) { ... }
    
    // 取消预约功能
    @Tool(name = "取消预约挂号", value = "根据参数，查询预约是否存在...")
    public String cancelAppointment(Appointment appointment) { ... }
    
    // 查询号源功能
    @Tool(name = "查询是否有号源", value = "根据科室名称，日期，时间和医生查询是否有号源...")
    public boolean queryDepartment(...) { ... }
}
```

- **自然语言调用**: AI可以直接调用这些工具执行业务操作
- **智能验证**: 自动检查重复预约、验证用户信息
- **业务逻辑**: 集成数据库操作，确保数据一致性

### 4. 会话记忆管理
```java
@Component
public class MongoChatMemoryStore implements ChatMemoryStore {
    // 实现基于MongoDB的聊天记忆存储
    @Override
    public List<ChatMessage> getMessages(Object memoryId) { ... }
    
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) { ... }
}
```

- **持久化存储**: 对话历史保存在MongoDB中，重启后不会丢失
- **会话隔离**: 每个用户有独立的记忆空间
- **容量控制**: 支持最大消息数量限制，避免无限增长

## 系统架构

### 整体架构
```
┌─────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   用户端    │◄──►│  Spring Boot应用 │◄──►│   数据存储      │
│ (Web/移动)  │    │                  │    │                 │
└─────────────┘    └──────────────────┘    └─────────────────┘
                           │                         │
                    ┌──────▼─────────────┐           │
                    │    AI服务层        │           │
                    │   (LangChain4j)    │◄──────────┼───┐
                    └────────────────────┘           │   │
                           │                         │   │
                    ┌──────▼─────────────┐           │   │
                    │   外部AI服务       │           │   │
                    │ (通义千问,DeepSeek) │           │   │
                    └────────────────────┘           │   │
                           │                         │   │
                    ┌──────▼─────────────┐           │   │
                    │   向量数据库       │           │   │
                    │   (Pinecone)       │◄──────────┼───┤
                    └────────────────────┘           │   │
                                                     │   │
                    ┌────────────────────┐           │   │
                    │   语音服务         │◄──────────┼───┤
                    │ (阿里云TTS/ASR)    │           │   │
                    └────────────────────┘           │   │
                                                     │   │
                    ┌────────────────────┐           │   │
                    │   关系数据库       │◄──────────┼───┘
                    │   (MySQL)          │           │
                    └────────────────────┘           │
                                                     │
                    ┌────────────────────┐           │
                    │   文档数据库       │◄──────────┘
                    │   (MongoDB)        │
                    └────────────────────┘
```

### 微服务架构
项目虽然以单体形式部署，但采用了微服务设计理念：

1. **Controller层**: 处理HTTP请求，协调各个服务
2. **AI Agent层**: 核心AI逻辑，处理自然语言理解和生成
3. **Service层**: 业务逻辑处理，包括预约管理和语音服务
4. **Data Access层**: 数据访问，使用MyBatis-Plus简化操作
5. **Storage层**: 多种存储方式，满足不同数据需求

### 数据流向
1. **用户请求**: 前端发起请求 → Controller → AI Agent
2. **AI处理**: AI Agent → 调用LLM → 生成响应
3. **记忆管理**: 对话历史 → MongoDB存储
4. **工具调用**: AI识别意图 → 调用业务工具 → 数据库操作
5. **知识检索**: 用户问题 → 向量化 → Pinecone检索 → 结果融合

## 技术实现细节

### 1. AI Agent配置
```java
@Configuration
public class XiaozhiAgentConfig {
    // 聊天记忆提供者配置
    @Bean
    ChatMemoryProvider chatMemoryProviderXiaozhi() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)  // 最多保存20条消息
                .chatMemoryStore(mongoChatMemoryStore)  // 使用MongoDB存储
                .build();
    }
    
    // 内容检索器配置（基于Pinecone）
    @Bean
    ContentRetriever contentRetrieverXiaozhiPincone() {
        return EmbeddingStoreContentRetriever
                .builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(1)      // 最大返回结果数
                .minScore(0.8)      // 最小相似度分数
                .build();
    }
}
```

### 2. WebSocket语音通信
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册语音识别处理器
        registry.addHandler(new VoiceHandler(aliyunTokenUtil, appKey), "/voice-asr")
                .setAllowedOrigins("*");
        
        // 注册通话处理器
        registry.addHandler(new CallHandler(voiceService, xiaozhiAgent, aliyunTokenUtil, appKey), "/voice-call")
                .setAllowedOrigins("*");
    }
}
```

### 3. 流式响应处理
```java
@PostMapping(value = "/chat", produces = "text/stream;charset=utf-8")
public Flux<String> chat(@RequestBody ChatForm chatForm) {
    return xiaozhiAgent.chat(chatForm.getMemoryId(), chatForm.getMessage());
}
```

### 4. 语音通话状态管理
```java
public class CallHandler extends BinaryWebSocketHandler {
    // 状态锁：标记 AI 是否正在处理
    private final AtomicBoolean isAiSpeaking = new AtomicBoolean(false);
    
    // 在用户说完后，等待AI思考，然后等待5秒，最后播放完整语音
    new Thread(() -> {
        try {
            isAiSpeaking.set(true); // 锁定 ASR
            
            // 1. 等待大模型完全生成好文字
            StringBuilder fullAiResponse = new StringBuilder();
            xiaozhiAgent.chat(memoryId, userText)
                    .doOnNext(text -> fullAiResponse.append(text))
                    .blockLast(); // 同步等待生成结束
            
            String finalReply = fullAiResponse.toString();
            
            // 2. 等待5秒再语音播报
            Thread.sleep(5000);
            
            // 3. 将完整回答转语音
            byte[] fullAudioData = voiceService.textToSpeech(finalReply);
            
            // 4. 一次性发送完整音频
            session.sendMessage(new BinaryMessage(fullAudioData));
            
            isAiSpeaking.set(false); // 解锁 ASR
        } catch (Exception e) {
            isAiSpeaking.set(false);
        }
    }).start();
}
```

## 部署与运行

### 环境要求
- **JDK**: 17或更高版本
- **数据库**: 
  - MySQL 8.0+ (用于业务数据)
  - MongoDB 6.0+ (用于对话历史)
- **外部服务**:
  - 阿里云通义千问API密钥
  - DeepSeek API密钥
  - Pinecone向量数据库账户
  - 阿里云语音服务账户

### 配置文件
在`application.properties`中配置必要的参数：
```properties
# AI模型配置
langchain4j.community.dashscope.chat-model.api-key=${DASH_SCOPE_API_KEY}
langchain4j.community.dashscope.chat-model.model-name=qwen-max

# 数据库配置
spring.data.mongodb.uri=mongodb://localhost:27017/chat_memory_db
spring.datasource.url=jdbc:mysql://localhost:3306/guiguxiaozhi

# 阿里云服务配置
aliyun.accessKeyId=${ALIBABA_CLOUD_ACCESS_KEY_ID}
aliyun.accessKeySecret=${ALIBABA_CLOUD_ACCESS_KEY_SECRET}
```

### 启动步骤
1. 确保所有依赖服务正常运行（数据库、外部API等）
2. 设置环境变量（API密钥等）
3. 使用Maven打包：`mvn clean package`
4. 运行应用：`java -jar java-ai-langchain4j-1.0-SNAPSHOT.jar`

## 开发指南

### 添加新功能
1. **AI工具扩展**: 在`AppointmentTools`基础上添加新的工具方法
2. **知识库更新**: 向Pinecone向量数据库添加新的知识文档
3. **API接口扩展**: 在`XiaozhiController`中添加新的REST端点
4. **语音功能增强**: 扩展`VoiceHandler`或`CallHandler`功能

### 性能优化建议
1. **缓存策略**: 为频繁查询的知识库添加缓存
2. **异步处理**: 对耗时操作使用异步处理
3. **连接池优化**: 优化数据库连接池配置
4. **向量检索优化**: 调整Pinecone检索参数

### 测试策略
1. **单元测试**: 针对业务逻辑编写单元测试
2. **集成测试**: 测试AI服务与数据库的集成
3. **性能测试**: 验证流式响应性能
4. **语音测试**: 验证语音识别和合成质量

## 扩展与优化

### 功能扩展
1. **多语言支持**: 添加多语言AI模型支持
2. **个性化推荐**: 基于用户历史提供个性化服务
3. **情感分析**: 检测用户情绪并调整回应策略
4. **多模态支持**: 支持图像、视频等多媒体内容

### 架构演进
1. **微服务拆分**: 将AI服务、语音服务等拆分为独立微服务
2. **容器化部署**: 使用Docker和Kubernetes进行部署
3. **监控告警**: 集成Prometheus和Grafana进行监控
4. **弹性伸缩**: 根据负载自动调整服务实例数量

### 安全考虑
1. **API安全**: 实施API限流和认证机制
2. **数据加密**: 对敏感数据进行加密存储
3. **审计日志**: 记录所有AI交互和业务操作
4. **隐私保护**: 遵循数据保护法规

这个项目展示了如何使用现代AI技术和传统企业应用相结合，构建智能客服系统的最佳实践。