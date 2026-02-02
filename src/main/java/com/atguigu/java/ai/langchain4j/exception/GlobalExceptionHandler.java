package com.atguigu.java.ai.langchain4j.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理应用程序中的各种异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理语音服务异常
     *
     * @param e 语音服务异常
     * @return 包含错误信息的响应实体
     */
    @ExceptionHandler(VoiceServiceException.class)
    public ResponseEntity<String> handleVoiceServiceException(VoiceServiceException e) {
        logger.error("语音服务异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("语音服务异常: " + e.getMessage());
    }

    /**
     * 处理数据库异常
     *
     * @param e 数据库异常
     * @return 包含错误信息的响应实体
     */
    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<String> handleDatabaseException(DatabaseException e) {
        logger.error("数据库异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("数据库异常: " + e.getMessage());
    }

    /**
     * 处理通用异常
     *
     * @param e 通用异常
     * @return 包含错误信息的响应实体
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        logger.error("未处理的异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("系统内部错误: " + e.getMessage());
    }
}