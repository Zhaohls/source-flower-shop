package com.flowershop.modules.address.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.flowershop.common.context.UserContextHolder;
import com.flowershop.common.exception.BusinessException;
import com.flowershop.modules.address.dto.AddressModels.AddressSaveRequest;
import com.flowershop.modules.address.dto.AddressModels.AddressVO;
import com.flowershop.modules.address.entity.AddressEntity;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressAppService {

    private final AddressService addressService;

    public List<AddressVO> list() {
        return addressService.lambdaQuery()
            .eq(AddressEntity::getUserId, UserContextHolder.getUserId())
            .orderByDesc(AddressEntity::getIsDefault, AddressEntity::getUpdateTime)
            .list()
            .stream()
            .map(this::toVO)
            .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(AddressSaveRequest request) {
        long count = addressService.lambdaQuery().eq(AddressEntity::getUserId, UserContextHolder.getUserId()).count();
        if (count >= 20) {
            throw new BusinessException("每个用户最多维护20个收货地址");
        }
        AddressEntity addressEntity = new AddressEntity();
        fillAddress(addressEntity, request);
        addressEntity.setUserId(UserContextHolder.getUserId());
        if (Boolean.TRUE.equals(request.isDefault()) || count == 0) {
            clearDefault();
            addressEntity.setIsDefault(1);
        } else {
            addressEntity.setIsDefault(0);
        }
        addressService.save(addressEntity);
        return addressEntity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, AddressSaveRequest request) {
        AddressEntity addressEntity = loadAddress(id);
        fillAddress(addressEntity, request);
        if (Boolean.TRUE.equals(request.isDefault())) {
            clearDefault();
            addressEntity.setIsDefault(1);
        }
        addressService.updateById(addressEntity);
    }

    public void delete(Long id) {
        AddressEntity addressEntity = loadAddress(id);
        addressService.removeById(addressEntity.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id) {
        AddressEntity addressEntity = loadAddress(id);
        clearDefault();
        addressEntity.setIsDefault(1);
        addressService.updateById(addressEntity);
    }

    public AddressEntity getCurrentUserAddress(Long id) {
        return loadAddress(id);
    }

    private void clearDefault() {
        addressService.update(new LambdaUpdateWrapper<AddressEntity>()
            .eq(AddressEntity::getUserId, UserContextHolder.getUserId())
            .set(AddressEntity::getIsDefault, 0));
    }

    private AddressEntity loadAddress(Long id) {
        AddressEntity addressEntity = addressService.getById(id);
        if (addressEntity == null || !Objects.equals(addressEntity.getUserId(), UserContextHolder.getUserId())) {
            throw new BusinessException("地址不存在");
        }
        return addressEntity;
    }

    private void fillAddress(AddressEntity addressEntity, AddressSaveRequest request) {
        addressEntity.setReceiverName(StringUtils.trim(request.receiverName()));
        addressEntity.setReceiverPhone(StringUtils.trim(request.receiverPhone()));
        addressEntity.setProvince(StringUtils.trimToEmpty(request.province()));
        addressEntity.setCity(StringUtils.trimToEmpty(request.city()));
        addressEntity.setDistrict(StringUtils.trimToEmpty(request.district()));
        addressEntity.setDetailAddress(StringUtils.trim(request.detailAddress()));
    }

    private AddressVO toVO(AddressEntity item) {
        return new AddressVO(
            item.getId(),
            item.getReceiverName(),
            item.getReceiverPhone(),
            StringUtils.defaultString(item.getProvince()),
            StringUtils.defaultString(item.getCity()),
            StringUtils.defaultString(item.getDistrict()),
            StringUtils.defaultString(item.getDetailAddress()),
            Objects.equals(item.getIsDefault(), 1)
        );
    }
}
