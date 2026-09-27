package com.flowershop.modules.banner.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowershop.common.exception.BusinessException;
import com.flowershop.common.model.PageQuery;
import com.flowershop.modules.banner.dto.BannerModels.BannerSaveRequest;
import com.flowershop.modules.banner.dto.BannerModels.BannerVO;
import com.flowershop.modules.banner.dto.BannerModels.PageResult;
import com.flowershop.modules.banner.entity.BannerEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BannerAppService {

    private final BannerService bannerService;

    public PageResult<BannerVO> list(PageQuery pageQuery) {
        Page<BannerEntity> page = bannerService.page(new Page<>(pageQuery.current(), pageQuery.size()),
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BannerEntity>().orderByAsc(BannerEntity::getSort, BannerEntity::getId));
        List<BannerVO> records = page.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    public Long create(BannerSaveRequest request) {
        BannerEntity entity = new BannerEntity();
        fill(entity, request);
        bannerService.save(entity);
        return entity.getId();
    }

    public void update(Long id, BannerSaveRequest request) {
        BannerEntity entity = load(id);
        fill(entity, request);
        bannerService.updateById(entity);
    }

    public void delete(Long id) {
        bannerService.removeById(id);
    }

    public void updateStatus(Long id, Integer status) {
        BannerEntity entity = load(id);
        entity.setStatus(status);
        bannerService.updateById(entity);
    }

    private BannerEntity load(Long id) {
        BannerEntity entity = bannerService.getById(id);
        if (entity == null) {
            throw new BusinessException("轮播图不存在");
        }
        return entity;
    }

    private void fill(BannerEntity entity, BannerSaveRequest request) {
        entity.setTitle(request.title());
        entity.setImageUrl(request.imageUrl());
        entity.setLinkUrl(request.linkUrl());
        entity.setSort(request.sort());
        entity.setStatus(request.status());
    }

    private BannerVO toVO(BannerEntity entity) {
        return new BannerVO(entity.getId(), entity.getTitle(), entity.getImageUrl(), entity.getLinkUrl(), entity.getSort(), entity.getStatus(), entity.getCreateTime());
    }
}
