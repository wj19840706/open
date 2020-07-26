package com.wj.process.api.pc.process.service;

import com.feida.omms.dao.process.mapper.FormFieldDefineMapper;
import com.feida.omms.dao.process.model.FormFieldDefine;
import com.feida.omms.dao.process.model.FormType;
import com.feida.omms.tk.BaseService;
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
