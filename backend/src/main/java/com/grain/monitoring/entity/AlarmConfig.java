package com.grain.monitoring.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("alarm_config")
public class AlarmConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceId;
    private Double maxTemp;
    private Double minTemp;
    private Double maxHum;
    private Double minHum;
    private LocalDateTime updateTime;
}
