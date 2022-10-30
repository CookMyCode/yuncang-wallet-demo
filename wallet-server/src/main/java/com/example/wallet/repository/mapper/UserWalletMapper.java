package com.example.wallet.repository.mapper;

import com.example.wallet.repository.po.UserWalletPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserWalletMapper {

    UserWalletPO getUserWalletByWalletId(String walletId);

    UserWalletPO getUserWalletByUserIdLockShare(String userId);

    int updateWalletBalance(@Param("walletId") String walletId, @Param("balance") String balance);

}
