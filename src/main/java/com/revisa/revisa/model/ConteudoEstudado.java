package com.revisa.revisa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@Entity
@Table(name = "conteudos_estudados")
public class ConteudoEstudado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Título é obrigatório")
    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 1000)
    private String descricao;

    @Column(nullable = false)
    private LocalDate dataEstudo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    @Column(nullable = false)
    private LocalDate proximaRevisao;

    @Column(nullable = false)
    private Integer quantidadeRevisoes = 0;

    @Column(nullable = false)
    private Integer nivelDominio = 1;

    @Column(length = 1000)
    private String observacaoEvolucao;

    @Column(nullable = false)
    private Boolean concluido = false;

    public void setConcluido(Boolean concluido) {
        this.concluido = concluido;
    }

    public Boolean getConcluido() {
        return concluido;
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

    public Materia getMateria() {
        return materia;
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

    public void setMateria(Materia materia) {
        this.materia = materia;
    }

    public LocalDate getProximaRevisao() {
        return proximaRevisao;
    }

    public Integer getQuantidadeRevisoes() {
        return quantidadeRevisoes;
    }

    public void setProximaRevisao(LocalDate proximaRevisao) {
        this.proximaRevisao = proximaRevisao;
    }

    public void setQuantidadeRevisoes(Integer quantidadeRevisoes) {this.quantidadeRevisoes = quantidadeRevisoes;
    }

    public void setNivelDominio(Integer nivelDominio) {
        this.nivelDominio = nivelDominio;
    }

    public Integer getNivelDominio() {
        return nivelDominio;
    }

    public String getObservacaoEvolucao() {
        return observacaoEvolucao;
    }

    public void setObservacaoEvolucao(String observacaoEvolucao) {
        this.observacaoEvolucao = observacaoEvolucao;
    }
}
