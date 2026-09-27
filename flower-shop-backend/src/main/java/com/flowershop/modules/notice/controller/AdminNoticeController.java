package com.flowershop.modules.notice.controller;

import com.flowershop.common.annotation.RequireRole;
import com.flowershop.common.result.Result;
import com.flowershop.modules.admin.dto.AdminModels.StatusRequest;
import com.flowershop.modules.notice.dto.NoticeModels.NoticeSaveRequest;
import com.flowershop.modules.notice.dto.NoticeModels.NoticeVO;
import com.flowershop.modules.notice.dto.NoticeModels.PageResult;
import com.flowershop.modules.notice.service.NoticeAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员公告管理")
@RequireRole({"ADMIN"})
@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticeAppService noticeAppService;

    @Operation(summary = "公告列表", description = "分页查看公告信息")
    @GetMapping
    public Result<PageResult<NoticeVO>> list(@RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(noticeAppService.list(new com.flowershop.common.model.PageQuery(pageNum, pageSize)));
    }

    @Operation(summary = "新增公告", description = "新增系统公告")
    @PostMapping
    public Result<Map<String, Long>> create(@Valid @RequestBody NoticeSaveRequest request) {
        return Result.success(Map.of("id", noticeAppService.create(request)));
    }

    @Operation(summary = "修改公告", description = "更新公告标题、内容和状态")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody NoticeSaveRequest request) {
        noticeAppService.update(id, request);
        return Result.success();
    }

    @Operation(summary = "删除公告", description = "删除指定公告")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noticeAppService.delete(id);
        return Result.success();
    }

    @Operation(summary = "发布下架公告", description = "快速切换公告状态")
    @PatchMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        noticeAppService.updateStatus(id, request.status());
        return Result.success();
    }
}
