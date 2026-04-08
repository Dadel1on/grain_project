package com.grain.monitoring.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("alarm_log")
public class AlarmLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceId;
    private String alarmType; // TEMPERATURE_HIGH, TEMPERATURE_LOW, HUMIDITY_HIGH, HUMIDITY_LOW
    private Double alarmValue;
    private Double thresholdValue;
    private Integer status; // 0-未处理, 1-已处理
    private LocalDateTime createTime;
}
