package com.feida.ams.api.pc.process.mapper;

import com.feida.ams.api.pc.process.model.ProcessLog;
import com.feida.common.domain.BaseDto;
import com.feida.common.domain.Dto;
import com.feida.common.mybatis.MyMapper;

import java.util.List;

public interface ProcessLogMapper extends MyMapper<ProcessLog> {

    List<Dto> getListByProcessInstanceOrderByTime(Dto params);
}