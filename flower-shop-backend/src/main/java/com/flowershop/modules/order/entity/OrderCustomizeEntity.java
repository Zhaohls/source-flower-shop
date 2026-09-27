package com.flowershop.modules.order.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("order_customize")
public class OrderCustomizeEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderItemId;

    private Long cardTemplateId;

    private String cardContent;

    private String wrappingType;

    private String ribbonColor;

    private String extraNote;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
