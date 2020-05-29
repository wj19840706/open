package com.feida.ams.framework.errors;

import com.feida.common.error.MyError;

public enum ConsumeMaterialError implements MyError {
    TOTAL_NUM((short)5002,"还有库存不能删除");
    private short code;
    private String msg;
    ConsumeMaterialError(short code, String msg){
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
