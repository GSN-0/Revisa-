package com.revisa.revisa.service;

import com.revisa.revisa.dto.DashboardResponse;
import com.revisa.revisa.model.ConteudoEstudado;
import com.revisa.revisa.repository.ConteudoEstudadoRepository;
import com.revisa.revisa.repository.MateriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private MateriaRepository materiaRepository;

    @Mock
    private ConteudoEstudadoRepository conteudoRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private final String email = "teste@email.com";

    private ConteudoEstudado conteudo1;
    private ConteudoEstudado conteudo2;

    @BeforeEach
    void setUp() {
        conteudo1 = new ConteudoEstudado();
        conteudo1.setNivelDominio(4);

        conteudo2 = new ConteudoEstudado();
        conteudo2.setNivelDominio(2);
    }

    @Test
    void deveBuscarResumoDoDashboard() {
        when(materiaRepository.countByUserEmail(email)).thenReturn(3L);

        when(conteudoRepository.countByMateriaUserEmail(email)).thenReturn(10L);

        when(conteudoRepository.countByMateriaUserEmailAndProximaRevisaoLessThanEqualAndConcluido(
                eq(email),
                any(LocalDate.class),
                eq(false)
        )).thenReturn(4L);

        when(conteudoRepository.countByMateriaUserEmailAndQuantidadeRevisoesGreaterThan(email, 0))
                .thenReturn(6L);

        when(conteudoRepository.countByMateriaUserEmailAndConcluido(email, true))
                .thenReturn(2L);

        when(conteudoRepository.countByMateriaUserEmailAndConcluido(email, false))
                .thenReturn(8L);

        when(conteudoRepository.findAllByMateriaUserEmail(email))
                .thenReturn(List.of(conteudo1, conteudo2));

        DashboardResponse response = dashboardService.buscarResumo(email);

        assertNotNull(response);
        assertEquals(3L, response.getTotalMaterias());
        assertEquals(10L, response.getTotalConteudos());
        assertEquals(4L, response.getConteudosPendentes());
        assertEquals(6L, response.getConteudosRevisados());
        assertEquals(2L, response.getConteudosConcluidos());
        assertEquals(8L, response.getConteudosAtivos());
        assertEquals(3.0, response.getMediaDominio());

        verify(materiaRepository).countByUserEmail(email);
        verify(conteudoRepository).countByMateriaUserEmail(email);
        verify(conteudoRepository).findAllByMateriaUserEmail(email);
    }

    @Test
    void deveRetornarMediaDominioZeroQuandoNaoExistemConteudos() {
        when(materiaRepository.countByUserEmail(email)).thenReturn(0L);

        when(conteudoRepository.countByMateriaUserEmail(email)).thenReturn(0L);

        when(conteudoRepository.countByMateriaUserEmailAndProximaRevisaoLessThanEqualAndConcluido(
                eq(email),
                any(LocalDate.class),
                eq(false)
        )).thenReturn(0L);

        when(conteudoRepository.countByMateriaUserEmailAndQuantidadeRevisoesGreaterThan(email, 0))
                .thenReturn(0L);

        when(conteudoRepository.countByMateriaUserEmailAndConcluido(email, true))
                .thenReturn(0L);

        when(conteudoRepository.countByMateriaUserEmailAndConcluido(email, false))
                .thenReturn(0L);

        when(conteudoRepository.findAllByMateriaUserEmail(email))
                .thenReturn(List.of());

        DashboardResponse response = dashboardService.buscarResumo(email);

        assertNotNull(response);
        assertEquals(0L, response.getTotalMaterias());
        assertEquals(0L, response.getTotalConteudos());
        assertEquals(0L, response.getConteudosPendentes());
        assertEquals(0L, response.getConteudosRevisados());
        assertEquals(0L, response.getConteudosConcluidos());
        assertEquals(0L, response.getConteudosAtivos());
        assertEquals(0.0, response.getMediaDominio());
    }
}