package com.example.wallet.service.impl;

import com.example.wallet.common.resp.RespPage;
import com.example.wallet.dto.TransactionDTO;
import com.example.wallet.dto.UserWalletDTO;
import com.example.wallet.dto.WalletTransactionInfoDTO;
import com.example.wallet.repository.mapper.UserWalletMapper;
import com.example.wallet.repository.mapper.WalletTransactionInfoMapper;
import com.example.wallet.repository.mapper.WalletTransactionPoolMapper;
import com.example.wallet.repository.po.UserWalletPO;
import com.example.wallet.repository.po.WalletTransactionInfoPO;
import com.example.wallet.repository.po.WalletTransactionPoolPO;
import com.example.wallet.service.WalletService;
import com.example.wallet.utils.BeanCopyUtils;
import com.example.wallet.utils.IdGenerator;
import com.example.wallet.utils.TxResultCode;
import com.example.wallet.utils.TxTypeCode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
public class WalletServiceImpl implements WalletService {

    @Resource
    private WalletTransactionPoolMapper walletTransactionPoolMapper;

    @Resource
    private WalletTransactionInfoMapper walletTransactionInfoMapper;

    @Resource
    private UserWalletMapper userWalletMapper;

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public UserWalletDTO getUserWallet(String walletId) {
        UserWalletPO po = userWalletMapper.getUserWalletByWalletId(walletId);
        return BeanCopyUtils.copyProperties(po, UserWalletDTO.class);
    }

    @Override
    public TransactionDTO pay(String orderId) {
        return walletTransaction(orderId, dto -> {
            BigDecimal walletBalance = new BigDecimal(dto.getWalletBalanceHis());
            BigDecimal orderAmount = new BigDecimal(dto.getOrderAmount());
            // ????????????????????????????????????
            BigDecimal balanceAfterTx = walletBalance.subtract(orderAmount);
            if (new BigDecimal("0").compareTo(balanceAfterTx) > 0) {
                log.warn("?????????????????? walletId:{}", dto.getWalletId());
                return dto.setResCode(TxResultCode.INSUFFICIENT);
            }
            return dto.setBalanceAfterTx(balanceAfterTx.setScale(2, RoundingMode.DOWN).toPlainString())
                    .setResCode(TxResultCode.SUCCESS);
        }, TxTypeCode.PAY);
    }

    @Override
    public TransactionDTO refund(String orderId) {
        return walletTransaction(orderId, dto -> {
            BigDecimal walletBalance = new BigDecimal(dto.getWalletBalanceHis());
            BigDecimal orderAmount = new BigDecimal(dto.getOrderAmount());
            BigDecimal balanceAfterTx = walletBalance.add(orderAmount);
            return dto.setBalanceAfterTx(balanceAfterTx.setScale(2, RoundingMode.DOWN).toPlainString())
                    .setResCode(TxResultCode.SUCCESS);
        }, TxTypeCode.REFUND);
    }

    @Override
    public RespPage<WalletTransactionInfoDTO> listWalletTransactionInfoByWalletId(String walletId, Integer pageNum, Integer pageSize) {
        if (pageNum != null && pageSize != null) {
            //TODO ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            PageHelper.startPage(pageNum, pageSize);
        }
        List<WalletTransactionInfoPO> list = walletTransactionInfoMapper.listWalletTransactionInfoByWalletId(walletId);
        PageInfo<WalletTransactionInfoPO> pageInfo = new PageInfo<>(list);
        return new RespPage<WalletTransactionInfoDTO>()
                .setPage(pageInfo.getPageNum())
                .setSize(pageInfo.getSize())
                .setTotal(pageInfo.getTotal())
                .setRecords(BeanCopyUtils.copyProperties(list, WalletTransactionInfoDTO.class));
    }

    @Override
    public TransactionDTO walletTransaction(String orderId, Function<TransactionDTO, TransactionDTO> processor, TxTypeCode txType) {
        TransactionDTO txRes = new TransactionDTO();
        // ??????????????????, ??????wallet_transaction_pool???, orderId???????????????, ????????????????????????
        String wtxId = IdGenerator.getId();
        txRes.setOrderId(orderId).setWtxId(wtxId).setWtxType(txType);
        try {
            // ?????????????????????????????????????????????????????????????????????, ????????????????????????TransactionPoolCleanScheduler.cleanTimeoutTransaction
            walletTransactionPoolMapper.insert(new WalletTransactionPoolPO()
                    .setWtxId(wtxId)
                    .setOrderId(orderId)
                    .setWtxType(txType.getCode()));
        } catch (Exception e) {
            log.warn("???????????? ", e);
            return txRes.setResCode(TxResultCode.DUPLICATE);
        }

        //TODO ???????????????, ????????????id?????????????????????????????????id???, ?????????????????????????????????
        String userId;
        String orderAmountStr;
        try {
            // Order order = orderClient.getOrderById(orderId);
            // String orderAmountStr = order.getAmount();
            // String userId = order.getUserId();
            userId = "123456";
            orderAmountStr = "100.00";
            if (StringUtils.isAnyBlank(userId, orderAmountStr)) {
                throw new RuntimeException("?????????????????????");
            }
            txRes.setUserId(userId).setOrderAmount(orderAmountStr);
        } catch (Exception e) {
            log.error("???????????????????????? ", e);
            // ????????????
            walletTransactionPoolMapper.deleteByWtxId(wtxId);
            return txRes.setResCode(TxResultCode.FAIL);
        }

        WalletService walletService = applicationContext.getBean(WalletService.class);
        try {
            // ?????????????????? ???:????????????/???????????????
            walletService.postProcessTransaction(txRes, processor);
        } catch (Exception e) {
            log.error("???????????????????????? ", e);
            txRes.setResCode(TxResultCode.FAIL);
        }

        try {
            // ??????????????????
            walletService.recordTransactionInfo(txRes);
        } catch (Exception e) {
            log.error("?????????????????? ", e);
        }

        // ????????????????????????
        if (!TxResultCode.SUCCESS.getCode().equals(txRes.getResCode().getCode())) {
            // ????????????, ????????????????????????????????????????????????????????????
            walletTransactionPoolMapper.deleteByWtxId(wtxId);
        }

        return txRes;
    }

    @Transactional
    @Override
    public void postProcessTransaction(TransactionDTO txRes, Function<TransactionDTO, TransactionDTO> processor) {
        String userId = txRes.getUserId();
        String orderAmountStr = txRes.getOrderAmount();
        // ?????????????????? LOCK IN SHARE MODE ??????
        UserWalletPO userWallet = userWalletMapper.getUserWalletByUserIdLockShare(userId);
        if (userWallet == null) {
            log.error("??????????????? userId:{}", userId);
            throw new RuntimeException("???????????????");
        }
        String walletId = userWallet.getWalletId();
        String walletBalanceStr = userWallet.getWalletBalance();
        txRes.setWalletId(walletId).setWalletBalanceHis(walletBalanceStr);
        // ????????????
        txRes = processor.apply(txRes);
        if (!TxResultCode.SUCCESS.getCode().equals(txRes.getResCode().getCode())) {
            return;
        }
        // ??????????????????
        userWalletMapper.updateWalletBalance(walletId, txRes.getBalanceAfterTx());

        // ?????????????????????
        txRes.setResCode(TxResultCode.SUCCESS);
    }

    @Transactional
    @Override
    public void recordTransactionInfo(TransactionDTO txRes) {
        String wtxId = txRes.getWtxId();
        // ??????????????????
        walletTransactionPoolMapper.updateWalletTransactionPool(new WalletTransactionInfoPO()
                .setWtxId(wtxId)
                .setOrderId(txRes.getOrderId())
                .setWalletId(txRes.getWalletId())
                .setWtxAmount(txRes.getOrderAmount())
                .setWalletBalanceHis(txRes.getWalletBalanceHis())
                .setWtxType(txRes.getWtxType().getCode())
                .setWtxChannel(1)
                .setWtxStatus(TxResultCode.SUCCESS.getCode().equals(txRes.getResCode().getCode()) ? 3 : 4));
        // ???????????????wallet_transaction_info???
        walletTransactionInfoMapper.insertWalletTransactionInfoFromPool(wtxId);
    }

}
