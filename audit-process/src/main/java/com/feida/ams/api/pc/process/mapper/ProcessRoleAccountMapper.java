package com.feida.ams.api.pc.process.mapper;

import com.feida.ams.api.pc.process.model.ProcessRoleAccount;
import com.feida.common.domain.Dto;
import com.feida.common.mybatis.MyMapper;

import java.util.List;

public interface ProcessRoleAccountMapper extends MyMapper<ProcessRoleAccount> {
    List<Dto> getAccountIdsByRoleId(List<Integer> roleIds);
}