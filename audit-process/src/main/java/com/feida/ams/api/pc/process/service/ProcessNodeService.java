package com.feida.ams.api.pc.process.service;

import com.feida.ams.api.pc.process.mapper.ProcessNodeMapper;
import com.feida.ams.api.pc.process.model.ProcessNode;
import com.feida.common.mybatis.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessNodeService extends BaseService<ProcessNode> {
    @Autowired
    private ProcessNodeMapper processNodeMapper;


}
