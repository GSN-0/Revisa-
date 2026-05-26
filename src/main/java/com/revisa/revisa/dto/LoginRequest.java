package com.revisa.revisa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Credenciais de login")
public class LoginRequest {

    @Schema(example = "admin@gmail.com")
    @Email(message = "Email inválido")
    @NotBlank(message = "Email é obrigatório")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
    private String email;

    @Schema(example = "123456")
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 72, message = "Senha deve ter entre 6 e 72 caracteres")
    private String senha;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
