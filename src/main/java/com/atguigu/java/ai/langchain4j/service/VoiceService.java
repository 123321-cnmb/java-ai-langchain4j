package com.atguigu.java.ai.langchain4j.service;

import com.alibaba.nls.client.protocol.NlsClient;
import com.atguigu.java.ai.langchain4j.exception.VoiceServiceException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * 语音服务接口，提供文本转语音功能
 */
public interface VoiceService {

    /**
     * 流式文本转语音
     * @param text 输入文本
     * @param audioConsumer 音频数据消费者
     * @param onComplete 完成回调
     * @throws VoiceServiceException 语音服务异常
     */
    void streamTextToSpeech(String text, Consumer<ByteBuffer> audioConsumer, Runnable onComplete) throws VoiceServiceException;

    /**
     * 获取 NLS 客户端
     * @return NlsClient 实例
     * @throws VoiceServiceException 语音服务异常
     */
    NlsClient getNlsClient() throws VoiceServiceException;

    /**
     * 文本转语音，返回音频字节数组
     * @param text 输入文本
     * @return 音频字节数组
     * @throws VoiceServiceException 语音服务异常
     */
    byte[] textToSpeech(String text) throws VoiceServiceException;
}
