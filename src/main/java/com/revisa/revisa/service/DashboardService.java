package com.revisa.revisa.service;

import com.revisa.revisa.dto.DashboardResponse;
import com.revisa.revisa.model.ConteudoEstudado;
import com.revisa.revisa.repository.ConteudoEstudadoRepository;
import com.revisa.revisa.repository.MateriaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private final MateriaRepository materiaRepository;
    private final ConteudoEstudadoRepository conteudoRepository;

    public DashboardService(
            MateriaRepository materiaRepository,
            ConteudoEstudadoRepository conteudoRepository
    ) {
        this.materiaRepository = materiaRepository;
        this.conteudoRepository = conteudoRepository;
    }

    public DashboardResponse buscarResumo(String email) {
        Long totalMaterias = materiaRepository.countByUserEmail(email);

        Long totalConteudos = conteudoRepository.countByMateriaUserEmail(email);

        Long conteudosPendentes = conteudoRepository
                .countByMateriaUserEmailAndProximaRevisaoLessThanEqualAndConcluido(
                        email,
                        LocalDate.now(),
                        false
                );

        Long conteudosRevisados = conteudoRepository
                .countByMateriaUserEmailAndQuantidadeRevisoesGreaterThan(email, 0);

        Long conteudosConcluidos = conteudoRepository
                .countByMateriaUserEmailAndConcluido(email, true);

        Long conteudosAtivos = conteudoRepository
                .countByMateriaUserEmailAndConcluido(email, false);

        List<ConteudoEstudado> conteudos = conteudoRepository.findAllByMateriaUserEmail(email);

        Double mediaDominio = conteudos.isEmpty()
                ? 0.0
                : conteudos.stream()
                  .mapToInt(ConteudoEstudado::getNivelDominio)
                  .average()
                  .orElse(0.0);

        return new DashboardResponse(
                totalMaterias,
                totalConteudos,
                conteudosPendentes,
                conteudosRevisados,
                conteudosConcluidos,
                conteudosAtivos,
                mediaDominio
        );
    }
}
