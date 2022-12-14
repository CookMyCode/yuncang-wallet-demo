#钱包动账交易信息
create TABLE `wallet_transaction_info` (
  `wtx_no` int unsigned AUTO_INCREMENT comment '自增序号',
  `wtx_id` varchar(64) NOT NULL comment '动账id',
  `order_id` varchar(64) NOT NULL comment '关联订单',
  `wallet_id` varchar(64) DEFAULT NULL comment '关联钱包',
  `wtx_amount` varchar(128) DEFAULT NULL comment '交易金额',
  `wallet_balance_his` varchar(32) DEFAULT NULL comment '交易前钱包余额',
  `wtx_status` tinyint DEFAULT NULL comment '交易状态, 1:发起 2:处理中 3:完成 4:失败',
  `wtx_type` tinyint DEFAULT NULL comment '动账类型, 1:充值 2:提现 3:消费 4:退款 5:冲正',
  `wtx_channel` tinyint DEFAULT NULL comment '动账交易渠道, 1:钱包 2:支付宝 3:微信 4:中行 5:农行 6:工行',
  `wtx_extension_id` varchar(512) DEFAULT NULL comment '关联的其它id, 如充值时第三方机构产生的交易id',
  `wtx_desc` varchar(1024) DEFAULT NULL comment '描述',
  `c_time` timestamp(3) NULL DEFAULT CURRENT_TIMESTAMP(3) comment '创建时间(ms)',
  `m_time` timestamp(3) NULL DEFAULT CURRENT_TIMESTAMP(3) ON update CURRENT_TIMESTAMP(3) comment '修改时间(ms)',
  `status` tinyint DEFAULT '1' comment '状态, 1:可用 0:失效',
  PRIMARY KEY (`wtx_id`),
  KEY `idx_wtxNo` (`wtx_no`),
  KEY `idx_orderId_wtxStatus` (`order_id`,`wtx_status`),
  KEY `idx_walletId_type_channel` (`wallet_id`,`wtx_type`,`wtx_channel`),
  KEY `idx_wtxExtensionId` (`wtx_extension_id`),
  KEY `idx_cTime` (`c_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 comment='钱包动账交易信息';