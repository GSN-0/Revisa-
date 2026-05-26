package com.revisa.revisa.controller;

import com.revisa.revisa.config.OpenApiConfig;
import com.revisa.revisa.dto.MateriaRequest;
import com.revisa.revisa.dto.MateriaResponse;
import com.revisa.revisa.dto.MateriaResumoResponse;
import com.revisa.revisa.exception.BadRequestException;
import com.revisa.revisa.service.MateriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/materias")
@Tag(name = "Matérias", description = "Cadastro, consulta e resumo de matérias do usuário autenticado")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class MateriaController {

    private static final Set<String> SORT_FIELDS = Set.of("id", "nome", "descricao", "cor");

    private final MateriaService service;

    public MateriaController(MateriaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Criar matéria", description = "Cria uma matéria vinculada ao usuário autenticado pelo token.")
    public ResponseEntity<MateriaResponse> criar(
            Authentication authentication,
            @RequestBody @Valid MateriaRequest request
    ) {
        MateriaResponse response = service.criar(authentication.getName(), request);
        URI location = URI.create("/materias/" + response.getId());

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar matérias", description = "Lista matérias do usuário. Sem paginação retorna lista simples; com page/size retorna Page.")
    public ResponseEntity<?> listar(
            Authentication authentication,
            @Parameter(example = "0")
            @RequestParam(required = false) Integer page,
            @Parameter(example = "10")
            @RequestParam(required = false) Integer size,
            @Parameter(example = "nome,asc")
            @RequestParam(defaultValue = "nome,asc") String sort
    ) {
        if (page != null || size != null) {
            return ResponseEntity.ok(service.listarPaginado(
                    authentication.getName(),
                    criarPageable(page, size, sort)
            ));
        }

        return ResponseEntity.ok(service.listar(authentication.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar matéria por ID", description = "Busca uma matéria do usuário autenticado.")
    public ResponseEntity<MateriaResponse> buscarPorId(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.buscarPorId(authentication.getName(), id));
    }

    @GetMapping("/{id}/resumo")
    @Operation(summary = "Buscar resumo da matéria", description = "Retorna total de conteúdos, pendentes, concluídos e média de domínio da matéria.")
    public ResponseEntity<MateriaResumoResponse> buscarResumo(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.buscarResumo(authentication.getName(), id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar matéria", description = "Atualiza nome, descrição e cor de uma matéria do usuário autenticado.")
    public ResponseEntity<MateriaResponse> atualizar(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody @Valid MateriaRequest request
    ) {
        return ResponseEntity.ok(service.atualizar(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir matéria", description = "Remove uma matéria do usuário autenticado.")
    public ResponseEntity<Void> deletar(
            Authentication authentication,
            @PathVariable Long id
    ) {
        service.deletar(authentication.getName(), id);
        return ResponseEntity.noContent().build();
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
