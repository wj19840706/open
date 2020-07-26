package com.wj.process.api.pc.process.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feida.common.domain.Dto;
import com.feida.common.exception.ServiceException;
import com.feida.common.util.BeanUtil;
import com.feida.common.util.WebUtils;
import com.feida.omms.common.framework.errors.ProcessError;
import com.feida.omms.dao.process.mapper.FormTypeMapper;
import com.feida.omms.dao.process.mapper.ProcessDefinitionMapper;
import com.feida.omms.dao.process.mapper.ProcessNodeMapper;
import com.feida.omms.dao.process.model.FormType;
import com.feida.omms.dao.process.model.ProcessCcList;
import com.feida.omms.dao.process.model.ProcessDefinition;
import com.feida.omms.dao.process.model.ProcessNode;
import com.feida.omms.tk.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.feida.common.util.MyStringUtils.splitStringToInteger;

@Slf4j
@Service
public class ProcessDefinitionService extends BaseService<ProcessDefinition> {

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;
    @Autowired
    private FormTypeMapper formTypeMapper;
    @Autowired
    private ProcessNodeMapper processNodeMapper;
    @Autowired
    private ProcessCcListService processCcListService;

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
        int defaultCount = 0;

        JSONObject next = null;
        while (iterator.hasNext()) {
            next = (JSONObject) iterator.next();

            ProcessDefinition processDefinition = BeanUtil.convertMap2Bean(next, ProcessDefinition.class);
            if (StringUtils.isBlank(processDefinition.getRelationRegex())) {
                defaultCount++;
            }
            if (defaultCount > 1) {
                throw new ServiceException(ProcessError.MATCH_DEFAULT_PROCESS);
            }
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
            ProcessNode pre = null;
            for (int i = 1; i <= processNodeList.size(); i++) {
                JSONObject nodeObj = processNodeList.get(i - 1);
                ProcessNode node = BeanUtil.convertMap2Bean(nodeObj, ProcessNode.class);
                Integer nodeType = node.getNodeType();
                if (nodeType.compareTo(1) == 0) {
                    Integer executiveType = node.getExecutiveType();
                    switch (executiveType) {
                        case 1:

                        case 6: //主管/多级主管
                            JSONArray executiveLevels = nodeObj.getJSONArray("executiveLevels");
                            if (CollectionUtils.isNotEmpty(executiveLevels)) {
                                StringBuilder sb = new StringBuilder();
                                executiveLevels.forEach(k -> {
                                    Integer id = (Integer) k;
                                    sb.append(',').append(id);
                                });
                                sb.deleteCharAt(0);
                                node.setExecutiveLevels(sb.toString());
                                node.setExecutiveIds("");
                            } else {
                                throw new ServiceException(ProcessError.NO_MANAGER_LEVEL);
                            }
                            node.setExecutiveIds("");
                            break;
                        case 2:  //审批角色
                        case 3:  //个人
                            JSONArray executiveIds3 = nodeObj.getJSONArray("executiveIds");
                            if (CollectionUtils.isNotEmpty(executiveIds3)) {
                                if (executiveIds3.size() > 1 && executiveType.compareTo(5) == 0) {  //表单字段超过了一个
                                    throw new ServiceException(ProcessError.FORM_FIELD_GREAT_THAN);
                                }
                                StringBuilder sb = new StringBuilder();
                                executiveIds3.forEach(k -> {
                                    Integer id = (Integer) k;
                                    sb.append(',').append(id);
                                });
                                sb.deleteCharAt(0);
                                node.setExecutiveIds(sb.toString());
                                node.setExecutiveLevels("");
                            } else {
                                throw new ServiceException(ProcessError.NO_EXCUTOR);
                            }
                            break;
                        case 5: // 表单字段
                            // TODO 不能排序
                            JSONArray executiveIds = nodeObj.getJSONArray("executiveIds");
                            if (CollectionUtils.isNotEmpty(executiveIds)) {
                                if (executiveIds.size() > 1 && executiveType.compareTo(5) == 0) {  //表单字段超过了一个
                                    throw new ServiceException(ProcessError.FORM_FIELD_GREAT_THAN);
                                }
                                Integer id = (Integer) executiveIds.get(0);
                                node.setExecutiveIds(id.toString());

                            } else {
                                throw new ServiceException(ProcessError.NO_EXCUTOR);
                            }
                            node.setExecutiveLevels("");

                                node.setMandatoryFlag(2);
                            break;
                        case 4:  // 发起人自己
                            node.setExecutiveLevels("");
                            node.setExecutiveIds("");
                            node.setMandatoryFlag(2);

                    }
                } else {
                    node.setExecutiveLevels("");
                    node.setExecutiveIds("");
                    if (node.getNodeType().compareTo(2) == 0 && null != pre) {
                        if (pre.getFormFixedNodeId() == null)
                            throw new ServiceException(ProcessError.SELF_CANOT_MAN);
                        if (pre.getMandatoryFlag().compareTo(1) == 0) {
                            pre.setMandatoryFlag(2);
                            processNodeMapper.updateByPrimaryKey(pre);
                        }
                    }

                }
                node.setProcessDefinitionId(processDefinition.getId());
                node.setNodeNo(i);
                node.setId(null);
                processNodeMapper.insert(node);
                pre = node;
            }
            JSONArray ccList = next.getJSONArray("processCcList");
            if (CollectionUtils.isNotEmpty(ccList)) {
                List<ProcessCcList> collect = ccList.stream().map(k -> {
                    ProcessCcList processCcList = new ProcessCcList();
                    processCcList.setAccountId((Integer) k);
                    processCcList.setProcessDefinitionId(processDefinition.getId());
                    processCcList.setFormType(formTypeId);
                    return processCcList;
                }).collect(Collectors.toList());
                processCcListService.insertListSelective(collect);
            }

        }
    }

    public Dto formProcesses(Dto params) {
        FormType formType = formTypeMapper.selectByPrimaryKey(params.getInteger("formTypeId"));
        params.put("processDefinitionVersion", formType.getProcessDefinitionVersion());
        List<ProcessDefinition> list = processDefinitionMapper.getListByFormTypeId(params);
        for (ProcessDefinition processDefinition : list) {
            List<Dto> processNodeList = processDefinition.getProcessNodeList();
            for (Dto node : processNodeList) {
                Integer nodeType = node.getInteger("nodeType");
                ArrayList<Object> objects = new ArrayList<>();
                if (nodeType.compareTo(1) == 0) {
                    Integer executiveType = node.getInteger("executiveType");
                    switch (executiveType) {
                        case 1:
                        case 6:
                            String executiveLevels = node.getString("executiveLevels");
                            if (StringUtils.isNotBlank(executiveLevels)) {
                                List<Integer> ids = splitStringToInteger(executiveLevels, ",");
                                node.put("executiveLevels", ids);
                                node.put("executiveIds", objects);
                            }
                            break;
                        case 2:
                        case 3:
                        case 5:
                            String executiveIds = node.getString("executiveIds");
                            if (StringUtils.isNotBlank(executiveIds)) {
                                List<Integer> ids = splitStringToInteger(executiveIds, ",");
                                node.put("executiveIds", ids);
                                node.put("executiveLevels", objects);
                            }
                            break;
                        case 4:
                            node.put("executiveLevels", objects);
                            node.put("executiveIds", objects);
                    }
                } else {
                    node.put("executiveLevels", objects);
                    node.put("executiveIds", objects);
                }
            }
        }
        Dto result = new Dto("formProcesses", list);
        return result;
    }


}
