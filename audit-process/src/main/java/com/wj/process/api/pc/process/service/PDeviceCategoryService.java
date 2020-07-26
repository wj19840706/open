package com.wj.process.api.pc.process.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feida.common.domain.Dto;
import com.feida.common.exception.ServiceException;
import com.feida.common.util.BeanUtil;
import com.feida.common.util.WebUtils;
import com.feida.omms.common.framework.errors.AccountError;
import com.feida.omms.common.framework.errors.LocationError;
import com.feida.omms.dao.asset.activeAsset.mapper.AssetCategoryExtendFieldMapper;
import com.feida.omms.dao.asset.activeAsset.mapper.DeviceExtendFieldValueMapper;
import com.feida.omms.dao.asset.activeAsset.model.AssetCategoryExtendField;
import com.feida.omms.dao.asset.activeAsset.model.DeviceExtendFieldValue;
import com.feida.omms.dao.businessData.deviceAsset.mapper.DeviceCategoryMapper;
import com.feida.omms.dao.businessData.deviceAsset.model.DeviceCategory;
import com.feida.omms.tk.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
public class PDeviceCategoryService extends BaseService<DeviceCategory> {

    private static Logger logger = LoggerFactory.getLogger(PDeviceCategoryService.class);

    @Autowired
    private DeviceCategoryMapper deviceCategoryMapper;

    @Autowired
    private AssetCategoryExtendFieldMapper assetCategoryExtendFieldMapper;

    @Autowired
    private DeviceExtendFieldValueMapper deviceExtendFieldValueMapper;

    public List<Dto> getListByCondition(Dto params) {
        params.put("roadId", WebUtils.getSessionAccountRoadId());
        List list = deviceCategoryMapper.getListByCondition(params);
        return list;
    }

    //封装成树
    public List<Dto> getDeviceCategoryTree(List<Dto> list) throws Exception {
        try {
            Map<Integer, Dto> map = new HashMap<>();
            for (Dto o1 : list) {
                map.put(o1.getInteger("id"), o1);
            }
            for (ListIterator<Dto> it = list.listIterator(); it.hasNext(); ) {
                Dto device = it.next();
                Integer parentId = device.getInteger("parentId");
                if (null != parentId) {
                    Dto parent = map.get(parentId);
                    if (null != parent) {
                        List<Dto> children = parent.getList("children");
                        if (null == children) {
                            children = new ArrayList<>();
                            parent.put("children", children);
                        }
                        children.add(device);
                        it.remove();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Integer getRootId(Integer id) {
        List<DeviceCategory> deviceCategories = selectAll();
        Map<Integer, DeviceCategory> map = new HashMap<>();

        for (DeviceCategory deviceCategorie : deviceCategories) {
            map.put(deviceCategorie.getId(), deviceCategorie);
        }

        DeviceCategory parent = null;
        Integer rootId = id;
        do {
            DeviceCategory son = map.get(rootId);
            if (null == son)
                return null;
            Integer parentId = son.getParentId();
            parent = map.get(parentId);
            if (null != parent)
                rootId = son.getId();

        } while (parent != null);
        return rootId;
    }

    public int saveOrUpdate(Dto params) throws Exception {
        DeviceCategory deviceCategory = BeanUtil.convertMap2Bean(params, DeviceCategory.class);
        Dto accountDto = WebUtils.getSessionAccount();
        if (null != deviceCategory.getId()) {
            if (null != deviceCategory.getDeleteFlag()) {
                if (deviceCategory.getDeleteFlag() == 2) {
                    params.put("parentId", deviceCategory.getId());
                    deviceCategory.setModifier(accountDto.getInteger("id"));
                    deviceCategory.setCreateTime(new Date());
                    List<Dto> list = deviceCategoryMapper.getListByCondition(params);
                    if (list.size() > 0) {
                        throw new ServiceException(LocationError.HAS_SUB);
                    } else {
                        deviceCategoryMapper.updateByPrimaryKeySelective(deviceCategory);
                    }
                }
            }
            deviceCategory.setCreator(params.getInteger("loggedAccountId"));
            deviceCategory.setCreateTime(new Date());
            deviceCategoryMapper.updateByPrimaryKeySelective(deviceCategory);
        } else {

            DeviceCategory d = new DeviceCategory();
            d.setCategoryName(deviceCategory.getCategoryName());
            d.setDeleteFlag(1);
            int num = deviceCategoryMapper.selectCount(d);
            if (num > 0) {
                logger.error("分类名称重复");
                throw new ServiceException(AccountError.ACCOUNT_NAME_DUPLICATE.getCode(), "分类名称重复！");
            }
            Date date = new Date();
            deviceCategory.setCreator(WebUtils.getSessionAccount().getInteger("id"));
            deviceCategory.setDeleteFlag(1);
            deviceCategory.setCreateTime(date);
            deviceCategory.setModifiedTime(date);
            deviceCategory.setModifier(WebUtils.getSessionAccount().getInteger("id"));
            deviceCategory.setRoadId(accountDto.getInteger("roadId"));
            deviceCategoryMapper.insertSelective(deviceCategory);
        }
        JSONArray jsonArray = (JSONArray) params.getList("assetCategoryExtendField");
        String jsonStr = JSONObject.toJSONString(jsonArray);
        List<AssetCategoryExtendField> list = JSONObject.parseArray(jsonStr, AssetCategoryExtendField.class);
        if (null != list && list.size() > 0) {
            for (AssetCategoryExtendField a : list) {
                Date date = new Date();
                a.setAssetCategoryId(deviceCategory.getId());
                a.setModifiedTime(date);
                a.setModifier(WebUtils.getSessionAccountId());
                a.setDeleteFlag(1);
                if (null != a.getId()) {
                    assetCategoryExtendFieldMapper.updateByPrimaryKeySelective(a);
                } else {
                    a.setCreateTime(date);
                    a.setCreator(WebUtils.getSessionAccountId());
                    assetCategoryExtendFieldMapper.insertSelective(a);
                }
            }
        }
        return 1;
    }

    public List<Dto> checkRepeatCode(Dto params) {
        params.put("repeatCode", params.getString("categoryCode"));
        params.put("repeatId", params.getInteger("id"));
        Dto account = WebUtils.getSessionAccount();
        params.put("roadId", account.getInteger("roadId"));
        List<Dto> list = deviceCategoryMapper.getListByCondition(params);
        return list;
    }

    public void delDeviceExtendsValue(Dto params) {
        Integer id = params.getInteger("id");
        Example example = new Example(DeviceExtendFieldValue.class);//先删除设备分类扩展字段的值
        example.createCriteria().andEqualTo("deviceCategoryExtendFieldId", id);
        DeviceExtendFieldValue d = new DeviceExtendFieldValue();
        Date date = new Date();
        d.setDeleteFlag(2);
        d.setCreateTime(date);
        d.setCreator(WebUtils.getSessionAccountId());
        d.setModifiedTime(date);
        d.setModifier(WebUtils.getSessionAccountId());
        deviceExtendFieldValueMapper.updateByExampleSelective(d, example);
        //再删除设备分类扩展字段的键
        Example e1 = new Example(AssetCategoryExtendField.class);
        e1.createCriteria().andEqualTo("id", id);
        AssetCategoryExtendField a1 = new AssetCategoryExtendField();
        a1.setDeleteFlag(2);
        a1.setCreateTime(date);
        a1.setCreator(WebUtils.getSessionAccountId());
        a1.setModifiedTime(date);
        a1.setModifier(WebUtils.getSessionAccountId());
        assetCategoryExtendFieldMapper.updateByExampleSelective(a1, e1);
    }

    public List<AssetCategoryExtendField> getDeviceExdentdsKey(Dto params) {
        Example example = new Example(AssetCategoryExtendField.class);
        example.createCriteria().andEqualTo("assetCategoryId", params.getInteger("deviceCategoryId"))
                .andEqualTo("deleteFlag", 1);
        List<AssetCategoryExtendField> list = assetCategoryExtendFieldMapper.selectByExample(example);
        return list;
    }

    public DeviceCategory getDetail(Dto params) {
        DeviceCategory d = new DeviceCategory();
        d.setId(params.getInteger("deviceCategoryId"));
        DeviceCategory deviceCategory = deviceCategoryMapper.selectByPrimaryKey(d);
        return deviceCategory;
    }

}
