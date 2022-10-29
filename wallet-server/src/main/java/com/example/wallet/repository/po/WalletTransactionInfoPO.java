package com.example.wallet.repository.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class WalletTransactionInfoPO {

    /**
     * 钱包动账id
     */
    private String wtxId;

    /**
     * 关联订单
     */
    private String orderId;

    /**
     * 钱包id
     */
    private String walletId;

    /**
     * 交易金额
     */
    private String wtxAmount;

    /**
     * 交易前钱包余额
     */
    private String walletBalanceHis;

    /**
     * 交易状态, 1:发起 2:处理中 3:完成 4:重试 5:失败
     */
    private int wtxStatus;

    /**
     * 动账类型, 1:充值 2:提现 3:消费 4:退款 5:冲正
     */
    private int wtxType;

    /**
     * 动账来源渠道, 1:钱包 2:支付宝 3:微信 4:中行 5:农行 6:工行
     */
    private int wtxChannel;

    /**
     * 关联的其它id, 如充值时第三方机构产生的交易id
     */
    private String wtxExtensionId;

    /**
     * 创建时间
     */
    private String cTime;

}
