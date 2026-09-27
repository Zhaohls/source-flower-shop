package com.flowershop.modules.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flowershop.modules.product.entity.ProductEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<ProductEntity> {

    @Update("""
        UPDATE product
        SET stock = stock - #{quantity}
        WHERE id = #{productId}
          AND stock >= #{quantity}
          AND status = 1
          AND deleted = 0
        """)
    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Update("""
        UPDATE product
        SET stock = stock + #{quantity}
        WHERE id = #{productId}
          AND deleted = 0
        """)
    int rollbackStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Update("""
        UPDATE product
        SET sales = sales + #{quantity}
        WHERE id = #{productId}
          AND deleted = 0
        """)
    int increaseSales(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Update("""
        UPDATE product
        SET sales = CASE WHEN sales >= #{quantity} THEN sales - #{quantity} ELSE 0 END
        WHERE id = #{productId}
          AND deleted = 0
        """)
    int decreaseSales(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
