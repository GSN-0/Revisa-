package com.revisa.revisa.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revisa.revisa.repository.ConteudoEstudadoRepository;
import com.revisa.revisa.repository.MateriaRepository;
import com.revisa.revisa.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthMateriaConteudoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ConteudoEstudadoRepository conteudoRepository;

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        conteudoRepository.deleteAll();
        materiaRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void deveExecutarFluxoPrincipalComJwt() throws Exception {
        String email = "integracao@email.com";
        String senha = "123456";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Usuário Integração",
                                  "email": "%s",
                                  "senha": "%s"
                                }
                                """.formatted(email, senha)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        String loginJson = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "senha": "%s"
                                }
                                """.formatted(email, senha)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginJson).get("token").asText();

        String materiaJson = mockMvc.perform(post("/materias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Java",
                                  "descricao": "Estudos de Java e Spring",
                                  "cor": "#3b82f6"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Java"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long materiaId = objectMapper.readTree(materiaJson).get("id").asLong();

        String conteudoJson = mockMvc.perform(post("/materias/{materiaId}/conteudos", materiaId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Spring Security com JWT",
                                  "descricao": "Estudei autenticação, filtros e geração de token JWT.",
                                  "dataEstudo": "%s"
                                }
                                """.formatted(LocalDate.now())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Spring Security com JWT"))
                .andExpect(jsonPath("$.quantidadeRevisoes").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long conteudoId = objectMapper.readTree(conteudoJson).get("id").asLong();

        mockMvc.perform(get("/conteudos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(conteudoId));

        mockMvc.perform(patch("/conteudos/{id}/evolucao", conteudoId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nivelDominio": 4,
                                  "observacaoEvolucao": "Boa evolução no tema"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivelDominio").value(4));

        mockMvc.perform(post("/conteudos/{id}/revisar", conteudoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeRevisoes").value(1));

        mockMvc.perform(patch("/conteudos/{id}/concluir", conteudoId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concluido").value(true));

        mockMvc.perform(get("/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMaterias").value(1))
                .andExpect(jsonPath("$.totalConteudos").value(1))
                .andExpect(jsonPath("$.conteudosConcluidos").value(1))
                .andExpect(jsonPath("$.mediaDominio", greaterThanOrEqualTo(4.0)));
    }

    @Test
    void deveBloquearConteudosSemToken() throws Exception {
        mockMvc.perform(get("/conteudos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Autenticação necessária"));
    }

    @Test
    void usuarioNaoDeveAcessarConteudoDeOutroUsuario() throws Exception {
        String tokenA = cadastrarELogar("ana@email.com");
        String tokenB = cadastrarELogar("bia@email.com");

        Long materiaA = criarMateria(tokenA, "Java");
        Long conteudoA = criarConteudo(tokenA, materiaA, "JWT");

        mockMvc.perform(get("/conteudos/{id}", conteudoA)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Conteúdo estudado não encontrado"));
    }

    private String cadastrarELogar(String email) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Teste",
                                  "email": "%s",
                                  "senha": "123456"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated());

        String loginJson = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "senha": "123456"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(loginJson).get("token").asText();
    }

    private Long criarMateria(String token, String nome) throws Exception {
        String json = mockMvc.perform(post("/materias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "descricao": "Descrição",
                                  "cor": "#3b82f6"
                                }
                                """.formatted(nome)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(json).get("id").asLong();
    }

    private Long criarConteudo(String token, Long materiaId, String titulo) throws Exception {
        String json = mockMvc.perform(post("/materias/{materiaId}/conteudos", materiaId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "%s",
                                  "descricao": "Descrição",
                                  "dataEstudo": "%s"
                                }
                                """.formatted(titulo, LocalDate.now())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = objectMapper.readTree(json);
        return node.get("id").asLong();
    }
}
