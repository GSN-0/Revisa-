package com.revisa.revisa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para registrar evolução de domínio de um conteúdo")
public class EvolucaoRequest {

    @Schema(example = "4", minimum = "1", maximum = "5")
    @NotNull(message = "Nível de domínio é obrigatório")
    @Min(value = 1, message = "Nível de domínio deve ser no mínimo 1")
    @Max(value = 5, message = "Nível de domínio deve ser no máximo 5")
    private Integer nivelDominio;

    @Schema(example = "Entendi autenticação e filtros, mas ainda preciso revisar permissões.")
    @Size(max = 1000, message = "Observação deve ter no máximo 1000 caracteres")
    private String observacaoEvolucao;

    public Integer getNivelDominio() {
        return nivelDominio;
    }

    public String getObservacaoEvolucao() {
        return observacaoEvolucao;
    }

    public void setNivelDominio(Integer nivelDominio) {
        this.nivelDominio = nivelDominio;
    }

    public void setObservacaoEvolucao(String observacaoEvolucao) {
        this.observacaoEvolucao = observacaoEvolucao;
    }
}
