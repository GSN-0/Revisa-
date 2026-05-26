package com.revisa.revisa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Dados para criar ou atualizar um conteúdo estudado")
public class ConteudoEstudadoRequest {

    @Schema(example = "Spring Security com JWT")
    @NotBlank(message = "Título é obrigatório")
    @Size(min = 3, max = 150,
            message = "Título deve ter entre 3 e 150 caracteres")
    private String titulo;

    @Schema(example = "Estudei autenticação, filtros e geração de token JWT.")
    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String descricao;

    @Schema(example = "2026-05-05")
    @NotNull(message = "Data de estudo é obrigatória")
    private LocalDate dataEstudo;

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDate getDataEstudo() {
        return dataEstudo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setDataEstudo(LocalDate dataEstudo) {
        this.dataEstudo = dataEstudo;
    }
}
