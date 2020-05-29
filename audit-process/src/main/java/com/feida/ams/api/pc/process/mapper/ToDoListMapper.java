package com.feida.ams.api.pc.process.mapper;

import com.feida.ams.api.pc.process.model.ToDoList;
import com.feida.common.domain.Dto;
import com.feida.common.mybatis.MyMapper;

import java.util.List;

public interface ToDoListMapper extends MyMapper<ToDoList> {

    List<Dto> getByCondition(Dto params);
}