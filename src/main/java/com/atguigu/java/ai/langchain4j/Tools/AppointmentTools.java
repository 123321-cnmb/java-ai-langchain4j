package com.atguigu.java.ai.langchain4j.Tools;

import com.atguigu.java.ai.langchain4j.entity.Appointment;
import com.atguigu.java.ai.langchain4j.exception.DatabaseException;
import com.atguigu.java.ai.langchain4j.service.AppointmentService;
import com.atguigu.java.ai.langchain4j.utils.ExceptionUtils;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 预约工具类，提供预约挂号、取消预约和查询号源等功能
 * 这些工具方法用于与AI助手集成，支持自然语言处理预约相关操作
 */
@Component
public class AppointmentTools {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentTools.class);

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 执行预约挂号操作
     * 根据传入的预约信息检查是否已存在相同预约，如不存在则保存新的预约记录
     *
     * @param appointment 预约信息对象，包含用户姓名、身份证、科室、日期、时间等信息
     * @return 预约结果字符串，可能的返回值包括："预约成功，并返回预约详情"、"预约失败"或"您在相同的科室和时间已有预约"
     */
    @Tool(name="预约挂号", value = "根据参数，先执行工具方法queryDepartment查询是否可预约，并直接给用户回答是否可预约，并让用户确认所有预约信息，用户确认后再进行预约。如果用户没有提供具体的医生姓名，请从向量存储中找到一位医生。")
    public String bookAppointment(Appointment appointment) {
        try {
            // 验证参数
            if (appointment == null) {
                logger.warn("预约挂号参数为空");
                return "预约信息不完整，请提供正确的预约信息";
            }

            // 验证必要字段
            ExceptionUtils.validateNotBlank(appointment.getUsername(), "用户名");
            ExceptionUtils.validateNotBlank(appointment.getIdCard(), "身份证");
            ExceptionUtils.validateNotBlank(appointment.getDepartment(), "科室");
            ExceptionUtils.validateNotBlank(appointment.getDate(), "日期");
            ExceptionUtils.validateNotBlank(appointment.getTime(), "时间");

            // 查询数据库中是否已存在相同的预约记录
            Appointment appointmentDB = appointmentService.getOne(appointment);
            if (appointmentDB == null) {
                // 防止大模型幻觉设置了id
                appointment.setId(null);
                if (appointmentService.save(appointment)) {
                    logger.info("预约成功: 用户={}, 科室={}, 日期={}, 时间={}",
                            appointment.getUsername(), appointment.getDepartment(),
                            appointment.getDate(), appointment.getTime());
                    return "预约成功，并返回预约详情";
                } else {
                    logger.error("预约保存失败: 用户={}, 科室={}",
                            appointment.getUsername(), appointment.getDepartment());
                    return "预约失败";
                }
            }
            // 已存在相同科室和时间的预约
            logger.info("重复预约: 用户={}已在相同时间预约了{}科室",
                    appointment.getUsername(), appointment.getDepartment());
            return "您在相同的科室和时间已有预约";
        } catch (IllegalArgumentException e) {
            logger.warn("预约挂号参数验证失败", e);
            return "预约信息不完整: " + e.getMessage();
        } catch (DatabaseException e) {
            logger.error("预约挂号数据库操作失败", e);
            return "预约过程中出现数据库错误，请稍后重试";
        } catch (Exception e) {
            logger.error("预约挂号过程中发生未知异常", e);
            return "预约过程中出现错误，请稍后重试";
        }
    }

    /**
     * 取消预约挂号操作
     * 根据传入的预约信息查找对应的预约记录，如存在则删除该记录
     *
     * @param appointment 预约信息对象，用于查询要取消的预约记录
     * @return 取消预约结果字符串，可能的返回值包括："取消预约成功"或"您没有预约记录，请核对预约科室和时间"
     */
    @Tool(name = "取消预约挂号", value = "根据参数，查询预约是否存在，如果存在则删除预约记录并返回取消预约成功，否则返回取消预约失败")
    public String cancelAppointment(Appointment appointment) {
        try {
            if (appointment == null) {
                logger.warn("取消预约参数为空");
                return "取消预约信息不完整";
            }

            // 验证必要字段
            ExceptionUtils.validateNotBlank(appointment.getUsername(), "用户名");
            ExceptionUtils.validateNotBlank(appointment.getIdCard(), "身份证");
            ExceptionUtils.validateNotBlank(appointment.getDepartment(), "科室");
            ExceptionUtils.validateNotBlank(appointment.getDate(), "日期");
            ExceptionUtils.validateNotBlank(appointment.getTime(), "时间");

            // 查询数据库中是否存在对应的预约记录
            Appointment appointmentDB = appointmentService.getOne(appointment);
            if (appointmentDB != null) {
                // 删除预约记录
                if (appointmentService.removeById(appointmentDB.getId())) {
                    logger.info("取消预约成功: 用户={}, 科室={}, 日期={}, 时间={}",
                            appointment.getUsername(), appointment.getDepartment(),
                            appointment.getDate(), appointment.getTime());
                    return "取消预约成功";
                } else {
                    logger.error("取消预约失败: 删除记录失败，用户={}, 科室={}",
                            appointment.getUsername(), appointment.getDepartment());
                    return "取消预约失败";
                }
            }
            // 未找到对应的预约记录
            logger.info("取消预约失败: 未找到匹配的预约记录，用户={}, 科室={}",
                    appointment.getUsername(), appointment.getDepartment());
            return "您没有预约记录，请核对预约科室和时间";
        } catch (IllegalArgumentException e) {
            logger.warn("取消预约参数验证失败", e);
            return "取消预约信息不完整: " + e.getMessage();
        } catch (DatabaseException e) {
            logger.error("取消预约数据库操作失败", e);
            return "取消预约过程中出现数据库错误，请稍后重试";
        } catch (Exception e) {
            logger.error("取消预约过程中发生未知异常", e);
            return "取消预约过程中出现错误，请稍后重试";
        }
    }

    /**
     * 查询指定科室、日期和时间是否有号源
     * 根据科室名称、日期、时间及可选的医生名称查询是否有可预约的号源
     *
     * @param name 科室名称，用于查询特定科室的号源情况
     * @param date 日期，指定要查询的具体日期
     * @param time 时间，可选值为"上午"或"下午"，表示要查询的时间段
     * @param doctorName 医生名称，可选参数，若为null或空则查询科室内任意医生的号源
     * @return 布尔值，true表示有号源可预约，false表示无号源
     */
    @Tool(name = "查询是否有号源", value = "根据科室名称，日期，时间和医生查询是否有号源，并返回给用户")
    public boolean queryDepartment(
            @P(value = "科室名称") String name,
            @P(value = "日期") String date,
            @P(value = "时间，可选值：上午、下午") String time,
            @P(value = "医生名称", required = false) String doctorName
    ) {
        try {
            // 验证必要参数
            ExceptionUtils.validateNotBlank(name, "科室名称");
            ExceptionUtils.validateNotBlank(date, "日期");
            ExceptionUtils.validateNotBlank(time, "时间");

            // 输出查询参数到控制台日志
            logger.info("查询是否有号源 - 科室名称：{}，日期：{}，时间：{}，医生名称：{}", name, date, time, doctorName);

            //TODO 维护医生的排班信息：
            //如果没有指定医生名字，则根据其他条件查询是否有可以预约的医生（有返回true，否则返回false）；
            //如果指定了医生名字，则判断医生是否有排班（没有排班返回false）
            //如果有排班，则判断医生排班时间段是否已约满（约满返回false，有空闲时间返回true）

            // 暂时返回true，实际实现需要完成排班逻辑
            return true;
        } catch (IllegalArgumentException e) {
            logger.warn("查询号源参数验证失败", e);
            return false;
        } catch (Exception e) {
            logger.error("查询号源过程中发生异常", e);
            return false;
        }
    }
}