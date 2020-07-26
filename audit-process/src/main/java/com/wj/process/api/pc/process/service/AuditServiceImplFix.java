package com.wj.process.api.pc.process.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feida.common.domain.Dto;
import com.feida.common.exception.ServiceException;
import com.feida.common.util.WebUtils;
import com.feida.omms.common.framework.enums.MessageEnum;
import com.feida.omms.common.framework.errors.ProcessError;
import com.feida.omms.dao.businessData.warehouse.mapper.WarehouseMapper;
import com.feida.omms.dao.businessData.warehouse.model.Warehouse;
import com.feida.omms.dao.process.mapper.CommentAttachmentMapper;
import com.feida.omms.dao.process.mapper.FormFieldDefineMapper;
import com.feida.omms.dao.process.model.*;
import com.feida.omms.dao.system.account.mapper.AccountGroupMapper;
import com.feida.omms.dao.system.account.mapper.AccountMapper;
import com.feida.omms.dao.system.account.model.Account;
import com.feida.omms.dao.system.organization.model.OrganizationTree;
import com.feida.process.api.pc.process.bizInterace.BizInterface;
import com.feida.process.api.pc.process.bizInterace.BizInterfaceFactory;
import com.feida.process.api.pc.system.message.service.PMessageService;
import com.feida.process.api.pc.system.organization.service.POrganizationManagerService;
import com.feida.process.api.pc.system.organization.service.POrganizationTreeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

import static com.feida.common.util.MyStringUtils.splitStringToInteger;

@Primary
@Slf4j
@Service
public class AuditServiceImplFix implements AuditService {
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
    private AccountMapper accountMapper;

    @Autowired
    private POrganizationTreeService organizationTreeService;

    @Autowired
    private FormTypeService formTypeService;

    @Autowired
    private CommentAttachmentMapper commentAttachmentMapper;

    @Autowired
    private ProcessCcListService processCcListService;

    @Autowired
    private ProcessRoleService processRoleService;

    @Autowired
    private FormFieldDefineMapper formFieldDefineMapper;

    @Autowired
    private POrganizationManagerService POrganizationManagerService;
    @Autowired
    private WarehouseMapper warehouseMapper;
    @Autowired
    private PDeviceCategoryService deviceCategoryService;
    @Autowired
    private AccountGroupMapper accountGroupMapper;
    @Autowired
    private PMessageService PMessageService;
    @Autowired
    private BizInterfaceFactory bizInterfaceFactory;
    @Override
    public Integer startProcess(Dto params) throws Exception {
        Integer formTypeId = params.getInteger("formTypeId");
        FormType formType = formTypeService.selectByPrimaryKey(formTypeId);
        // 走不走流程
        if (formType.getProcessType().compareTo(1) == 0) {
            BizInterface instance = bizInterfaceFactory.bizInterfaceInstance(formTypeId);
            params.put("id", params.get("formId"));
            instance.pass(params);
            return null;
        }

        // 发起日志 或者重新提交
        ProcessLog processLog = new ProcessLog();
        ProcessInstance processInstance = null;
        if (null != params.getInteger("processInstanceId")) {
            processInstance = processInstanceService.selectByPrimaryKey(params.getInteger("processInstanceId"));
            processLog.setActorType(8);
            processLog.setActorOpinion("重新提交");
            processLog.setFormId(params.getInteger("formId"));
            processLog.setActorId(params.getInteger("applicant"));
            formTypeId = processInstance.getFormTypeId();
        } else {
            processLog.setActorType(6);
            processLog.setActorOpinion("发起审批");
            processLog.setFormId(params.getInteger("formId"));
            processLog.setActorId(params.getInteger("applicant"));

            formTypeId = params.getInteger("formTypeId");
            processInstance = new ProcessInstance();
            processInstance.setApplicant(params.getInteger("applicant"));
            processInstance.setApplicantOrganization(params.getInteger("applicantOrganizationId"));
            processInstance.setFormTypeId(formTypeId);
            processInstance.setFormId(params.getInteger("formId"));
        }

        // 查询流程定义
        Dto processDefinition = processUtil.getRightProcess(params);
        // 生成标题
        String taskTitleTemplate = processDefinition.getString("taskTitleTemplate");
        Account applicant = accountMapper.selectByPrimaryKey(params.getInteger("applicant"));
        OrganizationTree org = organizationTreeService.selectByPrimaryKey(processInstance.getApplicantOrganization());
        taskTitleTemplate = taskTitleTemplate.replace("p2", org.getOrganizationName());
        taskTitleTemplate = taskTitleTemplate.replace("p1", applicant.getStaffName());
        processInstance.setTitle(taskTitleTemplate);

        processInstance.setProcessDefinitionId(processDefinition.getInteger("id"));
        processInstance.setStatus(1);
        processInstance.setCurrentLevel(-1);
        processInstance.setCurrentNodeId(-1);
        processInstance.setCurrentNodeNumber(0);
        processInstance.setCurrentOrder(-1);

        // 所有审批人
        Dto dto = setAllActors2Instance(processInstance, params);
        // 第一节点类型
        JSONObject nextNode = dto.getJSONObject("1");
        if (null == nextNode) {
            throw new ServiceException(ProcessError.NO_NODES);
        }
        Integer nodeType = nextNode.getInteger("nodeType");

        if (nodeType.compareTo(2) == 0) {
            Integer formFixedNodeId = nextNode.getInteger("formFixedNodeId");
            if (null != formFixedNodeId && formFixedNodeId.compareTo(0) > 0) {
                BizInterface instance = bizInterfaceFactory.bizInterfaceInstance(processInstance.getFormTypeId());
                Dto Dto = new Dto();
                Dto.put("id", processInstance.getFormId());
                Dto.put("fixedNodeId", formFixedNodeId);
                instance.beforeFixedNode(Dto);
            }
            List<Integer> transferIds = params.getList("transferIds");
            if (CollectionUtils.isEmpty(transferIds)) {
                throw new ServiceException(ProcessError.NEXT_NODE_NEED_SELECT_ACTOR);
            }
            processInstance.setCurrentNodeNumber(1);
            processInstance.setCurrentNodeId(nextNode.getInteger("id"));
            transferService.addBypProcessInstance(processInstance, transferIds);
            processInstance.setCurrentFixNodeId(formFixedNodeId);
            processUtil.sendTask(processInstance, transferIds, 1);
        } else {
            checkNextNodeWhenModeType1(processInstance, dto);
        }

        // 日志与抄送
        processInstance.setAllActorsJson(null);
        processInstanceService.saveOrUpdate(processInstance);
        processLog.setTransferFlag(1);
        processLog.setProcessNodeId(-1);
        processLog.setFormTypeId(formTypeId);

        processLog.setProcessInstanceId(processInstance.getId());
        processLogService.insertSelective(processLog);
        processCcListService.sendByInstance(processInstance, 2);

        return processInstance.getId();
    }

    @Override
    public void submitAudit(Dto params) throws Exception {
        ToDoList toDoList = processUtil.getTodoListFromParams(params);
        toDoList.setStatus((short) 2); //已完成
        toDolistService.updateByPrimaryKeySelective(toDoList);
        Integer processInstanceId = toDoList.getProcessInstanceId();
        ProcessInstance processInstance = processInstanceService.selectByPrimaryKey(processInstanceId);

        Integer auditResult = params.getInteger("auditResult");
        ProcessLog processLog = new ProcessLog();
        processLog.setActorOpinion(params.getString("actorOpinion"));
        BizInterface instance = bizInterfaceFactory.bizInterfaceInstance(processInstance.getFormTypeId());
        Set<Integer> history = processUtil.getProcessHisActors(processInstance.getId());
        switch (auditResult) {
            case 1:  // 审批同意
                processLog.setActorType(1);
                Integer currentNodeNumber = processInstance.getCurrentNodeNumber();
                JSONObject nodeMap = (JSONObject) JSON.parse(processInstance.getAllActorsJson());
                JSONObject currNode = nodeMap.getJSONObject(currentNodeNumber.toString());
                boolean b = checkCurrNode(processInstance, currNode);
                if (b) { // 进入下一节点
                    JSONObject nextNode = nodeMap.getJSONObject("" + (currentNodeNumber + 1));
                    if (null == nextNode) { // 流程结束
                        instance.pass(new Dto("id", processInstance.getFormId()));
                        processCcListService.sendByInstance(processInstance, 1);
                        processInstance.setStatus(3);
                        processInstanceService.saveOrUpdate(processInstance);
                        processLog.setFormId(processInstance.getFormId());
                        processLog.setProcessInstanceId(processInstanceId);
                        processLog.setProcessNodeId(processInstance.getCurrentNodeId());
                        processLog.setFormTypeId(processInstance.getFormTypeId());
                        processLog.setActorId(WebUtils.getSessionAccountId());
                        processLog.setActorTime(new Date());
                        processLogService.insertSelective(processLog);
                        return;
                    }
                    Integer nextNodeType = nextNode.getInteger("nodeType");
                    if (nextNodeType.compareTo(2) == 0) {
                        // 有关联业务节点
                        Integer formFixedNodeId = nextNode.getInteger("formFixedNodeId");
                        if (null != formFixedNodeId && formFixedNodeId.compareTo(0) > 0) {
                            Dto Dto = new Dto();
                            Dto.put("id", processInstance.getFormId());
                            Dto.put("fixedNodeId", formFixedNodeId);
                            instance.beforeFixedNode(Dto);
                        }
                        List<Integer> transferIds = params.getList("transferIds");
                        if (CollectionUtils.isEmpty(transferIds)) {
                            throw new ServiceException(ProcessError.NEXT_NODE_NEED_SELECT_ACTOR);
                        }
                        processInstance.setCurrentNodeId(nextNode.getInteger("id"));
                        processInstance.setCurrentNodeNumber(nextNode.getInteger("nodeNo"));
                        transferService.addBypProcessInstance(processInstance, transferIds);
                        processInstance.setCurrentFixNodeId(formFixedNodeId);
                        processUtil.sendTask(processInstance, transferIds, 1);
                    } else {
                        processInstance.setCurrentLevel(-1);
                        processInstance.setCurrentOrder(-1);
                        checkNextNodeWhenModeType1(processInstance, nodeMap);
                    }
                }
                processLog.setTransferFlag(1);
                processLog.setActorType(1);
                break;
            case 2: //不同意
                processInstance.setStatus(2);
                processLog.setTransferFlag(1);
                processLog.setActorType(2);
                toDolistService.updateByInsAndNodeId(processInstance.getId(), processInstance.getCurrentNodeId(), (short) 3);
                instance.reject(new Dto("id", processInstance.getFormId()));
                PMessageService.sendMes(2,processInstance.getApplicant(),
                        MessageEnum.RESOURCE_TYPE_FORM.getV(), processInstance.getFormTypeId(),
                        processInstance.getFormId(), "您的申请："+ processInstance.getTitle()+" 已被拒绝。",
                        Arrays.asList(processInstance.getApplicant()));
                break;
            case 3: // 上一节点
                lastStep(processInstance, history);
                processLog.setTransferFlag(1);
                processLog.setActorType(2);
                break;
            case 4: //转发
                List<Integer> transferIds = params.getList("transferIds");
                transferService.addBypProcessInstance(processInstance, transferIds);
                processLog.setTransferFlag(2);
                processLog.setActorType(4);
                ProcessNode processNode = processNodeService.selectByPrimaryKey(processInstance.getCurrentNodeId());
                processInstance.setCurrentFixNodeId(processNode.getFormFixedNodeId());
                processUtil.sendTask(processInstance, transferIds, 2);
                break;
        }
        processInstance.setAllActorsJson(null);
        processInstanceService.saveOrUpdate(processInstance);
        processLog.setFormId(processInstance.getFormId());
        processLog.setProcessInstanceId(processInstanceId);
        processLog.setProcessNodeId(processInstance.getCurrentNodeId());
        processLog.setFormTypeId(processInstance.getFormTypeId());
        processLog.setActorId(WebUtils.getSessionAccountId());
        processLog.setActorTime(new Date());
        processLogService.insertSelective(processLog);
    }

    @Override
    public void doRepeal(Integer processInstanceId) throws Exception {
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
        //处理表单
        Integer formId = processInstance.getFormId();
        Integer formTypeId = processInstance.getFormTypeId();
        Dto params = new Dto("id", formId);
        BizInterface instance;
        instance = bizInterfaceFactory.bizInterfaceInstance(formTypeId);
        instance.rePeal(params);
        processLogService.insertSelective(processLog);
    }

    @Override
    public void doComment(Dto params) {
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
        for (Object jsonObject : attachMents) {
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


    private void lastStep(ProcessInstance processInstance, Set<Integer> history) throws Exception {
        Integer currentNodeNumber = processInstance.getCurrentNodeNumber();
        BizInterface instance = bizInterfaceFactory.bizInterfaceInstance(processInstance.getFormTypeId());
        if (currentNodeNumber.compareTo(2) < 0) {

            instance.reject(new Dto("id", processInstance.getFormId()));
        }
        JSONObject nodeMap = (JSONObject) JSON.parse(processInstance.getAllActorsJson());
        JSONObject currNode = nodeMap.getJSONObject(currentNodeNumber.toString());
        currentNodeNumber--;
        JSONObject lastNode = nodeMap.getJSONObject(currentNodeNumber.toString());
        Integer formFixedNodeId = lastNode.getInteger("formFixedNodeId");

        if (null == formFixedNodeId && null != currNode.getInteger("formFixedNodeId")) {
            throw new ServiceException(ProcessError.CANOT_BACK);
        }
        Dto Dto = new Dto();
        Dto.put("id", processInstance.getFormId());
        Dto.put("fixedNodeId", formFixedNodeId);
        instance.beforeFixedNode(Dto);

        Integer nodeType = lastNode.getInteger("nodeType");

        if (nodeType.compareTo(2) == 0) {
            List<Integer> id = transferService.getAccountIdByProcessInstanceNodeId(processInstance.getId(), lastNode.getInteger("id"));
            processInstance.setCurrentFixNodeId(formFixedNodeId);
            processUtil.sendTask(processInstance, id, 1);
            return;
        }
        processInstance.setCurrentNodeNumber(currentNodeNumber - 1);
        processInstance.setCurrentLevel(-1);
        processInstance.setCurrentOrder(-1);
        checkNextNodeWhenModeType1(processInstance, nodeMap);
    }


    /**
     * 查看详情时查询历史执行人、未执行的执行人
     *
     * @param processInstanceId
     * @return
     */
    @Override
    public Dto getProcessActors(Integer processInstanceId) {
        ProcessInstance processInstance = processInstanceService.selectByPrimaryKey(processInstanceId);
        BizInterface bizInterface = bizInterfaceFactory.bizInterfaceInstance(processInstance.getFormTypeId());
        Dto dto = bizInterface.getDto(processInstance.getFormId());
        return getProcessActors(processInstanceId, dto);
    }

    /**
     * 查看详情时查询历史执行人、未执行的执行人
     *
     * @param processInstanceId
     * @param form
     * @return
     */
    @Override
    public Dto getProcessActors(Integer processInstanceId, Dto form) {
        Dto result = new Dto();
        ProcessInstance processInstance = processInstanceService.selectByPrimaryKey(processInstanceId);
        JSONObject nodeMap = (JSONObject) JSON.parse(processInstance.getAllActorsJson());

        int length = nodeMap.size();
        Integer currentNodeNumber = processInstance.getCurrentNodeNumber();
        if (currentNodeNumber.compareTo(0) < 0)
            currentNodeNumber = 1;
        List<Dto> logs = processUtil.getProcessHisLogWithAttchments(processInstance.getId());


        for (Dto log : logs) {
            if (log.getInteger("actorType").compareTo(6) == 0) {
                result.put("startNode", log);
                break;
            }
        }

        List nodeList = new ArrayList();

        List logNodes = new ArrayList<>();

        for (int i = 1; i <= length; i++) {
            JSONObject currNode = nodeMap.getJSONObject("" + i);
            int compare = currentNodeNumber.compareTo(i);
            switch (compare) {
                case 0:
                    Dto toDoParams = new Dto("processInstanceId", processInstance.getId());
                    toDoParams.put("processNodeId", currNode.getInteger("id"));
                    toDoParams.put("status", 1);
                    List<Dto> toDoLists = toDolistService.getByCondition(toDoParams);
                    result.put("toDoLists", toDoLists);
                case 1:
                    Integer id = currNode.getInteger("id");
                    List<Dto> nodeLogs = new ArrayList<>();
                    for (Dto log : logs) {
                        Integer processNodeId = log.getInteger("processNodeId");
                        if (id.compareTo(processNodeId) == 0) {
                            nodeLogs.add(log);
                        }
                        Integer actorType = log.getInteger("actorType");
                        if (actorType.compareTo(5) == 0) {
                            nodeLogs.clear();
                        }
                    }
                    if (CollectionUtils.isNotEmpty(nodeLogs)) {
                        currNode.put("nodeLogs", nodeLogs);
                        logNodes.add(currNode);
                    }
                    break;
                case -1:
                    if (null != currNode.getInteger("hasActor") && currNode.getInteger("hasActor").compareTo(2) == 0) {
                        nodeList.add(currNode);
                    }
                    break;
            }
        }

        result.put("logNodes", logNodes);
        result.put("nodeList", nodeList);
        return result;
    }

    /**
     * 申请时，预查询执行人
     *
     * @param params
     * @param applicant
     * @return
     * @throws Exception
     */
    @Override
    public List<JSONObject> getProcessActorsWhenApplicate(Dto params, Integer applicant) throws Exception {
        FormType formType = formTypeService.selectByPrimaryKey(params.getInteger("formTypeId"));
        // 走不走流程
        if (formType.getProcessType().compareTo(1) == 0) {
            return null;
        }
        // 查询流程定义
        Dto processDefinition = processUtil.getRightProcess(params);
        Dto dto = setAllActors(processDefinition.getInteger("id"), applicant, params);
        int size = dto.size();
        List<JSONObject> nodeList = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            JSONObject jsonObject = dto.getJSONObject("" + i);
            if (null != jsonObject)
                nodeList.add(jsonObject);
        }

        return nodeList;
    }

    /**
     * 根据表单字段查询流程执行人
     *
     * @param processDefinitionId
     * @param applicant
     * @param form
     * @return
     */
    private Dto setAllActors(Integer processDefinitionId, Integer applicant, Dto form) {
        List<Dto> processNodeList = processNodeService.getByProcessId(processDefinitionId);
        Set<Integer> history = new HashSet<>();
        history.add(applicant);
        Dto allActorsJson = new Dto();
        for (int i = 0; i < processNodeList.size(); i++) {
            Dto node = processNodeList.get(i);
            allActorsJson.put(node.getString("nodeNo"), node);


            Integer nodeType = node.getInteger("nodeType");
            boolean mandatory = node.getInteger("mandatoryFlag").compareTo(1) == 0;
            Dto nextNode = null;
            if (i + 1 > processNodeList.size()) nextNode = processNodeList.get(i + 1);
            if (null != nextNode && nextNode.getInteger("nodeType").compareTo(2) == 0) {
                mandatory = false;
            }
            int hasActor = 0;
            if (nodeType.compareTo(1) == 0) {
                Integer executiveType = node.getInteger("executiveType");
                switch (executiveType) {
                    case 1:
                        String executiveLevels1 = node.getString("executiveLevels");
                        List<Integer> ids1 = splitStringToInteger(executiveLevels1, ",");
                        Integer level1 = 1;
                        if (CollectionUtils.isNotEmpty(ids1)) {
                            level1 = ids1.get(0);
                        }
                        List<Dto> managersByAccountAndLevel1 = processUtil.getManagersByAccountAndLevel(applicant, level1);
                        if (CollectionUtils.isNotEmpty(managersByAccountAndLevel1)) {
                            for (Iterator<Dto> actorIterator = managersByAccountAndLevel1.iterator(); actorIterator.hasNext(); ) {
                                Dto actor = actorIterator.next();
                                if (mandatory && history.contains(actor.getInteger("accountId"))) {
                                    actorIterator.remove();
                                } else {
                                    history.add(actor.getInteger("accountId"));
                                    hasActor = 2;
                                }
                            }
                        }
                        if (hasActor == 2) {
                            node.put("actors", managersByAccountAndLevel1);
                            node.put("actorsIds", managersByAccountAndLevel1.stream().map(k -> k.getInteger("accountId")).collect(Collectors.toSet()));
                        }
                        break;
//                    case 6:
//                        String executiveLevels = node.getString("executiveLevels");
//                        List<Integer> ids = splitStringToInteger(executiveLevels, ",");
//                        JSONObject levelDto = new JSONObject();
//                        for (Iterator<Integer> levelIterator = ids.iterator(); levelIterator.hasNext(); ) {
//                            Integer level = levelIterator.next();
//                            List<Dto> managersByAccountAndLevel = processUtil.getManagersByAccountAndLevel(applicant, level);
//                            if (CollectionUtils.isNotEmpty(managersByAccountAndLevel)) {
//                                for (Iterator<Dto> actorIterator = managersByAccountAndLevel.iterator(); actorIterator.hasNext(); ) {
//                                    Dto actor = actorIterator.next();
//                                    if (mandatory && history.contains(actor.getInteger("accountId"))) {
//                                        actorIterator.remove();
//                                    } else {
//                                        history.add(actor.getInteger("accountId"));
//                                    }
//                                }
//                                hasActor = 1;
//                            }
//                            if (CollectionUtils.isEmpty(managersByAccountAndLevel)) {
//                                levelIterator.remove();
//                            } else {
//                                levelDto.put(level.toString(), managersByAccountAndLevel);
//                                levelDto.put("actorIds" + level, managersByAccountAndLevel.stream().map(k -> k.getInteger("accountId")).collect(Collectors.toSet()));
//                                hasActor = 2;
//                            }
//                        }
//                        if (CollectionUtils.isNotEmpty(ids)) {
//                            hasActor = 2;
//                            node.put("executiveLevels", ids);
//                            node.put("levelActors", levelDto);
//                        }
//                        break;

                    case 2:  // 角色
                        List<Integer> ids2 = splitStringToInteger(node.getString("executiveIds"), ",");
                        List<Dto> accountIdsByRoleId = processRoleService.getAccountIdsByRoleId(ids2);
                        if (CollectionUtils.isNotEmpty(accountIdsByRoleId)) {
                            hasActor = 1;
                            for (Iterator<Dto> iterator = accountIdsByRoleId.iterator(); iterator.hasNext(); ) {
                                Dto actor = iterator.next();
                                if (mandatory && history.contains(actor.getInteger("accountId"))) {
                                    accountIdsByRoleId.remove(actor);
                                } else {
                                    history.add(actor.getInteger("accountId"));
                                }
                            }
                        }
                        if (CollectionUtils.isNotEmpty(accountIdsByRoleId)) {
                            hasActor = 2;
                            node.put("actors", accountIdsByRoleId);
                            node.put("actorsIds", accountIdsByRoleId.stream().map(k -> k.getInteger("accountId")).collect(Collectors.toSet()));
                        }
                        break;
                    case 3: // 个人
                        List<Integer> ids3 = splitStringToInteger(node.getString("executiveIds"), ",");
                        for (Iterator<Integer> iterator = ids3.iterator(); iterator.hasNext(); ) {
                            Integer actor = iterator.next();
                            if (mandatory && history.contains(actor)) {
                                ids3.remove(actor);
                            } else {
                                history.add(actor.getInteger("accountId"));
                            }
                        }
                        if (CollectionUtils.isNotEmpty(ids3)) {
                            hasActor = 2;
                            List<Dto> dtos = accountMapper.selectActorByIds(ids3);
                            node.put("actors", dtos);
                            node.put("actorsIds", ids3);
                        }
                        break;
                    case 5:  // 表单字段
                        FormFieldDefine formFieldDefine = formFieldDefineMapper.selectByPrimaryKey(Integer.valueOf(node.getString("executiveIds")));
                        Integer relationType = formFieldDefine.getRelationType();
                        Integer executiveId = form.getInteger(formFieldDefine.getFieldName());
                        if (null == executiveId) {
                            throw new ServiceException(ProcessError.NO_FORM_FIELD_VALUE);
                        }
                        List<Integer> ids5 = new ArrayList<>();
                        if (null == relationType)
                            relationType = 0;
                        switch (relationType) {
                            case 1:    // 员工
                                if (null != executiveId) {
                                    ids5.add(executiveId);
                                }
                                break;
                            case 2:    // 员工关联联部门主管
                                List<Dto> managersByAccountAndLevel = processUtil.getManagersByAccountAndLevel(executiveId, 1);
                                if (CollectionUtils.isNotEmpty(managersByAccountAndLevel)) {
                                    ids5.add(managersByAccountAndLevel.get(0).getInteger("accountId"));
                                }
                                break;
                            case 3:    // 部门关联部门主管
                                List<Dto> managersByOrganizationTreeId = POrganizationManagerService.getManagersByOrganizationTreeId(executiveId);
                                if (CollectionUtils.isNotEmpty(managersByOrganizationTreeId)) {
                                    ids5.add(managersByOrganizationTreeId.get(0).getInteger("accountId"));
                                }
                                break;
                            case 6:    // 所属系统
                                Integer rootId = deviceCategoryService.getRootId(executiveId);
                                if (null != rootId)
                                    ids5.add(rootId);
                                List<Integer> byGroupId = accountGroupMapper.getByGroupId(rootId);
                                ids5.addAll(byGroupId);
                            case 7:  // 仓库id查询管理员
                                Warehouse warehouse = warehouseMapper.selectByPrimaryKey(executiveId);
                                if (null != warehouse)
                                    ids5.add(warehouse.getId());
                                break;
                        }
                        List<Dto> dtos5 = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(ids5)) {
                            hasActor = 1;
                            for (Iterator<Integer> iterator = ids5.iterator(); iterator.hasNext(); ) {
                                Integer actor = iterator.next();
                                if (mandatory && history.contains(actor)) {
                                    iterator.remove();
                                } else {
                                    history.add(actor.getInteger("accountId"));
                                }
                            }
                            dtos5 = accountMapper.selectActorByIds(ids5);
                        }
                        if (CollectionUtils.isNotEmpty(ids5)) {
                            hasActor = 2;
                            node.put("actors", dtos5);
                            node.put("actorsIds", ids5);
                        }
                        break;
                    case 4: // 发起人自己
                        List<Integer> ids4 = Arrays.asList(applicant);
                        List<Dto> dtos4 = accountMapper.selectActorByIds(ids4);
                        node.put("actors", dtos4);
                        node.put("actorsIds", ids4);
                        hasActor = 2;
                }

                if (!mandatory && hasActor != 2) {
                    throw new ServiceException(ProcessError.UNMANDATORY_NOACTORS);
                }
                node.put("hasActor", hasActor);
            }
        }
        return allActorsJson;

    }

    /**
     * 提交后流程实例执行人
     *
     * @param processInstance
     * @param form
     * @return
     */
    private Dto setAllActors2Instance(ProcessInstance processInstance, Dto form) {

        Dto allActorsJson = setAllActors(processInstance.getProcessDefinitionId(), processInstance.getApplicant(), form);

        processInstance.setAllActorsJson(allActorsJson.toJson());
        processInstanceService.saveOrUpdate(processInstance);
        return allActorsJson;
    }

    private boolean checkCurrNode(ProcessInstance processInstance, JSONObject currNode) {
        // 存在未完成任务
        Dto toDoParams = new Dto("processInstanceId", processInstance.getId());
        toDoParams.put("processNodeId", currNode.getInteger("id"));
        toDoParams.put("status", 1);
        List<Dto> toDoLists = toDolistService.getByCondition(toDoParams);
        if (CollectionUtils.isNotEmpty(toDoLists)) {
            Integer passCodition = currNode.getInteger("passCodition");
            if (passCodition.compareTo(2) == 0) { //如果需要节点所有审批人同意
                return false;
            }
            toDolistService.updateByInsAndNodeId(processInstance.getId(), currNode.getInteger("id"), (short) 3);
        }

        BizInterface instance = bizInterfaceFactory.bizInterfaceInstance(processInstance.getFormTypeId());
        Integer formFixedNodeId = currNode.getInteger("formFixedNodeId");
        if (null != formFixedNodeId && formFixedNodeId.compareTo(0) > 0) {
            Dto Dto = new Dto();
            Dto.put("id", processInstance.getFormId());
            Dto.put("fixedNodeId", formFixedNodeId);
            instance.afterFixedNode(Dto);
        }
        return true;
    }

    private void checkNextNodeWhenModeType1(ProcessInstance processInstance, JSONObject nodeMap) throws Exception {
        Integer currentNodeNumber = processInstance.getCurrentNodeNumber();
        currentNodeNumber++;
        JSONObject currNode = nodeMap.getJSONObject(currentNodeNumber.toString());
        // 有关联业务节点

        BizInterface instance = bizInterfaceFactory.bizInterfaceInstance(processInstance.getFormTypeId());

        if (currNode == null) {
            instance.pass(new Dto("id", processInstance.getFormId()));
            processCcListService.sendByInstance(processInstance, 1);
            return;
        }

        Integer formFixedNodeId = currNode.getInteger("formFixedNodeId");
        if (null != formFixedNodeId && formFixedNodeId.compareTo(0) > 0) {
            Dto Dto = new Dto();
            Dto.put("id", processInstance.getFormId());
            Dto.put("fixedNodeId", formFixedNodeId);
            instance.beforeFixedNode(Dto);
            processInstance.setCurrentFixNodeId(formFixedNodeId);
        }
        processInstance.setCurrentNodeNumber(currentNodeNumber);
        processInstance.setCurrentNodeId(currNode.getInteger("id"));
        Integer hasActor = currNode.getInteger("hasActor");
        if (null == hasActor || hasActor.compareTo(2) != 0) {
            checkNextNodeWhenModeType1(processInstance, nodeMap);
            return;
        }
        Integer executiveType = currNode.getInteger("executiveType");
        List<Integer> idsForSend = new ArrayList<>();
        switch (executiveType) {
            case 1:
//            case 6:
//                JSONArray levels = currNode.getJSONArray("executiveLevels");
//                while (++currentLevel < levels.size() && CollectionUtils.isEmpty(idsForSend)) {
//                    processInstance.setCurrentLevel(currentLevel);
//                    Integer level = (Integer) levels.get(currentLevel);
//                    JSONObject levelActors = (JSONObject) currNode.get("levelActors");
//                    JSONArray actorIds = levelActors.getJSONArray("actorIds" + level);
//                    for (Object id : actorIds) {
//                        Integer actorId = (Integer) id;
//                        idsForSend.add(actorId);
//                    }
//                }
//                break;
            case 2:
            case 3:
            case 4:
            case 5:
                JSONArray actorsIds = currNode.getJSONArray("actorsIds");
                if (CollectionUtils.isEmpty(actorsIds)) {
                    log.error("节点未查到审批人 nodeId:" + currNode.getInteger("id") + "  processInstanceId: " + processInstance.getId());
                    throw new ServiceException(ProcessError.ACTORS_ERROR);
                }
                for (Object id : actorsIds) {
                    Integer actorId = (Integer) id;
                    idsForSend.add(actorId);
                }
        }
        if (CollectionUtils.isNotEmpty(idsForSend)) {
            processUtil.sendTask(processInstance, idsForSend, 1);
            return;
        } else {
            log.error("节点未查到审批人 nodeId:" + currNode.getInteger("id") + "  processInstanceId: " + processInstance.getId());
            throw new ServiceException(ProcessError.ACTORS_ERROR);
        }
    }

}
