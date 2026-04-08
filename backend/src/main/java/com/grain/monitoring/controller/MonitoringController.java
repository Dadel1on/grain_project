package com.grain.monitoring.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.grain.monitoring.common.Result;
import com.grain.monitoring.entity.AlarmConfig;
import com.grain.monitoring.entity.AlarmLog;
import com.grain.monitoring.entity.DeviceInfo;
import com.grain.monitoring.entity.SensorData;
import com.grain.monitoring.service.AlarmConfigService;
import com.grain.monitoring.service.AlarmLogService;
import com.grain.monitoring.service.DeviceInfoService;
import com.grain.monitoring.service.SensorDataService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // 允许跨域
public class MonitoringController {

    @Autowired
    private DeviceInfoService deviceInfoService;

    @Autowired
    private SensorDataService sensorDataService;

    @Autowired
    private AlarmConfigService alarmConfigService;

    @Autowired
    private AlarmLogService alarmLogService;

    // --- 设备管理 ---
    @GetMapping("/devices")
    public Result<List<DeviceInfo>> getDevices() {
        return Result.success(deviceInfoService.list());
    }

    @PostMapping("/devices")
    public Result<Boolean> addDevice(@RequestBody DeviceInfo deviceInfo) {
        return Result.success(deviceInfoService.save(deviceInfo));
    }

    @PutMapping("/devices")
    public Result<Boolean> updateDevice(@RequestBody DeviceInfo deviceInfo) {
        return Result.success(deviceInfoService.updateById(deviceInfo));
    }

    @DeleteMapping("/devices/{id}")
    public Result<Boolean> deleteDevice(@PathVariable Long id) {
        return Result.success(deviceInfoService.removeById(id));
    }

    // --- 历史监控数据 ---
    @GetMapping("/sensor-data")
    public Result<List<SensorData>> getSensorData(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        LambdaQueryWrapper<SensorData> queryWrapper = new LambdaQueryWrapper<SensorData>()
                .eq(deviceId != null, SensorData::getDeviceId, deviceId)
                .ge(startTime != null, SensorData::getCollectTime, startTime)
                .le(endTime != null, SensorData::getCollectTime, endTime)
                .orderByDesc(SensorData::getCollectTime)
                .last("LIMIT 100"); // 限制返回数量
        
        return Result.success(sensorDataService.list(queryWrapper));
    }

    // --- 预警配置 ---
    @GetMapping("/alarm-configs")
    public Result<List<AlarmConfig>> getAlarmConfigs() {
        return Result.success(alarmConfigService.list());
    }

    @PutMapping("/alarm-configs")
    public Result<Boolean> updateAlarmConfig(@RequestBody AlarmConfig config) {
        return Result.success(alarmConfigService.updateById(config));
    }

    // --- 预警记录 ---
    @GetMapping("/alarm-logs")
    public Result<List<AlarmLog>> getAlarmLogs(@RequestParam(defaultValue = "0") Integer status) {
        return Result.success(alarmLogService.list(
                new LambdaQueryWrapper<AlarmLog>().eq(AlarmLog::getStatus, status).orderByDesc(AlarmLog::getCreateTime)
        ));
    }

    @PutMapping("/alarm-logs/{id}/handle")
    public Result<Boolean> handleAlarm(@PathVariable Long id) {
        AlarmLog alarmLog = new AlarmLog();
        alarmLog.setId(id);
        alarmLog.setStatus(1); // 标记为已处理
        return Result.success(alarmLogService.updateById(alarmLog));
    }

    @PutMapping("/alarm-logs/handle-all")
    public Result<Boolean> handleAllAlarms() {
        AlarmLog alarmLog = new AlarmLog();
        alarmLog.setStatus(1);
        return Result.success(alarmLogService.update(alarmLog, 
                new LambdaQueryWrapper<AlarmLog>().eq(AlarmLog::getStatus, 0)
        ));
    }
}
