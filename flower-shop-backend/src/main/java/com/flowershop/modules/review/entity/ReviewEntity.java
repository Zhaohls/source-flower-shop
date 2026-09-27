package com.flowershop.modules.review.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("review")
public class ReviewEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderItemId;

    private Long userId;

    private Long productId;

    private Integer rating;

    private String content;

    private String images;

    private String replyContent;

    private LocalDateTime replyTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
