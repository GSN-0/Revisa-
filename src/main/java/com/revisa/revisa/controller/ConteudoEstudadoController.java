package com.revisa.revisa.controller;

import com.revisa.revisa.config.OpenApiConfig;
import com.revisa.revisa.dto.ConteudoEstudadoRequest;
import com.revisa.revisa.dto.ConteudoEstudadoResponse;
import com.revisa.revisa.dto.EvolucaoRequest;
import com.revisa.revisa.exception.BadRequestException;
import com.revisa.revisa.service.ConteudoEstudadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@Tag(name = "Conteúdos", description = "Conteúdos estudados, revisões, filtros, conclusão e evolução de domínio")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ConteudoEstudadoController {

    private static final Set<String> SORT_FIELDS = Set.of(
            "id",
            "titulo",
            "dataEstudo",
            "proximaRevisao",
            "quantidadeRevisoes",
            "nivelDominio",
            "concluido"
    );

    private final ConteudoEstudadoService service;

    public ConteudoEstudadoController(ConteudoEstudadoService service) {
        this.service = service;
    }

    @PostMapping("/materias/{materiaId}/conteudos")
    @Operation(summary = "Criar conteúdo estudado", description = "Cria um conteúdo dentro de uma matéria do usuário autenticado.")
    public ResponseEntity<ConteudoEstudadoResponse> criar(
            Authentication authentication,
            @PathVariable Long materiaId,
            @RequestBody @Valid ConteudoEstudadoRequest request
    ) {
        String email = authentication.getName();
        return ResponseEntity.status(201).body(service.criar(email, materiaId, request));
    }

    @PostMapping("/conteudos/{id}/revisar")
    @Operation(summary = "Registrar revisão", description = "Incrementa a quantidade de revisões e calcula a próxima revisão usando o nível de domínio.")
    public ResponseEntity<ConteudoEstudadoResponse> revisar(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(service.revisar(email, id));
    }

    @GetMapping("/materias/{materiaId}/conteudos")
    @Operation(summary = "Listar conteúdos por matéria", description = "Lista conteúdos de uma matéria. Com page/size retorna Page; sem paginação retorna lista simples.")
    public ResponseEntity<?> listarPorMateria(
            Authentication authentication,
            @PathVariable Long materiaId,
            @Parameter(example = "0")
            @RequestParam(required = false) Integer page,
            @Parameter(example = "10")
            @RequestParam(required = false) Integer size,
            @Parameter(example = "proximaRevisao,asc")
            @RequestParam(defaultValue = "proximaRevisao,asc") String sort
    ) {
        String email = authentication.getName();

        if (page != null || size != null) {
            return ResponseEntity.ok(service.listarPorMateriaPaginado(
                    email,
                    materiaId,
                    criarPageable(page, size, sort)
            ));
        }

        return ResponseEntity.ok(service.listarPorMateria(email, materiaId));
    }

    @GetMapping("/conteudos")
    @Operation(summary = "Listar todos os conteúdos", description = "Lista conteúdos do usuário autenticado. Com page/size retorna Page; sem paginação retorna lista simples.")
    public ResponseEntity<?> listarTodos(
            Authentication authentication,
            @Parameter(example = "0")
            @RequestParam(required = false) Integer page,
            @Parameter(example = "10")
            @RequestParam(required = false) Integer size,
            @Parameter(example = "proximaRevisao,asc")
            @RequestParam(defaultValue = "proximaRevisao,asc") String sort
    ) {
        String email = authentication.getName();

        if (page != null || size != null) {
            return ResponseEntity.ok(service.listarTodosPaginado(
                    email,
                    criarPageable(page, size, sort)
            ));
        }

        return ResponseEntity.ok(service.listarTodos(email));
    }

    @GetMapping("/conteudos/{id}")
    @Operation(summary = "Buscar conteúdo por ID", description = "Busca um conteúdo do usuário autenticado.")
    public ResponseEntity<ConteudoEstudadoResponse> buscarPorId(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(service.buscarPorId(email, id));
    }

    @GetMapping("/conteudos/pendentes")
    @Operation(summary = "Listar revisões pendentes", description = "Lista conteúdos ativos com próxima revisão hoje ou atrasada.")
    public ResponseEntity<List<ConteudoEstudadoResponse>> listarPendentes(
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(service.listarPendentes(email));
    }

    @GetMapping("/conteudos/concluidos")
    @Operation(summary = "Listar conteúdos concluídos", description = "Lista conteúdos marcados como concluídos.")
    public ResponseEntity<List<ConteudoEstudadoResponse>> listarConcluidos(
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(service.listarConcluidos(email));
    }

    @GetMapping("/conteudos/ativos")
    @Operation(summary = "Listar conteúdos ativos", description = "Lista conteúdos ainda não concluídos.")
    public ResponseEntity<List<ConteudoEstudadoResponse>> listarAtivos(
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(service.listarAtivos(email));
    }

    @GetMapping("/conteudos/por-dominio")
    @Operation(summary = "Filtrar por domínio", description = "Lista conteúdos pelo nível de domínio informado, entre 1 e 5.")
    public ResponseEntity<List<ConteudoEstudadoResponse>> listarPorDominio(
            Authentication authentication,
            @Parameter(example = "4")
            @RequestParam Integer nivel
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(service.listarPorDominio(email, nivel));
    }

    @GetMapping("/conteudos/periodo")
    @Operation(summary = "Filtrar por período", description = "Lista conteúdos pela data de estudo dentro do intervalo informado.")
    public ResponseEntity<List<ConteudoEstudadoResponse>> listarPorPeriodo(
            Authentication authentication,
            @Parameter(example = "2026-05-01")
            @RequestParam LocalDate inicio,
            @Parameter(example = "2026-05-31")
            @RequestParam LocalDate fim
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(service.listarPorPeriodo(email, inicio, fim));
    }

    @PutMapping("/conteudos/{id}")
    @Operation(summary = "Atualizar conteúdo", description = "Atualiza título, descrição e data de estudo de um conteúdo do usuário autenticado.")
    public ResponseEntity<ConteudoEstudadoResponse> atualizar(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody @Valid ConteudoEstudadoRequest request
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(service.atualizar(email, id, request));
    }

    @DeleteMapping("/conteudos/{id}")
    @Operation(summary = "Excluir conteúdo", description = "Remove um conteúdo do usuário autenticado.")
    public ResponseEntity<Void> deletar(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String email = authentication.getName();
        service.deletar(email, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/conteudos/{id}/concluir")
    @Operation(summary = "Concluir conteúdo", description = "Marca um conteúdo como concluído.")
    public ResponseEntity<ConteudoEstudadoResponse> concluir(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(service.concluir(email, id));
    }

    @PatchMapping("/conteudos/{id}/reativar")
    @Operation(summary = "Reativar conteúdo", description = "Remove a marcação de concluído e retorna o conteúdo para ativo.")
    public ResponseEntity<ConteudoEstudadoResponse> reativar(
            Authentication authentication,
            @PathVariable Long id
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(service.reativar(email, id));
    }

    @PatchMapping("/conteudos/{id}/evolucao")
    @Operation(summary = "Registrar evolução de domínio", description = "Atualiza nível de domínio e observação de evolução do conteúdo.")
    public ResponseEntity<ConteudoEstudadoResponse> registrarEvolucao(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody @Valid EvolucaoRequest request
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(service.registrarEvolucao(email, id, request));
    }

    private Pageable criarPageable(Integer page, Integer size, String sort) {
        int pageNumber = page == null ? 0 : page;
        int pageSize = size == null ? 10 : size;

        if (pageNumber < 0) {
            throw new BadRequestException("Página não pode ser negativa");
        }

        if (pageSize < 1 || pageSize > 100) {
            throw new BadRequestException("Tamanho da página deve estar entre 1 e 100");
        }

        String[] sortParts = sort.split(",");
        String field = sortParts[0].trim();
        String direction = sortParts.length > 1 ? sortParts[1].trim() : "asc";

        if (!SORT_FIELDS.contains(field)) {
            throw new BadRequestException("Campo de ordenação inválido");
        }

        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Direção de ordenação deve ser asc ou desc");
        }

        return PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, field));
    }

}
