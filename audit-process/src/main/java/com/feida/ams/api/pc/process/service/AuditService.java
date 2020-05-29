package com.feida.ams.api.pc.process.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feida.ams.api.pc.process.bizInterace.BizInterface;
import com.feida.ams.api.pc.process.mapper.CommentAttachmentMapper;
import com.feida.ams.api.pc.process.model.*;
import com.feida.ams.api.pc.system.account.model.Account;
import com.feida.ams.api.pc.system.account.service.AccountService;
import com.feida.ams.api.pc.system.organization.model.OrganizationTree;
import com.feida.ams.api.pc.system.organization.service.OrganizationTreeService;
import com.feida.ams.framework.errors.ProcessError;
import com.feida.common.domain.BaseDto;
import com.feida.common.domain.Dto;
import com.feida.common.exception.ServiceException;
import com.feida.common.util.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

import static com.feida.ams.api.pc.process.bizInterace.BizInterfaceFactory.getInstance;

@Slf4j
@Service
public class AuditService {

    @Autowired
    private ProcessUtil processUtil;

    @Autowired
    private ProcessNodeService processNodeService;
    @Autowired
    private ProcessInstanceService processInstanceService;
    @Autowired
    private TransferService transferService;
    @Autowired
    private ToDolistService toDolistService;
    @Autowired
    private ProcessLogService processLogService;

    @Autowired
    private AccountService accountService;
    @Autowired
    private OrganizationTreeService organizationTreeService;

    @Autowired
    private FormTypeService formTypeService;

    @Autowired
    private CommentAttachmentMapper commentAttachmentMapper;

    /**
     * @param params 申请人id:              applicant
     *               申请人组织id             applicantOrganizationId
     *               表单类型id:            formTypeId
     *               表单id:                  formId
     *               提交的表单的相关字段内容  ………………
     */
    public void reStartProcess(Dto params) {
        Integer processInstanceId = params.getInteger("processInstanceId");
        ProcessInstance processInstance = processInstanceService.selectByPrimaryKey(processInstanceId);
        processInstance.setCurrentNodeId(0);

        ProcessLog processLog = new ProcessLog();
        processLog.setTransferFlag(1);
        processLog.setFormTypeId(params.getInteger("formTypeId"));
        processLog.setFormId(params.getInteger("formId"));
        processLog.setActorOpinion("发起审批");
        processLog.setActorId(params.getInteger("applicant"));
        processLog.setActorType(6);
        processLog.setProcessNodeId(0);

        nextStep(processInstance, params, processLog);
        processLog.setProcessInstanceId(processInstance.getId());
        processLogService.insertSelective(processLog);
    }

    public Integer startProcess(Dto params) {
        Dto processDefinition = processUtil.getRightProcess(params);
        if (null == processDefinition) {
            throw new ServiceException(ProcessError.NO_PROCESS);
        }
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setApplicant(params.getInteger("applicant"));
        processInstance.setApplicantOrganization(params.getInteger("applicantOrganizationId"));
        processInstance.setFormTypeId(params.getInteger("formTypeId"));
        processInstance.setFormId(params.getInteger("formId"));
        processInstance.setProcessDefinitionId(processDefinition.getInteger("id"));
        processInstance.setStatus(1);
        String taskTitleTemplate = processDefinition.getString("taskTitleTemplate");

        Account applicant = accountService.selectByPrimaryKey(params.getInteger("applicant"));
        OrganizationTree org = organizationTreeService.selectByPrimaryKey(params.getInteger("applicantOrganizationId"));

        taskTitleTemplate = taskTitleTemplate.replace("p2", org.getOrganizationName());
        taskTitleTemplate = taskTitleTemplate.replace("p1", applicant.getStaffName());
        processInstance.setTitle(taskTitleTemplate);

        ProcessLog processLog = new ProcessLog();
        processLog.setTransferFlag(1);
        processLog.setFormTypeId(params.getInteger("formTypeId"));
        processLog.setFormId(params.getInteger("formId"));
        processLog.setActorOpinion("发起审批");
        processLog.setActorId(params.getInteger("applicant"));
        processLog.setActorType(8);
        processLog.setProcessNodeId(0);
        nextStep(processInstance, params, processLog);
        processLog.setProcessInstanceId(processInstance.getId());
        processLogService.insertSelective(processLog);
        return processInstance.getId();
    }

    /**
     *  撤销流程
     * @param params processInstanceId
     */
    public void repeal(Dto params) {
        Integer processInstanceId = params.getInteger("processInstanceId");
        ProcessInstance processInstance = processInstanceService.selectByPrimaryKey(processInstanceId);
        Integer currentNodeId = processInstance.getCurrentNodeId();

        // 删除未完成待办
        Example example = new Example(ToDoList.class);
        example.createCriteria()
                .andEqualTo("processInstanceId", processInstanceId)
                .andEqualTo("processNodeId", currentNodeId);
        ToDoList toDoList = new ToDoList();
        toDoList.setStatus((short) 3);
        toDolistService.updateByPrimaryKeySelective(example, toDoList);
        // 流程状态
        processInstance.setStatus(4);

        // 流程日志
        ProcessLog processLog = new ProcessLog();
        processLog.setTransferFlag(1);
        processLog.setProcessInstanceId(processInstanceId);
        processLog.setFormTypeId(processInstance.getFormTypeId());
        processLog.setFormId(processInstance.getFormId());
        processLog.setActorOpinion("撤销流程");
        processLog.setActorId(WebUtils.getSessionAccountId());
        processLog.setActorType(5);
        processLog.setProcessNodeId(processInstance.getCurrentNodeId());
        processLogService.insertSelective(processLog);
    }
    /**
     * 审批人的审批提交
     *
     * @param params 审批意见：                auditResult :  1:同意，3：拒绝， 8：转发
     *               流程实例id：              processInstanceId
     */
    public void submitAudit(Dto params) {

        Integer taskId = params.getInteger("taskId");

        ToDoList toDoList = toDolistService.selectByPrimaryKey(taskId);

        toDoList.setStatus((short) 2); //已完成
        toDolistService.updateByPrimaryKeySelective(toDoList);
        params.put("processInstanceId", toDoList.getProcessInstanceId());
        Dto result = doAudit(params);
        Integer formTypeId = toDoList.getFormTypeId();

        Integer processInstanceResult = result.getInteger("processInstanceResult");

        BizInterface instance;

        switch (processInstanceResult) {
            case 10:
                instance = getInstance(formTypeId);
                instance.intermediate(params);
                break;
            case 2:
                instance = getInstance(formTypeId);
                instance.pass(params);
                break;
            case 3:
                instance = getInstance(formTypeId);
                instance.reject(params);
                break;
            default:
        }
    }

    /**
     * 审批的执行过程
     *
     * @param params auditResult :  审批意见：1:同意，3：拒绝， 8：转发
     *               流程实例id:  processInstanceId
     * @return 流程继续，但是有特殊业务： 10
     * 流程通过，流程结束： 2
     * 流程拒绝，流程结束 ：3
     */
    private Dto doAudit(Dto params) {
        Integer processInstanceId = params.getInteger("processInstanceId");
        ProcessInstance processInstance = processInstanceService.selectByPrimaryKey(processInstanceId);
        Dto result = new BaseDto("formTypeId", processInstance.getFormTypeId());
        Integer auditResult = params.getInteger("auditResult");

        ProcessLog processLog = new ProcessLog();

        switch (auditResult) {
            case 1:  // 审批同意
                BizInterface instance = getInstance(processInstance.getFormTypeId());
                Dto form = instance.getDto(processInstance.getFormId());
                Integer nextStepResult = nextStep(processInstance, form, processLog);
                result.put("processInstanceResult", nextStepResult);
                processLog.setTransferFlag(1);
                processLog.setActorType(1);
                break;
            case 2:  // 审批不同意
                processInstance.setStatus(3);
                processInstanceService.saveOrUpdate(processInstance);
                result.put("processInstanceResult", 3);
                processLog.setTransferFlag(1);
                processLog.setActorType(2);
                break;
            case 4: //转发
                Integer transferId = params.getInteger("transferId");
                Transfer transfer = new Transfer();
                transfer.setAccountId(transferId);
                transfer.setProcessInstanceId(processInstanceId);
                transfer.setProcessNodeId(processInstance.getCurrentNodeId());
                processLog.setTransferFlag(2);
                processLog.setActorType(4);
                processUtil.sendTask(processInstance, Arrays.asList(transferId), 2);
                transferService.insertSelective(transfer);
                break;
        }
        processLog.setActorId(WebUtils.getSessionAccountId());
        processLog.setActorOpinion(params.getString("actorOpinion"));
        processLog.setFormId(processInstance.getFormId());
        processLog.setFormTypeId(processInstance.getFormTypeId());
        processLog.setProcessInstanceId(processInstanceId);
        processLog.setProcessNodeId(processInstance.getCurrentNodeId());
        processLogService.insertSelective(processLog);
        return result;
    }

    /**
     * @param processInstance
     * @return 流程继续： 1
     * 流程继续有特殊业务： 10
     * 流程结束： 2
     */
    private Integer nextStep(ProcessInstance processInstance, Dto form, ProcessLog processLog) {
        Integer currentNodeId = processInstance.getCurrentNodeId();
        Integer result = 1;
        if (null != currentNodeId) {
            ProcessNode processNode = processNodeService.selectByPrimaryKey(currentNodeId);
            Integer passCodition = processNode.getPassCodition();
            BaseDto toDoParams = new BaseDto("processInstanceId", processInstance.getId());
            toDoParams.put("processNodeId", currentNodeId);
            toDoParams.put("status", 1);
            List<Dto> toDoLists = toDolistService.getByCondition(toDoParams);
            if (CollectionUtils.isNotEmpty(toDoLists)) {
                if (null != passCodition && passCodition.compareTo(2) == 0) { //如果需要节点所有审批人同意
                    return result;
                } else {
                    Example example = new Example(ToDoList.class);
                    example.createCriteria()
                            .andEqualTo("processInstanceId", processInstance.getId())
                            .andEqualTo("processNodeId", currentNodeId)
                            .andEqualTo("status", 1);
                    ToDoList toDoList = new ToDoList();
                    toDoList.setStatus((short) 3);
                    toDolistService.updateByPrimaryKeySelective(example, toDoList);
                }
            }
            // TODO node是否需要执行中间业务  result = 10
        }

        ProcessNode processNode = processUtil.nextNode(processInstance);
        if (null == processNode) {
            processInstance.setStatus(2);
            processInstanceService.saveOrUpdate(processInstance);
            return 2;
        }

        processInstance.setCurrentNodeNumber(processNode.getNodeNo());
        processInstance.setCurrentNodeId(processNode.getId());
        processInstanceService.saveOrUpdate(processInstance);
        processLog.setProcessInstanceId(processInstance.getId());

        Set<Integer> actors = processUtil.getTheNodeActors(processNode, processInstance, form);
        if (CollectionUtils.isEmpty(actors) && null != processNode.getMandatoryFlag() && processNode.getMandatoryFlag().compareTo(1) == 0) {
            nextStep(processInstance, form, processLog);
            return 0;
        } else {
            processUtil.sendTask(processInstance, actors, 1);
        }

        return result;
    }


    public void comment(Dto params) {
        ProcessInstance processInstance = processInstanceService.selectByPrimaryKey(params.getInteger("processInstanceId"));

        ProcessLog processLog = new ProcessLog();
        processLog.setTransferFlag(1);
        processLog.setFormTypeId(processInstance.getFormTypeId());
        processLog.setFormId(processInstance.getFormId());
        processLog.setActorOpinion(params.getString("actorOpinion"));
        processLog.setActorId(WebUtils.getSessionAccountId());
        processLog.setActorType(7);
        processLog.setProcessInstanceId(processInstance.getId());
        processLog.setProcessNodeId(processInstance.getCurrentNodeId());
        JSONArray attachMents = (JSONArray) params.get("attachMents");
        processLogService.insertSelective(processLog);
        List<CommentAttachment> commentAttachments = new ArrayList<>();
        for(Object jsonObject: attachMents){
            JSONObject att = (JSONObject) jsonObject;
            CommentAttachment commentAttachment = new CommentAttachment();
            commentAttachment.setFileName(att.getString("fileName"));
            commentAttachment.setFilePath(att.getString("filePath"));
            commentAttachment.setSourceName(att.getString("sourceName"));
            commentAttachment.setProcessLogId(processLog.getId());
            commentAttachments.add(commentAttachment);
        }
        commentAttachmentMapper.insertList(commentAttachments);
    }


    public Dto getProcessActors(Integer processInstanceId) {
        ProcessInstance processInstance = processInstanceService.selectByPrimaryKey(processInstanceId);
        Integer formId = processInstance.getFormId();
        BizInterface instance = getInstance(processInstance.getFormTypeId());
        Dto dto = instance.getDto(formId);
        return getProcessActors(processInstance, dto);
    }

    public Dto getProcessActors(Integer processInstanceId, Dto form) {
        ProcessInstance processInstance = processInstanceService.selectByPrimaryKey(processInstanceId);
        return getProcessActors(processInstance, form);
    }

    public Dto getProcessActors(ProcessInstance processInstance, Dto form) {
        Dto result = new BaseDto();
        Integer currentNodeNumber = processInstance.getCurrentNodeNumber();
        if (null == currentNodeNumber)
            currentNodeNumber = 0;

        List<Dto> logs = processUtil.getProcessHisLogWithAttchments(processInstance.getId());
        result.put("processLogs", logs);

        Set<Integer> history = new HashSet<>();
        if (CollectionUtils.isNotEmpty(logs)) {
            history = logs.stream().map(k -> {
                return k.getInteger("actorId");
            }).collect(Collectors.toSet());
        }

        Example example = new Example(ProcessNode.class);
        example.createCriteria()
                .andEqualTo("processDefinitionId", processInstance.getProcessDefinitionId());
        example.orderBy("nodeNo");
        List<ProcessNode> select = processNodeService.selectByExalple(example);

        List<Dto> nodeList = new ArrayList<>();
        for (ProcessNode node : select) {
            BaseDto nodeDto = new BaseDto();
            List<Dto> actors = new ArrayList<>();

            Integer nodeNo = node.getNodeNo();
            int i = nodeNo.compareTo(currentNodeNumber);
            if (i == 0) {
                BaseDto toDoParams = new BaseDto("processInstanceId", processInstance.getId());
                toDoParams.put("status", 1);
                toDoParams.put("processNodeId", node.getId());
                List<Dto> nodeUnresolved = toDolistService.getByCondition(toDoParams);
                if (CollectionUtils.isNotEmpty(nodeUnresolved)) {
                    for (Dto k : nodeUnresolved) {
                        if (!history.contains(k.getInteger("actorId"))) {
                            BaseDto baseDto = new BaseDto();
                            baseDto.put("actorName", k.get("staffName"));
                            actors.add(baseDto);
                            history.add(k.getInteger("actorId"));
                        }
                    }
                    if (CollectionUtils.isNotEmpty(actors)) {
                        nodeDto.put("actors", actors);
                        nodeList.add(nodeDto);
                    }
                }
            } else if (i > 0) {
                List<Dto> accountIdsByActors = processUtil.getAccountIdsByActors(processInstance, node.getId(), form);
                if (CollectionUtils.isNotEmpty(accountIdsByActors)) {
                    for (Dto k : accountIdsByActors) {
                        if (!history.contains(k.getInteger("accountId"))) {
                            BaseDto baseDto = new BaseDto();
                            baseDto.put("actorName", k.get("staffName"));
                            actors.add(baseDto);
                            history.add(k.getInteger("accountId"));
                        }
                    }
                    if (CollectionUtils.isNotEmpty(actors)) {
                        nodeDto.put("actors", actors);
                        nodeList.add(nodeDto);
                    }
                }
            }
        }
        result.put("nodeList", nodeList);
        return result;
    }

    public boolean hasProcess(Integer formTypeId) {
        FormType formType = formTypeService.selectByPrimaryKey(formTypeId);
        Integer processDefinitionVersion = formType.getProcessDefinitionVersion();
        if (null != processDefinitionVersion && processDefinitionVersion.compareTo(0) > 0) {
            return true;
        }
        return false;
    }


}
