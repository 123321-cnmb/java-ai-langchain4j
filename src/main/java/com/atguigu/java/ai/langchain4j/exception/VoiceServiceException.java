package com.atguigu.java.ai.langchain4j.exception;

/**
 * 语音服务异常类
 * 用于处理语音服务相关的异常情况
 */
public class VoiceServiceException extends RuntimeException {

    public VoiceServiceException(String message) {
        super(message);
    }

    public VoiceServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public VoiceServiceException(Throwable cause) {
        super(cause);
    }
}