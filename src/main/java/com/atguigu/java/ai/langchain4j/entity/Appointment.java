/*
 * 预约实体类
 * 用于表示医院预约挂号的相关信息
 */
package com.atguigu.java.ai.langchain4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预约实体类
 * 表示医院预约挂号的相关信息，包含用户信息、预约科室、日期时间等字段
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {
    @TableId(type = IdType.AUTO)  // 主键自增
    private Long id;              // 预约记录ID
    private String username;      // 用户姓名
    private String idCard;        // 身份证号码
    private String department;    // 预约科室
    private String date;          // 预约日期
    private String time;          // 预约时间（上午/下午）
    private String doctorName;    // 医生姓名
}