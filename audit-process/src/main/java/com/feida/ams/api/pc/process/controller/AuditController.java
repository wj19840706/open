package com.feida.ams.api.pc.process.controller;

import com.feida.ams.api.pc.process.model.ToDoList;
import com.feida.ams.api.pc.process.service.AuditService;
import com.feida.ams.api.pc.process.service.ToDolistService;
import com.feida.common.consts.SysConstants;
import com.feida.common.domain.BaseResponse;
import com.feida.common.domain.Dto;
import com.feida.common.exception.ServiceException;
import com.feida.common.util.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/pc/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;
    @Autowired
    private ToDolistService toDolistService;

    @RequestMapping(value = "/submitAudit")
    public BaseResponse submitAudit() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);
        if (!WebUtils.checkProperties(params, "taskId")) {
            //log.error("submitAudit params is null");
            throw new ServiceException((short) -1000, SysConstants.NO_PARAMS + "taskId");
        }
        auditService.submitAudit(params);
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS);
        return response;
    }


    /**
     * 查询待办
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getPendTask")
    public BaseResponse getPendTask() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);
        if (!WebUtils.checkProperties(params, "accountId")) {
            throw new ServiceException((short) -1000, SysConstants.NO_PARAMS + "用户");
        }
        List<ToDoList> tasks = toDolistService.getUnresolvedByAccountId(params.getInteger("accountId"));
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS, tasks);
        return response;
    }

    /**
     * 评论
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/comment")
    public BaseResponse comment() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);
        if (!WebUtils.checkProperties(params, "processInstanceId,actorOpinion")) {
            throw new ServiceException((short) -1000, SysConstants.NO_PARAMS + "用户");
        }
        auditService.comment(params);
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS);
        return response;
    }


}
