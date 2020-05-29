package com.feida.ams.api.pc.process.service;

import com.feida.ams.api.pc.process.model.ProcessInstance;
import com.feida.common.mybatis.BaseService;
import org.springframework.stereotype.Service;

@Service
public class ProcessInstanceService extends BaseService<ProcessInstance> {
    public void saveOrUpdate(ProcessInstance processInstance) {
        if(null != processInstance.getId())
            updateByPrimaryKeySelective(processInstance);
        else
            insertSelective(processInstance);
    }
}
