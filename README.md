# Revisa

Sistema mobile/web de planejamento de estudos baseado em revisão espaçada. A API permite cadastrar matérias, registrar conteúdos estudados, calcular revisões, acompanhar pendências, registrar evolução de domínio e visualizar indicadores no dashboard.

## Recursos principais

- Autenticação JWT com perfis `USER` e `ADMIN`.
- Matérias e conteúdos isolados por usuário.
- Agenda visual com revisões atrasadas, de hoje e futuras.
- Regra de espaçamento adaptada ao nível de domínio.
- Histórico persistido de revisões por conteúdo.
- Dashboard, calendário mensal, gráficos, busca, filtros, paginação e exportação CSV.
- Dados de exemplo opcionais para contas vazias.
- Área administrativa simples para listar usuários.

## Tecnologias

- Java 25
- Spring Boot 4
- Spring Security + JWT
- Spring Data JPA
- MySQL
- Flyway
- Swagger/OpenAPI
- HTML, CSS e JavaScript no frontend
- JUnit, Mockito, MockMvc e H2 para testes

## Como rodar localmente

Pré-requisitos:

- Java 25
- Maven
- MySQL rodando localmente

Crie um banco MySQL:

```sql
CREATE DATABASE revisa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Configure as variáveis de ambiente antes de rodar:

```bash
DB_URL=jdbc:mysql://localhost:3306/revisa
DB_USERNAME=root
DB_PASSWORD=sua_senha
JWT_SECRET=troque-por-um-segredo-grande-em-producao
JWT_EXPIRATION_MS=86400000
JWT_ISSUER=revisa-api
FIRST_USER_ADMIN_ENABLED=true
DEMO_DATA_ENABLED=true
```

Rode a API:

```bash
mvn spring-boot:run
```

A API fica em:

```text
http://localhost:8080
```

O frontend fica em:

```text
frontend/index.html
```

Por padrão, o frontend chama a API em `http://localhost:8080`. Se o backend estiver em outro endereço, ajuste:

```text
frontend/config.js
```

O arquivo `frontend/config.example.js` mostra um exemplo para publicação.

## Como rodar com Docker

Copie o arquivo de exemplo:

```bash
cp .env.example .env
```

Suba os containers:

```bash
docker compose up --build
```

Serviços principais:

- Frontend: `http://localhost:3000`
- API: `http://localhost:8080`
- MySQL: `localhost:3307`

Abra `http://localhost:3000` no navegador para usar o sistema. A porta `8080` expõe somente a API e exige autenticação nas rotas protegidas.

## Rodar backend local com banco Docker

O Maven não carrega automaticamente as variáveis do arquivo `.env`. Para executar a API fora do Docker usando o MySQL do Compose, primeiro deixe somente o banco ativo:

```powershell
docker compose up -d db
$env:DB_URL="jdbc:mysql://localhost:3307/revisa"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="troque-essa-senha"
mvn spring-boot:run
```

Se você alterou `MYSQL_ROOT_PASSWORD` no seu `.env`, use o mesmo valor em `DB_PASSWORD`. Antes de executar o Maven localmente, pare o container da API para liberar a porta `8080`:

```powershell
docker compose stop api
```

## Configuração para produção

O arquivo `.env.example` é voltado para uso local e apresentação. Para um ambiente mais rígido, use `.env.production.example` como base e ajuste:

- `JWT_SECRET` com segredo forte e aleatório.
- `CORS_ALLOWED_ORIGINS` com o domínio real do frontend.
- `FIRST_USER_ADMIN_ENABLED=false` depois de criar o admin inicial.
- `DEMO_DATA_ENABLED=false` para desativar a carga de dados de apresentação.
- Senhas de banco diferentes das credenciais de teste.

As credenciais `admin@gmail.com / 123456` e `teste@gmail.com / 123456` devem ficar restritas ao ambiente local de demonstração.

No frontend publicado, configure `window.REVISA_API_URL` para o endereço real da API antes de carregar `frontend/app.js`.

## Flyway

O projeto usa Flyway para versionar o banco. As migrations ficam em:

```text
src/main/resources/db/migration/
```

Migration inicial:

```text
V1__init.sql
```

Migration de histórico:

```text
V2__revisoes_historico.sql
```

Como o Flyway controla a criação das tabelas, o Hibernate não deve criar ou alterar tabelas automaticamente:

```properties
spring.jpa.hibernate.ddl-auto=none
```

Em banco limpo, basta iniciar a aplicação. Em banco já existente, use baseline com cuidado antes de migrar dados reais.

## Swagger

Com o backend rodando, acesse:

```text
http://localhost:8080/swagger-ui/index.html
```

Para testar rotas protegidas no Swagger:

1. Faça login em `/auth/login`.
2. Copie o token JWT retornado.
3. Clique em `Authorize`.
4. Informe `Bearer SEU_TOKEN`.

## Credenciais de teste

Para ambiente local de apresentação, use:

```text
Admin
Email: admin@gmail.com
Senha: 123456

Usuário de teste
Email: teste@gmail.com
Senha: 123456
```

Observação: se o banco estiver vazio e `FIRST_USER_ADMIN_ENABLED=true`, o primeiro usuário cadastrado recebe perfil `ADMIN`. Para uma entrega fora do ambiente de teste, use `FIRST_USER_ADMIN_ENABLED=false` depois de criar o admin inicial.

## Regra de revisão espaçada

Quando um conteúdo é criado, a próxima revisão começa a partir da data de estudo. A cada revisão registrada, o sistema incrementa a quantidade de revisões e calcula uma nova data.

A regra combina quantidade de revisões com nível de domínio:

- Domínio baixo aproxima a próxima revisão.
- Domínio alto distancia a próxima revisão.
- O objetivo é revisar mais cedo o que o usuário ainda não domina e espaçar melhor o que ele já domina.

Na prática, isso deixa o projeto mais inteligente que uma agenda fixa, porque a frequência se adapta ao progresso do estudante.

Cada revisão também gera uma entrada no histórico do conteúdo com data, domínio usado no cálculo e próxima revisão.

## Dados de demonstração

Com `DEMO_DATA_ENABLED=true`, uma conta vazia pode carregar dados de exemplo pelo dashboard ou por:

```text
POST /demo/seed
```

O carregamento só funciona uma vez por conta vazia. Em produção, mantenha `DEMO_DATA_ENABLED=false`.

## CI

O workflow `.github/workflows/ci.yml` executa `./mvnw test` em pushes e pull requests para `main`.

## Testes

Rodar todos os testes:

```bash
mvn test
```

Cobertura atual:

- Testes unitários dos serviços principais.
- Teste de integração do fluxo com autenticação JWT, matérias, conteúdos, evolução, revisão, conclusão e dashboard.
- Banco H2 em memória para teste, com Flyway ativado.

Última execução validada:

```text
Tests run: 37, Failures: 0, Errors: 0, Skipped: 0
```

## Publicação

Para um deploy simples de portfólio:

1. Publique a API Docker em um serviço com MySQL persistente.
2. Configure as variáveis usando `.env.production.example` como referência.
3. Publique a pasta `frontend/` em hospedagem estática.
4. Troque `window.REVISA_API_URL` em `frontend/config.js` pela URL HTTPS da API.
5. Configure `CORS_ALLOWED_ORIGINS` com a URL HTTPS exata do frontend.

## Postman

A coleção está em:

```text
postman/Revisa.postman_collection.json
```

Fluxo sugerido:

1. Cadastro de usuário.
2. Login.
3. Criar matéria.
4. Criar conteúdo.
5. Listar conteúdos.
6. Registrar evolução.
7. Revisar conteúdo.
8. Listar pendentes.
9. Concluir conteúdo.
10. Ver dashboard.
11. Ver resumo por matéria.

A coleção usa variáveis:

- `baseUrl`
- `token`
- `materiaId`
- `conteudoId`

Depois do login, o token é salvo automaticamente. Depois de criar matéria e conteúdo, os IDs também são salvos automaticamente.
