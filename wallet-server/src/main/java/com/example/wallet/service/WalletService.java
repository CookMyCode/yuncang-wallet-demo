package com.example.wallet.service;

import com.example.wallet.common.resp.RespPage;
import com.example.wallet.dto.TransactionDTO;
import com.example.wallet.dto.UserWalletDTO;
import com.example.wallet.dto.WalletTransactionInfoDTO;
import com.example.wallet.repository.po.WalletTransactionInfoPO;
import com.github.pagehelper.PageInfo;

import java.util.function.Function;

/**
 * 钱包相关服务
 */
public interface WalletService {

    /**
     * 获取用户钱包信息
     */
    UserWalletDTO getUserWallet(String walletId);

    /**
     * 支付订单
     */
    TransactionDTO pay(String orderId);

    /**
     * 订单退款
     */
    TransactionDTO refund(String orderId);

    /**
     * 分页查询交易历史
     */
    RespPage<WalletTransactionInfoDTO> listWalletTransactionInfoByWalletId(String walletId, Integer pageNum, Integer pageSize);

    /**
     * 交易处理
     */
    TransactionDTO walletTransaction(String orderId, Function<TransactionDTO, TransactionDTO> processor);

    /**
     * 交易处理流程
     */
    void postProcessTransaction(TransactionDTO txRes, Function<TransactionDTO, TransactionDTO> processor);

    /**
     * 记录交易信息
     */
    void recordTransactionInfo(TransactionDTO payRes);

}
