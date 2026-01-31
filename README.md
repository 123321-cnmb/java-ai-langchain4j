<img width="1528" height="1017" alt="image" src="https://github.com/user-attachments/assets/4d86f2d7-01c7-403f-be16-9a4605e7e619" />
这个项目是一个**基于大语言模型的智能医院客服系统**。它不仅仅是一个简单的聊天机器人，而是一个能够查询医疗知识、记住用户对话、甚至能通过调用后台接口执行“预约挂号”任务的**智能 Agent（代理）**。

### 项目技术栈
| **核心框架** | Spring Boot + LangChain4j |
| **大模型** | 通义千问 (Qwen) 流式模型 |
| **持久化记忆** | MongoDB (`MongoTemplate`) |
| **向量数据库** | Pinecone (分布式向量存储) |
| **业务数据库** | MySQL + MyBatis-Plus |

## 第一阶段：核心对话
项目的入口是 `XiaozhiController.java`
* **流式响应 (Streaming)**：项目采用了 `Flux<String>` 返回类型。这意味着 AI 的回复不是等全部写完才发给用户，而是像“打字机”一样，出一字发一字，极大地提升了用户体验。
* **角色设定**：通过 `@SystemMessage` 引入了 `zhaozhi-prompt-template.txt` 资源文件。这相当于给 AI 下达了“指令集”，规定它必须扮演一个“医院智能客服”的角色，而不是通用百科全书。

## 第二阶段：持久化记忆
普通的 AI 对话是“无状态”的（问完即忘）。本项目通过 `MongoChatMemoryStore` 解决了这个问题。
* **滑动窗口记忆**：在 `XiaozhiAgentConfig` 中配置了 `MessageWindowChatMemory`，只保留最近的 20 条消息。这样既能让 AI 联系上下文，又不会因为对话太长导致消耗过多的 Token。
* **数据库存储**：利用 **MongoDB** 存储 `ChatMessages` 实体。即使服务器重启，只要传入相同的 `memoryId`，AI 就能从 MongoDB 中读取之前的 JSON 记录并还原对话现场。

## 第三阶段：RAG 知识库
为了让它掌握医院内部的专业信息（如神经内科的具体位置），项目引入了 **RAG（检索增强生成）** 流程。
* **向量检索**：项目使用了 **Pinecone**（云端向量数据库）作为知识存储。
* **工作原理**：
1. 系统预先将医院的 `.md` 文档转化为向量数据。
2. 当用户提问时，`contentRetrieverXiaozhiPincone` 会去数据库匹配相似度大于 **0.8** 的知识片段。
3. 系统将这些真实文档和用户问题一起发给 AI，AI 基于这些“参考资料”给出准确回答。

## 第四阶段：工具调用
**Function Calling（工具调用）**。AI 除了会聊天，还能操作业务系统。
* **业务联动**：在 `XiaozhiAgent` 的注解中配置了 `tools = "appointmentTools"`。
* **执行逻辑**：
* 如果用户说：“我想查一下我在神经内科的预约”。
* AI 会识别出意图，自动调用 `AppointmentService` 中的方法。
* 系统会去 **MySQL** 数据库中查询 `Appointment` 表。
* AI 拿到数据库返回的结果后，再组织成自然语言告诉用户：“张三先生，为您查到您在 202X 年有一次预约……”。



