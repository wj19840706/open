package com.feida.ams.api.pc.process.mapper;

import com.feida.ams.api.pc.process.model.ProcessNode;
import com.feida.common.domain.Dto;
import com.feida.common.mybatis.MyMapper;

import java.util.List;

public interface ProcessNodeMapper extends MyMapper<ProcessNode> {
    List<Dto> selectByProcessId(Integer processDefinitionId);
    Dto getDto(Integer processDefinitionId);
}