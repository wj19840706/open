package com.feida.ams.framework.errors;

import com.feida.common.error.MyError;

public enum SupplierError implements MyError {
    HAS_SUB((short)5002,"有设备引用该供应商，不能删除！");
    private short code;
    private String msg;
    SupplierError(short code, String msg){
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
