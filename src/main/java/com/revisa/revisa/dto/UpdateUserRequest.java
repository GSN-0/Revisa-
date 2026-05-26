package com.revisa.revisa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para atualização de perfil")
public class UpdateUserRequest {

    @Schema(example = "Maria Silva")
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
