package com.wj.process.api.pc.process.controller;

import com.feida.common.consts.SysConstants;
import com.feida.common.domain.BaseResponse;
import com.feida.common.domain.Dto;
import com.feida.common.exception.ServiceException;
import com.feida.common.util.WebUtils;
import com.feida.omms.common.framework.annotation.OperateLog4Save;
import com.feida.omms.dao.process.model.FormType;
import com.feida.omms.dao.process.model.ProcessCcList;
import com.feida.process.api.pc.process.service.FormFixedNodeService;
import com.feida.process.api.pc.process.service.FormTypeService;
import com.feida.process.api.pc.process.service.ProcessCcListService;
import com.feida.process.api.pc.process.service.ProcessDefinitionService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.stream.Collectors;

@Log
@RestController
@RequestMapping(value = "/pc/process")
public class ProcessController {

    @Autowired
    private FormTypeService formTypeService;
    @Autowired
    private ProcessDefinitionService processDefinitionService;
    @Autowired
    private FormFixedNodeService formFixedNodeService;
    @Autowired
    private ProcessCcListService processCcListService;

    @OperateLog4Save(moduleIds="menu_process", template = "查看了流程列表")
    @RequestMapping(value = "/formTypeList")
    public BaseResponse formTypeList() throws Exception {
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS, formTypeService.selectAll());
        return response;
    }


    @RequestMapping(value = "/formTypeSet")
    public BaseResponse formTypeSet() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);
        if (!WebUtils.checkProperties(params, "formTypeId", "processType")) {
            throw new ServiceException((short) -1000, SysConstants.NO_PARAMS + "formTypeId" + " processType");
        }
        FormType formType = new FormType();
        formType.setId(params.getInteger("formTypeId"));
        formType.setProcessType(params.getInteger("processType"));
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS, formTypeService.updateByPrimaryKeySelective(formType));
        return response;
    }

    @RequestMapping(value = "/setCcList")
    public BaseResponse setCcList() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);
        if (!WebUtils.checkProperties(params, "formTypeId", "ccList")) {
            throw new ServiceException((short) -1000, SysConstants.NO_PARAMS + "formTypeId" + " ccList");
        }
        processCcListService.updateCcFormTypeCcList(params);
        return new BaseResponse(SysConstants.SUCCESS);
    }

    @RequestMapping(value = "/getCcList")
    public BaseResponse getCcList() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);
        if (!WebUtils.checkProperties(params, "formTypeId")) {
            throw new ServiceException((short) -1000, SysConstants.NO_PARAMS + "formTypeId");
        }
        Example example = new Example(ProcessCcList.class);
        example.createCriteria().andEqualTo("formType", params.getInteger("formTypeId"));
        List<ProcessCcList> processCcLists = processCcListService.selectByExalple(example);
        List<Integer> collect = processCcLists.stream().map(k -> k.getAccountId()).collect(Collectors.toList());
        return new BaseResponse(SysConstants.SUCCESS, collect);
    }


    @RequestMapping(value = "/formFieldDefineList")
    public BaseResponse formFieldDefineList() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);
        if (!WebUtils.checkProperties(params, "formTypeId")) {
            throw new ServiceException((short) -1000, SysConstants.NO_PARAMS + "formTypeId");
        }
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS, formTypeService.getFormFieldDefineByTypeId(params.getInteger("formTypeId")));
        return response;
    }

    @RequestMapping(value = "/formFixedNodeList")
    public BaseResponse formFixedNodeList() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);

        BaseResponse response = new BaseResponse(SysConstants.SUCCESS, formFixedNodeService.getByCondition(params));

        return response;
    }

    /**
     * @return
     * @throws Exception
     */
    @OperateLog4Save(moduleIds="btn_edit_process", template = "修改了k1的工作流程", paramsKeys = "formProcesses,0,name")
    @RequestMapping(value = "/submitProcesses")
    public BaseResponse submitProcesses() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);
        if (!WebUtils.checkProperties(params, "formTypeId", "formTypeId")) {
            if (!WebUtils.checkProperties(params, "id")) {
                throw new ServiceException((short) -1000, SysConstants.NO_PARAMS + "id");
            }
        }
        processDefinitionService.submitProcessses(params);
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS);
        return response;
    }

    /**
     * 查询
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/formProcesses")
    public BaseResponse formProcesses() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);
        if (!WebUtils.checkProperties(params, "formTypeId")) {
            if (!WebUtils.checkProperties(params, "id")) {
                throw new ServiceException((short) -1000, SysConstants.NO_PARAMS + "id");
            }
        }
        Dto list = processDefinitionService.formProcesses(params);
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS, list);
        return response;
    }

}
