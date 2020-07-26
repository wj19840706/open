package com.wj.process.api.pc.system.organization.service;

import com.feida.common.domain.Dto;
import com.feida.common.util.BeanUtil;
import com.feida.common.util.WebUtils;
import com.feida.omms.dao.system.organization.mapper.OrganizationManagerMapper;
import com.feida.omms.dao.system.organization.mapper.OrganizationTreeMapper;
import com.feida.omms.dao.system.organization.model.OrganizationManager;
import com.feida.omms.dao.system.organization.model.OrganizationTree;
import com.feida.omms.tk.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
public class POrganizationTreeService extends BaseService<OrganizationTree> {

    @Autowired
    private OrganizationTreeMapper organizationTreeMapper;

    @Autowired
    private OrganizationManagerMapper organizationManagerMapper;

    public List<OrganizationTree> getOrganizationTree(Dto params) throws Exception {
        //查询列表
        List<OrganizationTree> list = organizationTreeMapper.getListByCondition(params);
        //封装成树
        for(OrganizationTree tree :list){
            Example example = new Example(OrganizationManager.class);
            example.createCriteria().andEqualTo("rank",1)
                    .andEqualTo("organizationTreeId",tree.getId())
                    .andEqualTo("deleteFlag",1);
            OrganizationManager o = organizationManagerMapper.selectOneByExample(example);
            if(null!=o){
                tree.setManagerId(o.getAccountId());
            }
            example = new Example(OrganizationManager.class);
            example.createCriteria().andEqualTo("rank",2)
                    .andEqualTo("organizationTreeId",tree.getId())
                    .andEqualTo("deleteFlag",1);
            List<OrganizationManager> secondManagers = organizationManagerMapper.selectByExample(example);
            List<Integer> managerids = new ArrayList<>();
            if(secondManagers.size()>0) {
                for (OrganizationManager om : secondManagers) {
                    managerids.add(om.getAccountId());
                }
            }
            tree.setSecondManagerIds(managerids);
        }
        Map<Integer,OrganizationTree> map = new HashMap<>();

        for(OrganizationTree o :list){
            map.put(o.getId(),o);
        }
        for(Iterator<OrganizationTree> it = list.iterator();it.hasNext();){
            OrganizationTree o = it.next();
            Integer staffCount = o.getStaffCount();
            Integer parentId = o.getParentId();
            if(null!=parentId){
                OrganizationTree parent = map.get(parentId);
                if(null!=parent){
                    List<OrganizationTree> children = parent.getChildren();
                    if(null==children){
                        children = new ArrayList<>();
                        parent.setChildren(children);
                    }
                    //设置数量
                    while (null!=parent){
                        parent.setStaffCount(parent.getStaffCount()+staffCount);
                        Integer ppId = parent.getParentId();
                        parent = map.get(ppId);
                    }
                    children.add(o);
                    it.remove();
                }
            }
        }
        return list;
    }

    public Dto getDto(Dto params) throws Exception {
        Dto dto= organizationTreeMapper.getDto(params);
        return dto;
    }

    public int saveOrUpdate(Dto params) throws Exception{
        OrganizationTree organizationTree= BeanUtil.convertMap2Bean(params, OrganizationTree.class);
        if (organizationTree.getId() != null) {
            organizationTree.setModifiedTime(new Date());
            organizationTree.setModifier(WebUtils.getSessionAccount().getInteger("id"));
            organizationTreeMapper.updateByPrimaryKeySelective(organizationTree);
            //删除当前组织主管
            Example example = new Example(OrganizationManager.class);
            OrganizationManager organizationManager = new OrganizationManager();
            organizationManager.setDeleteFlag(2);
            example.createCriteria().andEqualTo("organizationTreeId", organizationTree.getId());
            organizationManagerMapper.updateByExampleSelective(organizationManager,example);
            addManagers(organizationTree);//添加组织主管
        } else {
            Dto accountDto=WebUtils.getSessionAccount();
            Date date = new Date();
            organizationTree.setRoadId(accountDto.getInteger("roadId"));
            organizationTree.setDeleteFlag(1);
            organizationTree.setCreator(WebUtils.getSessionAccount().getInteger("id"));
            organizationTree.setCreateTime(date);
            organizationTree.setModifier(WebUtils.getSessionAccount().getInteger("id"));
            organizationTree.setModifiedTime(date);
            //System.out.println(organizationTreeMapper.insertUseGeneratedKeys(OrganizationTree));
            organizationTreeMapper.insertSelective(organizationTree);//插入数据并获取主键ID
            addManagers(organizationTree);//添加组织主管
        }
        return 1;
    }

    public List<OrganizationTree> checkRepeatCode(Dto params){
        params.put("repeatCode",params.getString("organizationCode"));
        params.put("repeatId",params.getInteger("id"));
        Dto account = WebUtils.getSessionAccount();
        params.put("roadId",account.getInteger("roadId"));
        List<OrganizationTree> organizationList = organizationTreeMapper.getListByCondition(params);
        return organizationList;
    }

    /*
    增加组织主管
     */
    public void addManagers(OrganizationTree tree){
        Dto accountDto=WebUtils.getSessionAccount();
        List<OrganizationManager> list = new ArrayList<>();
        List<Integer> secondManagerIds = tree.getSecondManagerIds();
        Integer managerId = tree.getManagerId();
        OrganizationManager o = new OrganizationManager();//添加主主管
        o.setOrganizationTreeId(tree.getId());
        o.setRank(1);
        o.setAccountId(managerId);
        o.setRoadId(accountDto.getInteger("roadId"));
        o.setDeleteFlag(1);
        o.setCreator(WebUtils.getSessionAccount().getInteger("id"));
        o.setCreateTime(new Date());
        list.add(o);
        if(null!=secondManagerIds && secondManagerIds.size()>0) { //添加副主管
            for (Integer id : secondManagerIds) {
                OrganizationManager organizationManager = new OrganizationManager();
                organizationManager.setOrganizationTreeId(tree.getId());
                organizationManager.setAccountId(id);
                organizationManager.setRoadId(accountDto.getInteger("roadId"));
                organizationManager.setDeleteFlag(1);
                organizationManager.setCreator(WebUtils.getSessionAccount().getInteger("id"));
                organizationManager.setCreateTime(new Date());
                organizationManager.setRank(2);
                list.add(organizationManager);
            }
        }
        organizationManagerMapper.insertList(list);
    }

    /**
     *  查询上级部门
     * @param organizationTreeId level
     * @return
     */
    public Integer getParentOrganizationId(Integer organizationTreeId, Integer level){
        Integer result = organizationTreeId;
        while (level >1){
            level --;
            OrganizationTree organizationTree = organizationTreeMapper.selectByPrimaryKey(organizationTreeId);
            if(null == organizationTree){
                return null;
            }
            result = organizationTree.getId();
        }
        return result;
    }


}
