package com.revisa.revisa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para troca de senha")
public class UpdatePasswordRequest {

    @Schema(example = "123456")
    @NotBlank(message = "Senha atual é obrigatória")
    @Size(max = 72, message = "Senha atual deve ter no máximo 72 caracteres")
    private String senhaAtual;

    @Schema(example = "novaSenha123")
    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 6, max = 72, message = "Nova senha deve ter entre 6 e 72 caracteres")
    private String novaSenha;

    public String getSenhaAtual() {
        return senhaAtual;
    }

    public void setSenhaAtual(String senhaAtual) {
        this.senhaAtual = senhaAtual;
    }

    public String getNovaSenha() {
        return novaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }
}
