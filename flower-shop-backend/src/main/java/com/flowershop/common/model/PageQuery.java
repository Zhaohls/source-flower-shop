package com.flowershop.common.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PageQuery(
    @Min(value = 1, message = "页码最小为1") Integer pageNum,
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100") Integer pageSize
) {

    public long current() {
        return pageNum == null ? 1L : pageNum;
    }

    public long size() {
        return pageSize == null ? 10L : pageSize;
    }
}
