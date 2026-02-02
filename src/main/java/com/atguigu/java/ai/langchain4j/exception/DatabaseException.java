package com.atguigu.java.ai.langchain4j.exception;

/**
 * 数据库操作异常类
 * 用于处理数据库操作相关的异常情况
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(Throwable cause) {
        super(cause);
    }
}