package com.flowershop.modules.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flowershop.modules.order.entity.CardTemplateEntity;
import com.flowershop.modules.order.mapper.CardTemplateMapper;
import com.flowershop.modules.order.service.CardTemplateService;
import org.springframework.stereotype.Service;

@Service
public class CardTemplateServiceImpl extends ServiceImpl<CardTemplateMapper, CardTemplateEntity> implements CardTemplateService {
}
