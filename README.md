# AccountaBot 🤖💰

> Sistema de análise financeira inteligente com agentes de IA e integração de múltiplas fontes de dados

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0+-green.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Sobre o Projeto

O **AccountaBot** é um sistema avançado de análise financeira que utiliza inteligência artificial e arquitetura baseada em agentes para fornecer insights financeiros precisos e automatizados. O sistema integra múltiplas fontes de dados financeiros e oferece análises quantitativas sofisticadas.

### 🎯 Principais Funcionalidades

- **🤖 Sistema de Agentes Inteligentes**: Arquitetura baseada em agentes com workflow automatizado
- **📊 Múltiplas Fontes de Dados**: Integração com Yahoo Finance, Alpha Vantage e outras APIs
- **📈 Análise Quantitativa**: Módulos especializados para análise técnica e fundamental
- **⚡ Processamento Assíncrono**: Operações não-bloqueantes para alta performance
- **📋 Gerenciamento de Cache**: Sistema inteligente de cache para otimização
- **🔄 Rate Limiting**: Controle automático de limites de API
- **📊 Métricas e Monitoramento**: Sistema completo de métricas e observabilidade

## 🏗️ Arquitetura

### Componentes Principais

```
📦 AccountaBot
├── 🤖 Agentes (Agents)
│   ├── AgentLibrary - Biblioteca de agentes especializados
│   ├── WorkflowManager - Gerenciamento de fluxos de trabalho
│   ├── SmartScheduler - Agendamento inteligente de tarefas
│   └── BaseAgent - Classe base para todos os agentes
├── 📊 Fontes de Dados (DataSources)
│   ├── AlphaVantageDataSource - Integração Alpha Vantage
│   ├── YahooFinanceDataSource - Integração Yahoo Finance
│   └── DataSourceManager - Gerenciador centralizado
└── 🔧 Módulos Funcionais
    ├── Analyzer - Análise de dados financeiros
    ├── Charting - Geração de gráficos e visualizações
    └── Quantitative - Análises quantitativas avançadas
```

### Tipos de Dados Suportados

- **💰 Dados de Preços**: Cotações em tempo real e históricas
- **📊 Dados Fundamentais**: Balanços, demonstrativos e métricas
- **📰 Notícias Financeiras**: Análise de sentimento e impacto
- **📈 Dados Econômicos**: Indicadores macroeconômicos
- **🔍 Informações de Instrumentos**: Metadados de ativos financeiros

## 🚀 Instalação e Configuração

### Pré-requisitos

- **Java 17+**
- **Maven 3.6+**
- **Spring Boot 3.0+**

### Clonando o Repositório

```bash
git clone https://github.com/Andre-Brito-py/AccountaBot.git
cd AccountaBot
```

### Configuração

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

3. **Execute os testes**:

```bash
mvn test
```

4. **Execute a aplicação**:

```bash
mvn spring-boot:run
```

## 💡 Uso

### Exemplo Básico

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

### Configuração de Agentes

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

## 📝 Roadmap

- [ ] **Chain-of-Thought Prompting**: Implementação de raciocínio avançado
- [ ] **Interface Web**: Dashboard para visualização de dados
- [ ] **API REST**: Endpoints para integração externa
- [ ] **Machine Learning**: Modelos preditivos integrados
- [ ] **Alertas Inteligentes**: Sistema de notificações automáticas
- [ ] **Backtesting**: Framework para teste de estratégias

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

⭐ **Se este projeto foi útil para você, considere dar uma estrela!** ⭐