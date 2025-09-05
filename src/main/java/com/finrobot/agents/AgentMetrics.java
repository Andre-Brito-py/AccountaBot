package com.finrobot.agents;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Contains performance and operational metrics for an AI Agent.
 * 
 * This class tracks various metrics that are essential for monitoring
 * agent performance, resource usage, and operational health in the
 * FinRobot platform.
 */
@Data
@Builder
public class AgentMetrics {
    
    /**
     * Agent ID these metrics belong to
     */
    private String agentId;
    
    /**
     * When the agent was started
     */
    private LocalDateTime startTime;
    
    /**
     * Last time the agent processed a request
     */
    private LocalDateTime lastActivityTime;
    
    /**
     * Total number of requests processed
     */
    @Builder.Default
    private AtomicLong totalRequestsProcessed = new AtomicLong(0);
    
    /**
     * Number of successful requests
     */
    @Builder.Default
    private AtomicLong successfulRequests = new AtomicLong(0);
    
    /**
     * Number of failed requests
     */
    @Builder.Default
    private AtomicLong failedRequests = new AtomicLong(0);
    
    /**
     * Number of timed out requests
     */
    @Builder.Default
    private AtomicLong timedOutRequests = new AtomicLong(0);
    
    /**
     * Currently active/processing requests
     */
    @Builder.Default
    private AtomicInteger activeRequests = new AtomicInteger(0);
    
    /**
     * Average processing time in milliseconds
     */
    @Builder.Default
    private double averageProcessingTimeMs = 0.0;
    
    /**
     * Minimum processing time recorded
     */
    @Builder.Default
    private long minProcessingTimeMs = Long.MAX_VALUE;
    
    /**
     * Maximum processing time recorded
     */
    @Builder.Default
    private long maxProcessingTimeMs = 0L;
    
    /**
     * Total processing time across all requests
     */
    @Builder.Default
    private AtomicLong totalProcessingTimeMs = new AtomicLong(0);
    
    /**
     * Average confidence score of responses
     */
    @Builder.Default
    private double averageConfidence = 0.0;
    
    /**
     * Total confidence score sum (for calculating average)
     */
    @Builder.Default
    private double totalConfidenceSum = 0.0;
    
    /**
     * Current memory usage in bytes
     */
    @Builder.Default
    private long currentMemoryUsageBytes = 0L;
    
    /**
     * Peak memory usage in bytes
     */
    @Builder.Default
    private long peakMemoryUsageBytes = 0L;
    
    /**
     * Total CPU time used in milliseconds
     */
    @Builder.Default
    private AtomicLong totalCpuTimeMs = new AtomicLong(0);
    
    /**
     * Total LLM tokens consumed
     */
    @Builder.Default
    private AtomicLong totalLlmTokensUsed = new AtomicLong(0);
    
    /**
     * Total API calls made
     */
    @Builder.Default
    private AtomicLong totalApiCalls = new AtomicLong(0);
    
    /**
     * Total data processed in bytes
     */
    @Builder.Default
    private AtomicLong totalDataProcessedBytes = new AtomicLong(0);
    
    /**
     * Number of cache hits
     */
    @Builder.Default
    private AtomicLong cacheHits = new AtomicLong(0);
    
    /**
     * Number of cache misses
     */
    @Builder.Default
    private AtomicLong cacheMisses = new AtomicLong(0);
    
    /**
     * Records the completion of a request with its processing time and confidence.
     * @param processingTimeMs time taken to process the request
     * @param confidence confidence score of the response
     * @param successful whether the request was successful
     */
    public synchronized void recordRequest(long processingTimeMs, double confidence, boolean successful) {
        totalRequestsProcessed.incrementAndGet();
        
        if (successful) {
            successfulRequests.incrementAndGet();
        } else {
            failedRequests.incrementAndGet();
        }
        
        // Update processing time statistics
        totalProcessingTimeMs.addAndGet(processingTimeMs);
        minProcessingTimeMs = Math.min(minProcessingTimeMs, processingTimeMs);
        maxProcessingTimeMs = Math.max(maxProcessingTimeMs, processingTimeMs);
        averageProcessingTimeMs = (double) totalProcessingTimeMs.get() / totalRequestsProcessed.get();
        
        // Update confidence statistics
        totalConfidenceSum += confidence;
        averageConfidence = totalConfidenceSum / totalRequestsProcessed.get();
        
        lastActivityTime = LocalDateTime.now();
    }
    
    /**
     * Records a timed out request.
     */
    public void recordTimeout() {
        totalRequestsProcessed.incrementAndGet();
        timedOutRequests.incrementAndGet();
        lastActivityTime = LocalDateTime.now();
    }
    
    /**
     * Increments the active request counter.
     */
    public void incrementActiveRequests() {
        activeRequests.incrementAndGet();
    }
    
    /**
     * Decrements the active request counter.
     */
    public void decrementActiveRequests() {
        activeRequests.decrementAndGet();
    }
    
    /**
     * Updates resource usage metrics.
     * @param memoryBytes current memory usage
     * @param cpuTimeMs CPU time used
     * @param llmTokens LLM tokens consumed
     * @param apiCalls API calls made
     * @param dataBytes data processed
     */
    public void updateResourceUsage(long memoryBytes, long cpuTimeMs, int llmTokens, int apiCalls, long dataBytes) {
        currentMemoryUsageBytes = memoryBytes;
        peakMemoryUsageBytes = Math.max(peakMemoryUsageBytes, memoryBytes);
        totalCpuTimeMs.addAndGet(cpuTimeMs);
        totalLlmTokensUsed.addAndGet(llmTokens);
        totalApiCalls.addAndGet(apiCalls);
        totalDataProcessedBytes.addAndGet(dataBytes);
    }
    
    /**
     * Records a cache hit.
     */
    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }
    
    /**
     * Records a cache miss.
     */
    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }
    
    /**
     * Calculates the success rate as a percentage.
     * @return success rate (0.0 to 100.0)
     */
    public double getSuccessRate() {
        long total = totalRequestsProcessed.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) successfulRequests.get() / total * 100.0;
    }
    
    /**
     * Calculates the cache hit rate as a percentage.
     * @return cache hit rate (0.0 to 100.0)
     */
    public double getCacheHitRate() {
        long totalCacheAccess = cacheHits.get() + cacheMisses.get();
        if (totalCacheAccess == 0) {
            return 0.0;
        }
        return (double) cacheHits.get() / totalCacheAccess * 100.0;
    }
    
    /**
     * Gets the current throughput in requests per minute.
     * @return requests per minute
     */
    public double getThroughputPerMinute() {
        if (startTime == null) {
            return 0.0;
        }
        
        long minutesRunning = java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes();
        if (minutesRunning == 0) {
            return 0.0;
        }
        
        return (double) totalRequestsProcessed.get() / minutesRunning;
    }
    
    /**
     * Resets all metrics to their initial values.
     */
    public synchronized void reset() {
        totalRequestsProcessed.set(0);
        successfulRequests.set(0);
        failedRequests.set(0);
        timedOutRequests.set(0);
        activeRequests.set(0);
        averageProcessingTimeMs = 0.0;
        minProcessingTimeMs = Long.MAX_VALUE;
        maxProcessingTimeMs = 0L;
        totalProcessingTimeMs.set(0);
        averageConfidence = 0.0;
        totalConfidenceSum = 0.0;
        currentMemoryUsageBytes = 0L;
        peakMemoryUsageBytes = 0L;
        totalCpuTimeMs.set(0);
        totalLlmTokensUsed.set(0);
        totalApiCalls.set(0);
        totalDataProcessedBytes.set(0);
        cacheHits.set(0);
        cacheMisses.set(0);
        startTime = LocalDateTime.now();
        lastActivityTime = null;
    }
}