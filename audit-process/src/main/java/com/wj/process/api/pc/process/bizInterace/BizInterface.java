package com.wj.process.api.pc.process.bizInterace;

import com.feida.common.domain.Dto;

public interface BizInterface {
    Dto getDto(Integer id);
    /**
     *
     * @param ： 提交审批时的参数
     *
     * id:                 form的id
     *
     */
    void reject(Dto dto);

    /**
     *  id:                 form的id
     * @param dto
     * @throws Exception
     */
    void rePeal(Dto dto) throws Exception;

    /**
     *
     * @param： 提交审批时的参数
     * id:                 form的id
     *
     */
    void pass(Dto dto) throws Exception;

    /**
     *
     * @param：  提交审批时的参数
     *  id:                 form的id
     *  fixedNodeId:        固定节点id
     */
    void beforeFixedNode(Dto dto);
    void afterFixedNode(Dto dto);
}
