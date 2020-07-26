package com.wj.process.api.pc.process.service;

import com.feida.common.domain.Dto;
import com.feida.omms.dao.process.mapper.ProcessLogMapper;
import com.feida.omms.dao.process.model.ProcessLog;
import com.feida.omms.tk.BaseService;
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
