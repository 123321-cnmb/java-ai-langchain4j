/*
 * 小智AI助手控制器，提供REST API接口
 * 该控制器处理前端发送的聊天请求，并调用AI助手进行响应
 */
package com.atguigu.java.ai.langchain4j.controller;

import com.atguigu.java.ai.langchain4j.assistant.XiaozhiAgent;
import com.atguigu.java.ai.langchain4j.bean.ChatForm;
import com.atguigu.java.ai.langchain4j.service.VoiceService;
import com.atguigu.java.ai.langchain4j.service.impl.AppointmentServiceImpl;
import com.atguigu.java.ai.langchain4j.store.MongoChatMemoryStore;
import com.atguigu.java.ai.langchain4j.utils.AliyunTokenUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小智AI助手控制器
 * 提供与AI助手交互的REST API接口，支持流式响应
 */
@Slf4j
@CrossOrigin
@Tag(name = "硅谷小智")

@RestController
@RequestMapping("/xiaozhi")
public class XiaozhiController {
    @Autowired
    private XiaozhiAgent xiaozhiAgent;

    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;

    @Autowired
    private VoiceService voiceService;

    private static final Logger logger = LoggerFactory.getLogger(AppointmentServiceImpl.class);

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
        try {
            Long memoryId = chatForm.getMemoryId();
            String message = chatForm.getMessage();

            if (memoryId == null) {
                logger.warn("会话ID为空，使用默认ID");
                memoryId = System.currentTimeMillis();
            }

            if (message == null || message.trim().isEmpty()) {
                logger.warn("用户消息为空，返回空响应");
                return Flux.empty();
            }

            return xiaozhiAgent.chat(memoryId, message);
        } catch (Exception e) {
            logger.error("处理聊天请求时发生异常", e);
            return Flux.error(e);
        }
    }

    /**
     * 获取指定会话的历史记录 (POST 方式)
     * 解决 ChatMessage 序列化问题并适配前端数组需求
     */
    @PostMapping("/history")
    public List<Map<String, String>> getHistory(@RequestBody ChatForm chatForm) {
        List<Map<String, String>> result = new ArrayList<>();
        try {
            // 从提交的表单对象中获取 memoryId
            Long id = chatForm.getMemoryId();
            if (id == null) {
                logger.warn("获取历史记录时会话ID为空");
                return result;
            }

            // 获取 MongoDB 中的原始消息
            List<ChatMessage> messages = mongoChatMemoryStore.getMessages(id);

            if (messages != null) {
                for (ChatMessage m : messages) {
                    Map<String, String> map = new HashMap<>();
                    // 设置角色标识：user, ai
                    map.put("role", m.type().name().toLowerCase());

                    // 核心修复：根据消息类型提取内容，解决 m.text() 编译报错
                    String content = "";
                    if (m instanceof UserMessage) {
                        content = ((UserMessage) m).singleText();
                    } else if (m instanceof AiMessage) {
                        content = ((AiMessage) m).text();
                    } else if (m instanceof SystemMessage) {
                        content = ((SystemMessage) m).text();
                    }

                    map.put("content", content);
                    result.add(map);
                }
            }

            logger.debug("成功获取记忆ID为 {} 的历史记录，共 {} 条", id, result.size());
        } catch (Exception e) {
            logger.error("获取历史记录时发生异常，记忆ID: {}", chatForm.getMemoryId(), e);
            // 不抛出异常，返回空列表，避免前端错误
        }
        return result;
    }


    /**
     * 删除指定会话的历史记录
     * 根据传入的会话ID删除MongoDB中对应的聊天历史记录
     *
     * @param chatForm 包含会话ID的聊天表单
     * @return 包含操作结果的响应Map，包括成功状态和错误信息（如有）
     */
    @PostMapping("/delete-history")
    public Map<String, Object> deleteHistory(@RequestBody ChatForm chatForm) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long id = chatForm.getMemoryId();
            if (id != null) {
                // 调用存储类删除 MongoDB 中的数据
                mongoChatMemoryStore.deleteMessages(id);
                response.put("success", true);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * 启动语音通话接口
     * 处理客户端发起的语音通话请求，返回WebSocket连接地址等必要信息
     *
     * @param form 通话表单数据，包含通话相关参数
     * @return 包含WebSocket连接地址和操作结果状态的Map对象
     *         - wsUrl: WebSocket连接地址
     *         - success: 操作是否成功
     */
    @PostMapping("/start-call")
    public Map<String, Object> startCall(@RequestBody ChatForm form) {
        Map<String, Object> res = new HashMap<>();
        // 这里可以预处理通话所需的 memoryId 等信息
        res.put("wsUrl", "ws://localhost:8080/voice-call");
        res.put("success", true);
        return res;
    }


    /**
     * 文本转语音接口
     * 将输入的文本转换为音频数据流并返回
     *
     * @param request 请求参数Map，包含待转换的文本内容
     * @return ResponseEntity包装的音频字节数组，包含音频流和相关HTTP头信息
     *         成功时返回音频数据，失败时返回500状态码
     */
    @PostMapping(value = "/tts", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> textToSpeech(@RequestBody Map<String, String> request) {
        try {
            if (request == null || request.get("text") == null || request.get("text").trim().isEmpty()) {
                logger.warn("TTS请求参数无效");
                return ResponseEntity.badRequest().build();
            }

            String text = request.get("text");
            if (text.length() > 1000) { // 限制文本长度，防止过长文本导致的问题
                logger.warn("TTS请求文本过长: {} 字符", text.length());
                text = text.substring(0, 1000);
            }

            byte[] audioData = voiceService.textToSpeech(text);

            // 关键：如果后端拦截了异常并返回了空数组，在这里拦截它
            if (audioData == null || audioData.length == 0) {
                logger.warn("语音合成返回空数据");
                return ResponseEntity.status(500).build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg") // 明确指定为音频格式
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(audioData.length)) // 告知浏览器数据长度
                    .body(audioData);
        } catch (Exception e) {
            logger.error("TTS服务处理异常", e);
            return ResponseEntity.status(500).build();
        }
    }






}