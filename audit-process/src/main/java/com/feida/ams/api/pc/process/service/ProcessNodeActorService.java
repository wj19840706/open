package com.feida.ams.api.pc.process.service;

import com.feida.ams.api.pc.process.mapper.ProcessNodeActorMapper;
import com.feida.ams.api.pc.process.model.ProcessNodeActor;
import com.feida.common.domain.Dto;
import com.feida.common.mybatis.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessNodeActorService extends BaseService<ProcessNodeActor> {

    @Autowired
    private ProcessNodeActorMapper processNodeActorMapper;

    public List<Dto> getListByCondition(Dto params) {
       return processNodeActorMapper.getListByCondition(params);
    }

}
