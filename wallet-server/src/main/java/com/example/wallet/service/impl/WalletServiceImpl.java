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
            // 检查钱包余额是否足够支付
            BigDecimal balanceAfterTx = walletBalance.subtract(orderAmount);
            if (new BigDecimal("0").compareTo(balanceAfterTx) > 0) {
                log.warn("钱包余额不足 walletId:{}", dto.getWalletId());
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
            //TODO 如表中数据超过千万量级，深度分页可能导致性能问题，可能需要结合自增索引进行分页，这里不做实现
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
        // 新增一笔交易, 写入wallet_transaction_pool表, orderId是唯一索引, 用于防止重复请求
        String wtxId = IdGenerator.getId();
        txRes.setOrderId(orderId).setWtxId(wtxId).setWtxType(txType);
        try {
            // 如果交易处理期间没有发生异常则会在表中一直保留, 直到被定时器删除TransactionPoolCleanScheduler.cleanTimeoutTransaction
            walletTransactionPoolMapper.insert(new WalletTransactionPoolPO()
                    .setWtxId(wtxId)
                    .setOrderId(orderId)
                    .setWtxType(txType.getCode()));
        } catch (Exception e) {
            log.warn("订单重复 ", e);
            return txRes.setResCode(TxResultCode.DUPLICATE);
        }

        //TODO 服务间调用, 通过订单id获取订单交易金额、用户id等, 还需要校验订单是否生效
        String userId;
        String orderAmountStr;
        try {
            // Order order = orderClient.getOrderById(orderId);
            // String orderAmountStr = order.getAmount();
            // String userId = order.getUserId();
            userId = "123456";
            orderAmountStr = "100.00";
            if (StringUtils.isAnyBlank(userId, orderAmountStr)) {
                throw new RuntimeException("订单信息不完整");
            }
            txRes.setUserId(userId).setOrderAmount(orderAmountStr);
        } catch (Exception e) {
            log.error("获取订单信息失败 ", e);
            // 删除交易
            walletTransactionPoolMapper.deleteByWtxId(wtxId);
            return txRes.setResCode(TxResultCode.FAIL);
        }

        WalletService walletService = applicationContext.getBean(WalletService.class);
        try {
            // 执行交易流程 如:钱包扣费/退款等操作
            walletService.postProcessTransaction(txRes, processor);
        } catch (Exception e) {
            log.error("钱包交易处理异常 ", e);
            txRes.setResCode(TxResultCode.FAIL);
        }

        // 判断交易是否失败
        if (!TxResultCode.SUCCESS.getCode().equals(txRes.getResCode().getCode())) {
            try {
                // 记录交易失败信息
                walletService.recordTransactionInfo(txRes);
            } catch (Exception e) {
                log.error("日志写入失败 ", e);
            }
            // 删除交易, 否则在定时任务清理前，支付都无法再次发起
            walletTransactionPoolMapper.deleteByWtxId(wtxId);
        }

        return txRes;
    }

    @Transactional
    @Override
    public void postProcessTransaction(TransactionDTO txRes, Function<TransactionDTO, TransactionDTO> processor) {
        String userId = txRes.getUserId();
        String orderAmountStr = txRes.getOrderAmount();
        // 获取钱包信息 LOCK IN SHARE MODE 加锁
        UserWalletPO userWallet = userWalletMapper.getUserWalletByUserIdLockShare(userId);
        if (userWallet == null) {
            log.error("钱包不存在 userId:{}", userId);
            throw new RuntimeException("钱包不存在");
        }
        String walletId = userWallet.getWalletId();
        String walletBalanceStr = userWallet.getWalletBalance();
        txRes.setWalletId(walletId).setWalletBalanceHis(walletBalanceStr);
        // 计算余额
        txRes = processor.apply(txRes);
        if (!TxResultCode.SUCCESS.getCode().equals(txRes.getResCode().getCode())) {
            return;
        }
        // 更新钱包余额
        userWalletMapper.updateWalletBalance(walletId, txRes.getBalanceAfterTx());

        // 记录交易信息
        WalletService walletService = applicationContext.getBean(WalletService.class);
        walletService.recordTransactionInfo(txRes.setResCode(TxResultCode.SUCCESS));
    }

    @Transactional
    @Override
    public void recordTransactionInfo(TransactionDTO txRes) {
        String wtxId = txRes.getWtxId();
        // 更新交易信息
        walletTransactionPoolMapper.updateWalletTransactionPool(new WalletTransactionInfoPO()
                .setWtxId(wtxId)
                .setOrderId(txRes.getOrderId())
                .setWalletId(txRes.getWalletId())
                .setWtxAmount(txRes.getOrderAmount())
                .setWalletBalanceHis(txRes.getWalletBalanceHis())
                .setWtxType(txRes.getWtxType().getCode())
                .setWtxChannel(1)
                .setWtxStatus(TxResultCode.SUCCESS.getCode().equals(txRes.getResCode().getCode()) ? 3 : 4));
        // 复制信息到wallet_transaction_info表
        walletTransactionInfoMapper.insertWalletTransactionInfoFromPool(wtxId);
    }

}
