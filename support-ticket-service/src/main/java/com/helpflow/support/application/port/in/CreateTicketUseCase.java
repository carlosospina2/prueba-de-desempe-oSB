package com.helpflow.support.application.port.in;

import com.helpflow.support.domain.model.Ticket;
import com.helpflow.support.domain.model.enums.TicketCategory;
import com.helpflow.support.domain.model.enums.TicketPriority;

public interface CreateTicketUseCase {
    Ticket create(Long userId, String title, String description,
                  TicketCategory category, TicketPriority priority,
                  String sede, String affectedAsset);
}
