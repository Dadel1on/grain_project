package com.grain.monitoring.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grain.monitoring.entity.AlarmConfig;
import com.grain.monitoring.entity.AlarmLog;
import com.grain.monitoring.entity.DeviceInfo;
import com.grain.monitoring.entity.SensorData;
import com.grain.monitoring.mapper.DeviceInfoMapper;
import com.grain.monitoring.service.AlarmConfigService;
import com.grain.monitoring.service.AlarmLogService;
import com.grain.monitoring.service.DeviceInfoService;
import com.grain.monitoring.service.SensorDataService;

@Service
public class DeviceInfoServiceImpl extends ServiceImpl<DeviceInfoMapper, DeviceInfo> implements DeviceInfoService {

    @Autowired
    private AlarmLogService alarmLogService;

    @Autowired
    private AlarmConfigService alarmConfigService;

    @Autowired
    private SensorDataService sensorDataService;

    @Override
    @Transactional
    public boolean removeById(java.io.Serializable id) {
        DeviceInfo device = getById(id);
        if (device == null) {
            return false;
        }
        String deviceId = device.getDeviceId();

        alarmLogService.remove(new LambdaQueryWrapper<AlarmLog>().eq(AlarmLog::getDeviceId, deviceId));
        alarmConfigService.remove(new LambdaQueryWrapper<AlarmConfig>().eq(AlarmConfig::getDeviceId, deviceId));
        sensorDataService.remove(new LambdaQueryWrapper<SensorData>().eq(SensorData::getDeviceId, deviceId));

        return super.removeById(id);
    }
}
