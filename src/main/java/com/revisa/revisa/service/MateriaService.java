package com.revisa.revisa.service;

import com.revisa.revisa.dto.MateriaRequest;
import com.revisa.revisa.dto.MateriaResponse;
import com.revisa.revisa.dto.MateriaResumoResponse;
import com.revisa.revisa.exception.NotFoundException;
import com.revisa.revisa.model.ConteudoEstudado;
import com.revisa.revisa.model.Materia;
import com.revisa.revisa.model.User;
import com.revisa.revisa.repository.ConteudoEstudadoRepository;
import com.revisa.revisa.repository.MateriaRepository;
import com.revisa.revisa.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MateriaService {

    private static final Logger logger = LoggerFactory.getLogger(MateriaService.class);

    private final MateriaRepository materiaRepository;
    private final UserRepository userRepository;
    private final ConteudoEstudadoRepository conteudoRepository;

    public MateriaService(
            MateriaRepository materiaRepository,
            UserRepository userRepository,
            ConteudoEstudadoRepository conteudoRepository
    ) {
        this.materiaRepository = materiaRepository;
        this.userRepository = userRepository;
        this.conteudoRepository = conteudoRepository;
    }

    public MateriaResponse criar(String email, MateriaRequest request) {
        User user = buscarUsuario(email);

        Materia materia = new Materia();
        materia.setNome(request.getNome());
        materia.setDescricao(request.getDescricao());
        materia.setCor(request.getCor());
        materia.setUser(user);

        Materia salva = materiaRepository.save(materia);
        logger.info("Matéria criada: email={}, materiaId={}", email, salva.getId());

        return MateriaResponse.fromEntity(salva);
    }

    public List<MateriaResponse> listar(String email) {
        return materiaRepository.findAllByUserEmail(email)
                .stream()
                .map(MateriaResponse::fromEntity)
                .toList();
    }

    public Page<MateriaResponse> listarPaginado(String email, Pageable pageable) {
        return materiaRepository.findAllByUserEmail(email, pageable)
                .map(MateriaResponse::fromEntity);
    }

    public MateriaResponse buscarPorId(String email, Long id) {
        Materia materia = buscarMateriaDoUsuario(email, id);

        return MateriaResponse.fromEntity(materia);
    }

    public MateriaResponse atualizar(String email, Long id, MateriaRequest request) {
        Materia materia = buscarMateriaDoUsuario(email, id);

        materia.setNome(request.getNome());
        materia.setDescricao(request.getDescricao());
        materia.setCor(request.getCor());

        Materia salva = materiaRepository.save(materia);
        logger.info("Matéria atualizada: email={}, materiaId={}", email, salva.getId());

        return MateriaResponse.fromEntity(salva);
    }

    public void deletar(String email, Long id) {
        Materia materia = buscarMateriaDoUsuario(email, id);

        materiaRepository.delete(materia);
        logger.info("Matéria deletada: email={}, materiaId={}", email, id);
    }

    public MateriaResumoResponse buscarResumo(String email, Long id) {
        Materia materia = buscarMateriaDoUsuario(email, id);
        List<ConteudoEstudado> conteudos = conteudoRepository.findAllByMateriaIdAndMateriaUserEmail(id, email);

        Long totalConteudos = conteudoRepository.countByMateriaIdAndMateriaUserEmail(id, email);
        Long pendentes = conteudoRepository
                .countByMateriaIdAndMateriaUserEmailAndProximaRevisaoLessThanEqualAndConcluido(
                        id,
                        email,
                        LocalDate.now(),
                        false
                );
        Long concluidos = conteudoRepository
                .countByMateriaIdAndMateriaUserEmailAndConcluido(id, email, true);

        Double mediaDominio = conteudos.isEmpty()
                ? 0.0
                : conteudos.stream()
                .mapToInt(ConteudoEstudado::getNivelDominio)
                .average()
                .orElse(0.0);

        return new MateriaResumoResponse(
                materia.getNome(),
                totalConteudos,
                pendentes,
                concluidos,
                mediaDominio
        );
    }

    private User buscarUsuario(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }

    private Materia buscarMateriaDoUsuario(String email, Long id) {
        return materiaRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new NotFoundException("Matéria não encontrada"));
    }
}
