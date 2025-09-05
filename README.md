# AccountaBot 🤖💰

> Sistema completo de análise financeira com IA - Backend Java + Frontend React moderno

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-green.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18+-blue.svg)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-4.9+-blue.svg)](https://www.typescriptlang.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-3.3+-38B2AC.svg)](https://tailwindcss.com/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Sobre o Projeto

O **AccountaBot** é uma plataforma completa de análise financeira que combina:
- **Backend Java/Spring Boot** com arquitetura de agentes inteligentes
- **Frontend React moderno** com dashboard financeiro profissional
- **Integração com APIs** de dados financeiros (Yahoo Finance, Alpha Vantage)
- **Sistema de autenticação** e gerenciamento de portfólio

🎯 **Status Atual**: Frontend funcional com dados demonstrativos + Backend com lógica de negócio (APIs REST em desenvolvimento)

### 🎯 Funcionalidades Implementadas

#### 🖥️ **Frontend React (Funcional)**
- **🔐 Sistema de Autenticação**: Login com credenciais demonstrativas
- **📊 Dashboard Financeiro**: Visão geral do mercado e portfólio
- **📈 Página de Analytics**: Análises e relatórios detalhados
- **🤖 Gerenciamento de Agentes**: Interface para controle de agentes IA
- **💼 Portfolio**: Gestão de carteiras e posições
- **⚙️ Configurações**: Personalização do usuário
- **📱 Design Responsivo**: Interface moderna com Tailwind CSS

#### ⚙️ **Backend Java (Lógica de Negócio)**
- **🤖 Sistema de Agentes Inteligentes**: Arquitetura baseada em agentes com workflow automatizado
- **📊 Múltiplas Fontes de Dados**: Integração com Yahoo Finance, Alpha Vantage e outras APIs
- **📈 Análise Quantitativa**: Módulos especializados para análise técnica e fundamental
- **⚡ Processamento Assíncrono**: Operações não-bloqueantes para alta performance
- **📋 Gerenciamento de Cache**: Sistema inteligente de cache para otimização
- **🔄 Rate Limiting**: Controle automático de limites de API

## 🏗️ Arquitetura

### Estrutura do Projeto

```
📦 AccountaBot
├── 🖥️ Frontend (React + TypeScript)
│   ├── 📄 Páginas: Login, Dashboard, Analytics, Agents, Portfolio, Settings
│   ├── 🧩 Componentes: Charts, Auth, Layout, UI
│   ├── 🔧 Serviços: API, Autenticação
│   ├── 🎨 Styling: Tailwind CSS
│   └── 🛣️ Roteamento: React Router
├── ⚙️ Backend (Java + Spring Boot)
│   ├── 🤖 Agentes (Agents)
│   │   ├── AgentLibrary - Biblioteca de agentes especializados
│   │   ├── WorkflowManager - Gerenciamento de fluxos de trabalho
│   │   ├── SmartScheduler - Agendamento inteligente de tarefas
│   │   └── BaseAgent - Classe base para todos os agentes
│   ├── 📊 Fontes de Dados (DataSources)
│   │   ├── AlphaVantageDataSource - Integração Alpha Vantage
│   │   ├── YahooFinanceDataSource - Integração Yahoo Finance
│   │   └── DataSourceManager - Gerenciador centralizado
│   └── 🔧 Módulos Funcionais
│       ├── Analyzer - Análise de dados financeiros
│       ├── Charting - Geração de gráficos e visualizações
│       └── Quantitative - Análises quantitativas avançadas
└── 🔗 Integração: APIs REST (em desenvolvimento)
```

### Tipos de Dados Suportados

- **💰 Dados de Preços**: Cotações em tempo real e históricas
- **📊 Dados Fundamentais**: Balanços, demonstrativos e métricas
- **📰 Notícias Financeiras**: Análise de sentimento e impacto
- **📈 Dados Econômicos**: Indicadores macroeconômicos
- **🔍 Informações de Instrumentos**: Metadados de ativos financeiros

## 🚀 Instalação e Configuração

### Pré-requisitos

**Backend:**
- **Java 17+**
- **Maven 3.6+**
- **Spring Boot 3.0+**

**Frontend:**
- **Node.js 16+**
- **npm ou yarn**

### Clonando o Repositório

```bash
git clone https://github.com/Andre-Brito-py/AccountaBot.git
cd AccountaBot
```

### 🖥️ Executando o Frontend

1. **Navegue para o diretório do frontend**:
```bash
cd frontend
```

2. **Instale as dependências**:
```bash
npm install
```

3. **Execute o servidor de desenvolvimento**:
```bash
npm start
```

4. **Acesse a aplicação**:
- Frontend: http://localhost:3000
- **Credenciais para demonstração**:
  - **Admin**: `admin@accountabot.com` / `admin123`
  - **Usuário**: `user@accountabot.com` / `user123`
  - **Demo**: `demo@demo.com` / `demo123`

### ⚙️ Executando o Backend

1. **Configure as chaves de API** no arquivo `src/main/resources/application.yml`:

```yaml
finrobot:
  datasources:
    alpha-vantage:
      api-key: "SUA_CHAVE_ALPHA_VANTAGE"
    yahoo-finance:
      enabled: true
```

2. **Compile o projeto**:
```bash
mvn clean compile
```

3. **Execute a aplicação**:
```bash
mvn spring-boot:run
```

4. **Acesse os endpoints**:
- API: http://localhost:8080/api
- H2 Console: http://localhost:8080/api/h2-console
- Health Check: http://localhost:8080/api/actuator/health

## 💡 Uso

### 🖥️ Interface Web (Frontend)

1. **Acesse a aplicação**: http://localhost:3000
2. **Faça login** com uma das credenciais demonstrativas:
   - Admin: `admin@accountabot.com` / `admin123`
   - User: `user@accountabot.com` / `user123`
   - Demo: `demo@demo.com` / `demo123`

3. **Navegue pelas funcionalidades**:
   - **Dashboard**: Visão geral do mercado e métricas
   - **Analytics**: Análises detalhadas e relatórios
   - **Agents**: Gerenciamento de agentes IA
   - **Portfolio**: Gestão de carteiras
   - **Settings**: Configurações pessoais

### ⚙️ API Backend (Em Desenvolvimento)

```java
// Inicializar o sistema de agentes
AgentLibrary agentLibrary = new AgentLibrary();
WorkflowManager workflowManager = new WorkflowManager(agentLibrary);

// Configurar fonte de dados
DataSourceManager dataSourceManager = new DataSourceManager();
dataSourceManager.initialize();

// Executar análise
Analyzer analyzer = new Analyzer(dataSourceManager);
var resultado = analyzer.analisarAtivo("AAPL");
```

### 🔧 Configuração de Agentes

```java
// Criar agente personalizado
BaseAgent meuAgente = new BaseAgent() {
    @Override
    public AgentResponse execute(AgentRequest request) {
        // Lógica do agente
        return new AgentResponse("Análise concluída");
    }
};

// Registrar no sistema
agentLibrary.registerAgent("meu-agente", meuAgente);
```

### 📊 Dados Demonstrativos

O frontend atualmente utiliza dados demonstrativos para:
- **Índices de Mercado**: S&P 500, NASDAQ, Dow Jones
- **Ações Populares**: AAPL, GOOGL, MSFT, TSLA, AMZN
- **Análises Simuladas**: Relatórios técnicos e fundamentais
- **Status de Agentes**: Estados simulados dos agentes IA
- **Métricas de Portfolio**: Dados fictícios para demonstração

## 🔧 Configuração Avançada

### Rate Limiting

```yaml
finrobot:
  rate-limiting:
    requests-per-minute: 60
    burst-capacity: 10
```

### Cache

```yaml
finrobot:
  cache:
    ttl: 300 # segundos
    max-size: 1000
```

## 📊 Monitoramento

O sistema inclui métricas detalhadas:

- **Latência de requisições**
- **Taxa de sucesso/erro**
- **Uso de cache**
- **Limites de rate limiting**
- **Performance dos agentes**

## 🤝 Contribuição

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 🛣️ Roadmap

### ✅ **Concluído**
- [x] ✅ Interface web React moderna e responsiva
- [x] ✅ Sistema de autenticação frontend
- [x] ✅ Dashboard financeiro interativo
- [x] ✅ Componentes de visualização de dados
- [x] ✅ Arquitetura de agentes backend
- [x] ✅ Integração com fontes de dados financeiros

### 🚧 **Em Desenvolvimento**
- [ ] 🔄 API REST para integração frontend-backend
- [ ] 🔄 Endpoints de autenticação e autorização
- [ ] 🔄 Persistência de dados (banco de dados)

### 📋 **Próximos Passos**
- [ ] 📊 Gráficos interativos com dados reais
- [ ] 🤖 Interface de configuração de agentes
- [ ] 🔔 Sistema de alertas em tempo real
- [ ] 📰 Análise de sentimento de notícias
- [ ] 🏦 Integração com corretoras
- [ ] 📱 Mobile app

## 🎯 Status do Projeto

| Componente | Status | Funcionalidade |
|------------|--------|----------------|
| 🖥️ **Frontend React** | ✅ **95% Funcional** | Interface completa, navegação, componentes |
| 🔐 **Autenticação Frontend** | ✅ **100% Demo** | Login com credenciais demonstrativas |
| 📊 **Dashboard** | ✅ **100% Visual** | Métricas e gráficos com dados mock |
| ⚙️ **Backend Logic** | ✅ **70% Implementado** | Agentes, fontes de dados, análises |
| 🔗 **APIs REST** | ⚠️ **Em Desenvolvimento** | Endpoints para integração |
| 💾 **Persistência** | ⚠️ **Pendente** | Banco de dados e modelos |
| 🧪 **Testes** | ⚠️ **Básicos** | Testes unitários limitados |

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 👨‍💻 Autor

**Andre Brito**
- GitHub: [@Andre-Brito-py](https://github.com/Andre-Brito-py)
- Email: andre.brito.py@gmail.com

## 🙏 Agradecimentos

- [Spring Boot](https://spring.io/projects/spring-boot) - Framework principal
- [Alpha Vantage](https://www.alphavantage.co/) - API de dados financeiros
- [Yahoo Finance](https://finance.yahoo.com/) - Fonte de dados gratuita
- Comunidade Java e Spring

---

> 💡 **Dica**: Para uma experiência completa, execute tanto o frontend (porta 3000) quanto o backend (porta 8080) simultaneamente. O frontend está totalmente funcional para demonstrações, enquanto o backend fornece a lógica de negócio robusta.

⭐ **Se este projeto foi útil para você, considere dar uma estrela!** ⭐