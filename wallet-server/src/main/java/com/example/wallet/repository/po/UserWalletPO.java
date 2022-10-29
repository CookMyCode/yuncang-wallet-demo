package com.example.wallet.repository.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserWalletPO {

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
