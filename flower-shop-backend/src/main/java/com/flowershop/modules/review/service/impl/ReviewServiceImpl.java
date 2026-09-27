package com.flowershop.modules.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flowershop.modules.review.entity.ReviewEntity;
import com.flowershop.modules.review.mapper.ReviewMapper;
import com.flowershop.modules.review.service.ReviewService;
import org.springframework.stereotype.Service;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, ReviewEntity> implements ReviewService {
}
