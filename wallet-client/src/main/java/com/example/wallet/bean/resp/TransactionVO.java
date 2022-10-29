package com.example.wallet.bean.resp;

import com.example.wallet.utils.TxResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class TransactionVO {

    /**
     * 交易执行结果
     */
    private TxResultCode resCode;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 关联的钱包交易id
     */
    private String wtxId;

    /**
     * 钱包id
     */
    private String walletId;

    /**
     * 订单
     */
    private String orderId;

    /**
     * 订单待支付金额
     */
    private String orderAmount;

    /**
     * 交易前钱包余额
     */
    private String walletBalanceHis;

    /**
     * 交易后钱包余额
     */
    private String balanceAfterTx;

    /**
     * 动账类型, 1:充值 2:提现 3:消费 4:退款 5:冲正
     */
    private int wtxType;

}
