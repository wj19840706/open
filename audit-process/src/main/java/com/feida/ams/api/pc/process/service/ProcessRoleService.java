package com.feida.ams.api.pc.process.service;

import com.feida.ams.api.pc.process.mapper.ProcessRoleAccountMapper;
import com.feida.ams.api.pc.process.mapper.ProcessRoleMapper;
import com.feida.ams.api.pc.process.model.ProcessRoleAccount;
import com.feida.ams.api.pc.process.model.ProcessRole;
import com.feida.common.domain.Dto;
import com.feida.common.mybatis.BaseService;
import com.feida.common.util.BeanUtil;
import com.feida.common.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ProcessRoleService extends BaseService<ProcessRole> {
    @Autowired
    private ProcessRoleMapper processRoleMapper;
    @Autowired
    private ProcessRoleAccountMapper processRoleAccountMapper;

    public void submit(Dto params) throws Exception {
        ProcessRole processRole = BeanUtil.convertMap2Bean(params, ProcessRole.class);
        processRole.setModifiedTime(new Date());
        processRole.setModifier(WebUtils.getSessionAccountId());
        if (processRole.getId() == null) {
            processRole.setDeleteFlag(1);
            processRole.setCreateTime(new Date());
            processRole.setCreator(WebUtils.getSessionAccountId());
            processRoleMapper.insertSelective(processRole);
        } else {
            processRoleMapper.updateByPrimaryKeySelective(processRole);
        }

        List<Integer> accountIds = params.getList("accountIds");
        for (Object accountId : accountIds) {
            Integer aid = (Integer) accountId;
            ProcessRoleAccount processRoleAccount = new ProcessRoleAccount();
            processRoleAccount.setAccountId(aid);
            processRoleAccount.setRoleId(processRole.getId());
            processRoleAccountMapper.insertSelective(processRoleAccount);
        }
    }

    public List<Dto> getAccountIdsByRoleId(List<Integer> roleIds) {
        return processRoleAccountMapper.getAccountIdsByRoleId(roleIds);
    }

}
