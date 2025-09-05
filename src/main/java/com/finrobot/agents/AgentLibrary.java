package com.finrobot.agents;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Central registry and factory for all FinRobot AI Agents.
 * 
 * The AgentLibrary manages the lifecycle of all agents in the system,
 * providing registration, discovery, and instantiation capabilities.
 * It serves as the main entry point for accessing and managing agents
 * in the FinRobot platform.
 */
@Slf4j
@Component
public class AgentLibrary {
    
    private final Map<String, Agent> activeAgents = new ConcurrentHashMap<>();
    private final Map<Agent.AgentType, Class<? extends Agent>> agentTypeRegistry = new ConcurrentHashMap<>();
    private final Map<String, AgentRegistration> agentRegistrations = new ConcurrentHashMap<>();
    
    /**
     * Represents the registration information for an agent type.
     */
    public static class AgentRegistration {
        private final Agent.AgentType agentType;
        private final Class<? extends Agent> agentClass;
        private final String description;
        private final Set<AgentRequest.RequestType> supportedRequestTypes;
        private final Map<String, Object> defaultConfiguration;
        private final boolean enabled;
        
        public AgentRegistration(Agent.AgentType agentType, Class<? extends Agent> agentClass,
                               String description, Set<AgentRequest.RequestType> supportedRequestTypes,
                               Map<String, Object> defaultConfiguration, boolean enabled) {
            this.agentType = agentType;
            this.agentClass = agentClass;
            this.description = description;
            this.supportedRequestTypes = supportedRequestTypes;
            this.defaultConfiguration = defaultConfiguration;
            this.enabled = enabled;
        }
        
        // Getters
        public Agent.AgentType getAgentType() { return agentType; }
        public Class<? extends Agent> getAgentClass() { return agentClass; }
        public String getDescription() { return description; }
        public Set<AgentRequest.RequestType> getSupportedRequestTypes() { return supportedRequestTypes; }
        public Map<String, Object> getDefaultConfiguration() { return defaultConfiguration; }
        public boolean isEnabled() { return enabled; }
    }
    
    @PostConstruct
    public void initialize() {
        log.info("Initializing FinRobot Agent Library...");
        registerBuiltInAgents();
        log.info("Agent Library initialized with {} agent types", agentTypeRegistry.size());
    }
    
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down Agent Library...");
        shutdownAllAgents();
        log.info("Agent Library shutdown complete");
    }
    
    /**
     * Registers built-in agent types.
     */
    private void registerBuiltInAgents() {
        // Note: These would be actual implementations in a complete system
        // For now, we're registering the types without implementations
        
        registerAgentType(
            Agent.AgentType.MARKET_FORECASTING,
            null, // MarketForecastingAgent.class would go here
            "Predicts market trends and price movements using advanced ML models",
            Set.of(
                AgentRequest.RequestType.PRICE_PREDICTION,
                AgentRequest.RequestType.TREND_ANALYSIS,
                AgentRequest.RequestType.VOLATILITY_FORECAST
            ),
            Map.of(
                "chainOfThought", true,
                "maxAnalysisDepth", 5,
                "confidenceThreshold", 0.7
            ),
            true
        );
        
        registerAgentType(
            Agent.AgentType.DOCUMENT_ANALYSIS,
            null, // DocumentAnalysisAgent.class would go here
            "Analyzes financial documents, reports, and news articles",
            Set.of(
                AgentRequest.RequestType.EARNINGS_REPORT_ANALYSIS,
                AgentRequest.RequestType.NEWS_ANALYSIS,
                AgentRequest.RequestType.RESEARCH_REPORT_ANALYSIS
            ),
            Map.of(
                "supportedFormats", List.of("pdf", "txt", "csv", "html"),
                "maxFileSize", "10MB",
                "extractTables", true
            ),
            true
        );
        
        registerAgentType(
            Agent.AgentType.TRADING_STRATEGY,
            null, // TradingStrategyAgent.class would go here
            "Develops and backtests trading strategies",
            Set.of(
                AgentRequest.RequestType.STRATEGY_DEVELOPMENT,
                AgentRequest.RequestType.BACKTEST_STRATEGY,
                AgentRequest.RequestType.RISK_ASSESSMENT
            ),
            Map.of(
                "riskManagement", true,
                "backtestingEnabled", true,
                "maxDrawdown", 0.15
            ),
            true
        );
        
        registerAgentType(
            Agent.AgentType.NEWS_SENTIMENT,
            null, // NewsSentimentAgent.class would go here
            "Analyzes market sentiment from news and social media",
            Set.of(
                AgentRequest.RequestType.SENTIMENT_ANALYSIS,
                AgentRequest.RequestType.NEWS_ANALYSIS
            ),
            Map.of(
                "sources", List.of("reuters", "bloomberg", "cnbc"),
                "sentimentModel", "transformer",
                "realTimeUpdates", true
            ),
            true
        );
    }
    
    /**
     * Registers a new agent type in the library.
     * 
     * @param agentType the agent type
     * @param agentClass the agent implementation class
     * @param description description of the agent
     * @param supportedRequestTypes types of requests the agent can handle
     * @param defaultConfiguration default configuration for the agent
     * @param enabled whether the agent type is enabled
     */
    public void registerAgentType(Agent.AgentType agentType, Class<? extends Agent> agentClass,
                                String description, Set<AgentRequest.RequestType> supportedRequestTypes,
                                Map<String, Object> defaultConfiguration, boolean enabled) {
        
        AgentRegistration registration = new AgentRegistration(
            agentType, agentClass, description, supportedRequestTypes, defaultConfiguration, enabled
        );
        
        agentRegistrations.put(agentType.name(), registration);
        
        if (agentClass != null) {
            agentTypeRegistry.put(agentType, agentClass);
        }
        
        log.info("Registered agent type: {} - {}", agentType.getDisplayName(), description);
    }
    
    /**
     * Creates and initializes a new agent instance.
     * 
     * @param agentType the type of agent to create
     * @param configuration optional configuration (uses defaults if null)
     * @return the created and initialized agent
     * @throws IllegalArgumentException if agent type is not registered or disabled
     * @throws RuntimeException if agent creation fails
     */
    public Agent createAgent(Agent.AgentType agentType, Map<String, Object> configuration) {
        AgentRegistration registration = agentRegistrations.get(agentType.name());
        
        if (registration == null) {
            throw new IllegalArgumentException("Agent type not registered: " + agentType);
        }
        
        if (!registration.isEnabled()) {
            throw new IllegalArgumentException("Agent type is disabled: " + agentType);
        }
        
        Class<? extends Agent> agentClass = registration.getAgentClass();
        if (agentClass == null) {
            throw new RuntimeException("Agent implementation not available for type: " + agentType);
        }
        
        try {
            // Create agent instance
            Agent agent = agentClass.getDeclaredConstructor().newInstance();
            
            // Merge default configuration with provided configuration
            Map<String, Object> finalConfig = new HashMap<>(registration.getDefaultConfiguration());
            if (configuration != null) {
                finalConfig.putAll(configuration);
            }
            
            // Initialize the agent
            agent.initialize(finalConfig);
            
            // Register the active agent
            activeAgents.put(agent.getAgentId(), agent);
            
            log.info("Created and initialized agent: {} ({})", agent.getAgentId(), agentType.getDisplayName());
            
            return agent;
            
        } catch (Exception e) {
            log.error("Failed to create agent of type {}: {}", agentType, e.getMessage(), e);
            throw new RuntimeException("Agent creation failed", e);
        }
    }
    
    /**
     * Creates an agent with default configuration.
     * 
     * @param agentType the type of agent to create
     * @return the created agent
     */
    public Agent createAgent(Agent.AgentType agentType) {
        return createAgent(agentType, null);
    }
    
    /**
     * Gets an active agent by ID.
     * 
     * @param agentId the agent ID
     * @return the agent, or null if not found
     */
    public Agent getAgent(String agentId) {
        return activeAgents.get(agentId);
    }
    
    /**
     * Gets all active agents of a specific type.
     * 
     * @param agentType the agent type
     * @return list of active agents of the specified type
     */
    public List<Agent> getAgentsByType(Agent.AgentType agentType) {
        return activeAgents.values().stream()
                .filter(agent -> agent.getAgentType() == agentType)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all active agents.
     * 
     * @return collection of all active agents
     */
    public Collection<Agent> getAllActiveAgents() {
        return new ArrayList<>(activeAgents.values());
    }
    
    /**
     * Finds agents that can handle a specific request type.
     * 
     * @param requestType the request type
     * @return list of agent types that can handle the request
     */
    public List<Agent.AgentType> findAgentsForRequestType(AgentRequest.RequestType requestType) {
        return agentRegistrations.values().stream()
                .filter(reg -> reg.isEnabled() && reg.getSupportedRequestTypes().contains(requestType))
                .map(AgentRegistration::getAgentType)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets registration information for all agent types.
     * 
     * @return map of agent type name to registration info
     */
    public Map<String, AgentRegistration> getAllRegistrations() {
        return new HashMap<>(agentRegistrations);
    }
    
    /**
     * Gets registration information for a specific agent type.
     * 
     * @param agentType the agent type
     * @return registration info, or null if not found
     */
    public AgentRegistration getRegistration(Agent.AgentType agentType) {
        return agentRegistrations.get(agentType.name());
    }
    
    /**
     * Removes an agent from the active agents registry.
     * 
     * @param agentId the agent ID to remove
     * @return true if the agent was removed, false if not found
     */
    public boolean removeAgent(String agentId) {
        Agent agent = activeAgents.remove(agentId);
        if (agent != null) {
            try {
                agent.shutdown();
                log.info("Removed and shut down agent: {}", agentId);
                return true;
            } catch (Exception e) {
                log.error("Error shutting down agent {}: {}", agentId, e.getMessage(), e);
            }
        }
        return false;
    }
    
    /**
     * Shuts down all active agents.
     */
    public void shutdownAllAgents() {
        List<String> agentIds = new ArrayList<>(activeAgents.keySet());
        for (String agentId : agentIds) {
            removeAgent(agentId);
        }
    }
    
    /**
     * Gets statistics about the agent library.
     * 
     * @return statistics map
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRegisteredTypes", agentRegistrations.size());
        stats.put("enabledTypes", agentRegistrations.values().stream().mapToLong(reg -> reg.isEnabled() ? 1 : 0).sum());
        stats.put("totalActiveAgents", activeAgents.size());
        
        Map<String, Long> agentsByType = activeAgents.values().stream()
                .collect(Collectors.groupingBy(
                    agent -> agent.getAgentType().name(),
                    Collectors.counting()
                ));
        stats.put("activeAgentsByType", agentsByType);
        
        return stats;
    }
}