package com.example.wallet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class UserWalletDTO {

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
