package com.helpflow.support.infrastructure.adapter.out.persistence.mapper;

import com.helpflow.support.domain.model.TicketHistory;
import com.helpflow.support.infrastructure.adapter.out.persistence.entity.TicketHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketHistoryMapper {
    @Mapping(source = "ticket.id", target = "ticketId")
    TicketHistory toDomain(TicketHistoryEntity entity);

    @Mapping(target = "ticket", ignore = true)
    TicketHistoryEntity toEntity(TicketHistory domain);
}
