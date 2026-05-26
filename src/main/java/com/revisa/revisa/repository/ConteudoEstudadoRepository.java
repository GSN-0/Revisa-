package com.revisa.revisa.repository;

import com.revisa.revisa.model.ConteudoEstudado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConteudoEstudadoRepository extends JpaRepository<ConteudoEstudado, Long> {

    @EntityGraph(attributePaths = "materia")
    List<ConteudoEstudado> findAllByMateriaUserEmail(String email);

    @EntityGraph(attributePaths = "materia")
    Page<ConteudoEstudado> findAllByMateriaUserEmail(String email, Pageable pageable);

    @EntityGraph(attributePaths = "materia")
    List<ConteudoEstudado> findAllByMateriaUserEmailAndConcluido(
            String email,
            Boolean concluido
    );

    @EntityGraph(attributePaths = "materia")
    List<ConteudoEstudado> findAllByMateriaIdAndMateriaUserEmail(Long materiaId, String email);

    @EntityGraph(attributePaths = "materia")
    Page<ConteudoEstudado> findAllByMateriaIdAndMateriaUserEmail(
            Long materiaId,
            String email,
            Pageable pageable
    );

    Long countByMateriaUserEmail(String email);

    Long countByMateriaUserEmailAndProximaRevisaoLessThanEqual(String email, LocalDate data);

    Long countByMateriaUserEmailAndProximaRevisaoLessThanEqualAndConcluido(
            String email,
            LocalDate data,
            Boolean concluido
    );

    Long countByMateriaUserEmailAndQuantidadeRevisoesGreaterThan(String email, Integer quantidade);

    Long countByMateriaUserEmailAndConcluido(String email, Boolean concluido);

    Long countByMateriaIdAndMateriaUserEmail(Long materiaId, String email);

    Long countByMateriaIdAndMateriaUserEmailAndConcluido(Long materiaId, String email, Boolean concluido);

    Long countByMateriaIdAndMateriaUserEmailAndProximaRevisaoLessThanEqualAndConcluido(
            Long materiaId,
            String email,
            LocalDate data,
            Boolean concluido
    );

    @EntityGraph(attributePaths = "materia")
    Optional<ConteudoEstudado> findByIdAndMateriaUserEmail(Long id, String email);

    @EntityGraph(attributePaths = "materia")
    List<ConteudoEstudado> findAllByMateriaUserEmailAndProximaRevisaoLessThanEqual(
            String email,
            LocalDate data
    );

    @EntityGraph(attributePaths = "materia")
    List<ConteudoEstudado> findAllByMateriaUserEmailAndProximaRevisaoLessThanEqualAndConcluido(
            String email,
            LocalDate data,
            Boolean concluido
    );

    @EntityGraph(attributePaths = "materia")
    List<ConteudoEstudado> findAllByMateriaUserEmailAndNivelDominio(
            String email,
            Integer nivelDominio
    );

    @EntityGraph(attributePaths = "materia")
    List<ConteudoEstudado> findAllByMateriaUserEmailAndDataEstudoBetween(
            String email,
            LocalDate inicio,
            LocalDate fim
    );

}
