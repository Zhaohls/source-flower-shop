package com.flowershop.modules.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flowershop.modules.order.entity.OrderItemEntity;
import com.flowershop.modules.order.mapper.OrderItemMapper;
import com.flowershop.modules.order.service.OrderItemService;
import org.springframework.stereotype.Service;

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItemEntity> implements OrderItemService {
}
