package com.revisa.revisa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criar ou atualizar uma matéria")
public class MateriaRequest {

    @Schema(example = "Java")
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @Schema(example = "Estudos de Java, Spring Boot e APIs REST")
    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String descricao;

    @Schema(example = "#2563EB")
    @Size(max = 20, message = "Cor deve ter no máximo 20 caracteres")
    @Pattern(
            regexp = "^#([A-Fa-f0-9]{6})$",
            message = "Cor deve estar no formato hexadecimal. Ex: #FF0000"
    )
    private String cor;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }
}
