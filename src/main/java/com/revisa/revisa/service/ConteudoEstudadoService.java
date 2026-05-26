package com.revisa.revisa.service;

import com.revisa.revisa.dto.ConteudoEstudadoRequest;
import com.revisa.revisa.dto.ConteudoEstudadoResponse;
import com.revisa.revisa.dto.EvolucaoRequest;
import com.revisa.revisa.exception.BadRequestException;
import com.revisa.revisa.exception.NotFoundException;
import com.revisa.revisa.model.ConteudoEstudado;
import com.revisa.revisa.model.Materia;
import com.revisa.revisa.repository.ConteudoEstudadoRepository;
import com.revisa.revisa.repository.MateriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConteudoEstudadoService {

    private static final Logger logger = LoggerFactory.getLogger(ConteudoEstudadoService.class);
    private static final ZoneId APP_ZONE = ZoneId.of("America/Sao_Paulo");

    private final ConteudoEstudadoRepository conteudoRepository;
    private final MateriaRepository materiaRepository;

    public ConteudoEstudadoService(
            ConteudoEstudadoRepository conteudoRepository,
            MateriaRepository materiaRepository
    ) {
        this.conteudoRepository = conteudoRepository;
        this.materiaRepository = materiaRepository;
    }

    @Transactional
    public ConteudoEstudadoResponse criar(String email, Long materiaId, ConteudoEstudadoRequest request) {
        validarDataEstudo(request.getDataEstudo());

        Materia materia = buscarMateriaDoUsuario(email, materiaId);

        ConteudoEstudado conteudo = new ConteudoEstudado();
        conteudo.setTitulo(request.getTitulo());
        conteudo.setDescricao(request.getDescricao());
        conteudo.setDataEstudo(request.getDataEstudo());
        conteudo.setProximaRevisao(request.getDataEstudo().plusDays(1));
        conteudo.setQuantidadeRevisoes(0);
        conteudo.setMateria(materia);

        ConteudoEstudado salvo = conteudoRepository.save(conteudo);
        logger.info("Conteúdo criado: email={}, materiaId={}, conteudoId={}", email, materiaId, salvo.getId());

        return ConteudoEstudadoResponse.fromEntity(salvo);
    }

    public List<ConteudoEstudadoResponse> listarPorMateria(String email, Long materiaId) {
        buscarMateriaDoUsuario(email, materiaId);

        return conteudoRepository.findAllByMateriaIdAndMateriaUserEmail(materiaId, email)
                .stream()
                .map(ConteudoEstudadoResponse::fromEntity)
                .toList();
    }

    public Page<ConteudoEstudadoResponse> listarPorMateriaPaginado(
            String email,
            Long materiaId,
            Pageable pageable
    ) {
        buscarMateriaDoUsuario(email, materiaId);

        return conteudoRepository.findAllByMateriaIdAndMateriaUserEmail(materiaId, email, pageable)
                .map(ConteudoEstudadoResponse::fromEntity);
    }

    public ConteudoEstudadoResponse buscarPorId(String email, Long id) {
        ConteudoEstudado conteudo = buscarConteudoDoUsuario(email, id);

        return ConteudoEstudadoResponse.fromEntity(conteudo);
    }

    @Transactional
    public ConteudoEstudadoResponse atualizar(String email, Long id, ConteudoEstudadoRequest request) {
        validarDataEstudo(request.getDataEstudo());

        ConteudoEstudado conteudo = buscarConteudoDoUsuario(email, id);

        conteudo.setTitulo(request.getTitulo());
        conteudo.setDescricao(request.getDescricao());
        conteudo.setDataEstudo(request.getDataEstudo());

        ConteudoEstudado salvo = conteudoRepository.save(conteudo);
        logger.info("Conteúdo atualizado: email={}, conteudoId={}", email, salvo.getId());

        return ConteudoEstudadoResponse.fromEntity(salvo);
    }

    @Transactional
    public ConteudoEstudadoResponse revisar(String email, Long id) {
        ConteudoEstudado conteudo = buscarConteudoDoUsuario(email, id);

        int revisoesAtuais = conteudo.getQuantidadeRevisoes();
        int novaQuantidade = revisoesAtuais + 1;

        conteudo.setQuantidadeRevisoes(novaQuantidade);
        conteudo.setProximaRevisao(calcularProximaRevisao(novaQuantidade, conteudo.getNivelDominio()));

        ConteudoEstudado salvo = conteudoRepository.save(conteudo);
        logger.info(
                "Revisão registrada: email={}, conteudoId={}, quantidadeRevisoes={}, proximaRevisao={}",
                email,
                salvo.getId(),
                salvo.getQuantidadeRevisoes(),
                salvo.getProximaRevisao()
        );

        return ConteudoEstudadoResponse.fromEntity(salvo);
    }

    @Transactional
    public void deletar(String email, Long id) {
        ConteudoEstudado conteudo = buscarConteudoDoUsuario(email, id);

        conteudoRepository.delete(conteudo);
        logger.info("Conteúdo deletado: email={}, conteudoId={}", email, id);
    }

    public List<ConteudoEstudadoResponse> listarPendentes(String email) {
        return conteudoRepository
                .findAllByMateriaUserEmailAndProximaRevisaoLessThanEqualAndConcluido(
                        email,
                        LocalDate.now(),
                        false
                )
                .stream()
                .map(ConteudoEstudadoResponse::fromEntity)
                .toList();
    }

    public List<ConteudoEstudadoResponse> listarTodos(String email) {
        return conteudoRepository.findAllByMateriaUserEmail(email)
                .stream()
                .map(ConteudoEstudadoResponse::fromEntity)
                .toList();
    }

    public Page<ConteudoEstudadoResponse> listarTodosPaginado(String email, Pageable pageable) {
        return conteudoRepository.findAllByMateriaUserEmail(email, pageable)
                .map(ConteudoEstudadoResponse::fromEntity);
    }

    @Transactional
    public ConteudoEstudadoResponse concluir(String email, Long id) {
        ConteudoEstudado conteudo = buscarConteudoDoUsuario(email, id);

        conteudo.setConcluido(true);

        ConteudoEstudado salvo = conteudoRepository.save(conteudo);
        logger.info("Conteúdo concluído: email={}, conteudoId={}", email, salvo.getId());

        return ConteudoEstudadoResponse.fromEntity(salvo);
    }

    @Transactional
    public ConteudoEstudadoResponse reativar(String email, Long id) {
        ConteudoEstudado conteudo = buscarConteudoDoUsuario(email, id);

        conteudo.setConcluido(false);

        ConteudoEstudado salvo = conteudoRepository.save(conteudo);
        logger.info("Conteúdo reativado: email={}, conteudoId={}", email, salvo.getId());

        return ConteudoEstudadoResponse.fromEntity(salvo);
    }

    private Materia buscarMateriaDoUsuario(String email, Long materiaId) {
        return materiaRepository.findByIdAndUserEmail(materiaId, email)
                .orElseThrow(() -> new NotFoundException("Matéria não encontrada"));
    }

    private ConteudoEstudado buscarConteudoDoUsuario(String email, Long id) {
        return conteudoRepository.findByIdAndMateriaUserEmail(id, email)
                .orElseThrow(() -> new NotFoundException("Conteúdo estudado não encontrado"));
    }

    public List<ConteudoEstudadoResponse> listarConcluidos(String email) {
        return conteudoRepository.findAllByMateriaUserEmailAndConcluido(email, true)
                .stream()
                .map(ConteudoEstudadoResponse::fromEntity)
                .toList();
    }

    public List<ConteudoEstudadoResponse> listarAtivos(String email) {
        return conteudoRepository.findAllByMateriaUserEmailAndConcluido(email, false)
                .stream()
                .map(ConteudoEstudadoResponse::fromEntity)
                .toList();
    }

    public List<ConteudoEstudadoResponse> listarPorDominio(String email, Integer nivel) {
        validarNivelDominio(nivel);

        return conteudoRepository.findAllByMateriaUserEmailAndNivelDominio(email, nivel)
                .stream()
                .map(ConteudoEstudadoResponse::fromEntity)
                .toList();
    }

    public List<ConteudoEstudadoResponse> listarPorPeriodo(String email, LocalDate inicio, LocalDate fim) {
        if (inicio.isAfter(fim)) {
            throw new BadRequestException("Data inicial deve ser anterior ou igual à data final");
        }

        return conteudoRepository.findAllByMateriaUserEmailAndDataEstudoBetween(email, inicio, fim)
                .stream()
                .map(ConteudoEstudadoResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ConteudoEstudadoResponse registrarEvolucao(
            String email,
            Long id,
            EvolucaoRequest request
    ) {
        ConteudoEstudado conteudo = buscarConteudoDoUsuario(email, id);

        conteudo.setNivelDominio(request.getNivelDominio());
        conteudo.setObservacaoEvolucao(request.getObservacaoEvolucao());

        ConteudoEstudado salvo = conteudoRepository.save(conteudo);
        logger.info(
                "Evolução registrada: email={}, conteudoId={}, nivelDominio={}",
                email,
                salvo.getId(),
                salvo.getNivelDominio()
        );

        return ConteudoEstudadoResponse.fromEntity(salvo);
    }

    private void validarNivelDominio(Integer nivel) {
        if (nivel == null || nivel < 1 || nivel > 5) {
            throw new BadRequestException("Nível de domínio deve estar entre 1 e 5");
        }
    }

    private void validarDataEstudo(LocalDate dataEstudo) {
        if (dataEstudo != null && dataEstudo.isAfter(LocalDate.now(APP_ZONE))) {
            throw new BadRequestException("Data de estudo não pode ser futura");
        }
    }

    private LocalDate calcularProximaRevisao(int quantidadeRevisoes, int nivelDominio) {
        LocalDate hoje = LocalDate.now();
        int intervaloBase;

        if (quantidadeRevisoes == 1) {
            intervaloBase = 3;
        } else if (quantidadeRevisoes == 2) {
            intervaloBase = 7;
        } else if (quantidadeRevisoes == 3) {
            intervaloBase = 15;
        } else {
            intervaloBase = 30;
        }

        int intervaloAjustado = switch (nivelDominio) {
            case 1 -> Math.max(1, intervaloBase / 2);
            case 2 -> Math.max(1, intervaloBase - 2);
            case 4 -> intervaloBase + Math.max(1, intervaloBase / 2);
            case 5 -> intervaloBase * 2;
            default -> intervaloBase;
        };

        return hoje.plusDays(intervaloAjustado);
    }
}
