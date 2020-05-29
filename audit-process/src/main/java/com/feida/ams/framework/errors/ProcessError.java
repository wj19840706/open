package com.feida.ams.framework.errors;

import com.feida.common.error.MyError;

public enum  ProcessError implements MyError {
    NO_FORM((short)8001,"表单不存在"),
    NO_PROCESS((short)8002,"表单没有正确的流程"),
    NO_NODES((short)8003,"表单流程没有节点"),
    NO_ACTOR((short)8003,"流程节点没有审批人"),
    ;

    private short code;
    private String msg;

    ProcessError(short code, String msg) {
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
