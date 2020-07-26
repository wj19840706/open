package com.wj.process.api.pc.process.service;

import com.feida.common.domain.Dto;
import com.feida.common.util.BeanUtil;
import com.feida.common.util.WebUtils;
import com.feida.omms.dao.process.mapper.ProcessRoleGroupMapper;
import com.feida.omms.dao.process.model.ProcessRoleGroup;
import com.feida.omms.tk.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ProcessRoleGroupService extends BaseService<ProcessRoleGroup> {

    @Autowired
    private ProcessRoleGroupMapper processRoleGroupMapper;

    public void submit(Dto params) throws Exception {
        ProcessRoleGroup processRoleGroup = BeanUtil.convertMap2Bean(params, ProcessRoleGroup.class);
        processRoleGroup.setModifiedTime(new Date());
        processRoleGroup.setModifier(WebUtils.getSessionAccountId());
        if (processRoleGroup.getId() == null) {
            processRoleGroup.setCreateTime(new Date());
            processRoleGroup.setCreator(WebUtils.getSessionAccountId());
            processRoleGroup.setDeleteFlag(1);
            processRoleGroupMapper.insertSelective(processRoleGroup);
        } else {
            processRoleGroupMapper.updateByPrimaryKeySelective(processRoleGroup);
        }
    }
}
