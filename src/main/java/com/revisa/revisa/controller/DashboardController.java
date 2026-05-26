package com.revisa.revisa.controller;

import com.revisa.revisa.config.OpenApiConfig;
import com.revisa.revisa.dto.DashboardResponse;
import com.revisa.revisa.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Dashboard", description = "Indicadores gerais do progresso de estudos")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Buscar resumo do dashboard", description = "Retorna totais de matérias, conteúdos, pendentes, concluídos, ativos e média de domínio.")
    public ResponseEntity<DashboardResponse> buscarResumo(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(service.buscarResumo(email));
    }
}
