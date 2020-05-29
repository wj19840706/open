package com.feida.ams.api.pc.process.mapper;

import com.feida.ams.api.pc.process.model.ProcessDefinition;
import com.feida.common.domain.Dto;
import com.feida.common.mybatis.MyMapper;

import java.util.List;

public interface ProcessDefinitionMapper extends MyMapper<ProcessDefinition> {

    List<Dto> getByCondition(Dto params);

    Dto getDto(Integer id);

    List<ProcessDefinition> getListByFormTypeId(Dto params);

}