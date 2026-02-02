/*
 * 预约服务接口
 * 定义预约相关的业务操作方法
 */
package com.atguigu.java.ai.langchain4j.service;

import com.atguigu.java.ai.langchain4j.entity.Appointment;
import com.atguigu.java.ai.langchain4j.exception.DatabaseException;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 预约服务接口
 * 继承MyBatis-Plus的IService接口，并扩展自定义的查询方法
 */
public interface AppointmentService extends IService<Appointment> {
    /**
     * 根据预约信息查询单个预约记录
     * 通过用户姓名、身份证号、科室、日期和时间等条件查询预约记录
     *
     * @param appointment 包含查询条件的预约对象
     * @return 返回匹配的预约记录，如果没有找到则返回null
     * @throws DatabaseException 数据库操作异常
     */
    Appointment getOne(Appointment appointment) throws DatabaseException;
}