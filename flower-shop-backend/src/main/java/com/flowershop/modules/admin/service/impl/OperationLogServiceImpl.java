package com.flowershop.modules.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flowershop.modules.admin.entity.OperationLogEntity;
import com.flowershop.modules.admin.mapper.OperationLogMapper;
import com.flowershop.modules.admin.service.OperationLogService;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLogEntity> implements OperationLogService {
}
