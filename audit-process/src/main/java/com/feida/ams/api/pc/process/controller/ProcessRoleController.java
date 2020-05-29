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
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Controller
@RequestMapping(value = "/pc/processRole")
public class ProcessRoleController {
    @Autowired
    private ProcessRoleService processRoleService;

    @RequestMapping(value = "/list")
    public BaseResponse list() throws Exception {
        Example example = new Example(ProcessRole.class);
        example.createCriteria().andEqualTo("deleteFlag", 1);
        List<ProcessRole> processRoles = processRoleService.selectByExalple(example);
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS, processRoles);
        return response;
    }

    @RequestMapping(value = "/submit")
    public BaseResponse submit() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);
        processRoleService.submit(params);
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS);
        return response;
    }

    @RequestMapping(value = "/delete")
    public BaseResponse delete() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);
        Integer id = params.getInteger("id");
        ProcessRole processRole = new ProcessRole();
        processRole.setDeleteFlag(2);
        processRole.setId(id);
        processRoleService.updateByPrimaryKeySelective(processRole);
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS);
        return response;
    }

}
