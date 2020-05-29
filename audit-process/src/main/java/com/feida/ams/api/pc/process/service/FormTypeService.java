package com.feida.ams.api.pc.process.service;

import com.feida.ams.api.pc.process.mapper.FormFieldDefineMapper;
import com.feida.ams.api.pc.process.model.FormFieldDefine;
import com.feida.ams.api.pc.process.model.FormType;
import com.feida.common.mybatis.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class FormTypeService extends BaseService<FormType> {
    @Autowired
    private FormFieldDefineMapper formFieldDefineMapper;

    public List<FormFieldDefine> getFormFieldDefineByTypeId(Integer formTypeId){
        Example example = new Example(FormFieldDefine.class);
        example.createCriteria().andEqualTo("formTypeId", formTypeId);
        return formFieldDefineMapper.selectByExample(example);
    }

}
