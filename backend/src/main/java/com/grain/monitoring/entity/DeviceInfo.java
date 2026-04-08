package com.grain.monitoring.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("device_info")
public class DeviceInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceId;
    private String deviceName;
    private Integer status; // 0-离线, 1-在线
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
