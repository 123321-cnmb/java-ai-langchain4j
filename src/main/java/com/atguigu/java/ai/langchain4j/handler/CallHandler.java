package com.atguigu.java.ai.langchain4j.handler;

import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberListener;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberResponse;
import com.atguigu.java.ai.langchain4j.assistant.XiaozhiAgent;
import com.atguigu.java.ai.langchain4j.service.VoiceService;
import com.atguigu.java.ai.langchain4j.utils.AliyunTokenUtil;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 优化版通话处理器：解决断断续续问题
 * 策略：完整思考 -> 完整显示 -> 等待5s -> 完整播报
 */
public class CallHandler extends BinaryWebSocketHandler {
    private final VoiceService voiceService;
    private final XiaozhiAgent xiaozhiAgent;
    private final AliyunTokenUtil aliyunTokenUtil;
    private final String appKey;
    private SpeechTranscriber transcriber;

    // 状态锁：标记 AI 是否正在处理（思考或播报中）
    private final AtomicBoolean isAiSpeaking = new AtomicBoolean(false);

    public CallHandler(VoiceService voiceService, XiaozhiAgent xiaozhiAgent, AliyunTokenUtil aliyunTokenUtil, String appKey) {
        this.voiceService = voiceService;
        this.xiaozhiAgent = xiaozhiAgent;
        this.aliyunTokenUtil = aliyunTokenUtil;
        this.appKey = appKey;
    }

    /**
     * 发送文本指令，必须使用 TextMessage 而不是 BinaryMessage
     * 否则前端会将其误认为是音频数据
     */
    private void sendTextMessage(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        SpeechTranscriberListener listener = new SpeechTranscriberListener() {
            @Override
            public void onSentenceEnd(SpeechTranscriberResponse response) {
                if (isAiSpeaking.get()) return;

                final String userText = response.getTransSentenceText();
                final Long memoryId = (session.getAttributes().get("memoryId") != null) ?
                        (Long) session.getAttributes().get("memoryId") : System.currentTimeMillis();

                sendTextMessage(session, "USER_FINAL:" + userText);

                new Thread(() -> {
                    try {
                        isAiSpeaking.set(true); // 锁定 ASR
                        sendTextMessage(session, "STATE:AI_THINKING");

                        // 1. 等待大模型完全生成好文字
                        StringBuilder fullAiResponse = new StringBuilder();
                        xiaozhiAgent.chat(memoryId, userText)
                                .doOnNext(text -> {
                                    fullAiResponse.append(text);
                                    sendTextMessage(session, "AI_INTERIM:" + text);
                                })
                                .blockLast(); // 强行同步等待生成结束

                        String finalReply = fullAiResponse.toString();

                        // 2. 实现你的需求：等 5 秒再语音播报
                        sendTextMessage(session, "STATE:AI_WAIT_5S");
                        Thread.sleep(5000);

                        // 3. 将【完整回答】转语音并发送一个【完整的 BinaryMessage】
                        sendTextMessage(session, "STATE:AI_SPEAKING");

                        // 调用你已有的非流式 tts 方法，返回完整 byte[]
                        byte[] fullAudioData = voiceService.textToSpeech(finalReply);

                        if (fullAudioData != null && fullAudioData.length > 0) {
                            // 核心：一次性发送，彻底消除断断续续
                            session.sendMessage(new BinaryMessage(fullAudioData));
                        }

                        // 4. 解锁 ASR
                        isAiSpeaking.set(false);
                        sendTextMessage(session, "STATE:AI_SILENT");
                    } catch (Exception e) {
                        isAiSpeaking.set(false);
                        sendTextMessage(session, "STATE:AI_SILENT");
                        e.printStackTrace();
                    }
                }).start();
            }

            @Override public void onTranscriptionResultChange(SpeechTranscriberResponse res) {
                if (!isAiSpeaking.get()) sendTextMessage(session, "USER_INTERIM:" + res.getTransSentenceText());
            }
            @Override public void onFail(SpeechTranscriberResponse res) { isAiSpeaking.set(false); }
            @Override public void onTranscriberStart(SpeechTranscriberResponse res) {}
            @Override public void onSentenceBegin(SpeechTranscriberResponse res) {}
            @Override public void onTranscriptionComplete(SpeechTranscriberResponse res) {}
        };

        transcriber = new SpeechTranscriber(voiceService.getNlsClient(), listener);
        transcriber.setAppKey(appKey);
        transcriber.addCustomedParam("max_sentence_silence", 800);
        transcriber.start();
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        // AI 忙碌时，不处理任何用户音频输入
        if (transcriber != null && !isAiSpeaking.get()) {
            transcriber.send(message.getPayload().array());
        }
    }
}