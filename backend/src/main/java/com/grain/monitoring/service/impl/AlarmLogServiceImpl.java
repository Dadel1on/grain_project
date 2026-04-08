package com.grain.monitoring.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grain.monitoring.entity.AlarmLog;
import com.grain.monitoring.mapper.AlarmLogMapper;
import com.grain.monitoring.service.AlarmLogService;

@Service
public class AlarmLogServiceImpl extends ServiceImpl<AlarmLogMapper, AlarmLog> implements AlarmLogService {
}
