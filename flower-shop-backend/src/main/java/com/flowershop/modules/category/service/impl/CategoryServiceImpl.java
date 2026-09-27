package com.flowershop.modules.category.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flowershop.modules.category.entity.CategoryEntity;
import com.flowershop.modules.category.mapper.CategoryMapper;
import com.flowershop.modules.category.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {
}
