package com.feida.ams.api.pc.process.service;

import com.feida.ams.api.pc.process.mapper.ProcessLogMapper;
import com.feida.ams.api.pc.process.model.ProcessLog;
import com.feida.common.domain.BaseDto;
import com.feida.common.domain.Dto;
import com.feida.common.mybatis.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessLogService extends BaseService<ProcessLog> {
    @Autowired
    private ProcessLogMapper processLogMapper;

    public List<Dto> getListByProcessInstance(Dto params) {
       return processLogMapper.getListByProcessInstanceOrderByTime(params);
    }

}
