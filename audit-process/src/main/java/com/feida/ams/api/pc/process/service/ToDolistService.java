package com.feida.ams.api.pc.process.service;

import com.feida.ams.api.pc.process.mapper.ToDoListMapper;
import com.feida.ams.api.pc.process.model.ToDoList;
import com.feida.common.domain.Dto;
import com.feida.common.mybatis.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class ToDolistService extends BaseService<ToDoList> {

    @Autowired
    private ToDoListMapper toDoListMapper;

    /**
     *  节点未完成任务
     * @param processInstanceId
     * @param nodeId
     * @return
     */
    public List<Dto> getByCondition(Dto params) {
        List<Dto> auditProcessTasks = toDoListMapper.getByCondition(params);
        return auditProcessTasks;
    }

    public List<ToDoList> getUnresolvedByAccountId(Integer accountId) {
        Example example = new Example(ToDoList.class);
        example.createCriteria().andEqualTo("actorId", accountId)
                .andEqualTo("status", 1);
        List<ToDoList> auditProcessTasks = selectByExalple(example);
        return auditProcessTasks;
    }



}
