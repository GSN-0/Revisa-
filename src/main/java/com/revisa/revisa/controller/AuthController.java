package com.revisa.revisa.controller;

import com.revisa.revisa.dto.LoginRequest;
import com.revisa.revisa.dto.LoginResponse;
import com.revisa.revisa.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Login e geração de token JWT")
public class AuthController {

    private final UserService service;

    public AuthController(UserService service) {
        this.service = service;
    }

    @PostMapping("/login")
    @Operation(summary = "Realizar login", description = "Autentica usuário por email e senha e retorna um token JWT.")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(
                service.login(request.getEmail(), request.getSenha())
        );
    }
}
