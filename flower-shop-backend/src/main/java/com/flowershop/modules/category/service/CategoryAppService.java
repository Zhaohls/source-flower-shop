package com.flowershop.modules.category.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flowershop.common.exception.BusinessException;
import com.flowershop.modules.category.dto.CategoryModels.CategorySaveRequest;
import com.flowershop.modules.category.dto.CategoryModels.CategoryVO;
import com.flowershop.modules.category.entity.CategoryEntity;
import com.flowershop.modules.product.entity.ProductEntity;
import com.flowershop.modules.product.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryAppService {

    private final CategoryService categoryService;
    private final ProductService productService;

    public List<CategoryVO> list() {
        return categoryService.lambdaQuery()
            .orderByAsc(CategoryEntity::getSort, CategoryEntity::getId)
            .list()
            .stream()
            .map(item -> new CategoryVO(item.getId(), item.getCategoryName(), item.getCategoryIcon(), item.getSort(), item.getStatus()))
            .toList();
    }

    public Long create(CategorySaveRequest request) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setCategoryName(request.categoryName());
        categoryEntity.setCategoryIcon(request.categoryIcon());
        categoryEntity.setSort(request.sort());
        categoryEntity.setStatus(request.status());
        categoryService.save(categoryEntity);
        return categoryEntity.getId();
    }

    public void update(Long id, CategorySaveRequest request) {
        CategoryEntity categoryEntity = categoryService.getById(id);
        if (categoryEntity == null) {
            throw new BusinessException("分类不存在");
        }
        categoryEntity.setCategoryName(request.categoryName());
        categoryEntity.setCategoryIcon(request.categoryIcon());
        categoryEntity.setSort(request.sort());
        categoryEntity.setStatus(request.status());
        categoryService.updateById(categoryEntity);
    }

    public void delete(Long id) {
        long productCount = productService.count(new LambdaQueryWrapper<ProductEntity>().eq(ProductEntity::getCategoryId, id));
        if (productCount > 0) {
            throw new BusinessException("当前分类下存在商品，禁止删除");
        }
        categoryService.removeById(id);
    }
}
