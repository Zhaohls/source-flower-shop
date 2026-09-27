package com.flowershop.modules.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flowershop.modules.order.entity.OrderInfoEntity;
import com.flowershop.modules.order.mapper.OrderInfoMapper;
import com.flowershop.modules.order.service.OrderInfoService;
import org.springframework.stereotype.Service;

@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfoEntity> implements OrderInfoService {
}
