package com.atguigu.java.ai.langchain4j.service.impl;/*
 * 预约服务实现类
 * 实现预约相关的业务逻辑操作
 */


import com.atguigu.java.ai.langchain4j.entity.Appointment;
import com.atguigu.java.ai.langchain4j.exception.DatabaseException;
import com.atguigu.java.ai.langchain4j.mapper.AppointmentMapper;
import com.atguigu.java.ai.langchain4j.service.AppointmentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 预约服务实现类
 * 实现预约相关的业务逻辑，继承MyBatis-Plus的ServiceImpl基类
 */
@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment>
        implements AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    /**
     * 根据预约信息查询单个预约记录
     * 通过用户姓名、身份证号、科室、日期和时间等条件查询预约记录
     *
     * @param appointment 包含查询条件的预约对象
     * @return 返回匹配的预约记录，如果没有找到则返回null
     * @throws DatabaseException 数据库操作异常
     */
    @Override
    public Appointment getOne(Appointment appointment) throws DatabaseException {
        try {
            if (appointment == null) {
                logger.warn("查询参数为空");
                return null;
            }

            LambdaQueryWrapper<Appointment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Appointment::getUsername, appointment.getUsername());
            queryWrapper.eq(Appointment::getIdCard, appointment.getIdCard());
            queryWrapper.eq(Appointment::getDepartment, appointment.getDepartment());
            queryWrapper.eq(Appointment::getDate, appointment.getDate());
            queryWrapper.eq(Appointment::getTime, appointment.getTime());

            Appointment appointmentDB = baseMapper.selectOne(queryWrapper);
            logger.debug("查询预约记录完成，结果: {}", appointmentDB != null);
            return appointmentDB;
        } catch (DatabaseException e) {
            logger.error("查询预约记录时发生数据库异常", e);
            throw e;
        } catch (Exception e) {
            logger.error("查询预约记录时发生未知异常", e);
            throw new DatabaseException("查询预约记录失败", e);
        }
    }
}