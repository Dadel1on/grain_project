package com.grain.monitoring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grain.monitoring.entity.AlarmLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlarmLogMapper extends BaseMapper<AlarmLog> {
}
