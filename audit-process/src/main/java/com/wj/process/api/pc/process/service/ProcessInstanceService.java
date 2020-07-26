package com.wj.process.api.pc.process.service;

import com.feida.omms.dao.process.mapper.ProcessInstanceMapper;
import com.feida.omms.dao.process.model.ProcessInstance;
import com.feida.omms.tk.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessInstanceService extends BaseService<ProcessInstance> {
    @Autowired
    private ProcessInstanceMapper processInstanceMapper;
    public void saveOrUpdate(ProcessInstance processInstance) {
        if(null != processInstance.getId()){

            updateByPrimaryKeySelective(processInstance);
        }
        else
            processInstanceMapper.insertUseGeneratedKeys(processInstance);
    }
}
