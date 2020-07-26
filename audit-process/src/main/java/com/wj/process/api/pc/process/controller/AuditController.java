package com.wj.process.api.pc.process.controller;

import com.feida.common.consts.SysConstants;
import com.feida.common.domain.BaseResponse;
import com.feida.common.domain.Dto;
import com.feida.common.exception.ServiceException;
import com.feida.common.util.WebUtils;
import com.feida.process.api.pc.process.service.AuditService;
import com.feida.process.api.pc.process.service.ToDolistService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

        auditService.submitAudit(params);
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS);
        return response;
    }
    @RequestMapping(value = "/getProcessActorsByFormType")
    public BaseResponse getProcessActorsByFormType() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);

        auditService.getProcessActorsWhenApplicate(params, WebUtils.getSessionAccountId());
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
    public String getPendTask() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);
//
//        if (!WebUtils.checkProperties(params, "accountId")) {
//            throw new ServiceException((short) -1000, SysConstants.NO_PARAMS + "用户");
//        }
        if(StringUtils.isNotBlank(params.getString("needPage"))){
            WebUtils.startPage(params);
        }
        List<Dto> tasks = toDolistService.getUnresolvedByAccountId(params);
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS, tasks);
        return response.toJson();
    }

    /**
     * 撤回
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/rePeal")
    public String rePeal() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);
        if (!WebUtils.checkProperties(params, "processInstanceId")) {
            throw new ServiceException((short) -1000, SysConstants.NO_PARAMS + "表单流程Id");
        }
       auditService.doRepeal(params.getInteger("processInstanceId"));
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS);
        return response.toJson();
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
        auditService.doComment(params);
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS);
        return response;
    }


}
