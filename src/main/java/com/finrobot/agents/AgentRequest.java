package com.finrobot.agents;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a request sent to an AI Agent for processing.
 * 
 * This class encapsulates all the information needed by an agent to process
 * a financial analysis task, including the request type, input data, context,
 * and processing preferences.
 */
@Data
@Builder
public class AgentRequest {
    
    /**
     * Unique identifier for this request
     */
    @Builder.Default
    private String requestId = UUID.randomUUID().toString();
    
    /**
     * Type of request being made
     */
    @NonNull
    private RequestType requestType;
    
    /**
     * The main input data for processing
     */
    @NonNull
    private Object inputData;
    
    /**
     * Additional context information
     */
    private Map<String, Object> context;
    
    /**
     * Processing parameters and preferences
     */
    private Map<String, Object> parameters;
    
    /**
     * Timestamp when the request was created
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Priority level of the request
     */
    @Builder.Default
    private Priority priority = Priority.MEDIUM;
    
    /**
     * Maximum processing time allowed (in milliseconds)
     */
    @Builder.Default
    private long timeoutMs = 30000; // 30 seconds default
    
    /**
     * Whether to use Chain-of-Thought reasoning
     */
    @Builder.Default
    private boolean useChainOfThought = true;
    
    /**
     * User or system that initiated the request
     */
    private String requestor;
    
    /**
     * Session ID for tracking related requests
     */
    private String sessionId;
    
    /**
     * Enum defining the types of requests that can be made to agents.
     */
    public enum RequestType {
        // Market Forecasting requests
        PRICE_PREDICTION("Predict future price movements"),
        TREND_ANALYSIS("Analyze market trends"),
        VOLATILITY_FORECAST("Forecast market volatility"),
        
        // Document Analysis requests
        EARNINGS_REPORT_ANALYSIS("Analyze earnings reports"),
        NEWS_ANALYSIS("Analyze news articles"),
        RESEARCH_REPORT_ANALYSIS("Analyze research reports"),
        
        // Trading Strategy requests
        STRATEGY_DEVELOPMENT("Develop trading strategies"),
        BACKTEST_STRATEGY("Backtest trading strategies"),
        RISK_ASSESSMENT("Assess trading risks"),
        
        // Portfolio requests
        PORTFOLIO_OPTIMIZATION("Optimize investment portfolio"),
        ASSET_ALLOCATION("Determine optimal asset allocation"),
        REBALANCING_RECOMMENDATION("Recommend portfolio rebalancing"),
        
        // General Analysis requests
        SENTIMENT_ANALYSIS("Analyze market sentiment"),
        CORRELATION_ANALYSIS("Analyze asset correlations"),
        SCHEDULED_ANALYSIS("Scheduled automated analysis"),
        CUSTOM_ANALYSIS("Custom financial analysis");
        
        private final String description;
        
        RequestType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Enum defining request priority levels.
     */
    public enum Priority {
        LOW(1, "Low priority - process when resources available"),
        MEDIUM(2, "Medium priority - normal processing"),
        HIGH(3, "High priority - expedited processing"),
        URGENT(4, "Urgent priority - immediate processing");
        
        private final int level;
        private final String description;
        
        Priority(int level, String description) {
            this.level = level;
            this.description = description;
        }
        
        public int getLevel() {
            return level;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Convenience method to get a parameter value with type casting.
     * @param key parameter key
     * @param type expected type
     * @param <T> type parameter
     * @return parameter value cast to the specified type, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, Class<T> type) {
        if (parameters == null) {
            return null;
        }
        Object value = parameters.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Convenience method to get a context value with type casting.
     * @param key context key
     * @param type expected type
     * @param <T> type parameter
     * @return context value cast to the specified type, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getContext(String key, Class<T> type) {
        if (context == null) {
            return null;
        }
        Object value = context.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
}