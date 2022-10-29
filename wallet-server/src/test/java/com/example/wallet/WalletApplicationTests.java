package com.example.wallet;

import com.example.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class WalletApplicationTests {

	@Resource
	WalletService walletService;

	@Test
	void contextLoads() {
//		walletService.pay(new WalletTransactionDTO()
//				.setWalletId("12")
//				.setOrderId("12")
//				.setWtxAmount("se")
//				.setWtxChannel(1)
//				.setWtxId("se")
//				.setWtxStatus(1)
//				.setWtxType(1));
	}

}
