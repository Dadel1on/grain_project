package com.grain.monitoring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.grain.monitoring.entity.SensorData;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SensorDataMapper extends BaseMapper<SensorData> {
}
