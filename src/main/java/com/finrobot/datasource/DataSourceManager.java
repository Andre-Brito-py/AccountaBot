package com.finrobot.datasource;

import com.finrobot.datasource.FinancialData.*;
import com.finrobot.datasource.DataSource.DataType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Central manager for all financial data sources in the FinRobot system.
 * 
 * This service coordinates multiple data sources, handles failover scenarios,
 * implements intelligent routing based on data type and availability,
 * and provides unified access to financial data across different providers.
 */
@Slf4j
@Service
public class DataSourceManager {
    
    private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();
    private final Map<DataType, List<String>> dataTypeRouting = new ConcurrentHashMap<>();
    private final Map<String, Integer> sourceReliability = new ConcurrentHashMap<>();
    
    @Autowired(required = false)
    private List<DataSource> availableDataSources = new ArrayList<>();
    
    /**
     * Data source priority configuration.
     * Higher priority sources are preferred for data retrieval.
     */
    private static final Map<String, Integer> SOURCE_PRIORITIES = Map.of(
        "alpha-vantage", 100,
        "yahoo-finance", 80,
        "polygon", 90,
        "iex-cloud", 85,
        "quandl", 75
    );
    
    @PostConstruct
    public void initialize() {
        log.info("Initializing DataSourceManager with {} available sources", availableDataSources.size());
        
        // Register all available data sources
        for (DataSource dataSource : availableDataSources) {
            registerDataSource(dataSource);
        }
        
        // Initialize routing tables
        initializeRouting();
        
        // Start health monitoring
        startHealthMonitoring();
        
        log.info("DataSourceManager initialized with {} active sources", dataSources.size());
    }
    
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down DataSourceManager...");
        
        for (DataSource dataSource : dataSources.values()) {
            try {
                dataSource.shutdown();
            } catch (Exception e) {
                log.error("Error shutting down data source {}: {}", 
                    dataSource.getDataSourceId(), e.getMessage());
            }
        }
        
        dataSources.clear();
        dataTypeRouting.clear();
        sourceReliability.clear();
    }
    
    /**
     * Registers a data source with the manager.
     */
    public void registerDataSource(DataSource dataSource) {
        try {
            String sourceId = dataSource.getDataSourceId();
            
            // Initialize the data source if not already done
            if (!dataSource.isAvailable()) {
                dataSource.initialize(new HashMap<>());
            }
            
            if (dataSource.isAvailable()) {
                dataSources.put(sourceId, dataSource);
                sourceReliability.put(sourceId, 100); // Start with full reliability
                
                // Update routing for supported data types
                for (DataType dataType : dataSource.getSupportedDataTypes()) {
                    dataTypeRouting.computeIfAbsent(dataType, k -> new ArrayList<>()).add(sourceId);
                }
                
                log.info("Registered data source: {} ({})", 
                    dataSource.getDataSourceName(), sourceId);
            } else {
                log.warn("Data source {} is not available, skipping registration", sourceId);
            }
            
        } catch (Exception e) {
            log.error("Failed to register data source {}: {}", 
                dataSource.getDataSourceId(), e.getMessage());
        }
    }
    
    /**
     * Gets price data with automatic failover.
     */
    public CompletableFuture<PriceData> getPriceData(PriceDataRequest request) {
        return executeWithFailover(DataType.REAL_TIME_PRICE, 
            dataSource -> dataSource.getPriceData(request),
            "price data for " + request.getSymbol());
    }
    
    /**
     * Gets historical data with automatic failover.
     */
    public CompletableFuture<List<HistoricalData>> getHistoricalData(HistoricalDataRequest request) {
        return executeWithFailover(DataType.HISTORICAL_PRICE,
            dataSource -> dataSource.getHistoricalData(request),
            "historical data for " + request.getSymbol());
    }
    
    /**
     * Gets fundamental data with automatic failover.
     */
    public CompletableFuture<FundamentalData> getFundamentalData(FundamentalDataRequest request) {
        return executeWithFailover(DataType.FUNDAMENTAL_DATA,
            dataSource -> dataSource.getFundamentalData(request),
            "fundamental data for " + request.getSymbol());
    }
    
    /**
     * Gets news data with automatic failover.
     */
    public CompletableFuture<List<NewsData>> getNewsData(NewsDataRequest request) {
        return executeWithFailover(DataType.NEWS_SENTIMENT,
            dataSource -> dataSource.getNewsData(request),
            "news data");
    }
    
    /**
     * Gets economic data with automatic failover.
     */
    public CompletableFuture<List<EconomicData>> getEconomicData(EconomicDataRequest request) {
        return executeWithFailover(DataType.ECONOMIC_INDICATORS,
            dataSource -> dataSource.getEconomicData(request),
            "economic data for " + request.getIndicator());
    }
    
    /**
     * Searches for instruments across all available sources.
     */
    public CompletableFuture<List<InstrumentInfo>> searchInstruments(String query) {
        List<CompletableFuture<List<InstrumentInfo>>> futures = new ArrayList<>();
        
        for (DataSource dataSource : getAvailableDataSources()) {
            futures.add(dataSource.searchInstruments(query)
                .exceptionally(throwable -> {
                    log.warn("Search failed for source {}: {}", 
                        dataSource.getDataSourceId(), throwable.getMessage());
                    return new ArrayList<>();
                }));
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                Set<InstrumentInfo> uniqueInstruments = new HashSet<>();
                
                for (CompletableFuture<List<InstrumentInfo>> future : futures) {
                    try {
                        uniqueInstruments.addAll(future.get());
                    } catch (Exception e) {
                        log.debug("Failed to get search results: {}", e.getMessage());
                    }
                }
                
                return new ArrayList<>(uniqueInstruments);
            });
    }
    
    /**
     * Checks if a symbol is supported by any data source.
     */
    public CompletableFuture<Boolean> isSymbolSupported(String symbol) {
        List<CompletableFuture<Boolean>> futures = getAvailableDataSources().stream()
            .map(dataSource -> CompletableFuture.supplyAsync(() -> dataSource.isSymbolSupported(symbol)))
            .collect(Collectors.toList());
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .anyMatch(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        return false;
                    }
                }));
    }
    
    /**
     * Gets the best available data source for a specific data type.
     */
    public Optional<DataSource> getBestDataSource(DataType dataType) {
        List<String> sourceIds = dataTypeRouting.get(dataType);
        if (sourceIds == null || sourceIds.isEmpty()) {
            return Optional.empty();
        }
        
        return sourceIds.stream()
            .filter(sourceId -> dataSources.containsKey(sourceId))
            .filter(sourceId -> dataSources.get(sourceId).isAvailable())
            .max(Comparator.comparing(this::calculateSourceScore))
            .map(dataSources::get);
    }
    
    /**
     * Gets all available data sources.
     */
    public List<DataSource> getAvailableDataSources() {
        return dataSources.values().stream()
            .filter(DataSource::isAvailable)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets data source by ID.
     */
    public Optional<DataSource> getDataSource(String sourceId) {
        return Optional.ofNullable(dataSources.get(sourceId));
    }
    
    /**
     * Gets supported data types across all sources.
     */
    public Set<DataType> getSupportedDataTypes() {
        return dataSources.values().stream()
            .filter(DataSource::isAvailable)
            .flatMap(ds -> ds.getSupportedDataTypes().stream())
            .collect(Collectors.toSet());
    }
    
    /**
     * Clears cache for all data sources.
     */
    public void clearAllCaches() {
        dataSources.values().forEach(dataSource -> {
            try {
                dataSource.clearCache(null, null);
            } catch (Exception e) {
                log.warn("Failed to clear cache for {}: {}", 
                    dataSource.getDataSourceId(), e.getMessage());
            }
        });
    }
    
    /**
     * Gets aggregated metrics from all data sources.
     */
    public Map<String, DataSourceMetrics> getAllMetrics() {
        return dataSources.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getMetrics()
            ));
    }
    
    /**
     * Executes a data retrieval operation with automatic failover.
     */
    private <T> CompletableFuture<T> executeWithFailover(
            DataType dataType, 
            DataSourceFunction<T> operation,
            String operationDescription) {
        
        List<String> sourceIds = dataTypeRouting.get(dataType);
        if (sourceIds == null || sourceIds.isEmpty()) {
            return CompletableFuture.failedFuture(
                new RuntimeException("No data sources available for " + dataType));
        }
        
        // Sort sources by score (reliability + priority)
        List<String> sortedSources = sourceIds.stream()
            .filter(sourceId -> dataSources.containsKey(sourceId))
            .filter(sourceId -> dataSources.get(sourceId).isAvailable())
            .sorted(Comparator.comparing(this::calculateSourceScore).reversed())
            .collect(Collectors.toList());
        
        if (sortedSources.isEmpty()) {
            return CompletableFuture.failedFuture(
                new RuntimeException("No available data sources for " + dataType));
        }
        
        return executeWithFailoverRecursive(sortedSources, 0, operation, operationDescription);
    }
    
    /**
     * Recursive helper for failover execution.
     */
    private <T> CompletableFuture<T> executeWithFailoverRecursive(
            List<String> sourceIds,
            int currentIndex,
            DataSourceFunction<T> operation,
            String operationDescription) {
        
        if (currentIndex >= sourceIds.size()) {
            return CompletableFuture.failedFuture(
                new RuntimeException("All data sources failed for " + operationDescription));
        }
        
        String sourceId = sourceIds.get(currentIndex);
        DataSource dataSource = dataSources.get(sourceId);
        
        if (dataSource == null || !dataSource.isAvailable()) {
            return executeWithFailoverRecursive(sourceIds, currentIndex + 1, operation, operationDescription);
        }
        
        return operation.apply(dataSource)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    // Decrease reliability score for failed source
                    decreaseReliability(sourceId);
                    log.warn("Data source {} failed for {}: {}", 
                        sourceId, operationDescription, throwable.getMessage());
                } else {
                    // Increase reliability score for successful source
                    increaseReliability(sourceId);
                }
            })
            .exceptionally(throwable -> {
                // Try next source on failure
                try {
                    return executeWithFailoverRecursive(sourceIds, currentIndex + 1, operation, operationDescription).get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }
    
    /**
     * Initializes routing tables based on data source capabilities.
     */
    private void initializeRouting() {
        dataTypeRouting.clear();
        
        for (DataSource dataSource : dataSources.values()) {
            String sourceId = dataSource.getDataSourceId();
            
            for (DataType dataType : dataSource.getSupportedDataTypes()) {
                dataTypeRouting.computeIfAbsent(dataType, k -> new ArrayList<>()).add(sourceId);
            }
        }
        
        // Sort routing lists by priority
        for (List<String> sourceList : dataTypeRouting.values()) {
            sourceList.sort(Comparator.comparing(this::calculateSourceScore).reversed());
        }
        
        log.info("Initialized routing for {} data types", dataTypeRouting.size());
    }
    
    /**
     * Calculates a score for data source selection.
     */
    private double calculateSourceScore(String sourceId) {
        int priority = SOURCE_PRIORITIES.getOrDefault(sourceId, 50);
        int reliability = sourceReliability.getOrDefault(sourceId, 100);
        
        return (priority * 0.6) + (reliability * 0.4);
    }
    
    /**
     * Increases reliability score for a data source.
     */
    private void increaseReliability(String sourceId) {
        sourceReliability.compute(sourceId, (key, current) -> {
            int newValue = Math.min(100, (current != null ? current : 100) + 1);
            return newValue;
        });
    }
    
    /**
     * Decreases reliability score for a data source.
     */
    private void decreaseReliability(String sourceId) {
        sourceReliability.compute(sourceId, (key, current) -> {
            int newValue = Math.max(0, (current != null ? current : 100) - 5);
            return newValue;
        });
    }
    
    /**
     * Starts periodic health monitoring of data sources.
     */
    private void startHealthMonitoring() {
        // This could be implemented with @Scheduled annotation
        // For now, it's a placeholder for future implementation
        log.info("Health monitoring started for {} data sources", dataSources.size());
    }
    
    /**
     * Functional interface for data source operations.
     */
    @FunctionalInterface
    private interface DataSourceFunction<T> {
        CompletableFuture<T> apply(DataSource dataSource);
    }
    
    /**
     * Data source status information.
     */
    public static class DataSourceStatus {
        private final String sourceId;
        private final String sourceName;
        private final boolean available;
        private final int reliability;
        private final List<DataType> supportedDataTypes;
        private final LocalDateTime lastCheck;
        
        public DataSourceStatus(String sourceId, String sourceName, boolean available, 
                              int reliability, List<DataType> supportedDataTypes) {
            this.sourceId = sourceId;
            this.sourceName = sourceName;
            this.available = available;
            this.reliability = reliability;
            this.supportedDataTypes = new ArrayList<>(supportedDataTypes);
            this.lastCheck = LocalDateTime.now();
        }
        
        // Getters
        public String getSourceId() { return sourceId; }
        public String getSourceName() { return sourceName; }
        public boolean isAvailable() { return available; }
        public int getReliability() { return reliability; }
        public List<DataType> getSupportedDataTypes() { return supportedDataTypes; }
        public LocalDateTime getLastCheck() { return lastCheck; }
    }
    
    /**
     * Gets status of all registered data sources.
     */
    public List<DataSourceStatus> getDataSourceStatuses() {
        return dataSources.values().stream()
            .map(dataSource -> new DataSourceStatus(
                dataSource.getDataSourceId(),
                dataSource.getDataSourceName(),
                dataSource.isAvailable(),
                sourceReliability.getOrDefault(dataSource.getDataSourceId(), 100),
                dataSource.getSupportedDataTypes()
            ))
            .collect(Collectors.toList());
    }
}