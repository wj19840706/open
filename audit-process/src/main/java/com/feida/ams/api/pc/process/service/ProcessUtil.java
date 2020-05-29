package com.feida.ams.api.pc.process.service;

import com.feida.ams.api.pc.process.bizInterace.BizInterface;
import com.feida.ams.api.pc.process.mapper.CommentAttachmentMapper;
import com.feida.ams.api.pc.process.mapper.FormFieldDefineMapper;
import com.feida.ams.api.pc.process.model.*;
import com.feida.ams.api.pc.system.account.service.AccountService;
import com.feida.ams.api.pc.system.organization.service.OrganizationManagerService;
import com.feida.ams.framework.errors.ProcessError;
import com.feida.common.domain.BaseDto;
import com.feida.common.domain.Dto;
import com.feida.common.exception.ServiceException;
import com.feida.common.util.MyStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.stream.Collectors;

import static com.feida.ams.api.pc.process.bizInterace.BizInterfaceFactory.getInstance;
import static com.feida.common.util.MyStringUtils.splitStringToInteger;

@Slf4j
@Service
public class ProcessUtil {

    @Autowired
    private FormTypeService formTypeService;

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Autowired
    private ProcessLogService processLogService;

    @Autowired
    private ProcessNodeActorService processNodeActorService;
    @Autowired
    private ToDolistService toDolistService;
    @Autowired
    private OrganizationManagerService organizationManagerService;
    @Autowired
    private ProcessRoleService processRoleService;
    @Autowired
    private FormFieldDefineMapper formFieldDefineMapper;
    @Autowired
    private ProcessInstanceService processInstanceService;
    @Autowired
    private ProcessNodeService processNodeService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CommentAttachmentMapper commentAttachmentMapper;

    public Dto getRightProcess(Dto params) {

        Dto result = null;
        Integer formTypeId = params.getInteger("formTypeId");
        FormType formType = formTypeService.selectByPrimaryKey(formTypeId);
        Integer processDefinitionVersion = formType.getProcessDefinitionVersion();
        if (null == processDefinitionVersion) {
            log.error("getRightProcess error formTypeId: " + formTypeId + "version: " + processDefinitionVersion);
            throw new ServiceException(ProcessError.NO_PROCESS);
        }
        BaseDto processParams = new BaseDto("formTypeId", formTypeId);
        processParams.put("processDefinitionVersion", processDefinitionVersion);
        List<Dto> processes = processDefinitionService.getByCondition(processParams);

        if (CollectionUtils.isEmpty(processes)) {
            log.error("getRightProcess error formTypeId: " + formTypeId + "version: " + processDefinitionVersion);
            throw new ServiceException(ProcessError.NO_PROCESS);
        }

        List<FormFieldDefine> formFieldDefines = formTypeService.getFormFieldDefineByTypeId(formTypeId);
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        for (Dto process : processes) {
            for (FormFieldDefine formFieldDefine : formFieldDefines) {
                String fieldName = formFieldDefine.getFieldName();
                Object o = params.get(fieldName);
                if (null != o)
                    engine.put(fieldName, o);
            }
            try {
                Boolean relationRegex = (Boolean) engine.eval(process.getString("relationRegex"));
                if (null != relationRegex && relationRegex) {
                    result = process;
                    break;
                }
            } catch (ScriptException e) {
                if (CollectionUtils.isEmpty(processes)) {
                    log.error("getRightProcess engine.eval error relationRegex: " + process.getString("relationRegex") + "\nprocessDefinitionId: " + process.getInteger("id"), e);
                }
            }
        }
        if (null == result) {
            log.error("getRightProcess error formTypeId: " + formTypeId + "version: " + processDefinitionVersion);
            throw new ServiceException(ProcessError.NO_PROCESS);
        }
        result.put("taskTitleTemplate", formType.getTaskTitleTemplate());
        return result;
    }

    public List<Dto> getProcessHisLogWithAttchments(Integer processInstanceId) {
        if (null == processInstanceId)
            return new ArrayList<>();
        List<Dto> logs = processLogService.getListByProcessInstance(new BaseDto("processInstanceId", processInstanceId));
        for (Dto dto : logs) {
            Integer id = dto.getInteger("id");
            if (dto.getInteger("actorType").compareTo(7) == 0) {
                CommentAttachment commentAttachment = new CommentAttachment();
                commentAttachment.setProcessLogId(id);
                List<CommentAttachment> select = commentAttachmentMapper.select(commentAttachment);
                dto.put("commentAttachments", select);
            }
        }
        return logs;
    }

    public Set<Integer> getProcessHisActors(Integer processInstanceId) {
        if (null == processInstanceId)
            return new HashSet<>();
        Set<Integer> history = new HashSet<>();
        List<Dto> logs = processLogService.getListByProcessInstance(new BaseDto("processInstanceId", processInstanceId));
        logs.forEach(k -> {
            Integer actorId = k.getInteger("actorId");
            Integer actorType = k.getInteger("actorType");
            switch (actorType) {
                case 1:
                case 4:
                case 6:
                    history.add(actorId);
                    break;
                case 8:
                    history.clear();
                    break;
            }
        });
        return history;
    }


    /**
     * 节点的审批人
     * @param processNode
     * @param processInstance
     * @param form
     * @return
     */
    public Set<Integer> getTheNodeActors(ProcessNode processNode, ProcessInstance processInstance, Dto form) {
        Integer processInstanceId = processInstance.getId();
        Set<Integer> history = getProcessHisActors(processInstanceId);
        Integer nodeId = processNode.getId();

        Set<Integer> result = new HashSet<>();
        List<Dto> accountIdList = getAccountIdsByActors(processInstance, nodeId, form);
        if (CollectionUtils.isNotEmpty(accountIdList)) {
            for (Dto id : accountIdList) {
                if (!history.contains(id.getInteger("accountId"))){
                    history.add(id.getInteger("accountId"));
                    result.add(id.getInteger("accountId"));
                }
            }
        }
        return result;
    }

    /**
     *  节点审批人的审批人
     * @param processInstance
     * @param nodeId
     * @param form
     * @return
     */
    public List<Dto> getAccountIdsByActors(ProcessInstance processInstance, Integer nodeId, Dto form) {


        List<Dto> nodeActors = processNodeActorService.getListByCondition(new BaseDto("processNodeId", nodeId));
        if (CollectionUtils.isEmpty(nodeActors)) {
            log.error("get node actor processInstanceId:" + processInstance.getId() + " nodeId: " + nodeId);
            throw new ServiceException(ProcessError.NO_ACTOR);
        }

        for (Dto k : nodeActors) {
            Integer executiveType = k.getInteger("executiveType");

            List<Integer> accountIdList;
            switch (executiveType) {
                case 1: // 主管
                    Integer applicant = processInstance.getApplicant();
                    return organizationManagerService.getManagersByAccountId(applicant);
                case 2: // 角色
                    String executiveIds = k.getString("executiveIds");
                    List<Integer> integers = splitStringToInteger(executiveIds, ",");
                    if (CollectionUtils.isNotEmpty(integers))
                        return processRoleService.getAccountIdsByRoleId(integers);
                case 3: // 个人
                    String accountIds = k.getString("executiveIds");
                    accountIdList = splitStringToInteger(accountIds, ",");
                    if (CollectionUtils.isNotEmpty(accountIdList))
                        return accountService.selectActorByIds(accountIdList);

                case 4:  // 发起人自己
                    accountIdList = new ArrayList<>();
                    accountIdList.add(processInstance.getApplicant());
                    return accountService.selectActorByIds(accountIdList);
                case 5:  // 表单字段
                    String formFeildIds = k.getString("executiveIds");
                    List<FormFieldDefine> formFieldDefines = formFieldDefineMapper.selectByIds(formFeildIds);
                    accountIdList = new ArrayList<>();
                    for (FormFieldDefine formFieldDefine : formFieldDefines) {
                        Integer executiveId = form.getInteger(formFieldDefine.getFieldName());
                        if (null != executiveId)
                            accountIdList.add(executiveId);
                    }
                    if (CollectionUtils.isNotEmpty(accountIdList))
                        return accountService.selectActorByIds(accountIdList);
            }
        }
        return new ArrayList<>();
    }

    public void sendTask(ProcessInstance instance, Collection<Integer> ids, Integer transferFlag) {
        List<ToDoList> collect = ids.stream().map(k -> {
            ToDoList toDoList = new ToDoList();
            toDoList.setActorId(k);
            toDoList.setProcessInstanceId(instance.getId());
            toDoList.setProcessNodeId(instance.getCurrentNodeId());
            toDoList.setCreator(instance.getApplicant());
            toDoList.setTransferFlag(transferFlag);
            toDoList.setFormId(instance.getFormId());
            toDoList.setFormTypeId(instance.getFormTypeId());
            toDoList.setTitle(instance.getTitle());
            return toDoList;
        }).collect(Collectors.toList());
        toDolistService.insertListSelective(collect);
    }

    public ProcessNode getNodeByDefinitionAndNo(Integer processDefinitionId, Integer nodeNo) {
        Example example = new Example(ProcessNode.class);
        example.createCriteria().andEqualTo("processDefinitionId", processDefinitionId)
                .andEqualTo("nodeNo", nodeNo);
        List<ProcessNode> processNodes = processNodeService.selectByExalple(example);
        if (CollectionUtils.isEmpty(processNodes))
            return null;
        return processNodes.get(0);

    }

    /**
     * @param processInstance
     * @return 当前节点 nodeNo + 1 的下一个节点， 要求 nodeNo 要以1递增
     */
    public ProcessNode nextNode(ProcessInstance processInstance) {
        Integer currentNodeNumber = processInstance.getCurrentNodeNumber();
        if (null == currentNodeNumber || currentNodeNumber.compareTo(0) == 0) {
            currentNodeNumber = 1;
        } else {
            currentNodeNumber++;
        }
        ProcessNode nodeByDefinitionAndNo = getNodeByDefinitionAndNo(processInstance.getProcessDefinitionId(), currentNodeNumber);
        return nodeByDefinitionAndNo;
    }

}
