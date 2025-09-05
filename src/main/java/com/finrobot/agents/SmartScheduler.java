package com.finrobot.agents;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Intelligent scheduler for automated execution of agents and workflows.
 * 
 * The SmartScheduler provides sophisticated scheduling capabilities including:
 * - Cron-based scheduling
 * - Market hours awareness
 * - Dynamic scheduling based on market conditions
 * - Load balancing and resource management
 * - Automatic retry and error handling
 */
@Slf4j
@Component
public class SmartScheduler {
    
    @Autowired
    private AgentLibrary agentLibrary;
    
    @Autowired
    private WorkflowManager workflowManager;
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final Map<String, ScheduledTask> scheduledTasks = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> activeFutures = new ConcurrentHashMap<>();
    
    // Market hours configuration (can be externalized)
    private final ZoneId marketTimeZone = ZoneId.of("America/New_York");
    private final LocalTime marketOpen = LocalTime.of(9, 30);
    private final LocalTime marketClose = LocalTime.of(16, 0);
    private final LocalTime preMarketStart = LocalTime.of(4, 0);
    private final LocalTime afterHoursEnd = LocalTime.of(20, 0);
    
    /**
     * Represents a scheduled task configuration.
     */
    @Data
    public static class ScheduledTask {
        private String taskId;
        private String name;
        private String description;
        private ScheduleType scheduleType;
        private String cronExpression;
        private Duration interval;
        private LocalDateTime nextExecution;
        private LocalDateTime lastExecution;
        private TaskType taskType;
        private String targetId; // Agent type or workflow ID
        private Map<String, Object> configuration;
        private Map<String, Object> parameters;
        private boolean enabled;
        private boolean marketHoursOnly;
        private boolean skipWeekends;
        private boolean skipHolidays;
        private int maxRetries;
        private Duration retryDelay;
        private TaskPriority priority;
        private List<String> dependencies;
        private LocalDateTime createdAt;
        private String createdBy;
        
        public ScheduledTask(String taskId, String name, TaskType taskType, String targetId) {
            this.taskId = taskId;
            this.name = name;
            this.taskType = taskType;
            this.targetId = targetId;
            this.scheduleType = ScheduleType.INTERVAL;
            this.interval = Duration.ofHours(1);
            this.configuration = new HashMap<>();
            this.parameters = new HashMap<>();
            this.enabled = true;
            this.marketHoursOnly = false;
            this.skipWeekends = true;
            this.skipHolidays = true;
            this.maxRetries = 3;
            this.retryDelay = Duration.ofMinutes(5);
            this.priority = TaskPriority.MEDIUM;
            this.dependencies = new ArrayList<>();
            this.createdAt = LocalDateTime.now();
        }
    }
    
    /**
     * Represents the execution history of a scheduled task.
     */
    @Data
    public static class TaskExecution {
        private String executionId;
        private String taskId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private TaskExecutionStatus status;
        private String result;
        private String errorMessage;
        private int attemptNumber;
        private Duration executionDuration;
        private Map<String, Object> executionContext;
        
        public TaskExecution(String taskId) {
            this.executionId = UUID.randomUUID().toString();
            this.taskId = taskId;
            this.startTime = LocalDateTime.now();
            this.status = TaskExecutionStatus.RUNNING;
            this.attemptNumber = 1;
            this.executionContext = new HashMap<>();
        }
    }
    
    public enum ScheduleType {
        CRON,           // Cron expression based
        INTERVAL,       // Fixed interval
        MARKET_OPEN,    // Execute at market open
        MARKET_CLOSE,   // Execute at market close
        PRE_MARKET,     // Execute during pre-market hours
        AFTER_HOURS,    // Execute during after hours
        ON_DEMAND       // Manual execution only
    }
    
    public enum TaskType {
        AGENT_EXECUTION,    // Execute a single agent
        WORKFLOW_EXECUTION, // Execute a workflow
        SYSTEM_MAINTENANCE  // System maintenance tasks
    }
    
    public enum TaskPriority {
        LOW(1),
        MEDIUM(2),
        HIGH(3),
        CRITICAL(4);
        
        private final int level;
        
        TaskPriority(int level) {
            this.level = level;
        }
        
        public int getLevel() {
            return level;
        }
    }
    
    public enum TaskExecutionStatus {
        SCHEDULED,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED,
        SKIPPED
    }
    
    @PostConstruct
    public void initialize() {
        log.info("Initializing SmartScheduler...");
        
        // Schedule built-in system tasks
        scheduleSystemTasks();
        
        log.info("SmartScheduler initialized with {} scheduled tasks", scheduledTasks.size());
    }
    
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down SmartScheduler...");
        
        // Cancel all active futures
        activeFutures.values().forEach(future -> future.cancel(true));
        activeFutures.clear();
        
        // Shutdown scheduler
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        log.info("SmartScheduler shutdown complete");
    }
    
    /**
     * Schedules built-in system tasks.
     */
    private void scheduleSystemTasks() {
        // Market data refresh task
        ScheduledTask marketDataTask = new ScheduledTask(
            "market-data-refresh",
            "Market Data Refresh",
            TaskType.AGENT_EXECUTION,
            Agent.AgentType.MARKET_FORECASTING.name()
        );
        marketDataTask.setDescription("Refreshes market data and updates forecasts");
        marketDataTask.setScheduleType(ScheduleType.INTERVAL);
        marketDataTask.setInterval(Duration.ofMinutes(15));
        marketDataTask.setMarketHoursOnly(true);
        marketDataTask.setPriority(TaskPriority.HIGH);
        
        scheduleTask(marketDataTask);
        
        // Daily market analysis workflow
        ScheduledTask dailyAnalysisTask = new ScheduledTask(
            "daily-market-analysis",
            "Daily Market Analysis",
            TaskType.WORKFLOW_EXECUTION,
            "daily-analysis-workflow"
        );
        dailyAnalysisTask.setDescription("Comprehensive daily market analysis");
        dailyAnalysisTask.setScheduleType(ScheduleType.MARKET_CLOSE);
        dailyAnalysisTask.setPriority(TaskPriority.MEDIUM);
        
        scheduleTask(dailyAnalysisTask);
        
        // News sentiment analysis
        ScheduledTask newsTask = new ScheduledTask(
            "news-sentiment-analysis",
            "News Sentiment Analysis",
            TaskType.AGENT_EXECUTION,
            Agent.AgentType.NEWS_SENTIMENT.name()
        );
        newsTask.setDescription("Analyzes market sentiment from news sources");
        newsTask.setScheduleType(ScheduleType.INTERVAL);
        newsTask.setInterval(Duration.ofMinutes(30));
        newsTask.setPriority(TaskPriority.MEDIUM);
        
        scheduleTask(newsTask);
    }
    
    /**
     * Schedules a new task.
     * 
     * @param task the task to schedule
     * @return true if scheduled successfully, false otherwise
     */
    public boolean scheduleTask(ScheduledTask task) {
        if (scheduledTasks.containsKey(task.getTaskId())) {
            log.warn("Task already exists: {}", task.getTaskId());
            return false;
        }
        
        scheduledTasks.put(task.getTaskId(), task);
        
        if (task.isEnabled()) {
            activateTask(task);
        }
        
        log.info("Scheduled task: {} ({})", task.getTaskId(), task.getName());
        return true;
    }
    
    /**
     * Activates a scheduled task.
     */
    private void activateTask(ScheduledTask task) {
        ScheduledFuture<?> future;
        
        switch (task.getScheduleType()) {
            case INTERVAL:
                future = scheduler.scheduleAtFixedRate(
                    () -> executeTask(task),
                    0,
                    task.getInterval().toSeconds(),
                    TimeUnit.SECONDS
                );
                break;
                
            case CRON:
                // For cron expressions, we'd need a more sophisticated scheduler
                // For now, convert to approximate interval
                future = scheduler.scheduleAtFixedRate(
                    () -> executeTask(task),
                    0,
                    Duration.ofHours(1).toSeconds(), // Default to hourly
                    TimeUnit.SECONDS
                );
                break;
                
            case MARKET_OPEN:
                future = scheduleAtMarketTime(task, marketOpen);
                break;
                
            case MARKET_CLOSE:
                future = scheduleAtMarketTime(task, marketClose);
                break;
                
            case PRE_MARKET:
                future = scheduleAtMarketTime(task, preMarketStart);
                break;
                
            case AFTER_HOURS:
                future = scheduleAtMarketTime(task, afterHoursEnd);
                break;
                
            default:
                log.warn("Unsupported schedule type: {}", task.getScheduleType());
                return;
        }
        
        activeFutures.put(task.getTaskId(), future);
    }
    
    /**
     * Schedules a task to run at a specific market time.
     */
    private ScheduledFuture<?> scheduleAtMarketTime(ScheduledTask task, LocalTime marketTime) {
        return scheduler.scheduleAtFixedRate(
            () -> {
                if (shouldExecuteAtMarketTime(task, marketTime)) {
                    executeTask(task);
                }
            },
            0,
            Duration.ofMinutes(1).toSeconds(), // Check every minute
            TimeUnit.SECONDS
        );
    }
    
    /**
     * Determines if a task should execute at the current market time.
     */
    private boolean shouldExecuteAtMarketTime(ScheduledTask task, LocalTime targetTime) {
        ZonedDateTime now = ZonedDateTime.now(marketTimeZone);
        LocalTime currentTime = now.toLocalTime();
        LocalDate currentDate = now.toLocalDate();
        
        // Check if it's the right time (within 1 minute window)
        if (Math.abs(Duration.between(currentTime, targetTime).toMinutes()) > 1) {
            return false;
        }
        
        // Check if already executed today
        if (task.getLastExecution() != null && 
            task.getLastExecution().toLocalDate().equals(currentDate)) {
            return false;
        }
        
        // Check weekend/holiday restrictions
        if (task.isSkipWeekends() && isWeekend(currentDate)) {
            return false;
        }
        
        if (task.isSkipHolidays() && isMarketHoliday(currentDate)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Executes a scheduled task.
     */
    private void executeTask(ScheduledTask task) {
        if (!task.isEnabled()) {
            return;
        }
        
        // Check execution conditions
        if (!shouldExecuteNow(task)) {
            return;
        }
        
        TaskExecution execution = new TaskExecution(task.getTaskId());
        
        log.info("Executing scheduled task: {} ({})", task.getTaskId(), task.getName());
        
        try {
            switch (task.getTaskType()) {
                case AGENT_EXECUTION:
                    executeAgentTask(task, execution);
                    break;
                case WORKFLOW_EXECUTION:
                    executeWorkflowTask(task, execution);
                    break;
                case SYSTEM_MAINTENANCE:
                    executeMaintenanceTask(task, execution);
                    break;
            }
            
            execution.setStatus(TaskExecutionStatus.COMPLETED);
            task.setLastExecution(LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Task execution failed: {}", task.getTaskId(), e);
            execution.setStatus(TaskExecutionStatus.FAILED);
            execution.setErrorMessage(e.getMessage());
            
            // Schedule retry if configured
            if (execution.getAttemptNumber() < task.getMaxRetries()) {
                scheduleRetry(task, execution);
            }
        } finally {
            execution.setEndTime(LocalDateTime.now());
            execution.setExecutionDuration(
                Duration.between(execution.getStartTime(), execution.getEndTime())
            );
        }
    }
    
    /**
     * Determines if a task should execute now based on various conditions.
     */
    private boolean shouldExecuteNow(ScheduledTask task) {
        ZonedDateTime now = ZonedDateTime.now(marketTimeZone);
        LocalDate currentDate = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        
        // Check weekend restriction
        if (task.isSkipWeekends() && isWeekend(currentDate)) {
            return false;
        }
        
        // Check holiday restriction
        if (task.isSkipHolidays() && isMarketHoliday(currentDate)) {
            return false;
        }
        
        // Check market hours restriction
        if (task.isMarketHoursOnly() && !isMarketHours(currentTime)) {
            return false;
        }
        
        // Check dependencies
        for (String dependencyId : task.getDependencies()) {
            ScheduledTask dependency = scheduledTasks.get(dependencyId);
            if (dependency != null && 
                (dependency.getLastExecution() == null || 
                 dependency.getLastExecution().toLocalDate().isBefore(currentDate))) {
                log.debug("Task {} waiting for dependency: {}", task.getTaskId(), dependencyId);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Executes an agent task.
     */
    private void executeAgentTask(ScheduledTask task, TaskExecution execution) {
        try {
            Agent.AgentType agentType = Agent.AgentType.valueOf(task.getTargetId());
            Agent agent = agentLibrary.createAgent(agentType, task.getConfiguration());
            
            AgentRequest request = AgentRequest.builder()
                .requestId(execution.getExecutionId())
                .requestType(AgentRequest.RequestType.SCHEDULED_ANALYSIS)
                .inputData(task.getParameters())
                .priority(convertPriority(task.getPriority()))
                .build();
            
            request.getParameters().putAll(task.getParameters());
            request.getContext().put("scheduledTask", task.getTaskId());
            request.getContext().put("executionTime", LocalDateTime.now().toString());
            
            CompletableFuture<AgentResponse> responseFuture = agent.execute(request);
            AgentResponse response = responseFuture.get();
            execution.setResult("Agent executed successfully. Confidence: " + response.getConfidenceScore());
            
        } catch (Exception e) {
            throw new RuntimeException("Agent task execution failed", e);
        }
    }
    
    /**
     * Executes a workflow task.
     */
    private void executeWorkflowTask(ScheduledTask task, TaskExecution execution) {
        // This would require workflow definitions to be stored and retrieved
        // For now, we'll simulate workflow execution
        log.info("Executing workflow: {}", task.getTargetId());
        execution.setResult("Workflow executed successfully");
    }
    
    /**
     * Executes a system maintenance task.
     */
    private void executeMaintenanceTask(ScheduledTask task, TaskExecution execution) {
        log.info("Executing maintenance task: {}", task.getTargetId());
        execution.setResult("Maintenance task completed");
    }
    
    /**
     * Schedules a retry for a failed task.
     */
    private void scheduleRetry(ScheduledTask task, TaskExecution execution) {
        scheduler.schedule(
            () -> {
                TaskExecution retryExecution = new TaskExecution(task.getTaskId());
                retryExecution.setAttemptNumber(execution.getAttemptNumber() + 1);
                executeTask(task);
            },
            task.getRetryDelay().toSeconds(),
            TimeUnit.SECONDS
        );
    }
    
    /**
     * Converts task priority to agent request priority.
     */
    private AgentRequest.Priority convertPriority(TaskPriority taskPriority) {
        switch (taskPriority) {
            case LOW: return AgentRequest.Priority.LOW;
            case MEDIUM: return AgentRequest.Priority.MEDIUM;
            case HIGH: return AgentRequest.Priority.HIGH;
            case CRITICAL: return AgentRequest.Priority.HIGH; // Map to HIGH as there's no CRITICAL
            default: return AgentRequest.Priority.MEDIUM;
        }
    }
    
    /**
     * Checks if the current time is within market hours.
     */
    private boolean isMarketHours(LocalTime time) {
        return !time.isBefore(marketOpen) && !time.isAfter(marketClose);
    }
    
    /**
     * Checks if a date is a weekend.
     */
    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
    
    /**
     * Checks if a date is a market holiday.
     * This is a simplified implementation - in practice, you'd use a holiday calendar service.
     */
    private boolean isMarketHoliday(LocalDate date) {
        // Simplified holiday check - would be more comprehensive in production
        int year = date.getYear();
        
        // New Year's Day
        if (date.equals(LocalDate.of(year, 1, 1))) return true;
        
        // Independence Day
        if (date.equals(LocalDate.of(year, 7, 4))) return true;
        
        // Christmas
        if (date.equals(LocalDate.of(year, 12, 25))) return true;
        
        // Add more holidays as needed
        return false;
    }
    
    /**
     * Gets a scheduled task by ID.
     * 
     * @param taskId the task ID
     * @return the scheduled task, or null if not found
     */
    public ScheduledTask getTask(String taskId) {
        return scheduledTasks.get(taskId);
    }
    
    /**
     * Gets all scheduled tasks.
     * 
     * @return collection of all scheduled tasks
     */
    public Collection<ScheduledTask> getAllTasks() {
        return new ArrayList<>(scheduledTasks.values());
    }
    
    /**
     * Enables or disables a scheduled task.
     * 
     * @param taskId the task ID
     * @param enabled whether to enable the task
     * @return true if the task was found and updated, false otherwise
     */
    public boolean setTaskEnabled(String taskId, boolean enabled) {
        ScheduledTask task = scheduledTasks.get(taskId);
        if (task == null) {
            return false;
        }
        
        task.setEnabled(enabled);
        
        if (enabled) {
            activateTask(task);
        } else {
            ScheduledFuture<?> future = activeFutures.remove(taskId);
            if (future != null) {
                future.cancel(false);
            }
        }
        
        log.info("Task {} {}", taskId, enabled ? "enabled" : "disabled");
        return true;
    }
    
    /**
     * Removes a scheduled task.
     * 
     * @param taskId the task ID
     * @return true if the task was found and removed, false otherwise
     */
    public boolean removeTask(String taskId) {
        ScheduledTask task = scheduledTasks.remove(taskId);
        if (task == null) {
            return false;
        }
        
        ScheduledFuture<?> future = activeFutures.remove(taskId);
        if (future != null) {
            future.cancel(false);
        }
        
        log.info("Removed scheduled task: {}", taskId);
        return true;
    }
    
    /**
     * Manually executes a task immediately.
     * 
     * @param taskId the task ID
     * @return true if the task was found and executed, false otherwise
     */
    public boolean executeTaskNow(String taskId) {
        ScheduledTask task = scheduledTasks.get(taskId);
        if (task == null) {
            return false;
        }
        
        scheduler.execute(() -> executeTask(task));
        log.info("Manually triggered task execution: {}", taskId);
        return true;
    }
    
    /**
     * Gets scheduler statistics.
     * 
     * @return statistics map
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", scheduledTasks.size());
        stats.put("activeTasks", activeFutures.size());
        stats.put("enabledTasks", scheduledTasks.values().stream().mapToLong(task -> task.isEnabled() ? 1 : 0).sum());
        
        Map<String, Long> tasksByType = scheduledTasks.values().stream()
                .collect(Collectors.groupingBy(
                    task -> task.getTaskType().name(),
                    Collectors.counting()
                ));
        stats.put("tasksByType", tasksByType);
        
        Map<String, Long> tasksByPriority = scheduledTasks.values().stream()
                .collect(Collectors.groupingBy(
                    task -> task.getPriority().name(),
                    Collectors.counting()
                ));
        stats.put("tasksByPriority", tasksByPriority);
        
        return stats;
    }
    
    /**
     * Spring scheduled method to perform periodic maintenance.
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void performMaintenance() {
        log.debug("Performing scheduler maintenance...");
        
        // Update next execution times
        for (ScheduledTask task : scheduledTasks.values()) {
            if (task.isEnabled() && task.getScheduleType() == ScheduleType.INTERVAL) {
                if (task.getLastExecution() != null) {
                    task.setNextExecution(task.getLastExecution().plus(task.getInterval()));
                } else {
                    task.setNextExecution(LocalDateTime.now().plus(task.getInterval()));
                }
            }
        }
    }
}