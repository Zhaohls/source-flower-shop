package com.flowershop.modules.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowershop.common.exception.BusinessException;
import com.flowershop.common.model.PageQuery;
import com.flowershop.modules.admin.dto.AdminModels.CategoryDistributionVO;
import com.flowershop.modules.admin.dto.AdminModels.DashboardVO;
import com.flowershop.modules.admin.dto.AdminModels.MerchantAuditRequest;
import com.flowershop.modules.admin.dto.AdminModels.MerchantItemVO;
import com.flowershop.modules.admin.dto.AdminModels.MerchantRevenueRankingVO;
import com.flowershop.modules.admin.dto.AdminModels.PageResult;
import com.flowershop.modules.admin.dto.AdminModels.StatusDistributionVO;
import com.flowershop.modules.admin.dto.AdminModels.UserItemVO;
import com.flowershop.modules.category.entity.CategoryEntity;
import com.flowershop.modules.category.service.CategoryService;
import com.flowershop.modules.order.dto.OrderModels.TrendPointVO;
import com.flowershop.modules.order.entity.OrderInfoEntity;
import com.flowershop.modules.order.enums.OrderStatusEnum;
import com.flowershop.modules.order.service.OrderInfoService;
import com.flowershop.modules.product.entity.ProductEntity;
import com.flowershop.modules.product.service.ProductService;
import com.flowershop.modules.user.entity.UserEntity;
import com.flowershop.modules.user.enums.UserRoleEnum;
import com.flowershop.modules.user.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminConsoleAppService {

    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderInfoService orderInfoService;

    public DashboardVO dashboard() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<OrderInfoEntity> orders = orderInfoService.list();
        List<ProductEntity> products = productService.list();
        List<UserEntity> merchants = userService.lambdaQuery().eq(UserEntity::getRole, UserRoleEnum.MERCHANT.getCode()).list();
        Map<Long, String> categoryNameMap = categoryService.list().stream()
            .collect(Collectors.toMap(CategoryEntity::getId, CategoryEntity::getCategoryName, (left, right) -> left));
        Map<Long, String> merchantNameMap = merchants.stream()
            .collect(Collectors.toMap(UserEntity::getId, item -> StringUtils.defaultIfBlank(item.getShopName(), item.getUsername()), (left, right) -> left));

        long totalUsers = userService.lambdaQuery().eq(UserEntity::getRole, UserRoleEnum.CUSTOMER.getCode()).count();
        long totalMerchants = merchants.size();
        long totalProducts = products.size();
        long todayOrders = orders.stream().filter(order -> !order.getCreateTime().isBefore(todayStart) && order.getCreateTime().isBefore(tomorrowStart)).count();
        BigDecimal todayRevenue = sumRevenue(orders, todayStart, tomorrowStart);
        BigDecimal monthlyRevenue = sumRevenue(orders, monthStart, tomorrowStart);
        long newUsersToday = userService.lambdaQuery().ge(UserEntity::getCreateTime, todayStart).lt(UserEntity::getCreateTime, tomorrowStart).count();

        List<TrendPointVO> orderTrend7Days = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            long count = orders.stream().filter(order -> !order.getCreateTime().isBefore(start) && order.getCreateTime().isBefore(end)).count();
            BigDecimal revenue = sumRevenue(orders, start, end);
            orderTrend7Days.add(new TrendPointVO(date.toString(), count, revenue));
        }

        List<TrendPointVO> revenueTrend30Days = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            long count = orders.stream().filter(order -> !order.getCreateTime().isBefore(start) && order.getCreateTime().isBefore(end)).count();
            BigDecimal revenue = sumRevenue(orders, start, end);
            revenueTrend30Days.add(new TrendPointVO(date.toString(), count, revenue));
        }

        List<StatusDistributionVO> orderStatusDistribution = List.of(
            new StatusDistributionVO("待支付", orders.stream().filter(order -> Objects.equals(order.getOrderStatus(), OrderStatusEnum.PENDING_PAYMENT.getCode())).count()),
            new StatusDistributionVO("已支付", orders.stream().filter(order -> Objects.equals(order.getOrderStatus(), OrderStatusEnum.PAID.getCode())).count()),
            new StatusDistributionVO("备货中", orders.stream().filter(order -> Objects.equals(order.getOrderStatus(), OrderStatusEnum.PREPARING.getCode())).count()),
            new StatusDistributionVO("配送中", orders.stream().filter(order -> Objects.equals(order.getOrderStatus(), OrderStatusEnum.SHIPPING.getCode())).count()),
            new StatusDistributionVO("已完成", orders.stream().filter(order -> Objects.equals(order.getOrderStatus(), OrderStatusEnum.COMPLETED.getCode())).count()),
            new StatusDistributionVO("已取消", orders.stream().filter(order -> Objects.equals(order.getOrderStatus(), OrderStatusEnum.CANCELLED.getCode())).count())
        );
        List<CategoryDistributionVO> categoryDistribution = products.stream()
            .filter(item -> item.getCategoryId() != null)
            .collect(Collectors.groupingBy(ProductEntity::getCategoryId, Collectors.counting()))
            .entrySet()
            .stream()
            .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
            .map(entry -> new CategoryDistributionVO(categoryNameMap.getOrDefault(entry.getKey(), "未分类"), entry.getValue()))
            .toList();
        List<MerchantRevenueRankingVO> merchantRevenueRanking = orders.stream()
            .filter(order -> order.getMerchantId() != null)
            .filter(order -> order.getPayTime() != null)
            .filter(order -> !Objects.equals(order.getOrderStatus(), OrderStatusEnum.CANCELLED.getCode()))
            .collect(Collectors.groupingBy(OrderInfoEntity::getMerchantId))
            .entrySet()
            .stream()
            .map(entry -> {
                BigDecimal revenue = entry.getValue().stream()
                    .map(OrderInfoEntity::getPayAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                return new MerchantRevenueRankingVO(entry.getKey(), merchantNameMap.getOrDefault(entry.getKey(), "未命名店铺"), revenue, (long) entry.getValue().size());
            })
            .sorted(Comparator.comparing(MerchantRevenueRankingVO::revenue).reversed())
            .limit(6)
            .toList();
        return new DashboardVO(
            totalUsers,
            totalMerchants,
            totalProducts,
            todayOrders,
            todayRevenue,
            monthlyRevenue,
            newUsersToday,
            orderTrend7Days,
            revenueTrend30Days,
            orderStatusDistribution,
            categoryDistribution,
            merchantRevenueRanking
        );
    }

    public PageResult<MerchantItemVO> merchants(PageQuery pageQuery, Integer auditStatus) {
        Page<UserEntity> page = userService.page(new Page<>(pageQuery.current(), pageQuery.size()),
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getRole, UserRoleEnum.MERCHANT.getCode())
                .eq(auditStatus != null, UserEntity::getStatus, auditStatus)
                .orderByDesc(UserEntity::getCreateTime));
        List<MerchantItemVO> records = page.getRecords().stream()
            .map(item -> new MerchantItemVO(item.getId(), item.getUsername(), item.getNickname(), item.getShopName(), item.getPhone(), item.getStatus(), item.getAuditRemark(), item.getCreateTime()))
            .toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditMerchant(Long id, MerchantAuditRequest request) {
        UserEntity merchant = loadMerchant(id);
        merchant.setStatus(request.auditStatus());
        merchant.setAuditRemark(request.auditRemark());
        userService.updateById(merchant);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMerchantStatus(Long id, Integer status) {
        UserEntity merchant = loadMerchant(id);
        merchant.setStatus(status);
        userService.updateById(merchant);
    }

    public PageResult<UserItemVO> users(PageQuery pageQuery, String keyword, Integer status) {
        Page<UserEntity> page = userService.page(new Page<>(pageQuery.current(), pageQuery.size()),
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getRole, UserRoleEnum.CUSTOMER.getCode())
                .eq(status != null, UserEntity::getStatus, status)
                .and(StringUtils.isNotBlank(keyword), wrapper -> wrapper.like(UserEntity::getUsername, keyword).or().like(UserEntity::getNickname, keyword).or().like(UserEntity::getPhone, keyword))
                .orderByDesc(UserEntity::getCreateTime));
        List<UserItemVO> records = page.getRecords().stream()
            .map(item -> new UserItemVO(item.getId(), item.getUsername(), item.getNickname(), item.getRole(), item.getPhone(), item.getStatus(), item.getCreateTime()))
            .toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long id, Integer status) {
        UserEntity user = userService.getById(id);
        if (user == null || !Objects.equals(user.getRole(), UserRoleEnum.CUSTOMER.getCode())) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        userService.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        UserEntity user = userService.getById(id);
        if (user == null || !Objects.equals(user.getRole(), UserRoleEnum.CUSTOMER.getCode())) {
            throw new BusinessException("用户不存在");
        }
        userService.removeById(id);
    }

    private UserEntity loadMerchant(Long id) {
        UserEntity merchant = userService.getById(id);
        if (merchant == null || !Objects.equals(merchant.getRole(), UserRoleEnum.MERCHANT.getCode())) {
            throw new BusinessException("商家不存在");
        }
        return merchant;
    }

    private BigDecimal sumRevenue(List<OrderInfoEntity> orders, LocalDateTime start, LocalDateTime end) {
        return orders.stream()
            .filter(order -> order.getPayTime() != null && !order.getPayTime().isBefore(start) && order.getPayTime().isBefore(end))
            .filter(order -> !Objects.equals(order.getOrderStatus(), OrderStatusEnum.CANCELLED.getCode()))
            .map(OrderInfoEntity::getPayAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
