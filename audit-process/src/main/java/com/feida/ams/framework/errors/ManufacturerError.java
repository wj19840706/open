package com.feida.ams.framework.errors;

import com.feida.common.error.MyError;

public enum ManufacturerError implements MyError {
    HAS_SUB((short)5002,"有设备引用该生产厂家，不能删除！");
    private short code;
    private String msg;
    ManufacturerError(short code, String msg){
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
