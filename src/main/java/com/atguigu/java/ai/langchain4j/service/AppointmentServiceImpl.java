/*
 * 预约服务实现类
 * 实现预约相关的业务逻辑操作
 */
package com.atguigu.java.ai.langchain4j.service;

import com.atguigu.java.ai.langchain4j.entity.Appointment;
import com.atguigu.java.ai.langchain4j.mapper.AppointmentMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 预约服务实现类
 * 实现预约相关的业务逻辑，继承MyBatis-Plus的ServiceImpl基类
 */
@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment>
        implements AppointmentService {
    /**
     * 根据预约信息查询单个预约记录
     * 通过用户姓名、身份证号、科室、日期和时间等条件查询预约记录
     *
     * @param appointment 包含查询条件的预约对象
     * @return 返回匹配的预约记录，如果没有找到则返回null
     */
    @Override
    public Appointment getOne(Appointment appointment) {
        LambdaQueryWrapper<Appointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Appointment::getUsername, appointment.getUsername());
        queryWrapper.eq(Appointment::getIdCard, appointment.getIdCard());
        queryWrapper.eq(Appointment::getDepartment, appointment.getDepartment());
        queryWrapper.eq(Appointment::getDate, appointment.getDate());
        queryWrapper.eq(Appointment::getTime, appointment.getTime());
        Appointment appointmentDB = baseMapper.selectOne(queryWrapper);
        return appointmentDB;
    }
}