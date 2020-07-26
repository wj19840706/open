package com.wj.process.api.pc.process.service;

import com.alibaba.fastjson.JSONObject;
import com.feida.common.domain.Dto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AuditService {

    Integer startProcess(Dto params) throws Exception;

    /**
     * 审批人的审批提交
     *
     * @param params 审批意见：                auditResult :  1:同意，2：拒绝，3：驳回上一节点 4：转发
     *               流程实例id：              processInstanceId
     */
    void submitAudit(Dto params) throws Exception;

    /**
     * 撤销流程
     */
    void doRepeal(Integer processInstanceId) throws Exception;

    void doComment(Dto params) throws Exception;

    List<JSONObject> getProcessActorsWhenApplicate(Dto params, Integer applicant) throws Exception;

    Dto getProcessActors(Integer processInstanceId);

    Dto getProcessActors(Integer processInstanceId, Dto form) throws Exception;


}
