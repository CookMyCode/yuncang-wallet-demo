package com.example.wallet.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TxTypeCode {

    RECHARGE(1, "充值"),
    WITHDRAWAL(2, "提现"),
    PAY(3, "消费"),
    REFUND(4, "退款");

    private final Integer code;
    private final String msg;

}
