package com.campus.proposal.exception;

/** 业务异常: message 面向用户可直接展示,code 用于区分 400/401/403 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(400, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
