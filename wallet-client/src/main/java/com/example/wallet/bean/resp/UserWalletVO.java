package com.example.wallet.bean.resp;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class UserWalletVO {

    /**
     * 钱包id
     */
    private String walletId;

    /**
     * 钱包所属用户
     */
    private String userId;

    /**
     * 钱包余额
     */
    private String walletBalance;

}
