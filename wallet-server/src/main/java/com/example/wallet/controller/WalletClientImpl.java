package com.example.wallet.controller;

import com.example.wallet.bean.resp.TransactionVO;
import com.example.wallet.bean.resp.UserWalletVO;
import com.example.wallet.bean.resp.WalletTransactionInfoVO;
import com.example.wallet.client.WalletClient;
import com.example.wallet.common.resp.RespPage;
import com.example.wallet.dto.TransactionDTO;
import com.example.wallet.dto.UserWalletDTO;
import com.example.wallet.dto.WalletTransactionInfoDTO;
import com.example.wallet.service.WalletService;
import com.example.wallet.utils.BeanCopyUtils;

import javax.annotation.Resource;

public class WalletClientImpl implements WalletClient {

    @Resource
    private WalletService walletService;

    @Override
    public UserWalletVO getUserWallet(String walletId) {
        UserWalletDTO dto = walletService.getUserWallet(walletId);
        return BeanCopyUtils.copyProperties(dto, UserWalletVO.class);
    }

    @Override
    public TransactionVO pay(String orderId) {
        TransactionDTO dto = walletService.pay(orderId);
        return BeanCopyUtils.copyProperties(dto, TransactionVO.class);
    }

    @Override
    public TransactionVO refund(String orderId) {
        TransactionDTO dto = walletService.refund(orderId);
        return BeanCopyUtils.copyProperties(dto, TransactionVO.class);
    }

    @Override
    public RespPage<WalletTransactionInfoVO> listWalletTransaction(String walletId, Integer page, Integer size) {
        RespPage<WalletTransactionInfoDTO> dtoPage = walletService.listWalletTransactionInfoByWalletId(walletId, page, size);
        return new RespPage<WalletTransactionInfoVO>()
                .setPage(dtoPage.getPage())
                .setSize(dtoPage.getSize())
                .setTotal(dtoPage.getTotal())
                .setRecords(BeanCopyUtils.copyProperties(dtoPage.getRecords(), WalletTransactionInfoVO.class));
    }
}
