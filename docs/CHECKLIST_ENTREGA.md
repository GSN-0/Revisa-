# Checklist de Entrega

## Backend

- [x] Rodar `mvn test` e confirmar todos os testes passando.
- [x] Rodar a API local com MySQL.
- [x] Conferir Swagger em `http://localhost:8080/swagger-ui/index.html`.
- [x] Testar fluxo principal pela coleção Postman.
- [x] Confirmar que `spring.jpa.hibernate.ddl-auto=none`.
- [x] Confirmar que o Flyway criou as tabelas em banco limpo.

## Docker

- [x] Criar `.env` a partir do `.env.example`.
- [x] Subir com `docker compose up --build`.
- [x] Confirmar API em `http://localhost:8080`.
- [x] Confirmar MySQL exposto em `localhost:3307`.
- [x] Testar login, matéria, conteúdo e dashboard usando Docker.

## Segurança

- [x] Não versionar `.env`.
- [x] Documentar `JWT_SECRET` forte para produção em `.env.production.example`.
- [x] Usar senha real do MySQL apenas no `.env`.
- [x] Documentar que `admin@gmail.com / 123456` deve ficar apenas em apresentação/teste.
- [x] Documentar `FIRST_USER_ADMIN_ENABLED=false` para produção em `.env.production.example`.
- [x] Documentar `CORS_ALLOWED_ORIGINS` específico para produção em `.env.production.example`.

## Frontend

- [x] Testar login.
- [x] Testar token expirado ou inválido.
- [x] Criar matéria.
- [x] Criar conteúdo.
- [x] Registrar domínio pelo modal.
- [x] Revisar conteúdo.
- [x] Concluir e reativar conteúdo.
- [x] Conferir calendário de revisões.
- [x] Conferir gráficos do dashboard.
- [x] Verificar estados vazios.
- [x] Verificar tema escuro.

## Apresentação

- [x] Tirar print da tela de login.
- [x] Tirar print do dashboard.
- [x] Tirar print do calendário.
- [x] Tirar print do cadastro/listagem de conteúdos.
- [x] Tirar print do Swagger.
- [ ] Explicar rapidamente JWT, dono por token e revisão espaçada.
- [x] Mostrar que há testes unitários, integração, Docker, Flyway e Postman.
