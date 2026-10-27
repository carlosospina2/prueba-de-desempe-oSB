package com.helpflow.support.infrastructure.adapter.out.persistence.entity;

import com.helpflow.support.domain.model.enums.Role;
import com.helpflow.support.domain.model.enums.UserStatus;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String document;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String sede;

    @Column(nullable = false, length = 100)
    private String area;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<TicketEntity> tickets;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getSede() { return sede; }
    public void setSede(String sede) { this.sede = sede; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public List<TicketEntity> getTickets() { return tickets; }
    public void setTickets(List<TicketEntity> tickets) { this.tickets = tickets; }
}
