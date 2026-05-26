package com.revisa.revisa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "materias")
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(length = 20)
    private String cor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    public User getUser() {
        return user;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public void setUser(User user) {
        this.user = user;
    }
}