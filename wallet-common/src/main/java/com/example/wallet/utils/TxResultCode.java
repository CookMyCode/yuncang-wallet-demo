package com.example.wallet.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TxResultCode {

    SUCCESS(1, "交易处理成功"),
    INSUFFICIENT(2, "余额不足"),
    DUPLICATE(3, "订单重复"),
    FAIL(4, "交易处理失败");

    private final Integer code;
    private final String msg;

}
