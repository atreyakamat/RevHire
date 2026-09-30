package com.revhire.userservice.dto.request;

import com.revhire.userservice.entity.Role;
import jakarta.validation.constraints.NotNull;

public class RoleUpdateRequest {

    @NotNull(message = "Role is required")
    private Role role;

    public RoleUpdateRequest() {}

    public RoleUpdateRequest(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
