package com.helpflow.support.infrastructure.adapter.out.persistence.mapper;

import com.helpflow.support.domain.model.User;
import com.helpflow.support.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toDomain(UserEntity entity);
    UserEntity toEntity(User domain);
}
