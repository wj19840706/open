package com.feida.ams.api.pc.process.service;

import com.feida.ams.api.pc.process.mapper.ProcessRoleGroupMapper;
import com.feida.ams.api.pc.process.mapper.ProcessRoleMapper;
import com.feida.ams.api.pc.process.model.ProcessRole;
import com.feida.ams.api.pc.process.model.ProcessRoleGroup;
import com.feida.common.domain.Dto;
import com.feida.common.mybatis.BaseService;
import com.feida.common.util.BeanUtil;
import com.feida.common.util.WebUtils;
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
