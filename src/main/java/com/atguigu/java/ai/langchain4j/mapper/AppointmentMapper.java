/*
 * 预约数据访问接口
 * 提供对预约数据的基本CRUD操作
 */
package com.atguigu.java.ai.langchain4j.mapper;


import com.atguigu.java.ai.langchain4j.entity.Appointment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约数据访问接口
 * 继承MyBatis-Plus的BaseMapper，自动获得对预约实体的CRUD操作能力
 */
@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
}