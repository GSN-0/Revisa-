# Status de Entrega

Atualizado em 2026-05-23.

## Concluído

- Testes unitários e de integração rodando com sucesso.
- Coleção Postman criada em `postman/Revisa.postman_collection.json`.
- README criado com local, Docker, MySQL, Swagger, Flyway, testes e credenciais de apresentação.
- Checklist de entrega criado em `docs/CHECKLIST_ENTREGA.md`.
- Senha real removida do fallback do `application.properties`.
- `.env` criado localmente a partir do `.env.example`.
- `.env` está ignorado pelo Git.
- Docker Compose validado e containers rodando.
- MySQL do Docker validado em `localhost:3307`.
- API do Docker validada em `http://localhost:8080`.
- Swagger validado em `http://localhost:8080/swagger-ui/index.html`.
- OpenAPI validado em `http://localhost:8080/v3/api-docs`.
- Fluxo Docker validado: cadastro, login, criação de matéria, criação de conteúdo e dashboard.
- Fluxo principal completo validado contra a API em Docker: cadastro, login, matérias, conteúdos, evolução, revisão, pendentes, conclusão, reativação, filtros e dashboard.
- Frontend validado com fluxo real: cadastro, login, criar matéria, criar conteúdo, modal de domínio, revisar, concluir, reativar, dashboard, calendário e tema escuro.
- Frontend validado com token inválido: remove o token, volta para login e mostra mensagem de sessão expirada.
- Frontend validado com usuário novo sem dados: dashboard, matérias, conteúdos, revisões, calendário e gráficos mostram estados vazios corretos.
- Prints salvos em `docs/screenshots/`.
- Corrigido carregamento de matéria nos conteúdos para evitar erro de proxy lazy com `spring.jpa.open-in-view=false`.
- Adicionado `.env.production.example` com configurações recomendadas para produção.
- Frontend permite sobrescrever a API por `window.REVISA_API_URL`.

## Validado por teste automatizado

```text
mvn test
Tests run: 33, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Validado no Docker

Containers:

```text
revisa-api: Up, porta 8080
revisa-db: Up healthy, porta 3307 -> 3306
```

Endpoints verificados:

```text
GET /swagger-ui/index.html -> 200
GET /v3/api-docs -> 200
```

Fluxo real validado:

```text
POST /users
POST /auth/login
POST /materias
POST /materias/{id}/conteudos
GET /materias
GET /materias?page=0&size=10&sort=nome,asc
GET /materias/{id}/resumo
GET /conteudos
GET /conteudos?page=0&size=10&sort=proximaRevisao,asc
PATCH /conteudos/{id}/evolucao
POST /conteudos/{id}/revisar
GET /conteudos/pendentes
PATCH /conteudos/{id}/concluir
PATCH /conteudos/{id}/reativar
GET /conteudos/por-dominio?nivel=4
GET /conteudos/por-dominio?nivel=8
GET /conteudos/periodo?inicio=2026-05-01&fim=2026-05-31
GET /dashboard
```

Resultado do fluxo:

```text
Usuário criado
Token JWT gerado
Matéria criada
Conteúdo criado
Dashboard retornando totalMaterias=1 e totalConteudos=1 para o usuário de teste
```

## Validado no Frontend

Fluxo real validado com Chrome headless:

```text
Criar conta
Login
Criar matéria
Criar conteúdo
Registrar domínio pelo modal
Revisar conteúdo
Concluir conteúdo
Reativar conteúdo
Conferir dashboard
Conferir calendário
Ativar tema escuro
Token inválido ou expirado
Estados vazios com usuário novo
```

Prints gerados:

```text
docs/screenshots/01-login.png
docs/screenshots/02-dashboard.png
docs/screenshots/03-calendario.png
docs/screenshots/04-conteudos.png
docs/screenshots/05-modal-dominio.png
docs/screenshots/06-swagger.png
```

## Próximo foco recomendado

- Se for publicar o projeto, criar um `.env` real a partir de `.env.production.example`.
- Trocar as credenciais fracas de apresentação antes de qualquer uso fora de teste.
