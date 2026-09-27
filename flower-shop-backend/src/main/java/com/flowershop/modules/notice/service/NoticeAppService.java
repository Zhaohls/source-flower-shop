package com.flowershop.modules.notice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowershop.common.context.UserContextHolder;
import com.flowershop.common.exception.BusinessException;
import com.flowershop.common.model.PageQuery;
import com.flowershop.modules.notice.dto.NoticeModels.NoticeSaveRequest;
import com.flowershop.modules.notice.dto.NoticeModels.NoticeVO;
import com.flowershop.modules.notice.dto.NoticeModels.PageResult;
import com.flowershop.modules.notice.entity.NoticeEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoticeAppService {

    private final NoticeService noticeService;

    public PageResult<NoticeVO> list(PageQuery pageQuery) {
        Page<NoticeEntity> page = noticeService.page(new Page<>(pageQuery.current(), pageQuery.size()),
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NoticeEntity>().orderByDesc(NoticeEntity::getCreateTime));
        List<NoticeVO> records = page.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    public Long create(NoticeSaveRequest request) {
        NoticeEntity entity = new NoticeEntity();
        entity.setTitle(request.title());
        entity.setContent(request.content());
        entity.setStatus(request.status());
        entity.setCreateBy(UserContextHolder.getUserId());
        noticeService.save(entity);
        return entity.getId();
    }

    public void update(Long id, NoticeSaveRequest request) {
        NoticeEntity entity = load(id);
        entity.setTitle(request.title());
        entity.setContent(request.content());
        entity.setStatus(request.status());
        noticeService.updateById(entity);
    }

    public void updateStatus(Long id, Integer status) {
        NoticeEntity entity = load(id);
        entity.setStatus(status);
        noticeService.updateById(entity);
    }

    public void delete(Long id) {
        noticeService.removeById(id);
    }

    private NoticeEntity load(Long id) {
        NoticeEntity entity = noticeService.getById(id);
        if (entity == null) {
            throw new BusinessException("公告不存在");
        }
        return entity;
    }

    private NoticeVO toVO(NoticeEntity entity) {
        return new NoticeVO(entity.getId(), entity.getTitle(), entity.getContent(), entity.getStatus(), entity.getCreateTime(), entity.getUpdateTime());
    }
}
