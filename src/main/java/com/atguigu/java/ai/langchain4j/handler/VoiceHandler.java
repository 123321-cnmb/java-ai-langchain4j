package com.atguigu.java.ai.langchain4j.handler;

import com.alibaba.nls.client.protocol.InputFormatEnum;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberListener;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberResponse;
import com.atguigu.java.ai.langchain4j.utils.AliyunTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;

/**
 * 语音识别 WebSocket 处理器
 * 接收前端 PCM 音频流，调用阿里 NLS SDK 进行实时转写
 */
public class VoiceHandler extends BinaryWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(VoiceHandler.class);
    private final AliyunTokenUtil aliyunTokenUtil;
    private final String appKey;
    private SpeechTranscriber transcriber;

    public VoiceHandler(AliyunTokenUtil aliyunTokenUtil, String appKey) {
        this.aliyunTokenUtil = aliyunTokenUtil;
        this.appKey = appKey;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = aliyunTokenUtil.getToken();
        NlsClient client = new NlsClient(token);

        // 核心修复：补全 SpeechTranscriberListener 接口要求的所有抽象方法
        SpeechTranscriberListener listener = new SpeechTranscriberListener() {

            @Override
            public void onTranscriberStart(SpeechTranscriberResponse response) {
                // 核心修复：覆盖报错中提到的任务开始回调
                logger.info("阿里语音识别任务已正式开始: {}", response.getTaskId());
            }

            @Override
            public void onTranscriptionResultChange(SpeechTranscriberResponse response) {
                // 识别中：实时返回中间结果
                sendText(session, response.getTransSentenceText());
            }

            @Override
            public void onSentenceBegin(SpeechTranscriberResponse response) {
                // 句子开始
            }

            @Override
            public void onSentenceEnd(SpeechTranscriberResponse response) {
                // 句子结束：返回最终确定的文本
                sendText(session, response.getTransSentenceText());
            }

            @Override
            public void onTranscriptionComplete(SpeechTranscriberResponse response) {
                // 整个转写过程完成
                logger.info("ASR 识别完成: {}", response.getTaskId());
            }

            @Override
            public void onFail(SpeechTranscriberResponse response) {
                // 识别失败
                logger.error("ASR失败: {}", response.getStatusText());
            }
        };

// 在 afterConnectionEstablished 方法中
        transcriber = new SpeechTranscriber(client, listener);
        transcriber.setAppKey(appKey);
        transcriber.setFormat(InputFormatEnum.PCM);
        transcriber.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);

        transcriber.addCustomedParam("max_sentence_silence", 1000);

// --- 关键配置：开启中间结果 ---
        transcriber.setEnableIntermediateResult(true);
// 开启标点符号预测，让流式文字更自然
        transcriber.setEnablePunctuation(true);
// 开启逆向文本转换，将“二零二三”自动转为“2023”
        transcriber.setEnableITN(true);

        transcriber.start();
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        // 接收前端采集的二进制 PCM 音频块并转发给阿里服务端
        if (transcriber != null) {
            transcriber.send(message.getPayload().array());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 连接关闭时务必释放资源
        if (transcriber != null) {
            transcriber.stop();
            transcriber.close();
            logger.info("WebSocket 连接已关闭，ASR 资源已释放");
        }
    }

    /**
     * 将识别后的文本通过 WebSocket 发送回前端
     */
    private void sendText(WebSocketSession session, String text) {
        try {
            if (session.isOpen() && text != null) {
                session.sendMessage(new TextMessage(text));
            }
        } catch (IOException e) {
            logger.error("通过 WebSocket 发送识别文本失败", e);
        }
    }
}