package com.wj.process.api.pc.process.service;

import com.feida.common.domain.Dto;
import com.feida.omms.dao.process.mapper.FormFixedNodeMapper;
import com.feida.omms.dao.process.model.FormFixedNode;
import com.feida.omms.tk.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.feida.common.util.MyStringUtils.splitStringToInteger;

@Service
public class FormFixedNodeService extends BaseService<FormFixedNode> {
    @Autowired
    private FormFixedNodeMapper formFixedNodeMapper;

    public List<Dto> getByCondition(Dto params) {

        List<Dto> byCondition = formFixedNodeMapper.getByCondition(params);
        byCondition.forEach(k->{
            String executiveIds = k.getString("executiveIds");
            if (StringUtils.isNotBlank(executiveIds)) {
                List<Integer> ids = splitStringToInteger(executiveIds, ",");
                k.put("executiveIds", ids);
            }
            String executiveLevels = k.getString("executiveLevels");
            if (StringUtils.isNotBlank(executiveLevels)) {
                List<Integer> ids = splitStringToInteger(executiveLevels, ",");
                k.put("executiveLevels", ids);
            }
        });
        return byCondition;
    }

}
