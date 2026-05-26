package com.revisa.revisa.dto;

import com.revisa.revisa.model.Materia;

public class MateriaResponse {

    private Long id;
    private String nome;
    private String descricao;
    private String cor;

    public MateriaResponse() {
    }

    public MateriaResponse(Long id, String nome, String descricao, String cor) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.cor = cor;
    }

    public static MateriaResponse fromEntity(Materia materia) {
        return new MateriaResponse(
                materia.getId(),
                materia.getNome(),
                materia.getDescricao(),
                materia.getCor()
        );
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getCor() {
        return cor;
    }
}