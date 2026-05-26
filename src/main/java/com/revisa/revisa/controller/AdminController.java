package com.revisa.revisa.controller;

import com.revisa.revisa.config.OpenApiConfig;
import com.revisa.revisa.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@Tag(name = "Administração", description = "Rotas restritas para usuários ADMIN")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Validar acesso ADMIN", description = "Endpoint simples para confirmar se o token possui perfil ADMIN.")
    public ResponseEntity<String> teste() {
        return ResponseEntity.ok("Acesso ADMIN OK");
    }

    @PutMapping("/promote/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Promover usuário para ADMIN", description = "Altera o perfil de um usuário para ADMIN.")
    public ResponseEntity<String> promover(@PathVariable Long id) {
        userService.promoverParaAdmin(id);
        return ResponseEntity.ok("Usuário promovido para ADMIN");
    }
}
