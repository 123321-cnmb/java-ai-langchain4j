package com.atguigu.java.ai.langchain4j.utils;

import com.atguigu.java.ai.langchain4j.exception.DatabaseException;
import com.atguigu.java.ai.langchain4j.exception.VoiceServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 异常处理工具类
 * 提供异常处理的通用方法
 */
public class ExceptionUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ExceptionUtils.class);

    /**
     * 验证参数是否为空
     *
     * @param param 参数
     * @param paramName 参数名称
     * @throws IllegalArgumentException 当参数为空时抛出
     */
    public static void validateNotNull(Object param, String paramName) {
        if (param == null) {
            String errorMsg = String.format("%s不能为空", paramName);
            logger.warn(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * 验证字符串参数是否为空
     *
     * @param str 字符串参数
     * @param paramName 参数名称
     * @throws IllegalArgumentException 当字符串为空时抛出
     */
    public static void validateNotBlank(String str, String paramName) {
        if (str == null || str.trim().isEmpty()) {
            String errorMsg = String.format("%s不能为空", paramName);
            logger.warn(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * 包装数据库操作异常
     *
     * @param runnable 需要执行的操作
     * @param operationName 操作名称，用于错误信息
     */
    public static void wrapDatabaseOperation(Runnable runnable, String operationName) {
        try {
            runnable.run();
        } catch (DatabaseException e) {
            logger.error("{}操作失败: {}", operationName, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("{}操作发生未知异常", operationName, e);
            throw new DatabaseException(operationName + "操作失败", e);
        }
    }

    /**
     * 包装语音服务操作异常
     *
     * @param runnable 需要执行的操作
     * @param operationName 操作名称，用于错误信息
     */
    public static void wrapVoiceServiceOperation(Runnable runnable, String operationName) {
        try {
            runnable.run();
        } catch (VoiceServiceException e) {
            logger.error("{}操作失败: {}", operationName, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("{}操作发生未知异常", operationName, e);
            throw new VoiceServiceException(operationName + "操作失败", e);
        }
    }
}