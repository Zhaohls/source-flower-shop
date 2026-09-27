package com.flowershop.modules.banner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flowershop.modules.banner.entity.BannerEntity;
import com.flowershop.modules.banner.mapper.BannerMapper;
import com.flowershop.modules.banner.service.BannerService;
import org.springframework.stereotype.Service;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, BannerEntity> implements BannerService {
}
