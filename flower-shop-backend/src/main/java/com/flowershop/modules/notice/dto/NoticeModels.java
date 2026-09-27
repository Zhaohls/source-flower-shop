package com.flowershop.modules.notice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public final class NoticeModels {

    private NoticeModels() {
    }

    public record NoticeSaveRequest(@NotBlank(message = "标题不能为空") String title, @NotBlank(message = "内容不能为空") String content, @NotNull(message = "状态不能为空") Integer status) {
    }

    public record NoticeVO(Long id, String title, String content, Integer status, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createTime, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updateTime) {
    }

    public record PageResult<T>(List<T> records, Long total, Long pageNum, Long pageSize, Long pages) {
    }
}
