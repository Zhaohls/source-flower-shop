package com.flowershop.modules.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public final class AddressModels {

    private AddressModels() {
    }

    public record AddressSaveRequest(
        @NotBlank(message = "收货人不能为空") String receiverName,
        @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确") String receiverPhone,
        String province,
        String city,
        String district,
        @NotBlank(message = "详细地址不能为空") String detailAddress,
        @NotNull(message = "默认标识不能为空") Boolean isDefault
    ) {
    }

    public record AddressVO(
        Long id,
        String receiverName,
        String receiverPhone,
        String province,
        String city,
        String district,
        String detailAddress,
        Boolean isDefault
    ) {
    }

    public record AddressListVO(List<AddressVO> items) {
    }
}
