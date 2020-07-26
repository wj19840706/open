package com.wj.process.api.pc.process.service;

import com.feida.common.domain.Dto;
import com.feida.omms.dao.process.mapper.ProcessNodeMapper;
import com.feida.omms.dao.process.model.ProcessNode;
import com.feida.omms.tk.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessNodeService extends BaseService<ProcessNode> {
    @Autowired
    private ProcessNodeMapper processNodeMapper;

    public List<Dto>  getByProcessId(Integer processDefinitionId) {
        return processNodeMapper.selectByProcessId(processDefinitionId);
    }


}
