package com.helpflow.support.domain.model;

import com.helpflow.support.domain.model.enums.Role;
import com.helpflow.support.domain.model.enums.UserStatus;

public class User {
    private Long id;
    private String document;
    private String name;
    private String email;
    private String password;
    private String sede;
    private String area;
    private UserStatus status;
    private Role role;

    public User() {}

    public User(Long id, String document, String name, String email,
                String password, String sede, String area, UserStatus status, Role role) {
        this.id = id;
        this.document = document;
        this.name = name;
        this.email = email;
        this.password = password;
        this.sede = sede;
        this.area = area;
        this.status = status;
        this.role = role;
    }

    public boolean isActive() {
        return UserStatus.ACTIVO.equals(this.status);
    }

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
}
