package com.revisa.revisa.dto;

import com.revisa.revisa.model.ConteudoEstudado;

import java.time.LocalDate;

public class ConteudoEstudadoResponse {

    private Long id;
    private String titulo;
    private String descricao;
    private LocalDate dataEstudo;
    private Long materiaId;
    private String materiaNome;
    private LocalDate proximaRevisao;
    private Integer quantidadeRevisoes;
    private Integer nivelDominio;
    private String observacaoEvolucao;
    private Boolean concluido;

    public ConteudoEstudadoResponse(
            Long id,
            String titulo,
            String descricao,
            LocalDate dataEstudo,
            Long materiaId,
            String materiaNome,
            LocalDate proximaRevisao,
            Integer quantidadeRevisoes,
            Integer nivelDominio,
            String observacaoEvolucao,
            Boolean concluido

    ) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.dataEstudo = dataEstudo;
        this.materiaId = materiaId;
        this.materiaNome = materiaNome;
        this.proximaRevisao = proximaRevisao;
        this.quantidadeRevisoes = quantidadeRevisoes;
        this.nivelDominio = nivelDominio;
        this.observacaoEvolucao = observacaoEvolucao;
        this.concluido = concluido;
    }

    public static ConteudoEstudadoResponse fromEntity(ConteudoEstudado conteudo) {
        return new ConteudoEstudadoResponse(
                conteudo.getId(),
                conteudo.getTitulo(),
                conteudo.getDescricao(),
                conteudo.getDataEstudo(),
                conteudo.getMateria().getId(),
                conteudo.getMateria().getNome(),
                conteudo.getProximaRevisao(),
                conteudo.getQuantidadeRevisoes(),
                conteudo.getNivelDominio(),
                conteudo.getObservacaoEvolucao(),
                conteudo.getConcluido()
        );
    }

    public LocalDate getProximaRevisao() {
        return proximaRevisao;
    }

    public Integer getQuantidadeRevisoes() {
        return quantidadeRevisoes;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDate getDataEstudo() {
        return dataEstudo;
    }

    public Long getMateriaId() {
        return materiaId;
    }

    public String getMateriaNome() {
        return materiaNome;
    }

    public Boolean getConcluido() {
        return concluido;
    }

    public Integer getNivelDominio() {
        return nivelDominio;
    }

    public String getObservacaoEvolucao() {
        return observacaoEvolucao;
    }
}
