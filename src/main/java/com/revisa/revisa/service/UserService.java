package com.revisa.revisa.service;

import com.revisa.revisa.dto.LoginResponse;
import com.revisa.revisa.dto.UpdateUserRequest;
import com.revisa.revisa.dto.UserRequest;
import com.revisa.revisa.dto.UserResponse;
import com.revisa.revisa.exception.BadRequestException;
import com.revisa.revisa.exception.EmailJaCadastradoException;
import com.revisa.revisa.exception.NotFoundException;
import com.revisa.revisa.exception.UnauthorizedException;
import com.revisa.revisa.model.Role;
import com.revisa.revisa.model.User;
import com.revisa.revisa.repository.UserRepository;
import com.revisa.revisa.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository repository;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private final boolean firstUserAdminEnabled;

    public UserService(UserRepository repository,
                       JwtService jwtService,
                       PasswordEncoder encoder,
                       @Value("${app.security.first-user-admin-enabled:true}") boolean firstUserAdminEnabled) {
        this.repository = repository;
        this.jwtService = jwtService;
        this.encoder = encoder;
        this.firstUserAdminEnabled = firstUserAdminEnabled;
    }

    public UserResponse salvar(UserRequest request) {
        String email = normalizarEmail(request.getEmail());

        if (repository.existsByEmail(email)) {
            throw new EmailJaCadastradoException("Email já cadastrado");
        }

        User user = new User();
        user.setNome(request.getNome().trim());
        user.setEmail(email);

        if (firstUserAdminEnabled && repository.count() == 0) {
            user.setRole(Role.ADMIN);
        } else {
            user.setRole(Role.USER);
        }

        user.setSenha(encoder.encode(request.getSenha()));

        User salvo = repository.save(user);

        return UserResponse.fromEntity(salvo);
    }

    public LoginResponse login(String email, String senha) {
        User user = repository.findByEmail(normalizarEmail(email))
                .orElseThrow(() -> new UnauthorizedException("Email ou senha inválidos"));

        if (!encoder.matches(senha, user.getSenha())) {
            throw new UnauthorizedException("Email ou senha inválidos");
        }

        String token = jwtService.gerarToken(user.getEmail(), user.getRole().name());

        return new LoginResponse(token);
    }

    public List<UserResponse> listar() {
        return repository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public UserResponse buscarPorEmail(String email) {
        User user = repository.findByEmail(normalizarEmail(email))
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        return UserResponse.fromEntity(user);
    }

    public UserResponse promoverParaAdmin(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        user.setRole(Role.ADMIN);
        User salvo = repository.save(user);

        return UserResponse.fromEntity(salvo);
    }

    public UserResponse atualizarPerfil(String email, UpdateUserRequest request) {
        User user = repository.findByEmail(normalizarEmail(email))
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        user.setNome(request.getNome().trim());

        User salvo = repository.save(user);

        return UserResponse.fromEntity(salvo);
    }

    public void atualizarSenha(String email, String senhaAtual, String novaSenha) {
        User user = repository.findByEmail(normalizarEmail(email))
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        if (!encoder.matches(senhaAtual, user.getSenha())) {
            throw new UnauthorizedException("Senha atual inválida");
        }

        if (encoder.matches(novaSenha, user.getSenha())) {
            throw new BadRequestException("Nova senha deve ser diferente da atual");
        }

        user.setSenha(encoder.encode(novaSenha));

        repository.save(user);
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
