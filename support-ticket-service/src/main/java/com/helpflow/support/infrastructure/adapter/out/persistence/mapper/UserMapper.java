package com.helpflow.support.infrastructure.adapter.out.persistence.mapper;

import com.helpflow.support.domain.model.User;
import com.helpflow.support.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toDomain(UserEntity entity);

    // tickets is a JPA relationship on the entity with no domain counterpart; ignore it.
    @Mapping(target = "tickets", ignore = true)
    UserEntity toEntity(User domain);
}
