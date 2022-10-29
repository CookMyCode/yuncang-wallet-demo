package com.example.wallet.repository.mapper;

import com.example.wallet.repository.po.WalletTransactionInfoPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WalletTransactionInfoMapper {

    int insertWalletTransactionInfo(WalletTransactionInfoPO po);

    int insertWalletTransactionInfoFromPool(String wtxId);

    List<WalletTransactionInfoPO> listWalletTransactionInfoByWalletId(String walletId);

}
