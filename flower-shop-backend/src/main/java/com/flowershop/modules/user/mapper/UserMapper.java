package com.flowershop.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flowershop.modules.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
