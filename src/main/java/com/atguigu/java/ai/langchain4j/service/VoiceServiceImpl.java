package com.atguigu.java.ai.langchain4j.service;

import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.OutputFormatEnum;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizer;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizerListener;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizerResponse;
import com.atguigu.java.ai.langchain4j.utils.AliyunTokenUtil;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * 语音服务实现类，提供文本转语音功能的具体实现
 */
@Service
public class VoiceServiceImpl implements VoiceService {

    private static final Logger logger = LoggerFactory.getLogger(VoiceServiceImpl.class);

    @Autowired
    private AliyunTokenUtil aliyunTokenUtil;

    @Value("${aliyun.tts.appKey}")
    private String appKey;

    private NlsClient nlsClient;

    /**
     * 获取 NlsClient
     */
    private synchronized NlsClient getClient() throws Exception {
        String token = aliyunTokenUtil.getToken();
        if (this.nlsClient == null) {
            this.nlsClient = new NlsClient(token);
        }
        return this.nlsClient;
    }

    @Override
    public void streamTextToSpeech(String text,
                                   Consumer<ByteBuffer> audioConsumer,
                                   Runnable onComplete) throws Exception {
        SpeechSynthesizer synthesizer = new SpeechSynthesizer(getClient(), new SpeechSynthesizerListener() {
            @Override
            public void onMessage(ByteBuffer message) {
                audioConsumer.accept(message); // 将音频片断实时推给 WebSocket
            }
            @Override
            public void onComplete(SpeechSynthesizerResponse response) {
                if (onComplete != null) onComplete.run(); // 播报结束回调
            }
            @Override
            public void onFail(SpeechSynthesizerResponse response) {
                if (onComplete != null) onComplete.run(); // 失败也要解锁，防止死锁
            }
        });

        synthesizer.setAppKey(appKey);
        synthesizer.setFormat(OutputFormatEnum.MP3);
        synthesizer.setText(text);
        synthesizer.start();
        synthesizer.waitForComplete(); // 保持线程直到合成完毕
        synthesizer.close();
    }

    @Override
    public NlsClient getNlsClient() throws Exception {
        // 确保 client 已初始化
        return getClient();
    }

    @Override
    public byte[] textToSpeech(String text) throws Exception {
        // 1. 文本清洗（借鉴你提供的逻辑）
        String cleanText = text.replaceAll("[\\[\\]{}()]", " ").trim();
        if (cleanText.isEmpty()) return new byte[0];

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 2. 创建监听器
        SpeechSynthesizerListener listener = new SpeechSynthesizerListener() {
            @Override
            public void onMessage(ByteBuffer message) {
                byte[] bytes = new byte[message.remaining()];
                message.get(bytes);
                try {
                    baos.write(bytes); // 持续接收字节
                } catch (IOException e) {
                    logger.error("写入字节失败", e);
                }
            }

            @Override
            public void onComplete(SpeechSynthesizerResponse response) {
                logger.info("TTS任务完成: {}", response.getTaskId());
            }

            @Override
            public void onFail(SpeechSynthesizerResponse response) {
                logger.error("TTS任务失败: {}", response.getStatusText());
            }
        };

        // 3. 配置合成器
        SpeechSynthesizer synthesizer = new SpeechSynthesizer(getClient(), listener);
        synthesizer.setAppKey(appKey);
        synthesizer.setFormat(OutputFormatEnum.MP3); // Web端建议用MP3
        synthesizer.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);
        synthesizer.setText(cleanText);
        synthesizer.setSpeechRate(150);
        synthesizer.setVoice("xiaoyun");

        try {
            synthesizer.start();
            // 4. 【核心点】调用你之前成功的同步方法，确保主线程等到数据传完
            synthesizer.waitForComplete();
        } finally {
            synthesizer.close();
        }

        byte[] result = baos.toByteArray();
        logger.info("音频生成完毕，大小: {} 字节", result.length); // 这里必须看到大于0的数字
        return result;
    }

    @PreDestroy
    public void shutdown() {
        if (nlsClient != null) {
            nlsClient.shutdown();
        }
    }
}
