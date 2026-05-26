package com.revisa.revisa.dto;

import com.revisa.revisa.model.User;

public class UserResponse {

    private Long id;
    private String nome;
    private String email;
    private String role;

    public UserResponse(Long id, String nome, String email, String role) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.role = role;
    }

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : "USER"
        );
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
