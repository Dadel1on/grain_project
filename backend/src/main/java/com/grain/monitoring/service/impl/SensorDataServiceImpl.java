package com.grain.monitoring.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grain.monitoring.entity.SensorData;
import com.grain.monitoring.mapper.SensorDataMapper;
import com.grain.monitoring.service.SensorDataService;

@Service
public class SensorDataServiceImpl extends ServiceImpl<SensorDataMapper, SensorData> implements SensorDataService {
}
