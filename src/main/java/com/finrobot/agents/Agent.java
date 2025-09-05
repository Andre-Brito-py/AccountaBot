package com.finrobot.agents;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Base interface for all FinRobot AI Agents.
 * 
 * An AI Agent is an intelligent entity that uses large language models as its brain
 * to perceive its environment, make decisions, and execute actions. Unlike traditional
 * artificial intelligence, AI Agents possess the ability to independently think and
 * utilize tools to progressively achieve given objectives.
 * 
 * This interface defines the core contract that all financial AI agents must implement,
 * following the FinRobot architecture:
 * - Perception: Capture and interpret multimodal financial data
 * - Brain: Process data using LLMs with Chain-of-Thought reasoning
 * - Action: Execute instructions and generate actionable outcomes
 */
public interface Agent {
    
    /**
     * Gets the unique identifier for this agent.
     * @return agent ID
     */
    String getAgentId();
    
    /**
     * Gets the agent type (e.g., "MARKET_FORECASTING", "DOCUMENT_ANALYSIS", "TRADING_STRATEGY")
     * @return agent type
     */
    AgentType getAgentType();
    
    /**
     * Gets the current status of the agent.
     * @return agent status
     */
    AgentStatus getStatus();
    
    /**
     * Initializes the agent with configuration parameters.
     * @param config configuration parameters
     */
    void initialize(Map<String, Object> config);
    
    /**
     * Executes the agent's main processing logic.
     * This method implements the core Perception -> Brain -> Action workflow.
     * 
     * @param input input data for processing
     * @return CompletableFuture containing the agent's response
     */
    CompletableFuture<AgentResponse> execute(AgentRequest input);
    
    /**
     * Validates if the agent can handle the given request.
     * @param request the request to validate
     * @return true if the agent can handle the request
     */
    boolean canHandle(AgentRequest request);
    
    /**
     * Gets the agent's performance metrics.
     * @return performance metrics
     */
    AgentMetrics getMetrics();
    
    /**
     * Shuts down the agent and releases resources.
     */
    void shutdown();
    
    /**
     * Enum defining the types of agents available in FinRobot.
     */
    enum AgentType {
        MARKET_FORECASTING("Market Forecasting Agent", "Predicts market trends and price movements"),
        DOCUMENT_ANALYSIS("Document Analysis Agent", "Analyzes financial documents and reports"),
        TRADING_STRATEGY("Trading Strategy Agent", "Develops and backtests trading strategies"),
        RISK_MANAGEMENT("Risk Management Agent", "Assesses and manages financial risks"),
        PORTFOLIO_OPTIMIZATION("Portfolio Optimization Agent", "Optimizes investment portfolios"),
        NEWS_SENTIMENT("News Sentiment Agent", "Analyzes market sentiment from news and social media");
        
        private final String displayName;
        private final String description;
        
        AgentType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Enum defining the possible states of an agent.
     */
    enum AgentStatus {
        INITIALIZING("Agent is being initialized"),
        READY("Agent is ready to process requests"),
        BUSY("Agent is currently processing a request"),
        ERROR("Agent encountered an error"),
        SHUTDOWN("Agent has been shut down");
        
        private final String description;
        
        AgentStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}