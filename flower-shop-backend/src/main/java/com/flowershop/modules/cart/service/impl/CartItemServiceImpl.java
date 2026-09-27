package com.flowershop.modules.cart.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flowershop.modules.cart.entity.CartItemEntity;
import com.flowershop.modules.cart.mapper.CartItemMapper;
import com.flowershop.modules.cart.service.CartItemService;
import org.springframework.stereotype.Service;

@Service
public class CartItemServiceImpl extends ServiceImpl<CartItemMapper, CartItemEntity> implements CartItemService {
}
