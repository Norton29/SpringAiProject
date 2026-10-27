# Bookstore API

API REST para cadastro e consulta de livros, desenvolvida com Spring Boot. Na criação, o livro e uma mensagem pendente de enriquecimento são salvos no PostgreSQL. Um publicador agenda o envio ao RabbitMQ; o consumidor processa a solicitação e usa Google Gemini para gerar resumo, recomendações e classificação do livro.

## Tecnologias

- Java 25, Spring Boot 4.1 e Maven
- PostgreSQL, com schema atualizado pelo Hibernate (`ddl-auto: update`)
- RabbitMQ para processamento assíncrono do enriquecimento
- Spring AI com Google Gemini
- JUnit 5, Mockito e Testcontainers para testes

## Arquitetura

O código da funcionalidade de livros está organizado em camadas inspiradas na arquitetura clean:

- `book/domain`: entidades de domínio, estados e contratos
- `book/application`: casos de uso e regras da aplicação
- `book/adapter/input`: entrada HTTP e mensagens RabbitMQ
- `book/adapter/output`: integrações com Gemini, banco de dados, outbox e publicação de mensagens
- `config`: configuração da aplicação e do RabbitMQ

Os controllers recebem e devolvem DTOs. O modelo de domínio não é usado diretamente como contrato HTTP.

## Pré-requisitos

- JDK 25
- Docker e Docker Compose para iniciar PostgreSQL e RabbitMQ locais
- Uma chave da API Google Gemini

## Configuração

A aplicação lê configurações de um arquivo `.env` na raiz do projeto. Para usar os serviços locais definidos no `compose.yaml`, crie o arquivo com:

```properties
DB_URL=jdbc:postgresql://localhost:5433/bookstore_db
DB_USER=bookstore
DB_PASSWORD=bookstore_dev
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=bookstore
RABBITMQ_PASSWORD=bookstore_dev
GEMINI_API_KEY=sua-chave-da-api

# Usadas pelo Docker Compose para configurar os containers locais
POSTGRES_USER=bookstore
POSTGRES_PASSWORD=bookstore_dev
```

O `.env` está listado no `.gitignore`; não o adicione ao repositório. Para conectar a um PostgreSQL externo, substitua `DB_URL`, `DB_USER` e `DB_PASSWORD` pelos valores da conexão e mantenha os nomes das variáveis.

## Como iniciar

Inicie PostgreSQL e RabbitMQ:

```bash
docker compose up -d
```

Depois, inicie a aplicação:

```bash
./mvnw spring-boot:run
```

A API ficará disponível em `http://localhost:8080`. O painel de gerenciamento do RabbitMQ fica em `http://localhost:15672`; no ambiente local, use as credenciais configuradas em `RABBITMQ_USER` e `RABBITMQ_PASSWORD`.

Para parar os containers:

```bash
docker compose down
```

Para compilar, executar os testes e verificar o projeto:

```bash
./mvnw -q verify
```

## Processamento assíncrono

O `POST /books` responde depois de salvar o livro e a solicitação pendente no PostgreSQL, sem esperar o Gemini. A resposta inicial contém `enrichmentStatus: "PENDING"`. Um agendador consulta as mensagens ainda não publicadas a cada 5 segundos, em lotes de até 20, e as envia ao exchange `book.enrichment.exchange` com a routing key `book.enrichment`. As mensagens do RabbitMQ são persistentes; a aplicação marca a solicitação como publicada depois de receber a confirmação do broker. Assim, se o envio falhar antes da confirmação, a mensagem continua pendente para uma nova tentativa.

O consumidor da fila `book.enrichment.queue` enriquece o livro e persiste o resultado. Mensagens com falha são tentadas novamente até três vezes, com intervalo crescente. Depois das tentativas, o RabbitMQ encaminha a mensagem para o exchange e a fila de dead letter (`book.enrichment.dead-letter.exchange` e `book.enrichment.dead-letter.queue`); o consumidor dessa fila marca o livro como `FAILED`. Consulte `GET /books/{id}` para acompanhar o estado:

- `PENDING`: aguardando processamento ou em processamento
- `COMPLETED`: resumo, recomendações e classificação foram salvos
- `FAILED`: o processamento terminou sem sucesso

O enriquecimento inclui `review`, `recommendations`, `genre`, `targetAudience`, `language` e `keywords`.

## Endpoints

Todos os endpoints usam o caminho `/books`. Envie `X-API-VERSION: v1` ou `X-API-VERSION: v2`; se o header for omitido, a versão padrão é `v1`.

| Método | Caminho | Descrição | Resposta de sucesso |
|---|---|---|---|
| `GET` | `/books` | Lista os livros | `200 OK` |
| `GET` | `/books/{id}` | Consulta um livro e o estado do enriquecimento | `200 OK` |
| `POST` | `/books` | Cria um livro e agenda o enriquecimento | `201 Created` |
| `PUT` | `/books/{id}` | Atualiza os dados do livro | `200 OK` |
| `DELETE` | `/books/{id}` | Exclui um livro | `200 OK` |

Consultas, atualizações ou exclusões de um ID inexistente retornam `404 Not Found`. Os corpos de criação e atualização devem conter `title`, `author`, `publisher` e `publicationYear`. Os campos gerados pela IA são preenchidos após o processamento da mensagem.

Exemplo de criação:

```bash
curl -X POST http://localhost:8080/books \
  -H 'Content-Type: application/json' \
  -H 'X-API-VERSION: v1' \
  -d '{
    "title": "Dom Casmurro",
    "author": "Machado de Assis",
    "publisher": "Editora Exemplo",
    "publicationYear": 1899
  }'
```

## Testes

Os testes unitários de `BookService` usam JUnit e Mockito para verificar regras isoladas, como salvar o livro inicialmente como `PENDING` e registrar o ID para publicação sem chamar Gemini durante a requisição HTTP. Também verificam o enriquecimento, o comportamento para livros inexistentes e a prevenção de novo enriquecimento de livros já concluídos.

O teste de integração `BookEnrichmentFlowIntegrationTest` usa Testcontainers para iniciar instâncias descartáveis de PostgreSQL e RabbitMQ. Ele verifica o fluxo de salvar o livro e a mensagem pendente, publicar no RabbitMQ, consumir a mensagem e persistir os dados enriquecidos. Também envia mensagens duplicadas e verifica que o livro é enriquecido apenas uma vez. O Gemini é substituído por uma implementação falsa, então os testes não dependem da rede nem de uma chave válida. Docker precisa estar ativo para executar os testes de integração.

Execute a suíte com:

```bash
./mvnw test
```
