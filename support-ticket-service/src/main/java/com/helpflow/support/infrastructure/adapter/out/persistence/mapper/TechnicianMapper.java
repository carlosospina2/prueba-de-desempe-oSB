package com.helpflow.support.infrastructure.adapter.out.persistence.mapper;

import com.helpflow.support.domain.model.Technician;
import com.helpflow.support.infrastructure.adapter.out.persistence.entity.TechnicianEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TechnicianMapper {
    Technician toDomain(TechnicianEntity entity);
    TechnicianEntity toEntity(Technician domain);
}
