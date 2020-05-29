package com.feida.ams.framework.errors;

import com.feida.common.error.MyError;

public enum WarehouseError implements MyError {
    HAS_SUB((short)5001,"请先删除仓库区域");
    private short code;
    private String msg;
    WarehouseError(short i, String msg) {
        this.code = i;
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
