#用户钱包
create TABLE `user_wallet` (
  `wallet_no` int unsigned AUTO_INCREMENT comment '自增序号',
  `wallet_id` varchar(64) NOT NULL comment '钱包id',
  `user_id` varchar(64) NOT NULL comment '钱包所属用户',
  `wallet_balance` varchar(32) NOT NULL comment '钱包余额',
  `c_time` timestamp(3) NULL DEFAULT CURRENT_TIMESTAMP(3) comment '创建时间(ms)',
  `m_time` timestamp(3) NULL DEFAULT CURRENT_TIMESTAMP(3) ON update CURRENT_TIMESTAMP(3) comment '修改时间(ms)',
  `status` tinyint DEFAULT '1' comment '状态, 1:可用 0:失效',
  PRIMARY KEY (`wallet_id`),
  KEY `idx_walletNo` (`wallet_no`),
  KEY `idx_userId` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 comment='用户钱包';