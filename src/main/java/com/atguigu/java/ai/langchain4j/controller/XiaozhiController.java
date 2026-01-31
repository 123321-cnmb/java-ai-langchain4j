/*
 * 小智AI助手控制器，提供REST API接口
 * 该控制器处理前端发送的聊天请求，并调用AI助手进行响应
 */
package com.atguigu.java.ai.langchain4j.controller;

import com.atguigu.java.ai.langchain4j.assistant.XiaozhiAgent;
import com.atguigu.java.ai.langchain4j.bean.ChatForm;
import com.atguigu.java.ai.langchain4j.service.VoiceService;
import com.atguigu.java.ai.langchain4j.store.MongoChatMemoryStore;
import com.atguigu.java.ai.langchain4j.utils.AliyunTokenUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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
    private AliyunTokenUtil aliyunTokenUtil;

    @Autowired
    private VoiceService voiceService;

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
            if (id == null) return result;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // 在 XiaozhiController.java 中添加

    /**
     * 删除指定会话的历史记录
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

    @PostMapping("/start-call")
    public Map<String, Object> startCall(@RequestBody ChatForm form) {
        Map<String, Object> res = new HashMap<>();
        // 这里可以预处理通话所需的 memoryId 等信息
        res.put("wsUrl", "ws://localhost:8080/voice-call");
        res.put("success", true);
        return res;
    }


    @PostMapping(value = "/tts", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> textToSpeech(@RequestBody Map<String, String> request) {
        try {
            byte[] audioData = voiceService.textToSpeech(request.get("text"));

            // 关键：如果后端拦截了异常并返回了空数组，在这里拦截它
            if (audioData == null || audioData.length == 0) {
                return ResponseEntity.status(500).build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg") // 明确指定为音频格式
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(audioData.length)) // 告知浏览器数据长度
                    .body(audioData);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }





}