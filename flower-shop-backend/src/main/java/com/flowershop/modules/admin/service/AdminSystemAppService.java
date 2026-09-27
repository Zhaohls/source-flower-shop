package com.flowershop.modules.admin.service;

import com.flowershop.common.exception.BusinessException;
import com.flowershop.common.model.PageQuery;
import com.flowershop.modules.admin.dto.AdminModels.DictDataVO;
import com.flowershop.modules.admin.dto.AdminModels.DictTypeVO;
import com.flowershop.modules.admin.dto.AdminModels.OperationLogVO;
import com.flowershop.modules.admin.dto.AdminModels.PageResult;
import com.flowershop.modules.admin.entity.OperationLogEntity;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminSystemAppService {

    private static final List<DictTypeVO> DICT_TYPES = List.of(
        new DictTypeVO(1L, "订单状态", "order_status", "订单状态枚举值"),
        new DictTypeVO(2L, "支付方式", "pay_type", "支付方式类型"),
        new DictTypeVO(3L, "配送状态", "delivery_status", "配送状态枚举值")
    );

    private static final Map<Long, List<DictDataVO>> DICT_DATA = Map.of(
        1L, List.of(
            new DictDataVO("待支付", "0", 0),
            new DictDataVO("已支付待接单", "1", 1),
            new DictDataVO("备货中", "2", 2),
            new DictDataVO("配送中", "3", 3),
            new DictDataVO("已完成", "4", 4),
            new DictDataVO("已取消", "5", 5)
        ),
        2L, List.of(
            new DictDataVO("微信支付", "1", 0),
            new DictDataVO("支付宝", "2", 1),
            new DictDataVO("模拟支付", "3", 2)
        ),
        3L, List.of(
            new DictDataVO("待配送", "0", 0),
            new DictDataVO("配送中", "1", 1),
            new DictDataVO("已送达", "2", 2)
        )
    );

    private final OperationLogService operationLogService;

    public PageResult<OperationLogVO> logs(PageQuery pageQuery) {
        Page<OperationLogEntity> page = operationLogService.lambdaQuery()
            .orderByDesc(OperationLogEntity::getId)
            .page(new Page<>(pageQuery.current(), pageQuery.size()));

        List<OperationLogVO> records = page.getRecords()
            .stream()
            .map(item -> new OperationLogVO(
                item.getId(),
                item.getModule(),
                item.getAction(),
                item.getOperator(),
                item.getIp(),
                item.getCreateTime()
            ))
            .toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }

    public void clearLogs() {
        operationLogService.remove(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>());
    }

    public List<DictTypeVO> dictTypes() {
        return DICT_TYPES;
    }

    public List<DictDataVO> dictData(Long dictTypeId) {
        if (!DICT_DATA.containsKey(dictTypeId)) {
            throw new BusinessException("字典类型不存在");
        }
        return DICT_DATA.get(dictTypeId);
    }
}
