package com.example.wallet.repository.mapper;

import com.example.wallet.repository.po.WalletTransactionInfoPO;
import com.example.wallet.repository.po.WalletTransactionPoolPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WalletTransactionPoolMapper {

    int insert(WalletTransactionPoolPO po);

    int deleteByWtxId(String wtxId);

    WalletTransactionInfoPO getActiveWalletTransactionByOrderId(String orderId);

    int updateWalletTransactionInfo(WalletTransactionInfoPO po);

    int deleteTimeoutTransaction(String lastTime);

}
