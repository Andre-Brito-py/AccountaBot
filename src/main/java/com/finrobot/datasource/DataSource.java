package com.finrobot.datasource;

import com.finrobot.datasource.FinancialData.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Base interface for all financial data sources in the FinRobot system.
 * 
 * This interface defines the contract for retrieving various types of financial data
 * from different providers such as Yahoo Finance, Alpha Vantage, Bloomberg, etc.
 * All data source implementations must implement this interface to ensure
 * consistency and interoperability within the system.
 */
public interface DataSource {
    
    /**
     * Gets the unique identifier for this data source.
     * 
     * @return the data source ID
     */
    String getDataSourceId();
    
    /**
     * Gets the display name of this data source.
     * 
     * @return the data source name
     */
    String getDataSourceName();
    
    /**
     * Gets the data source provider information.
     * 
     * @return the provider info
     */
    DataSourceProvider getProvider();
    
    /**
     * Checks if the data source is currently available and operational.
     * 
     * @return true if available, false otherwise
     */
    boolean isAvailable();
    
    /**
     * Gets the supported data types by this data source.
     * 
     * @return list of supported data types
     */
    List<DataType> getSupportedDataTypes();
    
    /**
     * Gets the rate limits for this data source.
     * 
     * @return rate limit information
     */
    RateLimit getRateLimit();
    
    /**
     * Initializes the data source with configuration.
     * 
     * @param configuration the configuration parameters
     */
    void initialize(Map<String, Object> configuration);
    
    /**
     * Shuts down the data source and releases resources.
     */
    void shutdown();
    
    /**
     * Retrieves stock price data for a given symbol.
     * 
     * @param request the price data request
     * @return CompletableFuture containing the price data
     */
    CompletableFuture<PriceData> getPriceData(PriceDataRequest request);
    
    /**
     * Retrieves historical price data for a given symbol and time range.
     * 
     * @param request the historical data request
     * @return CompletableFuture containing the historical data
     */
    CompletableFuture<List<HistoricalData>> getHistoricalData(HistoricalDataRequest request);
    
    /**
     * Retrieves company fundamental data.
     * 
     * @param request the fundamental data request
     * @return CompletableFuture containing the fundamental data
     */
    CompletableFuture<FundamentalData> getFundamentalData(FundamentalDataRequest request);
    
    /**
     * Retrieves market news and sentiment data.
     * 
     * @param request the news data request
     * @return CompletableFuture containing the news data
     */
    CompletableFuture<List<NewsData>> getNewsData(NewsDataRequest request);
    
    /**
     * Retrieves economic indicators and macro data.
     * 
     * @param request the economic data request
     * @return CompletableFuture containing the economic data
     */
    CompletableFuture<List<EconomicData>> getEconomicData(EconomicDataRequest request);
    
    /**
     * Searches for financial instruments (stocks, bonds, etc.).
     * 
     * @param query the search query
     * @return CompletableFuture containing the search results
     */
    CompletableFuture<List<InstrumentInfo>> searchInstruments(String query);
    
    /**
     * Gets real-time market data stream.
     * 
     * @param symbols the symbols to stream
     * @param callback the callback for receiving data
     * @return stream subscription handle
     */
    StreamSubscription subscribeToRealTimeData(List<String> symbols, RealTimeDataCallback callback);
    
    /**
     * Validates if a symbol is supported by this data source.
     * 
     * @param symbol the symbol to validate
     * @return true if supported, false otherwise
     */
    boolean isSymbolSupported(String symbol);
    
    /**
     * Gets the last update time for cached data.
     * 
     * @param symbol the symbol
     * @param dataType the data type
     * @return the last update time, or null if not cached
     */
    LocalDateTime getLastUpdateTime(String symbol, DataType dataType);
    
    /**
     * Clears cached data for optimization.
     * 
     * @param symbol the symbol (null for all symbols)
     * @param dataType the data type (null for all types)
     */
    void clearCache(String symbol, DataType dataType);
    
    /**
     * Gets data source statistics and health metrics.
     * 
     * @return the data source metrics
     */
    DataSourceMetrics getMetrics();
    
    /**
     * Represents different types of financial data.
     */
    enum DataType {
        REAL_TIME_PRICE,
        HISTORICAL_PRICE,
        INTRADAY_PRICE,
        FUNDAMENTAL_DATA,
        NEWS_SENTIMENT,
        ECONOMIC_INDICATORS,
        TECHNICAL_INDICATORS,
        OPTIONS_DATA,
        FOREX_DATA,
        CRYPTO_DATA,
        COMMODITY_DATA
    }
    
    /**
     * Represents data source provider information.
     */
    @Data
    class DataSourceProvider {
        private String name;
        private String website;
        private String apiVersion;
        private boolean requiresApiKey;
        private boolean isPremium;
        private String documentation;
        
        public DataSourceProvider(String name, String website) {
            this.name = name;
            this.website = website;
        }
        
        public void setIsPremium(boolean isPremium) {
            this.isPremium = isPremium;
        }
    }
    
    /**
     * Represents rate limiting information.
     */
    @Data
    class RateLimit {
        private int requestsPerMinute;
        private int requestsPerHour;
        private int requestsPerDay;
        private int currentUsage;
        private LocalDateTime resetTime;
        
        public RateLimit(int requestsPerMinute, int requestsPerHour, int requestsPerDay) {
            this.requestsPerMinute = requestsPerMinute;
            this.requestsPerHour = requestsPerHour;
            this.requestsPerDay = requestsPerDay;
            this.currentUsage = 0;
        }
        
        public boolean canMakeRequest() {
            return currentUsage < requestsPerMinute;
        }
        
        public void incrementUsage() {
            currentUsage++;
        }
    }
    
    /**
     * Callback interface for real-time data streaming.
     */
    interface RealTimeDataCallback {
        void onData(String symbol, PriceData data);
        void onError(String symbol, Exception error);
        void onConnectionStatusChange(boolean connected);
    }
    
    /**
     * Represents a stream subscription handle.
     */
    interface StreamSubscription {
        String getSubscriptionId();
        List<String> getSymbols();
        boolean isActive();
        void unsubscribe();
        void addSymbol(String symbol);
        void removeSymbol(String symbol);
    }
    
    /**
     * Exception thrown by data source operations.
     */
    class DataSourceException extends Exception {
        private final String dataSourceId;
        private final ErrorCode errorCode;
        
        public DataSourceException(String dataSourceId, ErrorCode errorCode, String message) {
            super(message);
            this.dataSourceId = dataSourceId;
            this.errorCode = errorCode;
        }
        
        public DataSourceException(String dataSourceId, ErrorCode errorCode, String message, Throwable cause) {
            super(message, cause);
            this.dataSourceId = dataSourceId;
            this.errorCode = errorCode;
        }
        
        public String getDataSourceId() {
            return dataSourceId;
        }
        
        public ErrorCode getErrorCode() {
            return errorCode;
        }
        
        public enum ErrorCode {
            AUTHENTICATION_FAILED,
            RATE_LIMIT_EXCEEDED,
            INVALID_SYMBOL,
            DATA_NOT_AVAILABLE,
            NETWORK_ERROR,
            API_ERROR,
            CONFIGURATION_ERROR,
            TIMEOUT,
            UNKNOWN_ERROR
        }
    }
}