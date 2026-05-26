package com.revisa.revisa.dto;

public class MateriaResumoResponse {

    private String materia;
    private Long totalConteudos;
    private Long pendentes;
    private Long concluidos;
    private Double mediaDominio;

    public MateriaResumoResponse(
            String materia,
            Long totalConteudos,
            Long pendentes,
            Long concluidos,
            Double mediaDominio
    ) {
        this.materia = materia;
        this.totalConteudos = totalConteudos;
        this.pendentes = pendentes;
        this.concluidos = concluidos;
        this.mediaDominio = mediaDominio;
    }

    public String getMateria() {
        return materia;
    }

    public Long getTotalConteudos() {
        return totalConteudos;
    }

    public Long getPendentes() {
        return pendentes;
    }

    public Long getConcluidos() {
        return concluidos;
    }

    public Double getMediaDominio() {
        return mediaDominio;
    }
}
