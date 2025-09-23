# Agenda AI Backend

Um backend inteligente para gerenciamento de agenda com integração ao Google Calendar e IA para processamento de comandos em linguagem natural.

## 🚀 Funcionalidades

- **Autenticação OAuth2** com Google
- **Integração completa** com Google Calendar API
- **Chat com IA** para comandos de calendário em linguagem natural
- **CRUD completo** de eventos de calendário
- **Sincronização** automática com Google Calendar
- **API RESTful** bem estruturada

## 🛠️ Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3.5.4**
- **Spring Security** com OAuth2
- **Spring Data JPA**
- **H2 Database** (desenvolvimento)
- **PostgreSQL** (produção)
- **Google Calendar API**
- **OpenAI API**
- **JWT** para autenticação
- **Lombok**
- **Maven**

## 📋 Pré-requisitos

- Java 21+
- Maven 3.6+
- Conta Google (para OAuth2 e Calendar API)
- Chave da API OpenAI

## ⚙️ Configuração

### 1. Configurar Google OAuth2

1. Acesse o [Google Cloud Console](https://console.cloud.google.com/)
2. Crie um novo projeto ou selecione um existente
3. Ative as APIs:
   - Google Calendar API
   - Google+ API
4. Crie credenciais OAuth 2.0:
   - Tipo: Aplicação Web
   - URIs de redirecionamento autorizados: `http://localhost:8080/login/oauth2/code/google`

### 2. Configurar OpenAI

1. Acesse [OpenAI Platform](https://platform.openai.com/)
2. Crie uma conta e obtenha sua API Key

### 3. Variáveis de Ambiente

Crie um arquivo `.env` ou configure as seguintes variáveis de ambiente:

```bash
GOOGLE_CLIENT_ID=seu-google-client-id
GOOGLE_CLIENT_SECRET=seu-google-client-secret
OPENAI_API_KEY=sua-openai-api-key
JWT_SECRET=seu-jwt-secret-super-seguro
FRONTEND_URL=http://localhost:3000
```

### 4. Banco de Dados

**Desenvolvimento (H2):**
- Configuração automática
- Console H2: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:agendaai`
- Username: `sa`
- Password: `password`

**Produção (PostgreSQL):**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/agendaai
spring.datasource.username=postgres
spring.datasource.password=sua-senha
```

## 🚀 Como Executar

### 1. Clone o repositório
```bash
git clone <url-do-repositorio>
cd Agenda-AI-Backend
```

### 2. Configure as variáveis de ambiente
```bash
export GOOGLE_CLIENT_ID=seu-google-client-id
export GOOGLE_CLIENT_SECRET=seu-google-client-secret
export OPENAI_API_KEY=sua-openai-api-key
```

### 3. Execute a aplicação
```bash
mvn spring-boot:run
```

A aplicação estará disponível em: http://localhost:8080

## 📚 Endpoints da API

### Autenticação
- `GET /api/auth/me` - Obter usuário atual
- `POST /api/auth/logout` - Logout
- `GET /api/auth/status` - Status de autenticação

### Chat
- `POST /api/chat/message` - Enviar mensagem para IA
- `GET /api/chat/history` - Histórico de chat
- `GET /api/chat/recent` - Mensagens recentes
- `DELETE /api/chat/history` - Limpar histórico

### Calendário
- `POST /api/calendar/events` - Criar evento
- `GET /api/calendar/events` - Listar eventos
- `GET /api/calendar/events/range` - Eventos por período
- `GET /api/calendar/events/{id}` - Obter evento específico
- `PUT /api/calendar/events/{id}` - Atualizar evento
- `DELETE /api/calendar/events/{id}` - Deletar evento
- `POST /api/calendar/sync` - Sincronizar com Google Calendar

### OAuth2
- `GET /oauth2/authorization/google` - Iniciar login com Google

## 💬 Exemplos de Comandos de Chat

A IA pode processar comandos em linguagem natural como:

- "Criar uma reunião amanhã às 14h sobre projeto X"
- "Agendar consulta médica na sexta-feira às 10h"
- "Mostrar meus eventos da próxima semana"
- "Cancelar a reunião de hoje às 15h"

## 🏗️ Estrutura do Projeto

```
src/main/java/com/vini/agendaai/
├── config/          # Configurações (Security, etc.)
├── controller/      # Controllers REST
├── dto/            # Data Transfer Objects
├── model/          # Entidades JPA
├── repository/     # Repositórios JPA
├── security/       # Classes de segurança
└── service/        # Lógica de negócio
```

## 🧪 Testes

```bash
mvn test
```

## 📝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 🤝 Suporte

Se você tiver alguma dúvida ou problema, abra uma issue no GitHub.
