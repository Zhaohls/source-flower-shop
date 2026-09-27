package com.flowershop.modules.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("card_template")
public class CardTemplateEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String templateName;

    private String templateImage;

    private String sceneType;

    private Integer sort;

    private Integer status;
}
