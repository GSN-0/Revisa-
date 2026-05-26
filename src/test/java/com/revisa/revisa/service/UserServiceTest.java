package com.revisa.revisa.service;

import com.revisa.revisa.dto.LoginResponse;
import com.revisa.revisa.dto.UpdatePasswordRequest;
import com.revisa.revisa.dto.UserRequest;
import com.revisa.revisa.dto.UserResponse;
import com.revisa.revisa.exception.BadRequestException;
import com.revisa.revisa.exception.EmailJaCadastradoException;
import com.revisa.revisa.exception.UnauthorizedException;
import com.revisa.revisa.model.Role;
import com.revisa.revisa.model.User;
import com.revisa.revisa.repository.UserRepository;
import com.revisa.revisa.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository repository;
    private JwtService jwtService;
    private PasswordEncoder encoder;
    private UserService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        jwtService = mock(JwtService.class);
        encoder = mock(PasswordEncoder.class);
        service = new UserService(repository, jwtService, encoder, true);
    }

    @Test
    void deveCadastrarPrimeiroUsuarioComoAdminNormalizandoEmail() {
        UserRequest request = new UserRequest();
        request.setNome(" Ana ");
        request.setEmail(" ANA@EMAIL.COM ");
        request.setSenha("123456");

        when(repository.existsByEmail("ana@email.com")).thenReturn(false);
        when(repository.count()).thenReturn(0L);
        when(encoder.encode("123456")).thenReturn("hash");
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = service.salvar(request);

        assertEquals("Ana", response.getNome());
        assertEquals("ana@email.com", response.getEmail());
        assertEquals("ADMIN", response.getRole());
        verify(repository).existsByEmail("ana@email.com");
    }

    @Test
    void deveLancarErroQuandoEmailJaExiste() {
        UserRequest request = new UserRequest();
        request.setEmail("teste@email.com");

        when(repository.existsByEmail("teste@email.com")).thenReturn(true);

        assertThrows(EmailJaCadastradoException.class, () -> service.salvar(request));
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void deveRealizarLoginComCredenciaisValidas() {
        User user = new User();
        user.setEmail("teste@email.com");
        user.setSenha("hash");
        user.setRole(Role.USER);

        when(repository.findByEmail("teste@email.com")).thenReturn(Optional.of(user));
        when(encoder.matches("123456", "hash")).thenReturn(true);
        when(jwtService.gerarToken("teste@email.com", "USER")).thenReturn("token");

        LoginResponse response = service.login(" TESTE@EMAIL.COM ", "123456");

        assertEquals("token", response.getToken());
    }

    @Test
    void deveNegarLoginComSenhaInvalida() {
        User user = new User();
        user.setEmail("teste@email.com");
        user.setSenha("hash");
        user.setRole(Role.USER);

        when(repository.findByEmail("teste@email.com")).thenReturn(Optional.of(user));
        when(encoder.matches("errada", "hash")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> service.login("teste@email.com", "errada"));
        verify(jwtService, never()).gerarToken(anyString(), anyString());
    }

    @Test
    void deveImpedirNovaSenhaIgualAtual() {
        User user = new User();
        user.setEmail("teste@email.com");
        user.setSenha("hash");

        when(repository.findByEmail("teste@email.com")).thenReturn(Optional.of(user));
        when(encoder.matches("atual", "hash")).thenReturn(true);
        when(encoder.matches("atual", "hash")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.atualizarSenha("teste@email.com", "atual", "atual"));
    }

    @Test
    void deveAtualizarSenhaQuandoDadosValidos() {
        User user = new User();
        user.setEmail("teste@email.com");
        user.setSenha("hash");

        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setSenhaAtual("123456");
        request.setNovaSenha("nova123");

        when(repository.findByEmail("teste@email.com")).thenReturn(Optional.of(user));
        when(encoder.matches("123456", "hash")).thenReturn(true);
        when(encoder.matches("nova123", "hash")).thenReturn(false);
        when(encoder.encode("nova123")).thenReturn("novoHash");

        service.atualizarSenha("teste@email.com", request.getSenhaAtual(), request.getNovaSenha());

        assertEquals("novoHash", user.getSenha());
        verify(repository).save(user);
    }
}
