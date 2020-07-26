package com.wj.process.api.pc.process.service;

import com.feida.common.domain.Dto;
import com.feida.omms.dao.process.mapper.TransferMapper;
import com.feida.omms.dao.process.model.ProcessInstance;
import com.feida.omms.dao.process.model.Transfer;
import com.feida.omms.tk.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransferService extends BaseService<Transfer> {
    @Autowired
    private TransferMapper transferMapper;
    public void addBypProcessInstance(ProcessInstance processInstance, List<Integer> transferIds) {
        for (Integer transferId : transferIds) {
            Transfer transfer = new Transfer();
            transfer.setAccountId(transferId);
            transfer.setProcessInstanceId(processInstance.getId());
            transfer.setProcessNodeId(processInstance.getCurrentNodeId());
            insertSelective(transfer);
        }
    }

    public List<Transfer> getByCondition(Dto params) {
        List<Transfer> list= transferMapper.getByCondition(params);
        return list;
    }

    public List<Integer> getAccountIdByProcessInstanceNodeId(Integer processInstanceId, Integer nodeId){
        Dto params = new Dto("processInstanceId", processInstanceId);
        params.put("nodeId", nodeId);
        return transferMapper.getAccountIdByProcessInstanceNodeId(params);
    }
}
