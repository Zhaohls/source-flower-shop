package com.flowershop.common.model;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

public record PageResponse<T>(Long total, Long pageNum, Long pageSize, List<T> list) {

    public static <T> PageResponse<T> of(IPage<T> page) {
        return new PageResponse<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }
}
