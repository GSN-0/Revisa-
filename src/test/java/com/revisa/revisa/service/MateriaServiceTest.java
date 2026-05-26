package com.revisa.revisa.service;

import com.revisa.revisa.dto.MateriaRequest;
import com.revisa.revisa.dto.MateriaResponse;
import com.revisa.revisa.model.Materia;
import com.revisa.revisa.model.User;
import com.revisa.revisa.repository.MateriaRepository;
import com.revisa.revisa.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MateriaServiceTest {

    @Mock
    private MateriaRepository materiaRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MateriaService materiaService;

    private User user;
    private Materia materia;
    private MateriaRequest request;

    private final String email = "teste@email.com";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail(email);

        materia = new Materia();
        materia.setNome("Java");
        materia.setDescricao("Estudos de Java");
        materia.setCor("#FF0000");
        materia.setUser(user);

        request = new MateriaRequest();
        request.setNome("Java");
        request.setDescricao("Estudos de Java");
        request.setCor("#FF0000");
    }

    @Test
    void deveCriarMateriaQuandoUsuarioExiste() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(materiaRepository.save(any(Materia.class))).thenReturn(materia);

        MateriaResponse response = materiaService.criar(email, request);

        assertNotNull(response);
        assertEquals("Java", response.getNome());
        assertEquals("Estudos de Java", response.getDescricao());
        assertEquals("#FF0000", response.getCor());

        verify(userRepository).findByEmail(email);
        verify(materiaRepository).save(any(Materia.class));
    }

    @Test
    void deveListarMateriasDoUsuario() {
        when(materiaRepository.findAllByUserEmail(email)).thenReturn(List.of(materia));

        List<MateriaResponse> response = materiaService.listar(email);

        assertEquals(1, response.size());
        assertEquals("Java", response.get(0).getNome());

        verify(materiaRepository).findAllByUserEmail(email);
    }

    @Test
    void deveBuscarMateriaPorIdQuandoPertenceAoUsuario() {
        Long materiaId = 1L;

        when(materiaRepository.findByIdAndUserEmail(materiaId, email))
                .thenReturn(Optional.of(materia));

        MateriaResponse response = materiaService.buscarPorId(email, materiaId);

        assertNotNull(response);
        assertEquals("Java", response.getNome());

        verify(materiaRepository).findByIdAndUserEmail(materiaId, email);
    }

    @Test
    void deveAtualizarMateriaQuandoPertenceAoUsuario() {
        Long materiaId = 1L;

        MateriaRequest updateRequest = new MateriaRequest();
        updateRequest.setNome("Spring Boot");
        updateRequest.setDescricao("Estudos de Spring");
        updateRequest.setCor("#00FF00");

        when(materiaRepository.findByIdAndUserEmail(materiaId, email))
                .thenReturn(Optional.of(materia));

        when(materiaRepository.save(materia)).thenReturn(materia);

        MateriaResponse response = materiaService.atualizar(email, materiaId, updateRequest);

        assertEquals("Spring Boot", response.getNome());
        assertEquals("Estudos de Spring", response.getDescricao());
        assertEquals("#00FF00", response.getCor());

        verify(materiaRepository).findByIdAndUserEmail(materiaId, email);
        verify(materiaRepository).save(materia);
    }

    @Test
    void deveDeletarMateriaQuandoPertenceAoUsuario() {
        Long materiaId = 1L;

        when(materiaRepository.findByIdAndUserEmail(materiaId, email))
                .thenReturn(Optional.of(materia));

        materiaService.deletar(email, materiaId);

        verify(materiaRepository).findByIdAndUserEmail(materiaId, email);
        verify(materiaRepository).delete(materia);
    }

    @Test
    void deveLancarErroQuandoUsuarioNaoExisteAoCriarMateria() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> materiaService.criar(email, request)
        );

        assertEquals("Usuário não encontrado", exception.getMessage());

        verify(userRepository).findByEmail(email);
        verify(materiaRepository, never()).save(any(Materia.class));
    }

    @Test
    void deveLancarErroQuandoMateriaNaoPertenceAoUsuario() {
        Long materiaId = 99L;

        when(materiaRepository.findByIdAndUserEmail(materiaId, email))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> materiaService.buscarPorId(email, materiaId)
        );

        assertEquals("Matéria não encontrada", exception.getMessage());

        verify(materiaRepository).findByIdAndUserEmail(materiaId, email);
    }
}