package com.example.wallet.common.resp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RespCode {

    SUCCESS("00000", "请求执行成功"),

    PARAM_IS_BLANK("A0410", "请求必填参数为空"),
    PARAM_IS_INVALID("A0400", "用户请求参数错误"),

    SYS_FAIL("B0001", "系统执行出错");

    private final String code;
    private final String msg;

}
