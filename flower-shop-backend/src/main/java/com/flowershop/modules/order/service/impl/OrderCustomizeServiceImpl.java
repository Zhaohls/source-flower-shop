package com.flowershop.modules.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flowershop.modules.order.entity.OrderCustomizeEntity;
import com.flowershop.modules.order.mapper.OrderCustomizeMapper;
import com.flowershop.modules.order.service.OrderCustomizeService;
import org.springframework.stereotype.Service;

@Service
public class OrderCustomizeServiceImpl extends ServiceImpl<OrderCustomizeMapper, OrderCustomizeEntity> implements OrderCustomizeService {
}
