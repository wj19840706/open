package com.wj.process.api.pc.process.service;

import com.feida.common.domain.Dto;
import com.feida.common.util.WebUtils;
import com.feida.omms.dao.process.mapper.ToDoListMapper;
import com.feida.omms.dao.process.model.ToDoList;
import com.feida.omms.tk.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class ToDolistService extends BaseService<ToDoList> {

    @Autowired
    private ToDoListMapper toDoListMapper;


    public List<Dto> getByCondition(Dto params) {
        List<Dto> auditProcessTasks = toDoListMapper.getByCondition(params);
        return auditProcessTasks;
    }

    /**
     * 个人未完成任务  工作台查询待办用
     * @param
     * @return
     */
    public List<Dto> getUnresolvedByAccountId(Dto params) {
        Dto account = WebUtils.getSessionAccount();
        params.put("actorId", account.getInteger("id"));
        params.put("status", 1);
        List<Dto> byCondition = toDoListMapper.getByCondition(params);
        return byCondition;
    }

    // 设置任务状态
    public Integer updateByInsAndNodeId(Integer processInstanceId, Integer nodeId, Short status) {
        Example example = new Example(ToDoList.class);
        example.createCriteria()
                .andEqualTo("processInstanceId", processInstanceId)
                .andEqualTo("processNodeId", nodeId)
                .andEqualTo("status", 1);
        ToDoList toDoList = new ToDoList();
        toDoList.setStatus(status);

        return updateByPrimaryKeySelective(example, toDoList);
    }
}
