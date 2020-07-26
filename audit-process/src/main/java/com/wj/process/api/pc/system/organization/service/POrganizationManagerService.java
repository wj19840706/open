package com.wj.process.api.pc.system.organization.service;

import com.feida.common.domain.Dto;
import com.feida.common.util.BeanUtil;
import com.feida.common.util.WebUtils;
import com.feida.omms.dao.system.organization.mapper.OrganizationManagerMapper;
import com.feida.omms.dao.system.organization.model.OrganizationManager;
import com.feida.omms.tk.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class POrganizationManagerService extends BaseService<OrganizationManager> {

    @Autowired
    private OrganizationManagerMapper organizationManagerMapper;

    public List<Dto> getListByCondition(Dto params) throws Exception {
        List<Dto> list = organizationManagerMapper.getListByCondition(params);
        return list;
    }

    public Dto getDto(Dto params) throws Exception {
        Dto dto= organizationManagerMapper.getDto(params);
        return dto;
    }

    public int saveOrUpdate(Dto params) throws Exception {
        OrganizationManager organizationManager= BeanUtil.convertMap2Bean(params, OrganizationManager.class);
        if (organizationManager.getId() != null) {
            organizationManager.setModifiedTime(new Date());
            organizationManager.setModifier(WebUtils.getSessionAccount().getInteger("id"));
            organizationManagerMapper.updateByPrimaryKeySelective(organizationManager);
        } else {
            Dto accountDto=WebUtils.getSessionAccount();
            Date date = new Date();
            organizationManager.setRoadId(accountDto.getInteger("roadId"));
            organizationManager.setDeleteFlag(1);
            organizationManager.setModifier(WebUtils.getSessionAccount().getInteger("id"));
            organizationManager.setModifiedTime(date);
            organizationManager.setCreator(WebUtils.getSessionAccount().getInteger("id"));
            organizationManager.setCreateTime(date);
            organizationManagerMapper.insert(organizationManager);
        }
        return 1;
    }

    /**
     * 根据 accountId 查询部门主管
     * @param organizationTreeId
     * @return
     */
    public List<Dto> getManagersByOrganizationTreeId(Integer organizationTreeId){

        List<Dto> ids = organizationManagerMapper.getManagersByOrganizationTreeId(organizationTreeId);
        return ids;
    }

}
