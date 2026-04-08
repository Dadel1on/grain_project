package com.grain.monitoring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grain.monitoring.entity.AlarmConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlarmConfigMapper extends BaseMapper<AlarmConfig> {
}
