package com.example.wallet.client;

import com.example.wallet.bean.resp.TransactionVO;
import com.example.wallet.bean.resp.UserWalletVO;
import com.example.wallet.bean.resp.WalletTransactionInfoVO;
import com.example.wallet.common.resp.RespPage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(value = "wallet-server")
public interface WalletClient {

    /**
     * 获取用户钱包信息
     * @param walletId 钱包id
     */
    @GetMapping("/wallet/{walletId}")
    UserWalletVO getUserWallet(@PathVariable("walletId") String walletId);

    /**
     * 支付订单
     * @param orderId 订单id
     */
    @PutMapping("/pay/{orderId}")
    TransactionVO pay(@PathVariable("orderId") String orderId);

    /**
     * 订单退款
     * @param orderId 订单id
     */
    @PutMapping("/refund/{orderId}")
    TransactionVO refund(@PathVariable("orderId") String orderId);

    /**
     * 钱包动账信息
     * @param walletId 钱包id
     * @param page 第几页
     * @param size 每页大小
     */
    @GetMapping("/wallet/{walletId}/txn")
    RespPage<WalletTransactionInfoVO> listWalletTransaction(@PathVariable("walletId") String walletId, Integer page, Integer size);

}
