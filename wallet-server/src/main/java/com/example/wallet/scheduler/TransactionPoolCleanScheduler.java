package com.example.wallet.scheduler;

import com.example.wallet.repository.mapper.WalletTransactionPoolMapper;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 定时器用于定时清理wallet_transaction_pool表中的超时数据
 * TODO 需结合订单超时时间、流量高峰时段进行配置
 */
@Component
public class TransactionPoolCleanScheduler {

    @Resource
    private WalletTransactionPoolMapper walletTransactionPoolMapper;

    @Scheduled(cron = "0 0 2,13 * * ?")
    public void cleanTimeoutTransaction() {
        String lastTime = DateFormatUtils.format(DateUtils.addHours(new Date(), 1),
                "yyyy-MM-dd HH:mm:ss.SSS");
        walletTransactionPoolMapper.deleteTimeoutTransaction(lastTime);
    }

}
