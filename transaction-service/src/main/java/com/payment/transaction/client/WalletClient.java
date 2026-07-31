package com.payment.transaction.client;

import com.payment.transaction.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "wallet-service", url = "${wallet.service.url}")
public interface WalletClient {

    @PostMapping("/debit")
    WalletResponse debit(@RequestBody DebitRequest request);

    @PostMapping("/credit")
    WalletResponse credit(@RequestBody CreditRequest request);

    @PostMapping("/hold")
    HoldResponse placeHold(@RequestBody HoldRequest request);

    @PostMapping("/capture")
    WalletResponse capture(@RequestBody CaptureRequest request);

    @PostMapping("/release/{holdReference}")
    HoldResponse release(@PathVariable("holdReference") String holdReference);

    @GetMapping("/{userId}")
    WalletResponse getWallet(@PathVariable("userId") Long userId);
}