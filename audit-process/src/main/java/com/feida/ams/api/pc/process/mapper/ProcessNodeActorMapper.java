package com.feida.ams.api.pc.process.mapper;

import com.feida.ams.api.pc.process.model.ProcessNodeActor;
import com.feida.common.domain.Dto;
import com.feida.common.mybatis.MyMapper;

import java.util.List;

public interface ProcessNodeActorMapper extends MyMapper<ProcessNodeActor> {

    List<Dto> getListByCondition(Dto params);
}