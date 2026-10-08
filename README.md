# EcoCycle — Back-end

Back-end do **EcoCycle**, um aplicativo de educação e engajamento em reciclagem de lixo eletrônico. Este repositório contém a API REST em Java/Spring Boot que sustenta os quatro pilares do produto: **educação**, **gamificação**, **logística de descarte** e **reconhecimento de componentes via IA on-device**.

Projeto desenvolvido como Trabalho de Conclusão de Curso, seguindo a metodologia *Design Science Research* (DSR).

## Sobre o projeto

O EcoCycle ensina usuários sobre descarte responsável de lixo eletrônico através de trilhas de conteúdo, quizzes e um álbum de figurinhas colecionáveis. O app também ajuda a localizar pontos de coleta próximos e permite reconhecer componentes eletrônicos pela câmera, usando um modelo TFLite executado inteiramente no dispositivo (nenhuma imagem trafega para o servidor).

Este back-end é responsável por:
- Autenticação e gerenciamento de contas de usuário
- Conteúdo educativo, organizado em trilhas
- Quizzes de fixação, sistema de XP e álbum de figurinhas
- Localização de pontos de coleta por proximidade
- Recebimento e validação dos resultados do scanner (identificador + confiança, nunca a imagem)

## Tecnologias

- **Java 17** + **Spring Boot 3.3.4** (Web, Security, Data JPA, Validation, Actuator)
- **PostgreSQL** (produção, hospedado no [Neon](https://neon.tech)) com **Flyway** para versionamento de schema
- **H2** (banco em memória para desenvolvimento e testes automatizados)
- **JWT** (JJWT) para autenticação stateless
- **springdoc-openapi** (Swagger UI) para documentação interativa
- **JUnit 5 + MockMvc** para testes de integração, com **JaCoCo** aplicando um piso mínimo de cobertura
- **Docker** para build e deploy (hospedado no [Render](https://render.com))
- Maven

## Arquitetura

Monólito em camadas (`controller` → `service` → `repository`), com autenticação via filtro JWT próprio integrado ao Spring Security. O schema do banco é gerenciado de duas formas conforme o ambiente:

- **Desenvolvimento e testes:** H2 em memória, schema gerado automaticamente pelo Hibernate (`ddl-auto=update`)
- **Produção:** PostgreSQL, schema versionado explicitamente via migrations do Flyway (`ddl-auto=validate`)

## Como rodar localmente

### Pré-requisitos
- JDK 17+
- Maven 3.8+

### Passos

```bash
# 1. Entrar na pasta do projeto
cd ecocycle-backend

# 2. (Opcional, mas recomendado) definir uma chave JWT própria
export JWT_SECRET="uma-chave-bem-longa-e-aleatoria-de-producao-nunca-versionada"

# 3. Rodar a aplicação (perfil padrão: H2 em memória)
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. Sem o profile `prod` ativado, a aplicação usa H2 automaticamente — nenhuma configuração extra de banco é necessária para desenvolver localmente.

> Se `JWT_SECRET` não for definida, a aplicação usa uma chave de fallback apenas para facilitar testes locais — **não use esse fallback em produção**.

### Variáveis de ambiente

| Variável | Obrigatória | Padrão | Descrição |
|---|---|---|---|
| `JWT_SECRET` | Recomendada | chave de dev embutida | Chave secreta para assinar os tokens JWT (HS256, mínimo 256 bits) |
| `JWT_EXPIRATION_MS` | Não | `604800000` (7 dias) | Tempo de expiração do token em milissegundos |
| `CORS_ALLOWED_ORIGINS` | Não | `localhost:3000,5173,8080` | Origens de navegador autorizadas (lista separada por vírgula) — não afeta o app Flutter nativo, que não passa por CORS |
| `DB_URL` | Só no profile `prod` | — | URL JDBC do PostgreSQL (Neon) |
| `DB_USERNAME` | Só no profile `prod` | — | Usuário do banco Postgres |
| `DB_PASSWORD` | Só no profile `prod` | — | Senha do banco Postgres |

### Console do banco H2 (apenas em desenvolvimento)

Disponível em `http://localhost:8080/h2-console`
JDBC URL: `jdbc:h2:mem:ecocycledb`, usuário `sa`, senha em branco.

### Health check

`GET http://localhost:8080/actuator/health`

## Documentação da API

Com a aplicação rodando, a documentação interativa (Swagger UI) fica disponível em:

```
http://localhost:8080/swagger-ui.html
```

Ela é gerada automaticamente a partir do código — controllers, DTOs e anotações de validação —, então está sempre sincronizada com o comportamento real da API, sem risco de ficar desatualizada.

**Como testar direto pelo navegador:**
1. Rode `POST /api/v1/auth/register` ou `/login` pelo próprio Swagger UI (botão "Try it out")
2. Copie o `token` da resposta
3. Clique em **"Authorize"** (cadeado, canto superior direito)
4. Cole apenas o token (sem escrever "Bearer " — o Swagger adiciona isso sozinho)
5. Todos os endpoints protegidos ficam liberados para teste, sem precisar de Postman

Endpoints agrupados por módulo: Autenticação, Usuários, Trilhas & Conteúdo, Quizzes, Figurinhas, Progresso, Pontos de Coleta, Scanner.

> A especificação OpenAPI crua (JSON, útil para gerar código cliente no Flutter) fica em `http://localhost:8080/v3/api-docs`.

## Endpoints principais

Prefixo de versionamento: `/api/v1`. Rotas marcadas com 🔒 exigem header `Authorization: Bearer <token>`.

| Módulo | Método | Rota | Descrição |
|---|---|---|---|
| Auth | POST | `/auth/register` | Cria uma nova conta |
| Auth | POST | `/auth/login` | Autentica e retorna um token JWT |
| Usuários 🔒 | GET | `/users/me` | Dados do usuário autenticado |
| Usuários 🔒 | PATCH | `/users/me/avatar` | Atualiza o avatar |
| Usuários 🔒 | DELETE | `/users/me` | Desativa a conta (soft delete) |
| Trilhas 🔒 | GET | `/trilhas` | Lista as trilhas com status de leitura/quiz |
| Conteúdo 🔒 | GET | `/conteudos/{id}` | Texto completo de um conteúdo |
| Conteúdo 🔒 | POST | `/conteudos/{id}/marcar-lido` | Marca como lido (libera o quiz) |
| Quizzes 🔒 | GET | `/quizzes/por-conteudo/{id}` | Quiz de um conteúdo (sem gabarito) |
| Quizzes 🔒 | POST | `/quizzes/{id}/submeter` | Corrige, concede XP e desbloqueia figurinha |
| Figurinhas 🔒 | GET | `/figurinhas` | Catálogo do álbum |
| Figurinhas 🔒 | GET | `/figurinhas/{id}` | Detalhe de uma figurinha |
| Progresso 🔒 | GET | `/progresso/me` | XP, leituras, quizzes e figurinhas do usuário |
| Pontos de coleta 🔒 | GET | `/pontos-coleta` | Lista pontos cadastrados |
| Pontos de coleta 🔒 | GET | `/pontos-coleta?lat=&lng=&raioKm=` | Busca por proximidade |
| Scanner 🔒 | POST | `/scanner/reconhecer` | Recebe resultado do scan e desbloqueia figurinha |

Ver a documentação completa (parâmetros, DTOs, códigos de erro) no Swagger UI.

### Exemplos rápidos

**Registro:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "maria@example.com",
    "apelido": "maria_eco",
    "senha": "123456"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "maria@example.com",
    "senha": "123456"
  }'
```

**Buscar pontos de coleta próximos:**
```bash
curl "http://localhost:8080/api/v1/pontos-coleta?lat=-23.5433&lng=-46.6528&raioKm=5" \
  -H "Authorization: Bearer <token>"
```

**Enviar resultado do scanner:**
```bash
curl -X POST http://localhost:8080/api/v1/scanner/reconhecer \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "identificadorComponente": "MOUSE",
    "confianca": 87.5
  }'
```
> Identificadores válidos nesta fase: `PILHA`, `MOUSE`, `FERRO_PASSAR`.

### Formato de respostas

**Sucesso:**
```json
{
  "data": { },
  "message": "Login realizado com sucesso.",
  "timestamp": "2026-08-18T10:00:00"
}
```

**Erro:**
```json
{
  "status": 401,
  "message": "E-mail ou senha invalidos.",
  "timestamp": "2026-08-18T10:00:00"
}
```

## Testes automatizados

```bash
mvn test
```

A suíte sobe o contexto Spring completo (incluindo o seed de dados) contra um banco H2 isolado (`ecocycledb-test`), usando `MockMvc` para simular requisições HTTP reais — passando pela cadeia de segurança JWT de verdade, sem mockar a autenticação.

**Cobertura:** registro/login/autenticação, CRUD de usuário, trilhas e conteúdo, quizzes (incluindo XP e desbloqueio de figurinha só na primeira conclusão), álbum de figurinhas, progresso agregado, pontos de coleta por proximidade, scanner, e um teste de fluxo ponta a ponta (cadastro → scanner).

Cada teste roda em transação com rollback automático, garantindo isolamento mesmo reaproveitando o mesmo contexto Spring entre classes.

### Cobertura de código (JaCoCo)

```bash
mvn verify
```

Esse comando aplica um piso mínimo de **70% de cobertura de linha** — se a cobertura cair abaixo disso, o build falha. Relatório detalhado em `target/site/jacoco/index.html` após a execução.

## Banco de dados em produção

Em produção, a aplicação roda com **PostgreSQL** (hospedado no Neon), com o schema versionado via **Flyway** (`src/main/resources/db/migration`). Para rodar localmente contra o Postgres em vez do H2:

```bash
export DB_URL="jdbc:postgresql://<host>/<database>?sslmode=require"
export DB_USERNAME="<usuario>"
export DB_PASSWORD="<senha>"

mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Na primeira execução, o Flyway cria automaticamente todas as tabelas a partir das migrations versionadas.

## Deploy

A aplicação é containerizada via `Dockerfile` (build multi-stage: Maven para compilar, JRE 17 enxuto para rodar) e hospedada no [Render](https://render.com) como Web Service, conectado ao banco Postgres do Neon.

Variáveis de ambiente necessárias no Render: `SPRING_PROFILES_ACTIVE=prod`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

## Estrutura do projeto

```
src/main/java/com/ecocycle/backend/
├── controller/     # Endpoints REST
├── service/        # Regras de negócio
├── repository/     # Acesso a dados (Spring Data JPA)
├── entity/         # Entidades JPA
├── dto/            # Objetos de request/response
├── security/       # Filtro JWT, UserDetailsService, autenticação
├── config/         # Configurações (CORS, Swagger, etc.)
├── exception/      # Tratamento centralizado de erros
├── enums/          # Enums de domínio
└── seed/           # Seed inicial de dados (conteúdo, quizzes, pontos de coleta)

src/main/resources/
├── application.properties        # Perfil padrão (dev/test, H2)
├── application-prod.properties   # Perfil de produção (Postgres/Neon)
└── db/migration/                 # Migrations do Flyway
```

## Limitações conhecidas

Itens conscientemente fora do escopo desta fase, documentados como trabalho futuro:

- Verificação de e-mail no cadastro
- Fluxo de recuperação de senha
- Refresh token / invalidação real de token no logout
- Painel de administração de conteúdo

## Documentação adicional

Decisões técnicas detalhadas, com justificativas e trade-offs, estão em `EcoCycle_Decisoes_Tecnicas.docx`.
