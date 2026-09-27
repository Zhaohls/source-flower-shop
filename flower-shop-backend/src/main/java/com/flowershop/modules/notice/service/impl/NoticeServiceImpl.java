package com.flowershop.modules.notice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.flowershop.modules.notice.entity.NoticeEntity;
import com.flowershop.modules.notice.mapper.NoticeMapper;
import com.flowershop.modules.notice.service.NoticeService;
import org.springframework.stereotype.Service;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, NoticeEntity> implements NoticeService {
}
