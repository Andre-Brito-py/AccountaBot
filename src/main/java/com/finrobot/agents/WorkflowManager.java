package com.finrobot.agents;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Manages complex workflows involving multiple AI agents.
 * 
 * The WorkflowManager orchestrates the execution of multiple agents in sequence
 * or parallel, handling dependencies, data flow, and error recovery.
 * It provides a high-level abstraction for creating sophisticated
 * multi-agent financial analysis workflows.
 */
@Slf4j
@Component
public class WorkflowManager {
    
    @Autowired
    private AgentLibrary agentLibrary;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final Map<String, WorkflowExecution> activeWorkflows = new ConcurrentHashMap<>();
    
    /**
     * Represents a workflow definition with steps and dependencies.
     */
    @Data
    public static class WorkflowDefinition {
        private String workflowId;
        private String name;
        private String description;
        private List<WorkflowStep> steps;
        private Map<String, Object> globalContext;
        private WorkflowExecutionMode executionMode;
        private int maxRetries;
        private long timeoutMinutes;
        
        public WorkflowDefinition(String workflowId, String name) {
            this.workflowId = workflowId;
            this.name = name;
            this.steps = new ArrayList<>();
            this.globalContext = new HashMap<>();
            this.executionMode = WorkflowExecutionMode.SEQUENTIAL;
            this.maxRetries = 3;
            this.timeoutMinutes = 30;
        }
    }
    
    /**
     * Represents a single step in a workflow.
     */
    @Data
    public static class WorkflowStep {
        private String stepId;
        private String name;
        private Agent.AgentType agentType;
        private AgentRequest.RequestType requestType;
        private Map<String, Object> stepConfiguration;
        private Map<String, Object> requestParameters;
        private List<String> dependencies; // Step IDs this step depends on
        private boolean optional;
        private int maxRetries;
        private long timeoutMinutes;
        
        public WorkflowStep(String stepId, String name, Agent.AgentType agentType, AgentRequest.RequestType requestType) {
            this.stepId = stepId;
            this.name = name;
            this.agentType = agentType;
            this.requestType = requestType;
            this.stepConfiguration = new HashMap<>();
            this.requestParameters = new HashMap<>();
            this.dependencies = new ArrayList<>();
            this.optional = false;
            this.maxRetries = 3;
            this.timeoutMinutes = 10;
        }
    }
    
    /**
     * Represents the execution state of a workflow.
     */
    @Data
    public static class WorkflowExecution {
        private String executionId;
        private WorkflowDefinition definition;
        private WorkflowStatus status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Map<String, StepExecution> stepExecutions;
        private Map<String, Object> workflowContext;
        private List<String> errors;
        private AgentResponse finalResult;
        
        public WorkflowExecution(String executionId, WorkflowDefinition definition) {
            this.executionId = executionId;
            this.definition = definition;
            this.status = WorkflowStatus.PENDING;
            this.stepExecutions = new ConcurrentHashMap<>();
            this.workflowContext = new ConcurrentHashMap<>(definition.getGlobalContext());
            this.errors = new ArrayList<>();
        }
    }
    
    /**
     * Represents the execution state of a workflow step.
     */
    @Data
    public static class StepExecution {
        private String stepId;
        private WorkflowStep step;
        private StepStatus status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Agent agent;
        private AgentRequest request;
        private AgentResponse response;
        private int attemptCount;
        private List<String> errors;
        
        public StepExecution(WorkflowStep step) {
            this.stepId = step.getStepId();
            this.step = step;
            this.status = StepStatus.PENDING;
            this.attemptCount = 0;
            this.errors = new ArrayList<>();
        }
    }
    
    public enum WorkflowExecutionMode {
        SEQUENTIAL,    // Execute steps one by one
        PARALLEL,      // Execute independent steps in parallel
        MIXED          // Mix of sequential and parallel execution
    }
    
    public enum WorkflowStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED,
        TIMEOUT
    }
    
    public enum StepStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        SKIPPED,
        CANCELLED
    }
    
    /**
     * Executes a workflow asynchronously.
     * 
     * @param definition the workflow definition
     * @return CompletableFuture with the workflow execution result
     */
    public CompletableFuture<WorkflowExecution> executeWorkflowAsync(WorkflowDefinition definition) {
        String executionId = UUID.randomUUID().toString();
        WorkflowExecution execution = new WorkflowExecution(executionId, definition);
        
        activeWorkflows.put(executionId, execution);
        
        log.info("Starting workflow execution: {} ({})", executionId, definition.getName());
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeWorkflow(execution);
            } catch (Exception e) {
                log.error("Workflow execution failed: {}", executionId, e);
                execution.setStatus(WorkflowStatus.FAILED);
                execution.getErrors().add("Workflow execution failed: " + e.getMessage());
                execution.setEndTime(LocalDateTime.now());
                return execution;
            } finally {
                activeWorkflows.remove(executionId);
            }
        }, executorService);
    }
    
    /**
     * Executes a workflow synchronously.
     * 
     * @param definition the workflow definition
     * @return the workflow execution result
     */
    public WorkflowExecution executeWorkflow(WorkflowDefinition definition) {
        String executionId = UUID.randomUUID().toString();
        WorkflowExecution execution = new WorkflowExecution(executionId, definition);
        
        activeWorkflows.put(executionId, execution);
        
        try {
            return executeWorkflow(execution);
        } finally {
            activeWorkflows.remove(executionId);
        }
    }
    
    /**
     * Internal method to execute a workflow.
     */
    private WorkflowExecution executeWorkflow(WorkflowExecution execution) {
        execution.setStatus(WorkflowStatus.RUNNING);
        execution.setStartTime(LocalDateTime.now());
        
        try {
            // Initialize step executions
            for (WorkflowStep step : execution.getDefinition().getSteps()) {
                execution.getStepExecutions().put(step.getStepId(), new StepExecution(step));
            }
            
            // Execute steps based on execution mode
            switch (execution.getDefinition().getExecutionMode()) {
                case SEQUENTIAL:
                    executeSequential(execution);
                    break;
                case PARALLEL:
                    executeParallel(execution);
                    break;
                case MIXED:
                    executeMixed(execution);
                    break;
            }
            
            // Determine final status
            boolean hasFailures = execution.getStepExecutions().values().stream()
                    .anyMatch(step -> step.getStatus() == StepStatus.FAILED && !step.getStep().isOptional());
            
            if (hasFailures) {
                execution.setStatus(WorkflowStatus.FAILED);
            } else {
                execution.setStatus(WorkflowStatus.COMPLETED);
                
                // Create final result by aggregating step results
                execution.setFinalResult(aggregateResults(execution));
            }
            
        } catch (Exception e) {
            log.error("Workflow execution error: {}", execution.getExecutionId(), e);
            execution.setStatus(WorkflowStatus.FAILED);
            execution.getErrors().add("Execution error: " + e.getMessage());
        } finally {
            execution.setEndTime(LocalDateTime.now());
        }
        
        log.info("Workflow execution completed: {} - Status: {}", 
                execution.getExecutionId(), execution.getStatus());
        
        return execution;
    }
    
    /**
     * Executes workflow steps sequentially.
     */
    private void executeSequential(WorkflowExecution execution) {
        for (WorkflowStep step : execution.getDefinition().getSteps()) {
            if (!canExecuteStep(execution, step)) {
                continue;
            }
            
            executeStep(execution, step);
            
            // Stop if a required step failed
            StepExecution stepExecution = execution.getStepExecutions().get(step.getStepId());
            if (stepExecution.getStatus() == StepStatus.FAILED && !step.isOptional()) {
                log.warn("Required step failed, stopping workflow: {}", step.getStepId());
                break;
            }
        }
    }
    
    /**
     * Executes workflow steps in parallel where possible.
     */
    private void executeParallel(WorkflowExecution execution) {
        Set<String> completedSteps = new HashSet<>();
        List<WorkflowStep> remainingSteps = new ArrayList<>(execution.getDefinition().getSteps());
        
        while (!remainingSteps.isEmpty()) {
            // Find steps that can be executed (dependencies satisfied)
            List<WorkflowStep> executableSteps = remainingSteps.stream()
                    .filter(step -> canExecuteStep(execution, step, completedSteps))
                    .collect(Collectors.toList());
            
            if (executableSteps.isEmpty()) {
                log.error("No executable steps found, possible circular dependency");
                break;
            }
            
            // Execute steps in parallel
            List<CompletableFuture<Void>> futures = executableSteps.stream()
                    .map(step -> CompletableFuture.runAsync(() -> executeStep(execution, step), executorService))
                    .collect(Collectors.toList());
            
            // Wait for all to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            // Update completed steps
            for (WorkflowStep step : executableSteps) {
                StepExecution stepExecution = execution.getStepExecutions().get(step.getStepId());
                if (stepExecution.getStatus() == StepStatus.COMPLETED) {
                    completedSteps.add(step.getStepId());
                }
            }
            
            remainingSteps.removeAll(executableSteps);
        }
    }
    
    /**
     * Executes workflow with mixed sequential and parallel execution.
     */
    private void executeMixed(WorkflowExecution execution) {
        // For now, use parallel execution
        // In a more sophisticated implementation, this would analyze
        // dependencies and create execution groups
        executeParallel(execution);
    }
    
    /**
     * Checks if a step can be executed (dependencies satisfied).
     */
    private boolean canExecuteStep(WorkflowExecution execution, WorkflowStep step) {
        return canExecuteStep(execution, step, 
                execution.getStepExecutions().entrySet().stream()
                        .filter(entry -> entry.getValue().getStatus() == StepStatus.COMPLETED)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet()));
    }
    
    /**
     * Checks if a step can be executed with given completed steps.
     */
    private boolean canExecuteStep(WorkflowExecution execution, WorkflowStep step, Set<String> completedSteps) {
        return step.getDependencies().stream().allMatch(completedSteps::contains);
    }
    
    /**
     * Executes a single workflow step.
     */
    private void executeStep(WorkflowExecution execution, WorkflowStep step) {
        StepExecution stepExecution = execution.getStepExecutions().get(step.getStepId());
        stepExecution.setStatus(StepStatus.RUNNING);
        stepExecution.setStartTime(LocalDateTime.now());
        
        log.info("Executing workflow step: {} ({})", step.getStepId(), step.getName());
        
        try {
            // Create agent
            Agent agent = agentLibrary.createAgent(step.getAgentType(), step.getStepConfiguration());
            stepExecution.setAgent(agent);
            
            // Prepare request
            AgentRequest request = createStepRequest(execution, step);
            stepExecution.setRequest(request);
            
            // Execute with retries
            AgentResponse response = executeWithRetries(agent, request, step.getMaxRetries());
            stepExecution.setResponse(response);
            
            // Update workflow context with step results
            updateWorkflowContext(execution, step, response);
            
            stepExecution.setStatus(StepStatus.COMPLETED);
            log.info("Step completed successfully: {}", step.getStepId());
            
        } catch (Exception e) {
            log.error("Step execution failed: {}", step.getStepId(), e);
            stepExecution.getErrors().add("Step execution failed: " + e.getMessage());
            
            if (step.isOptional()) {
                stepExecution.setStatus(StepStatus.SKIPPED);
                log.info("Optional step failed, skipping: {}", step.getStepId());
            } else {
                stepExecution.setStatus(StepStatus.FAILED);
            }
        } finally {
            stepExecution.setEndTime(LocalDateTime.now());
        }
    }
    
    /**
     * Creates an agent request for a workflow step.
     */
    private AgentRequest createStepRequest(WorkflowExecution execution, WorkflowStep step) {
        AgentRequest request = AgentRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .requestType(step.getRequestType())
                .inputData(step.getRequestParameters())
                .priority(AgentRequest.Priority.MEDIUM)
                .build();
        
        // Add step parameters
        request.getParameters().putAll(step.getRequestParameters());
        
        // Add workflow context
        request.getContext().putAll(execution.getWorkflowContext());
        
        // Add results from dependency steps
        for (String dependencyId : step.getDependencies()) {
            StepExecution depExecution = execution.getStepExecutions().get(dependencyId);
            if (depExecution != null && depExecution.getResponse() != null) {
                request.getContext().put("step_" + dependencyId + "_result", depExecution.getResponse().getData());
                request.getContext().put("step_" + dependencyId + "_insights", depExecution.getResponse().getInsights());
            }
        }
        
        return request;
    }
    
    /**
     * Executes an agent request with retries.
     */
    private AgentResponse executeWithRetries(Agent agent, AgentRequest request, int maxRetries) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                CompletableFuture<AgentResponse> future = agent.execute(request);
                return future.get();
            } catch (Exception e) {
                lastException = e;
                log.warn("Agent execution attempt {} failed: {}", attempt, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000 * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        throw new RuntimeException("Agent execution failed after " + maxRetries + " attempts", lastException);
    }
    
    /**
     * Updates the workflow context with step results.
     */
    private void updateWorkflowContext(WorkflowExecution execution, WorkflowStep step, AgentResponse response) {
        execution.getWorkflowContext().put(step.getStepId() + "_result", response.getData());
        execution.getWorkflowContext().put(step.getStepId() + "_insights", response.getInsights());
        execution.getWorkflowContext().put(step.getStepId() + "_confidence", response.getConfidenceScore());
    }
    
    /**
     * Aggregates results from all completed steps into a final result.
     */
    private AgentResponse aggregateResults(WorkflowExecution execution) {
        AgentResponse finalResult = AgentResponse.builder()
                .responseId(UUID.randomUUID().toString())
                .requestId("WORKFLOW_RESULT")
                .agentId("WORKFLOW_MANAGER")
                .status(AgentResponse.ResponseStatus.SUCCESS)
                .build();
        
        Map<String, Object> aggregatedData = new HashMap<>();
        List<String> aggregatedInsights = new ArrayList<>();
        List<String> aggregatedRecommendations = new ArrayList<>();
        double totalConfidence = 0.0;
        int completedSteps = 0;
        
        for (StepExecution stepExecution : execution.getStepExecutions().values()) {
            if (stepExecution.getStatus() == StepStatus.COMPLETED && stepExecution.getResponse() != null) {
                AgentResponse response = stepExecution.getResponse();
                
                aggregatedData.put(stepExecution.getStepId(), response.getData());
                aggregatedInsights.addAll(response.getInsights());
                aggregatedRecommendations.addAll(response.getRecommendations());
                totalConfidence += response.getConfidenceScore();
                completedSteps++;
            }
        }
        
        finalResult.setData(aggregatedData);
        finalResult.setInsights(aggregatedInsights);
        finalResult.setRecommendations(aggregatedRecommendations);
        
        if (completedSteps > 0) {
            finalResult.setConfidenceScore(totalConfidence / completedSteps);
        }
        
        finalResult.setEndTime(LocalDateTime.now());
        
        return finalResult;
    }
    
    /**
     * Gets the status of a workflow execution.
     * 
     * @param executionId the execution ID
     * @return the workflow execution, or null if not found
     */
    public WorkflowExecution getWorkflowExecution(String executionId) {
        return activeWorkflows.get(executionId);
    }
    
    /**
     * Cancels a running workflow.
     * 
     * @param executionId the execution ID
     * @return true if cancelled, false if not found or already completed
     */
    public boolean cancelWorkflow(String executionId) {
        WorkflowExecution execution = activeWorkflows.get(executionId);
        if (execution != null && execution.getStatus() == WorkflowStatus.RUNNING) {
            execution.setStatus(WorkflowStatus.CANCELLED);
            execution.setEndTime(LocalDateTime.now());
            log.info("Workflow cancelled: {}", executionId);
            return true;
        }
        return false;
    }
    
    /**
     * Gets all active workflow executions.
     * 
     * @return collection of active executions
     */
    public Collection<WorkflowExecution> getActiveWorkflows() {
        return new ArrayList<>(activeWorkflows.values());
    }
    
    /**
     * Shuts down the workflow manager.
     */
    public void shutdown() {
        log.info("Shutting down WorkflowManager...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("WorkflowManager shutdown complete");
    }
}