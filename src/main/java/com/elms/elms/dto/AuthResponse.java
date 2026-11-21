package com.elms.elms.dto;

import com.elms.elms.entity.Role;

// Returned after login. The client then sends the token as:
// Authorization: Bearer <token>
public record AuthResponse(
        String token,
        String username,
        Role role) {
}
