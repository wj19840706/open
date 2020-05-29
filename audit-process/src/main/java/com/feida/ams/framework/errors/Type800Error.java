package com.feida.ams.framework.errors;

import com.feida.common.error.MyError;

public enum Type800Error implements MyError {
    HAS_SUB((short)5002,"请删除子800章信息");
    private short code;
    private String msg;
    Type800Error(short code, String msg){
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
