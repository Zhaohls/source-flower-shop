package com.flowershop.modules.delivery.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flowershop.modules.delivery.entity.DeliveryEntity;
import com.flowershop.modules.delivery.mapper.DeliveryMapper;
import com.flowershop.modules.delivery.service.DeliveryService;
import org.springframework.stereotype.Service;

@Service
public class DeliveryServiceImpl extends ServiceImpl<DeliveryMapper, DeliveryEntity> implements DeliveryService {
}
