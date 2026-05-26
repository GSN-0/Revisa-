package com.revisa.revisa.controller;

import com.revisa.revisa.dto.UpdatePasswordRequest;
import com.revisa.revisa.dto.UpdateUserRequest;
import com.revisa.revisa.dto.UserRequest;
import com.revisa.revisa.dto.UserResponse;
import com.revisa.revisa.config.OpenApiConfig;
import com.revisa.revisa.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuários", description = "Cadastro, perfil do usuário autenticado e administração de usuários")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Criar usuário", description = "Cria uma nova conta. O primeiro usuário cadastrado recebe perfil ADMIN.")
    public ResponseEntity<UserResponse> criar(@RequestBody @Valid UserRequest request) {
        return ResponseEntity.status(201).body(service.salvar(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Listar usuários", description = "Lista todos os usuários. Requer perfil ADMIN.")
    public ResponseEntity<List<UserResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/me")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Buscar meu perfil", description = "Retorna os dados do usuário autenticado pelo token JWT.")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(service.buscarPorEmail(email));
    }

    @PutMapping("/me")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Atualizar meu perfil", description = "Atualiza o nome do usuário autenticado.")
    public ResponseEntity<UserResponse> atualizarMeuPerfil(
            Authentication authentication,
            @RequestBody @Valid UpdateUserRequest request
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(service.atualizarPerfil(email, request));
    }

    @PutMapping("/me/password")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @Operation(summary = "Trocar minha senha", description = "Troca a senha do usuário autenticado validando a senha atual.")
    public ResponseEntity<String> atualizarMinhaSenha(
            Authentication authentication,
            @RequestBody @Valid UpdatePasswordRequest request
    ) {
        String email = authentication.getName();
        service.atualizarSenha(email, request.getSenhaAtual(), request.getNovaSenha());
        return ResponseEntity.ok("Senha atualizada com sucesso");
    }
}
