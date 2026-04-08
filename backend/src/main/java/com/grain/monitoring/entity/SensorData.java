package com.grain.monitoring.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("sensor_data")
public class SensorData {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceId;
    private Double temperature;
    private Double humidity;
    private LocalDateTime collectTime;
}
