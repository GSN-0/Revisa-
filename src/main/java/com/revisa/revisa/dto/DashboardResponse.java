package com.revisa.revisa.dto;

public class DashboardResponse {

    private Long totalMaterias;
    private Long totalConteudos;
    private Long conteudosPendentes;
    private Long conteudosRevisados;
    private Long conteudosConcluidos;
    private Long conteudosAtivos;
    private Double mediaDominio;

    public DashboardResponse(
            Long totalMaterias,
            Long totalConteudos,
            Long conteudosPendentes,
            Long conteudosRevisados,
            Long conteudosConcluidos,
            Long conteudosAtivos,
            Double mediaDominio
    ) {
        this.totalMaterias = totalMaterias;
        this.totalConteudos = totalConteudos;
        this.conteudosPendentes = conteudosPendentes;
        this.conteudosRevisados = conteudosRevisados;
        this.conteudosConcluidos = conteudosConcluidos;
        this.conteudosAtivos = conteudosAtivos;
        this.mediaDominio = mediaDominio;
    }

    public Long getTotalMaterias() {
        return totalMaterias;
    }

    public Long getTotalConteudos() {
        return totalConteudos;
    }

    public Long getConteudosPendentes() {
        return conteudosPendentes;
    }

    public Long getConteudosRevisados() {
        return conteudosRevisados;
    }

    public Long getConteudosConcluidos() {
        return conteudosConcluidos;
    }

    public Long getConteudosAtivos() {
        return conteudosAtivos;
    }

    public Double getMediaDominio() {
        return mediaDominio;
    }
}
