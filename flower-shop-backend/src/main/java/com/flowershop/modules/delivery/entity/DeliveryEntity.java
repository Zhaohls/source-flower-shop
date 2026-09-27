package com.flowershop.modules.delivery.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("delivery")
public class DeliveryEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String deliveryMan;

    private String deliveryPhone;

    private Integer deliveryStatus;

    private LocalDateTime shippedTime;

    private LocalDateTime deliveredTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
