package com.flowershop.modules.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flowershop.modules.cart.entity.CartItemEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItemEntity> {

    @Insert("""
        INSERT INTO cart_item(user_id, product_id, quantity, selected, create_time, update_time)
        VALUES(#{userId}, #{productId}, #{quantity}, 1, NOW(), NOW())
        ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity), selected = 1, update_time = NOW()
        """)
    int upsert(@Param("userId") Long userId, @Param("productId") Long productId, @Param("quantity") Integer quantity);
}
