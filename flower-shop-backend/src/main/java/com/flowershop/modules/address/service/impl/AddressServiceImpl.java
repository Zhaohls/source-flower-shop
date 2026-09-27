package com.flowershop.modules.address.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flowershop.modules.address.entity.AddressEntity;
import com.flowershop.modules.address.mapper.AddressMapper;
import com.flowershop.modules.address.service.AddressService;
import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, AddressEntity> implements AddressService {
}
