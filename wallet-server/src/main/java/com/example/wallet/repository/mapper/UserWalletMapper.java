package com.example.wallet.repository.mapper;

import com.example.wallet.repository.po.UserWalletPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserWalletMapper {

    UserWalletPO getUserWalletByWalletId(String walletId);

    UserWalletPO getUserWalletByUserIdForUpdate(String userId);

    int updateWalletBalance(String walletId, String balance);

}
