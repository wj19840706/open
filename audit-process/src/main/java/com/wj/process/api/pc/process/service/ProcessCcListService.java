package com.wj.process.api.pc.process.service;

import com.feida.common.domain.Dto;
import com.feida.omms.dao.process.mapper.ProcessCcListMapper;
import com.feida.omms.dao.process.model.ProcessCcList;
import com.feida.omms.dao.process.model.ProcessDefinition;
import com.feida.omms.dao.process.model.ProcessInstance;
import com.feida.omms.dao.system.message.model.Message;
import com.feida.omms.tk.BaseService;
import com.feida.process.api.pc.system.message.service.PMessageService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcessCcListService extends BaseService<ProcessCcList> {

    @Autowired
    private ProcessCcListMapper processCcListMapper;
    @Autowired
    private PMessageService PMessageService;
    @Autowired
    private ProcessDefinitionService processDefinitionService;

    public List<Dto> getByCondition(Dto params) {
        return processCcListMapper.getByCondition(params);
    }

    public int updateCcFormTypeCcList(Dto params) {
        Integer formTypeId = params.getInteger("formTypeId");
        List<Integer> ccList = params.getList("ccList");
        Example example = new Example(ProcessCcList.class);
        example.createCriteria().andEqualTo("formType", formTypeId);
        processCcListMapper.deleteByExample(example);
        List<ProcessCcList> collect = new HashSet<>(ccList).stream().map(k -> {
            ProcessCcList processCcList = new ProcessCcList();
            processCcList.setFormType(formTypeId);
            processCcList.setAccountId(k);
            return processCcList;
        }).collect(Collectors.toList());
        return processCcListMapper.insertList(collect);
    }


    public List<Integer> getByInstance(Integer formTypeId, Integer formId) {
        Dto dto = new Dto("formTypeId", formTypeId);
        if (null != formId)
            dto.put("formId", formId);
        return processCcListMapper.getByInstance(dto);
    }


    public void sendByInstance(ProcessInstance processInstance, Integer type) {
        ProcessDefinition processDefinition = processDefinitionService.selectByPrimaryKey(processInstance.getProcessDefinitionId());
        Integer copyCondition = processDefinition.getCopyCondition();
        if (copyCondition.compareTo(3) == 0 || type.compareTo(copyCondition) == 0 || type.compareTo(3) == 0) {
            List<Integer> byInstance = getByInstance(processInstance.getFormTypeId(), processInstance.getFormId());
            List<Message> collect = byInstance.stream().map(k -> {
                Message message = new Message();
                if (type.compareTo(2) == 0) message.setMessageTitle(processInstance.getTitle() + " 申请已通过");
                else message.setMessageTitle(processInstance.getTitle());
                message.setMessageType(2);
                message.setSenderId(processInstance.getApplicant());
                message.setStatus(1);
                message.setReceiverId(k);
                return message;
            }).collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(collect)) {
                PMessageService.insertListSelective(collect);
            }
        }
    }
}
