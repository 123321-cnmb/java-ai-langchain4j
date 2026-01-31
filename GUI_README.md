# 小智AI助手 - Swing图形用户界面

## 功能特性

- **聊天界面**：类似聊天框的用户界面，支持与AI助手进行交互
- **会话管理**：支持新建对话、清空会话
- **对话历史**：每个会话都有独立的历史记录，支持切换不同会话
- **流式响应**：实时显示AI助手的流式回复
- **时间戳**：每条消息都带有发送时间
- **视觉区分**：用户消息和AI回复采用不同颜色区分

## 启动方式

### 方式一：启动Web API + Swing UI
```bash
java -jar target/java-ai-langchain4j-1.0-SNAPSHOT.jar --ui=swing
```

### 方式二：直接运行UILauncher
```bash
java -cp target/java-ai-langchain4j-1.0-SNAPSHOT.jar com.atguigu.java.ai.langchain4j.ui.UILauncher
```

### 方式三：在IDE中运行
- 运行 [SwingUIApplication](file:///D:/IDEAProjectS/java-ai-langchain4j/src/main/java/com/atguigu/java/ai/langchain4j/ui/SwingUIApplication.java#L14-L27) 类或 [UILauncher](file:///D:/IDEAProjectS/java-ai-langchain4j/src/main/java/com/atguigu/java/ai/langchain4j/ui/UILauncher.java#L7-L11) 类，传入参数 `--ui=swing`

## 界面说明

1. **左侧会话列表**：显示所有对话会话，可点击切换
2. **右侧聊天区域**：显示对话内容，用户消息为蓝色，AI回复为绿色
3. **顶部工具栏**：
   - 新建对话：创建新的对话会话
   - 清空会话：清空当前会话的所有消息记录
4. **底部输入区域**：输入消息内容，按回车键或点击发送按钮发送

## 使用说明

- 输入消息后按回车键或点击发送按钮发送
- 在会话列表中点击可切换不同对话
- 每次新建对话会创建一个新的会话ID
- 清空会话只会清空当前选中的会话，不影响其他会话

## 技术实现

- 使用Java Swing构建图形界面
- 与现有的AI助手服务无缝集成
- 支持流式响应显示
- 会话状态在内存中维护
- 使用HTML格式渲染聊天消息