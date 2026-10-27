package com.helpflow.support.infrastructure.adapter.out.persistence.mapper;

import com.helpflow.support.domain.model.Ticket;
import com.helpflow.support.infrastructure.adapter.out.persistence.entity.TicketEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "technician.id", target = "assignedTechnicianId")
    Ticket toDomain(TicketEntity entity);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "technician", ignore = true)
    @Mapping(target = "history", ignore = true)
    TicketEntity toEntity(Ticket domain);
}
