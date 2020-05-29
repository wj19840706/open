package com.feida.ams.framework.errors;

import com.feida.common.error.MyError;

public enum ConsumeMaterialCatetoryError implements MyError {
    HAS_SUB((short)5002,"请删除子耗材分类信息"),
    BE_USED((short)5003,"此耗材类型已用，不能删除"),
    ;

//    ;

    private short code;
    private String msg;
    ConsumeMaterialCatetoryError(short code, String msg){
        this.code = code;
        this.msg = msg;
    }
    @Override
    public Short getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
