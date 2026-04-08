package com.grain.monitoring.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grain.monitoring.entity.DeviceInfo;
import com.grain.monitoring.mapper.DeviceInfoMapper;
import com.grain.monitoring.service.DeviceInfoService;

@Service
public class DeviceInfoServiceImpl extends ServiceImpl<DeviceInfoMapper, DeviceInfo> implements DeviceInfoService {
}
