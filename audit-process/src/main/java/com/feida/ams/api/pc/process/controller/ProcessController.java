package com.feida.ams.api.pc.process.controller;

import com.feida.ams.api.pc.process.service.FormTypeService;
import com.feida.ams.api.pc.process.service.ProcessDefinitionService;
import com.feida.common.consts.SysConstants;
import com.feida.common.domain.BaseResponse;
import com.feida.common.domain.Dto;
import com.feida.common.exception.ServiceException;
import com.feida.common.util.WebUtils;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log
@RestController
@RequestMapping(value="/pc/process")
public class ProcessController {

    @Autowired
    private FormTypeService formTypeService;
    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @RequestMapping(value = "/formTypeList")
    public BaseResponse formTypeList() throws Exception {
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS, formTypeService.selectAll());
        return response;
    }


    @RequestMapping(value = "/formFieldDefineList")
    public BaseResponse formFieldDefineList() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);
        if(!WebUtils.checkProperties(params, "formTypeId")){
            if (!WebUtils.checkProperties(params, "id")) {
                throw new ServiceException((short) -1000, SysConstants.NO_PARAMS + "id");
            }
        }
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS, formTypeService.getFormFieldDefineByTypeId(params.getInteger("formTypeId")));
        return response;
    }


    /**
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/submitProcesses")
    public BaseResponse submitProcesses() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);
        if(!WebUtils.checkProperties(params, "formTypeId", "formTypeId")){
            if (!WebUtils.checkProperties(params, "id")) {
                throw new ServiceException((short) -1000, SysConstants.NO_PARAMS + "id");
            }
        }
        processDefinitionService.submitProcessses(params);
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS);
        return response;
    }

    /**
     *  查询
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/formProcesses")
    public BaseResponse formProcesses() throws Exception {
        Dto params = (Dto) WebUtils.getRequest().getAttribute(SysConstants.REQUEST_PARAM_NAME);
        if(!WebUtils.checkProperties(params, "formTypeId")){
            if (!WebUtils.checkProperties(params, "id")) {
                throw new ServiceException((short) -1000, SysConstants.NO_PARAMS + "id");
            }
        }
        Dto list = processDefinitionService.formProcesses(params);
        BaseResponse response = new BaseResponse(SysConstants.SUCCESS, list);
        return response;
    }

}
