package com.flowershop.modules.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flowershop.modules.product.entity.ProductEntity;
import com.flowershop.modules.product.mapper.ProductMapper;
import com.flowershop.modules.product.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, ProductEntity> implements ProductService {
}
