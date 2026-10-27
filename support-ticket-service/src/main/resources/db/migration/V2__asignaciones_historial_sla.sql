CREATE TABLE ticket_history (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES tickets(id),
    previous_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    observation VARCHAR(500),
    changed_by VARCHAR(150),
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_ticket_history_ticket_id ON ticket_history(ticket_id);
CREATE INDEX idx_tickets_user_id ON tickets(user_id);
CREATE INDEX idx_tickets_technician_id ON tickets(technician_id);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_sla_deadline ON tickets(sla_deadline);
