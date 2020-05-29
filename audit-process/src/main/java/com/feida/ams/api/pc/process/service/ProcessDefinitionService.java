package com.feida.ams.api.pc.process.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feida.ams.api.pc.process.mapper.FormTypeMapper;
import com.feida.ams.api.pc.process.mapper.ProcessDefinitionMapper;
import com.feida.ams.api.pc.process.mapper.ProcessNodeMapper;
import com.feida.ams.api.pc.process.model.FormType;
import com.feida.ams.api.pc.process.model.ProcessDefinition;
import com.feida.ams.api.pc.process.model.ProcessNode;
import com.feida.ams.api.pc.process.model.ProcessNodeActor;
import com.feida.ams.framework.errors.ProcessError;
import com.feida.common.domain.BaseDto;
import com.feida.common.domain.Dto;
import com.feida.common.exception.ServiceException;
import com.feida.common.util.BeanUtil;
import com.feida.common.util.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.feida.common.util.MyStringUtils.splitStringToInteger;

@Slf4j
@Service
public class ProcessDefinitionService {

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;
    @Autowired
    private FormTypeMapper formTypeMapper;
    @Autowired
    private ProcessNodeMapper processNodeMapper;
    @Autowired
    private ProcessNodeActorService processNodeActorService;

    public List<Dto> getByCondition(Dto params) {
        return processDefinitionMapper.getByCondition(params);
    }

    public Dto getDto(Integer id) {
        return processDefinitionMapper.getDto(id);
    }

    public void submitProcessses(Dto params) throws Exception {

        Integer formTypeId = params.getInteger("formTypeId");
        FormType formType = formTypeMapper.selectByPrimaryKey(formTypeId);
        Integer processDefinitionVersion = formType.getProcessDefinitionVersion();
        if (null == processDefinitionVersion)
            processDefinitionVersion = 1;
        else
            processDefinitionVersion = processDefinitionVersion + 1;
        formType.setProcessDefinitionVersion(processDefinitionVersion);
        formTypeMapper.updateByPrimaryKeySelective(formType);

        JSONArray formProcesses = (JSONArray) params.get("formProcesses");
        Iterator<Object> iterator = formProcesses.iterator();
        while (iterator.hasNext()) {
            JSONObject next = (JSONObject) iterator.next();

            ProcessDefinition processDefinition = BeanUtil.convertMap2Bean(next, ProcessDefinition.class);
            processDefinition.setId(null);
            processDefinition.setProcessDefinitionVersion(processDefinitionVersion);
            processDefinition.setFormTypeId(formTypeId);
            processDefinition.setDeleteFlag(1);
            processDefinition.setRoadId(WebUtils.getSessionAccountRoadId());
            processDefinition.setCreator(WebUtils.getSessionAccountId());
            processDefinition.setCreateTime(new Date());
            processDefinition.setModifier(WebUtils.getSessionAccountId());
            processDefinition.setModifiedTime(new Date());
            processDefinitionMapper.insertSelective(processDefinition);

            List<JSONObject> processNodeList = (List<JSONObject>) next.get("processNodeList");
            for (int i = 1; i <= processNodeList.size(); i++) {
                JSONObject nodeObj = processNodeList.get(i - 1);
                ProcessNode node = BeanUtil.convertMap2Bean(nodeObj, ProcessNode.class);
                node.setProcessDefinitionId(processDefinition.getId());
                node.setNodeNo(i);
                node.setId(null);
                processNodeMapper.insert(node);
                ProcessNodeActor actor = new ProcessNodeActor();
                actor.setExecutiveLevel(nodeObj.getInteger("executiveLevel"));
                actor.setExecutiveType(nodeObj.getInteger("executiveType"));
                JSONArray executiveIds = nodeObj.getJSONArray("executiveIds");
                if (CollectionUtils.isNotEmpty(executiveIds)) {
                    StringBuilder sb = new StringBuilder();
                    executiveIds.forEach(k -> {
                        Integer id = (Integer) k;
                        sb.append(',').append(id);
                    });
                    sb.deleteCharAt(0);
                    actor.setExecutiveIds(sb.toString());
                }
                actor.setExecutiveType(nodeObj.getInteger("executiveType"));
                actor.setProcessNodeId(node.getId());
                actor.setId(null);
                processNodeActorService.insertSelective(actor);
            }
        }
    }

    public Dto formProcesses(Dto params) {
        FormType formType = formTypeMapper.selectByPrimaryKey(params.getInteger("formTypeId"));
//        if (null == formType) {
//            log.error("formProcesses : " + params.getInteger("formTypeId"), "表单不存在");
//            throw new ServiceException(ProcessError.NO_FORM);
//        }
        params.put("processDefinitionVersion", formType.getProcessDefinitionVersion());
        List<ProcessDefinition> list = processDefinitionMapper.getListByFormTypeId(params);
        for(ProcessDefinition processDefinition: list){
            List<Dto> processNodeList = processDefinition.getProcessNodeList();
            for(Dto node: processNodeList) {
                String executiveIds = node.getString("executiveIds");
                if(StringUtils.isNotBlank(executiveIds)){
                    List<Integer> ids = splitStringToInteger(executiveIds, ",");
                    node.put("executiveIds", ids);
                }
            }
        }
        BaseDto result = new BaseDto("formProcesses", list);
        return result;
    }


}
