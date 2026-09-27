package com.flowershop.modules.order.service;

import com.flowershop.common.constant.RedisKeyConstants;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class OrderTimeoutListener extends KeyExpirationEventMessageListener {

    private final OrderAppService orderAppService;

    public OrderTimeoutListener(RedisMessageListenerContainer listenerContainer, OrderAppService orderAppService) {
        super(listenerContainer);
        this.orderAppService = orderAppService;
    }

    @Override
    protected void doHandleMessage(Message message) {
        super.doHandleMessage(message);
        String expiredKey = new String(message.getBody(), StandardCharsets.UTF_8);
        if (StringUtils.startsWith(expiredKey, RedisKeyConstants.ORDER_TIMEOUT)) {
            String orderNo = StringUtils.removeStart(expiredKey, RedisKeyConstants.ORDER_TIMEOUT);
            orderAppService.autoCancelTimeoutOrder(orderNo);
        }
    }
}
