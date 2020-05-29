package com.feida.ams.api.pc.process.bizInterace;

import com.feida.common.domain.Dto;

public interface BizInterface {
    Dto getDto(Integer id);
    /**
     *
     * @param ： 提交审批时的参数
     *
     *
     *
     */
    void reject(Dto dto);

    /**
     *
     * @param： 提交审批时的参数
     *
     *
     */
    void pass(Dto dto);

    /**
     *
     * @param：  提交审批时的参数
     *
     */
    void intermediate(Dto dto);
}
