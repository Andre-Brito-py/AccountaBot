package com.finrobot.agents;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Abstract base implementation of the Agent interface.
 * 
 * This class provides common functionality for all FinRobot agents,
 * including metrics tracking, lifecycle management, and the core
 * execution framework following the Perception -> Brain -> Action pattern.
 */
@Slf4j
public abstract class BaseAgent implements Agent {
    
    protected final String agentId;
    protected final AgentType agentType;
    protected AgentStatus status;
    protected AgentMetrics metrics;
    protected Map<String, Object> configuration;
    protected ExecutorService executorService;
    
    /**
     * Constructor for BaseAgent.
     * @param agentType the type of agent
     */
    protected BaseAgent(AgentType agentType) {
        this.agentId = generateAgentId(agentType);
        this.agentType = agentType;
        this.status = AgentStatus.INITIALIZING;
        this.metrics = AgentMetrics.builder()
                .agentId(agentId)
                .startTime(LocalDateTime.now())
                .build();
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "Agent-" + agentId + "-Worker");
            t.setDaemon(true);
            return t;
        });
        
        log.info("Initialized {} with ID: {}", agentType.getDisplayName(), agentId);
    }
    
    @Override
    public String getAgentId() {
        return agentId;
    }
    
    @Override
    public AgentType getAgentType() {
        return agentType;
    }
    
    @Override
    public AgentStatus getStatus() {
        return status;
    }
    
    @Override
    public AgentMetrics getMetrics() {
        return metrics;
    }
    
    @Override
    public void initialize(Map<String, Object> config) {
        try {
            this.configuration = config;
            doInitialize(config);
            this.status = AgentStatus.READY;
            log.info("Agent {} initialized successfully", agentId);
        } catch (Exception e) {
            this.status = AgentStatus.ERROR;
            log.error("Failed to initialize agent {}: {}", agentId, e.getMessage(), e);
            throw new RuntimeException("Agent initialization failed", e);
        }
    }
    
    @Override
    public CompletableFuture<AgentResponse> execute(AgentRequest request) {
        if (status != AgentStatus.READY) {
            return CompletableFuture.completedFuture(
                createErrorResponse(request, "Agent is not ready. Current status: " + status)
            );
        }
        
        if (!canHandle(request)) {
            return CompletableFuture.completedFuture(
                createErrorResponse(request, "Agent cannot handle this type of request: " + request.getRequestType())
            );
        }
        
        return CompletableFuture.supplyAsync(() -> {
            LocalDateTime startTime = LocalDateTime.now();
            metrics.incrementActiveRequests();
            status = AgentStatus.BUSY;
            
            try {
                log.debug("Processing request {} with agent {}", request.getRequestId(), agentId);
                
                // Execute the core agent workflow: Perception -> Brain -> Action
                AgentResponse response = executeWorkflow(request);
                
                // Calculate processing time and update metrics
                response.setProcessingStartTime(startTime);
                response.calculateProcessingDuration();
                
                metrics.recordRequest(
                    response.getProcessingDurationMs(),
                    response.getConfidence(),
                    response.isSuccessful()
                );
                
                log.debug("Completed request {} in {}ms", 
                    request.getRequestId(), response.getProcessingDurationMs());
                
                return response;
                
            } catch (Exception e) {
                log.error("Error processing request {}: {}", request.getRequestId(), e.getMessage(), e);
                
                AgentResponse errorResponse = createErrorResponse(request, e.getMessage());
                errorResponse.setProcessingStartTime(startTime);
                errorResponse.calculateProcessingDuration();
                
                metrics.recordRequest(
                    errorResponse.getProcessingDurationMs(),
                    0.0,
                    false
                );
                
                return errorResponse;
                
            } finally {
                metrics.decrementActiveRequests();
                status = AgentStatus.READY;
            }
        }, executorService);
    }
    
    @Override
    public void shutdown() {
        try {
            status = AgentStatus.SHUTDOWN;
            doShutdown();
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }
            log.info("Agent {} shut down successfully", agentId);
        } catch (Exception e) {
            log.error("Error during agent {} shutdown: {}", agentId, e.getMessage(), e);
        }
    }
    
    /**
     * Executes the core agent workflow: Perception -> Brain -> Action.
     * This method implements the main processing logic following the FinRobot architecture.
     * 
     * @param request the request to process
     * @return the agent response
     */
    protected AgentResponse executeWorkflow(AgentRequest request) {
        AgentResponse.AgentResponseBuilder responseBuilder = AgentResponse.builder()
                .requestId(request.getRequestId())
                .agentId(agentId)
                .status(AgentResponse.ResponseStatus.SUCCESS);
        
        try {
            // Step 1: Perception - Capture and interpret input data
            Object perceivedData = perceive(request);
            log.debug("Perception phase completed for request {}", request.getRequestId());
            
            // Step 2: Brain - Process data using LLMs with Chain-of-Thought reasoning
            Object processedData = think(perceivedData, request);
            log.debug("Brain phase completed for request {}", request.getRequestId());
            
            // Step 3: Action - Execute instructions and generate actionable outcomes
            AgentResponse response = act(processedData, request, responseBuilder);
            log.debug("Action phase completed for request {}", request.getRequestId());
            
            return response;
            
        } catch (Exception e) {
            log.error("Workflow execution failed for request {}: {}", request.getRequestId(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Perception phase: Capture and interpret multimodal financial data.
     * Subclasses should override this method to implement specific data perception logic.
     * 
     * @param request the input request
     * @return perceived and structured data
     */
    protected abstract Object perceive(AgentRequest request);
    
    /**
     * Brain phase: Process data using LLMs with Chain-of-Thought reasoning.
     * Subclasses should override this method to implement specific thinking logic.
     * 
     * @param perceivedData data from the perception phase
     * @param request the original request
     * @return processed insights and analysis
     */
    protected abstract Object think(Object perceivedData, AgentRequest request);
    
    /**
     * Action phase: Execute instructions and generate actionable outcomes.
     * Subclasses should override this method to implement specific action logic.
     * 
     * @param processedData data from the brain phase
     * @param request the original request
     * @param responseBuilder builder for constructing the response
     * @return the final agent response
     */
    protected abstract AgentResponse act(Object processedData, AgentRequest request, 
                                       AgentResponse.AgentResponseBuilder responseBuilder);
    
    /**
     * Agent-specific initialization logic.
     * Subclasses should override this method to implement custom initialization.
     * 
     * @param config configuration parameters
     */
    protected abstract void doInitialize(Map<String, Object> config);
    
    /**
     * Agent-specific shutdown logic.
     * Subclasses should override this method to implement custom cleanup.
     */
    protected abstract void doShutdown();
    
    /**
     * Creates an error response for failed requests.
     * 
     * @param request the original request
     * @param errorMessage the error message
     * @return error response
     */
    protected AgentResponse createErrorResponse(AgentRequest request, String errorMessage) {
        return AgentResponse.builder()
                .requestId(request.getRequestId())
                .agentId(agentId)
                .status(AgentResponse.ResponseStatus.ERROR)
                .error(AgentResponse.ErrorInfo.builder()
                        .errorMessage(errorMessage)
                        .errorTime(LocalDateTime.now())
                        .build())
                .confidence(0.0)
                .build();
    }
    
    /**
     * Generates a unique agent ID based on the agent type.
     * 
     * @param agentType the agent type
     * @return unique agent ID
     */
    private String generateAgentId(AgentType agentType) {
        return agentType.name().toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Gets a configuration value with type casting.
     * 
     * @param key configuration key
     * @param type expected type
     * @param defaultValue default value if not found
     * @param <T> type parameter
     * @return configuration value or default
     */
    @SuppressWarnings("unchecked")
    protected <T> T getConfigValue(String key, Class<T> type, T defaultValue) {
        if (configuration == null) {
            return defaultValue;
        }
        
        Object value = configuration.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        
        return defaultValue;
    }
}