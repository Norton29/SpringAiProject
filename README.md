# Bookstore API

API REST para cadastro e consulta de livros, desenvolvida com Spring Boot. Na criação de um livro, a aplicação usa Spring AI e o modelo Google Gemini para gerar um resumo curto em português. O projeto também demonstra versionamento de API pelo header `X-API-VERSION`.

## Tecnologias

- Java 25 e Spring Boot 4.1
- Maven
- PostgreSQL, com schema atualizado pelo Hibernate (`ddl-auto: update`)
- Spring AI com Google Gemini
- Spring HATEOAS na versão `v1`

## Pré-requisitos

- JDK 25
- Docker e Docker Compose, caso use o PostgreSQL local
- Uma chave da API Google Gemini para gerar reviews

## Configuração

A aplicação lê as configurações de um arquivo `.env` na raiz do projeto. Crie-o com estas variáveis:

```properties
DB_URL=jdbc:postgresql://localhost:5433/bookstore_db
DB_USER=bookstore
DB_PASSWORD=bookstore_dev
GEMINI_API_KEY=sua-chave-da-api

# Usadas pelo Docker Compose para configurar o banco local
POSTGRES_USER=bookstore
POSTGRES_PASSWORD=bookstore_dev
```

O `.env` está listado no `.gitignore`; não o adicione ao repositório. Para conectar ao Neon, substitua `DB_URL`, `DB_USER` e `DB_PASSWORD` pelos valores da conexão Neon. Mantenha os nomes das variáveis.

## Como iniciar

Com PostgreSQL local, inicie o container:

```bash
docker compose up -d postgres
```

Depois, inicie a aplicação:

```bash
./mvnw spring-boot:run
```

A API ficará disponível em `http://localhost:8080`. Para encerrar o PostgreSQL local:

```bash
docker compose down
```

Para compilar e verificar o projeto:

```bash
./mvnw -q verify
```

## Endpoints

Todos os endpoints usam o caminho `/books`. Envie `X-API-VERSION: v1` para a versão com respostas HATEOAS ou `X-API-VERSION: v2` para respostas sem HATEOAS. Se o header for omitido, a versão padrão é `v1`.

| Método | Caminho | Descrição | Resposta de sucesso |
|---|---|---|---|
| `GET` | `/books` | Lista os livros | `200 OK` |
| `GET` | `/books/{id}` | Consulta um livro | `200 OK` |
| `POST` | `/books` | Cria um livro e gera seu review | `201 Created` |
| `PUT` | `/books/{id}` | Atualiza os dados do livro | `200 OK` |
| `DELETE` | `/books/{id}` | Exclui um livro | `200 OK` |

Consultas, atualizações ou exclusões de um ID inexistente retornam `404 Not Found`. O corpo de criação e atualização deve conter `title`, `author`, `publisher` e `publicationYear`. O campo `review` é gerado pela IA na criação.

Exemplo de criação usando `v2`:

```bash
curl -X POST http://localhost:8080/books \
  -H 'Content-Type: application/json' \
  -H 'X-API-VERSION: v2' \
  -d '{
    "title": "Dom Casmurro",
    "author": "Machado de Assis",
    "publisher": "Editora Exemplo",
    "publicationYear": 1899
  }'
```
