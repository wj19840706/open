package com.wj.process.api.pc.process.service;

import com.feida.common.domain.Dto;
import com.feida.common.exception.ServiceException;
import com.feida.common.util.MyStringUtils;
import com.feida.common.util.WebUtils;
import com.feida.omms.common.framework.errors.ProcessError;
import com.feida.omms.dao.process.mapper.CommentAttachmentMapper;
import com.feida.omms.dao.process.mapper.FormFieldDefineMapper;
import com.feida.omms.dao.process.mapper.FormFieldRelationAccountMapper;
import com.feida.omms.dao.process.model.*;
import com.feida.omms.dao.system.account.mapper.AccountMapper;
import com.feida.omms.dao.system.account.model.Account;
import com.feida.process.api.pc.system.organization.service.POrganizationManagerService;
import com.feida.process.api.pc.system.organization.service.POrganizationTreeService;
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
    private ToDolistService toDolistService;
    @Autowired
    private POrganizationManagerService POrganizationManagerService;
    @Autowired
    private ProcessRoleService processRoleService;
    @Autowired
    private FormFieldDefineMapper formFieldDefineMapper;
    @Autowired
    private ProcessNodeService processNodeService;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private CommentAttachmentMapper commentAttachmentMapper;
    @Autowired
    private POrganizationTreeService organizationTreeService;

    @Autowired
    private FormFieldRelationAccountMapper formFieldRelationAccountMapper;

    public Dto getRightProcess(Dto params) {

        Dto result = null;
        Dto noConditin = null;
        Integer formTypeId = params.getInteger("formTypeId");
        FormType formType = formTypeService.selectByPrimaryKey(formTypeId);
        Integer processDefinitionVersion = formType.getProcessDefinitionVersion();
        if (null == processDefinitionVersion) {
            log.error("getRightProcess error formTypeId: " + formTypeId + "version: " + processDefinitionVersion);
            throw new ServiceException(ProcessError.NO_PROCESS);
        }
        Dto processParams = new Dto("formTypeId", formTypeId);
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
            if(StringUtils.isBlank(process.getString("relationRegex"))){
                noConditin = process;
                continue;
            }

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
        if (null == result && null ==noConditin) {
            log.error("getRightProcess error formTypeId: " + formTypeId + "version: " + processDefinitionVersion);
            throw new ServiceException(ProcessError.NO_PROCESS);
        }
        if(null == result)
            result = noConditin;

        result.put("taskTitleTemplate", formType.getTaskTitleTemplate());
        return result;
    }

    public List<Dto> getProcessHisLogWithAttchments(Integer processInstanceId) {
        if (null == processInstanceId)
            return new ArrayList<>();
        List<Dto> logs = processLogService.getListByProcessInstance(new Dto("processInstanceId", processInstanceId));
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
    /**
     * 提交审批时，根据提交参数里 processInstanceId和登录人id查询待办，或者根据taskId查询待办
     *
     * @param params
     * @return
     */
    public ToDoList getTodoListFromParams(Dto params) {
        ToDoList toDoList = null;

        Integer taskId = params.getInteger("taskId");
        if (null == taskId) {
            Integer processInstanceId = params.getInteger("processInstanceId");
            if (null == processInstanceId) {
                throw new ServiceException(ProcessError.AUDIT_PARAMS_ERROR);
            }
            Example example = new Example(ToDoList.class);
            example.createCriteria()
                    .andEqualTo("processInstanceId", params.getInteger("processInstanceId"))
                    .andEqualTo("status", 1)
                    .andEqualTo("actorId", WebUtils.getSessionAccountId());

            List<ToDoList> byCondition = toDolistService.selectByExalple(example);
            if (CollectionUtils.isEmpty(byCondition)) {
                throw new ServiceException(ProcessError.NO_TO_DO_LIST);
            }
            toDoList = byCondition.get(0);
        } else {
            toDoList = toDolistService.selectByPrimaryKey(taskId);
        }
        if (null == toDoList) {
            log.error("new todo list by processInstanceId: " + params.getInteger("processInstanceId") + " actorId:" + WebUtils.getSessionAccountId());
            throw new ServiceException(ProcessError.NO_TO_DO_LIST);
        }
        return toDoList;
    }

    public Set<Integer> getProcessHisActors(Integer processInstanceId) {
        if (null == processInstanceId)
            return new HashSet<>();
        Set<Integer> history = new HashSet<>();
        List<Dto> logs = processLogService.getListByProcessInstance(new Dto("processInstanceId", processInstanceId));
        logs.forEach(k -> {
            Integer actorId = k.getInteger("actorId");
            Integer actorType = k.getInteger("actorType");
            switch (actorType) {
                case 1:
                case 4:
                case 6:
                case 8:
                    history.add(actorId);
                    break;
                case 2:
                case 5:
                    history.clear();
                    break;
            }
        });
        return history;
    }

    /**
     * 节点的审批人 加过滤
     *
     * @param processNode
     * @param processInstance
     * @param form
     * @return
     */
//    public Set<Integer> getTheNodeActors(ProcessNode processNode, ProcessInstance processInstance, Dto form) {
//        Integer processInstanceId = processInstance.getId();
//        Set<Integer> history = getProcessHisActors(processInstanceId);
//        Set<Integer> result = new HashSet<>();
//        // 是否排序
//        Integer orderFlag = processNode.getOrderFlag();
//        Integer executiveType = processNode.getExecutiveType();
//        if (null != executiveType && executiveType.compareTo(1) == 0) {
//            if (null != orderFlag && orderFlag.compareTo(1) == 0) {
//
//            }else {
//
//            }
//        }
//
//
//        List<Dto> accountIdList = getNodeActors(processInstance, processNode, form);
//        if (CollectionUtils.isNotEmpty(accountIdList)) {
//            for (Dto id : accountIdList) {
//                if (!history.contains(id.getInteger("accountId"))) {
//                    history.add(id.getInteger("accountId"));
//                    result.add(id.getInteger("accountId"));
//                }
//            }
//        }
//        return result;
//    }


    /**
     * 节点审批人的审批人
     *
     * @param processInstance
     * @param node
     * @param form
     * @return
     */
    public List<Dto> getNodeActors(ProcessInstance processInstance, ProcessNode node, Dto form) {


        Set<Integer> accountIdList = new HashSet<>();
        String executiveIds = node.getExecutiveIds();

        Integer executiveType = node.getExecutiveType();
        switch (executiveType) {
            case 1: // 主管
                String executiveLevels = node.getExecutiveLevels();
                List<Integer> levels = MyStringUtils.splitStringToInteger(executiveLevels, ",");
                if (null == levels || levels.size() == 0) {
                    return new ArrayList<>();
                }
                return getManagersByAccountAndLevel(processInstance.getApplicant(), levels.get(0));
            case 2: // 角色
                List<Integer> integers = splitStringToInteger(executiveIds, ",");
                if (CollectionUtils.isNotEmpty(integers))
                    return processRoleService.getAccountIdsByRoleId(integers);
            case 3: // 个人
                accountIdList.addAll(splitStringToInteger(executiveIds, ","));
                if (CollectionUtils.isNotEmpty(accountIdList))
                    return accountMapper.selectActorByIds(new ArrayList<>(accountIdList));
            case 4:  // 发起人自己
                accountIdList.add(processInstance.getApplicant());
                return accountMapper.selectActorByIds(new ArrayList<>(accountIdList));
            case 5:  // 表单字段
                FormFieldDefine formFieldDefine = formFieldDefineMapper.selectByPrimaryKey(Integer.valueOf(executiveIds));
                Integer relationType = formFieldDefine.getRelationType();
                Integer executiveId = form.getInteger(formFieldDefine.getFieldName());
                switch (relationType) {
                    case 0:    // 员工
                        if (null != executiveId)
                            accountIdList.add(executiveId);
                        break;
                    case 6:    // 所属系统
                    case 7:  // 仓库id查询管理员
                        Dto params = new Dto("relationType", relationType);
                        params.put("fieldValue", executiveId);
                        List<Integer> byCondition = formFieldRelationAccountMapper.getByCondition(params);
                        accountIdList.addAll(byCondition);
                        break;
                }

                if (CollectionUtils.isNotEmpty(accountIdList))
                    return accountMapper.selectActorByIds(new ArrayList<>(accountIdList));
            case 6: // 多级连续主管
                int index = 0;
                Integer currentLevel = processInstance.getCurrentLevel();
                String executiveLevels6 = node.getExecutiveLevels();
                List<Integer> levels6 = MyStringUtils.splitStringToInteger(executiveLevels6, ",");
                if (null != currentLevel) {
                    for (int i = 0; i < levels6.size(); i++) {
                        if (levels6.get(i).compareTo(currentLevel) == 0) {
                            index = i;
                            break;
                        }
                    }
                }
                if (index + 1 >= levels6.size()) {
                    processInstance.setCurrentLevel(0);
                } else {
                    processInstance.setCurrentLevel(levels6.get(index + 1));
                    List<Dto> managersByOrganizationTreeId = getManagersByAccountAndLevel(processInstance.getApplicant(), levels6.get(index + 1));
                    if (CollectionUtils.isNotEmpty(managersByOrganizationTreeId))
                        return managersByOrganizationTreeId;
                    else
                        return getNodeActors(processInstance, node, form);
                }
        }
        return new ArrayList<>();
    }

    public List<Dto> getManagersByAccountAndLevel(Integer applicant, Integer level) {
        Account account6 = accountMapper.selectByPrimaryKey(applicant);
        Integer parentOrganizationId6 = organizationTreeService.getParentOrganizationId(account6.getOrganizationTreeId(), level);
        return POrganizationManagerService.getManagersByOrganizationTreeId(parentOrganizationId6);
    }

    /**
     * 发送待办
     *
     * @param instance
     * @param ids
     * @param transferFlag
     */
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
            toDoList.setFormFixedNodeId(instance.getCurrentFixNodeId());
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
}
