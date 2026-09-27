package com.flowershop.modules.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final OrderAppService orderAppService;

    @Scheduled(cron = "0 */1 * * * ?")
    public void cancelTimeoutOrders() {
        orderAppService.scanAndCancelTimeoutOrders();
    }
}
