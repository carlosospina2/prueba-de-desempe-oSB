package com.helpflow.support.application.port.in;

import com.helpflow.support.domain.model.User;
import com.helpflow.support.domain.model.enums.Role;

public interface RegisterUserUseCase {
    User register(String document, String name, String email,
                  String password, String sede, String area, Role role);
}
