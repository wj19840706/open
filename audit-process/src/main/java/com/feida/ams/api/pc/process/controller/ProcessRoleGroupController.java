package com.feida.ams.api.pc.process.controller;

import com.feida.ams.api.pc.process.model.ProcessRole;
import com.feida.ams.api.pc.process.model.ProcessRoleGroup;
import com.feida.ams.api.pc.process.service.ProcessRoleGroupService;
import com.feida.ams.api.pc.process.service.ProcessRoleService;
import com.feida.common.consts.SysConstants;
import com.feida.common.domain.BaseResponse;
import com.feida.common.domain.Dto;
import com.feida.common.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping(value="/pc/processRoleGroup")
public class ProcessRoleGroupController {

    @Autowired
    private ProcessRoleGroupService processRoleGroupService;

    @RequestMapping(value = "/list")
    public BaseResponse list() throws Exception {
        List<ProcessRoleGroup> processRoleGroups = processRoleGroupService.selectAll();
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS, processRoleGroups);
        return response;
    }

    @RequestMapping(value = "/submit")
    public BaseResponse submit() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);
        processRoleGroupService.submit(params);
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS);
        return response;
    }

    @RequestMapping(value = "/delete")
    public BaseResponse delete() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);
        Integer id = params.getInteger("id");
        ProcessRoleGroup processRoleGroup = new ProcessRoleGroup();
        processRoleGroup.setDeleteFlag(2);
        processRoleGroup.setId(id);
        processRoleGroupService.updateByPrimaryKeySelective(processRoleGroup);
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS);
        return response;
    }

}
