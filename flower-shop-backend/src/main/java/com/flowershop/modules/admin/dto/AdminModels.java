package com.flowershop.modules.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.flowershop.modules.order.dto.OrderModels.TrendPointVO;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class AdminModels {

    private AdminModels() {
    }

    public record PageResult<T>(List<T> records, Long total, Long pageNum, Long pageSize, Long pages) {
    }

    public record StatusDistributionVO(String status, Long count) {
    }

    public record CategoryDistributionVO(String categoryName, Long productCount) {
    }

    public record MerchantRevenueRankingVO(Long merchantId, String merchantName, BigDecimal revenue, Long orderCount) {
    }

    public record DashboardVO(
        Long totalUsers,
        Long totalMerchants,
        Long totalProducts,
        Long todayOrders,
        BigDecimal todayRevenue,
        BigDecimal monthlyRevenue,
        Long newUsersToday,
        List<TrendPointVO> orderTrend7Days,
        List<TrendPointVO> revenueTrend30Days,
        List<StatusDistributionVO> orderStatusDistribution,
        List<CategoryDistributionVO> categoryDistribution,
        List<MerchantRevenueRankingVO> merchantRevenueRanking
    ) {
    }

    public record MerchantAuditRequest(@NotNull(message = "审核状态不能为空") Integer auditStatus, String auditRemark) {
    }

    public record StatusRequest(@NotNull(message = "状态不能为空") Integer status) {
    }

    public record MerchantItemVO(
        Long id,
        String username,
        String nickname,
        String shopName,
        String phone,
        Integer status,
        String auditRemark,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createTime
    ) {
    }

    public record UserItemVO(
        Long id,
        String username,
        String nickname,
        String role,
        String phone,
        Integer status,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createTime
    ) {
    }

    public record OperationLogVO(
        Long id,
        String module,
        String action,
        String operator,
        String ip,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createTime
    ) {
    }

    public record DictTypeVO(
        Long id,
        String dictName,
        String dictType,
        String remark
    ) {
    }

    public record DictDataVO(
        String dictLabel,
        String dictValue,
        Integer sort
    ) {
    }
}
