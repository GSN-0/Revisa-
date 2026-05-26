package com.revisa.revisa.service;

import com.revisa.revisa.dto.ConteudoEstudadoRequest;
import com.revisa.revisa.dto.ConteudoEstudadoResponse;
import com.revisa.revisa.dto.EvolucaoRequest;
import com.revisa.revisa.exception.BadRequestException;
import com.revisa.revisa.exception.NotFoundException;
import com.revisa.revisa.model.ConteudoEstudado;
import com.revisa.revisa.model.Materia;
import com.revisa.revisa.model.User;
import com.revisa.revisa.repository.ConteudoEstudadoRepository;
import com.revisa.revisa.repository.MateriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConteudoEstudadoServiceTest {

    @Mock
    private ConteudoEstudadoRepository conteudoRepository;

    @Mock
    private MateriaRepository materiaRepository;

    @InjectMocks
    private ConteudoEstudadoService service;

    private User user;
    private Materia materia;
    private ConteudoEstudado conteudo;
    private ConteudoEstudadoRequest request;

    private final String email = "teste@email.com";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail(email);

        materia = new Materia();
        materia.setNome("Java");
        materia.setDescricao("Estudos Java");
        materia.setCor("#FF0000");
        materia.setUser(user);

        conteudo = new ConteudoEstudado();
        conteudo.setTitulo("Spring Boot");
        conteudo.setDescricao("Estudos Spring");
        conteudo.setDataEstudo(LocalDate.now());
        conteudo.setProximaRevisao(LocalDate.now().plusDays(1));
        conteudo.setQuantidadeRevisoes(0);
        conteudo.setNivelDominio(3);
        conteudo.setConcluido(false);
        conteudo.setMateria(materia);

        request = new ConteudoEstudadoRequest();
        request.setTitulo("Spring Boot");
        request.setDescricao("Estudos Spring");
        request.setDataEstudo(LocalDate.now());
    }

    @Test
    void deveCriarConteudo() {
        when(materiaRepository.findByIdAndUserEmail(1L, email))
                .thenReturn(Optional.of(materia));

        when(conteudoRepository.save(any(ConteudoEstudado.class)))
                .thenReturn(conteudo);

        ConteudoEstudadoResponse response =
                service.criar(email, 1L, request);

        assertNotNull(response);
        assertEquals("Spring Boot", response.getTitulo());

        verify(conteudoRepository).save(any(ConteudoEstudado.class));
    }

    @Test
    void devePermitirCriarConteudoComDataDeHoje() {
        request.setDataEstudo(LocalDate.now(ZoneId.of("America/Sao_Paulo")));

        when(materiaRepository.findByIdAndUserEmail(1L, email))
                .thenReturn(Optional.of(materia));

        when(conteudoRepository.save(any(ConteudoEstudado.class)))
                .thenReturn(conteudo);

        ConteudoEstudadoResponse response = service.criar(email, 1L, request);

        assertNotNull(response);
        verify(conteudoRepository).save(any(ConteudoEstudado.class));
    }

    @Test
    void deveLancarErroQuandoDataEstudoForFutura() {
        request.setDataEstudo(LocalDate.now(ZoneId.of("America/Sao_Paulo")).plusDays(1));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.criar(email, 1L, request)
        );

        assertEquals("Data de estudo não pode ser futura", exception.getMessage());

        verify(materiaRepository, never()).findByIdAndUserEmail(anyLong(), anyString());
        verify(conteudoRepository, never()).save(any(ConteudoEstudado.class));
    }

    @Test
    void deveListarConteudosDaMateria() {
        when(materiaRepository.findByIdAndUserEmail(1L, email))
                .thenReturn(Optional.of(materia));

        when(conteudoRepository
                .findAllByMateriaIdAndMateriaUserEmail(1L, email))
                .thenReturn(List.of(conteudo));

        List<ConteudoEstudadoResponse> response =
                service.listarPorMateria(email, 1L);

        assertEquals(1, response.size());
        assertEquals("Spring Boot", response.get(0).getTitulo());
    }

    @Test
    void deveBuscarConteudoPorId() {
        when(conteudoRepository.findByIdAndMateriaUserEmail(1L, email))
                .thenReturn(Optional.of(conteudo));

        ConteudoEstudadoResponse response =
                service.buscarPorId(email, 1L);

        assertNotNull(response);
        assertEquals("Spring Boot", response.getTitulo());
    }

    @Test
    void deveConcluirConteudo() {
        when(conteudoRepository.findByIdAndMateriaUserEmail(1L, email))
                .thenReturn(Optional.of(conteudo));

        when(conteudoRepository.save(conteudo))
                .thenReturn(conteudo);

        ConteudoEstudadoResponse response =
                service.concluir(email, 1L);

        assertTrue(response.getConcluido());
    }

    @Test
    void deveReativarConteudo() {
        conteudo.setConcluido(true);

        when(conteudoRepository.findByIdAndMateriaUserEmail(1L, email))
                .thenReturn(Optional.of(conteudo));

        when(conteudoRepository.save(conteudo))
                .thenReturn(conteudo);

        ConteudoEstudadoResponse response =
                service.reativar(email, 1L);

        assertFalse(response.getConcluido());
    }

    @Test
    void deveRegistrarEvolucao() {
        EvolucaoRequest request = new EvolucaoRequest();
        request.setNivelDominio(5);
        request.setObservacaoEvolucao("Muito bom");

        when(conteudoRepository.findByIdAndMateriaUserEmail(1L, email))
                .thenReturn(Optional.of(conteudo));

        when(conteudoRepository.save(conteudo))
                .thenReturn(conteudo);

        ConteudoEstudadoResponse response =
                service.registrarEvolucao(email, 1L, request);

        assertEquals(5, response.getNivelDominio());
        assertEquals("Muito bom", response.getObservacaoEvolucao());
    }

    @Test
    void deveListarPendentes() {
        when(conteudoRepository
                .findAllByMateriaUserEmailAndProximaRevisaoLessThanEqualAndConcluido(
                        eq(email),
                        any(LocalDate.class),
                        eq(false)
                ))
                .thenReturn(List.of(conteudo));

        List<ConteudoEstudadoResponse> response =
                service.listarPendentes(email);

        assertEquals(1, response.size());
    }

    @Test
    void deveListarConcluidos() {
        conteudo.setConcluido(true);

        when(conteudoRepository
                .findAllByMateriaUserEmailAndConcluido(email, true))
                .thenReturn(List.of(conteudo));

        List<ConteudoEstudadoResponse> response =
                service.listarConcluidos(email);

        assertEquals(1, response.size());
        assertTrue(response.get(0).getConcluido());
    }

    @Test
    void deveListarAtivos() {
        when(conteudoRepository
                .findAllByMateriaUserEmailAndConcluido(email, false))
                .thenReturn(List.of(conteudo));

        List<ConteudoEstudadoResponse> response =
                service.listarAtivos(email);

        assertEquals(1, response.size());
        assertFalse(response.get(0).getConcluido());
    }

    @Test
    void deveLancarErroQuandoNivelDominioInvalido() {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.listarPorDominio(email, 10)
        );

        assertEquals(
                "Nível de domínio deve estar entre 1 e 5",
                exception.getMessage()
        );
    }

    @Test
    void deveLancarErroQuandoPeriodoInvalido() {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.listarPorPeriodo(
                        email,
                        LocalDate.now(),
                        LocalDate.now().minusDays(1)
                )
        );

        assertEquals(
                "Data inicial deve ser anterior ou igual à data final",
                exception.getMessage()
        );
    }

    @Test
    void deveLancarErroQuandoConteudoNaoExiste() {
        when(conteudoRepository.findByIdAndMateriaUserEmail(1L, email))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.buscarPorId(email, 1L)
        );

        assertEquals(
                "Conteúdo estudado não encontrado",
                exception.getMessage()
        );
    }
}
