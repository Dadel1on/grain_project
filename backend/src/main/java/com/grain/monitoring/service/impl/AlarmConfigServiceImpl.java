package com.grain.monitoring.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grain.monitoring.entity.AlarmConfig;
import com.grain.monitoring.mapper.AlarmConfigMapper;
import com.grain.monitoring.service.AlarmConfigService;

@Service
public class AlarmConfigServiceImpl extends ServiceImpl<AlarmConfigMapper, AlarmConfig> implements AlarmConfigService {
}
