package com.feida.ams.framework.errors;

import com.feida.common.error.MyError;

public enum LocationError implements MyError {
    HAS_SUB((short)5002,"请删除子位置信息");
    private short code;
    private String msg;
    LocationError(short code,String msg){
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
