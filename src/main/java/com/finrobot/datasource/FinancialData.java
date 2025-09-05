package com.finrobot.datasource;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Collection of data classes representing various types of financial data.
 * 
 * These classes serve as DTOs (Data Transfer Objects) for financial information
 * retrieved from different data sources. They provide a standardized format
 * for financial data across the FinRobot system.
 */
public class FinancialData {
    
    /**
     * Represents current price data for a financial instrument.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceData {
        private String symbol;
        private String name;
        private BigDecimal currentPrice;
        private BigDecimal previousClose;
        private BigDecimal change;
        private BigDecimal changePercent;
        private BigDecimal dayHigh;
        private BigDecimal dayLow;
        private Long volume;
        private BigDecimal marketCap;
        private BigDecimal peRatio;
        private BigDecimal eps;
        private BigDecimal dividendYield;
        private String currency;
        private String exchange;
        private LocalDateTime timestamp;
        private String dataSourceId;
        private Map<String, Object> additionalData;
        
        public PriceData(String symbol) {
            this.symbol = symbol;
            this.additionalData = new HashMap<>();
            this.timestamp = LocalDateTime.now();
        }
        
        public boolean isPositiveChange() {
            return change != null && change.compareTo(BigDecimal.ZERO) > 0;
        }
        
        public boolean isSignificantChange() {
            return changePercent != null && changePercent.abs().compareTo(new BigDecimal("5.0")) > 0;
        }
    }
    
    /**
     * Represents historical price data for a specific time period.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoricalData {
        private String symbol;
        private LocalDate date;
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal close;
        private BigDecimal adjustedClose;
        private Long volume;
        private BigDecimal dividendAmount;
        private BigDecimal splitCoefficient;
        private String dataSourceId;
        
        public BigDecimal getDailyReturn() {
            if (open != null && close != null && open.compareTo(BigDecimal.ZERO) != 0) {
                return close.subtract(open).divide(open, 6, BigDecimal.ROUND_HALF_UP);
            }
            return BigDecimal.ZERO;
        }
        
        public BigDecimal getTradingRange() {
            if (high != null && low != null) {
                return high.subtract(low);
            }
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Represents fundamental data for a company.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FundamentalData {
        private String symbol;
        private String companyName;
        private String sector;
        private String industry;
        private String description;
        private String country;
        private String currency;
        private Long sharesOutstanding;
        private BigDecimal marketCap;
        private BigDecimal enterpriseValue;
        private BigDecimal bookValue;
        private BigDecimal revenue;
        private BigDecimal grossProfit;
        private BigDecimal operatingIncome;
        private BigDecimal netIncome;
        private BigDecimal totalAssets;
        private BigDecimal totalDebt;
        private BigDecimal cashAndEquivalents;
        private BigDecimal freeCashFlow;
        private BigDecimal peRatio;
        private BigDecimal pbRatio;
        private BigDecimal psRatio;
        private BigDecimal pegRatio;
        private BigDecimal debtToEquity;
        private BigDecimal returnOnEquity;
        private BigDecimal returnOnAssets;
        private BigDecimal profitMargin;
        private BigDecimal operatingMargin;
        private BigDecimal dividendYield;
        private BigDecimal payoutRatio;
        private BigDecimal beta;
        private LocalDate lastReportDate;
        private String dataSourceId;
        private LocalDateTime timestamp;
        private Map<String, Object> additionalMetrics;
        
        public FundamentalData(String symbol) {
            this.symbol = symbol;
            this.additionalMetrics = new HashMap<>();
            this.timestamp = LocalDateTime.now();
        }
        
        public boolean isValueStock() {
            return peRatio != null && peRatio.compareTo(new BigDecimal("15")) < 0 &&
                   pbRatio != null && pbRatio.compareTo(new BigDecimal("1.5")) < 0;
        }
        
        public boolean isGrowthStock() {
            return pegRatio != null && pegRatio.compareTo(new BigDecimal("1.0")) < 0;
        }
    }
    
    /**
     * Represents news data and sentiment analysis.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewsData {
        private String id;
        private String title;
        private String summary;
        private String content;
        private String author;
        private String source;
        private String url;
        private LocalDateTime publishedAt;
        private List<String> symbols;
        private List<String> categories;
        private SentimentScore sentiment;
        private Double relevanceScore;
        private String language;
        private String dataSourceId;
        private Map<String, Object> metadata;
        

        
        public boolean isRecentNews() {
            return publishedAt != null && publishedAt.isAfter(LocalDateTime.now().minusHours(24));
        }
        
        public boolean isHighRelevance() {
            return relevanceScore != null && relevanceScore > 0.7;
        }
        
        public void setSentimentScore(BigDecimal sentimentScore) {
            if (this.sentiment == null) {
                this.sentiment = new SentimentScore();
            }
            this.sentiment.setCompound(sentimentScore.doubleValue());
        }
        
        public void setSentimentLabel(String sentimentLabel) {
            if (this.sentiment == null) {
                this.sentiment = new SentimentScore();
            }
            this.sentiment.setOverallSentiment(sentimentLabel);
        }
    }
    
    /**
     * Represents sentiment analysis scores.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SentimentScore {
        private Double positive;
        private Double negative;
        private Double neutral;
        private Double compound;
        private String overallSentiment; // POSITIVE, NEGATIVE, NEUTRAL
        private Double confidence;
        
        public boolean isBullish() {
            return "POSITIVE".equals(overallSentiment) && compound != null && compound > 0.1;
        }
        
        public boolean isBearish() {
            return "NEGATIVE".equals(overallSentiment) && compound != null && compound < -0.1;
        }
    }
    
    /**
     * Represents economic indicator data.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EconomicData {
        private String indicatorId;
        private String name;
        private String description;
        private BigDecimal value;
        private BigDecimal previousValue;
        private BigDecimal change;
        private BigDecimal changePercent;
        private String unit;
        private String frequency; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
        private LocalDate date;
        private LocalDate nextReleaseDate;
        private String country;
        private String category;
        private Double importance; // 1-5 scale
        private String dataSourceId;
        private LocalDateTime timestamp;
        
        public boolean isHighImpact() {
            return importance != null && importance >= 4.0;
        }
        
        public boolean hasSignificantChange() {
            return changePercent != null && changePercent.abs().compareTo(new BigDecimal("10.0")) > 0;
        }
    }
    
    /**
     * Represents financial instrument information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstrumentInfo {
        private String symbol;
        private String name;
        private String type; // STOCK, BOND, ETF, OPTION, FUTURE, FOREX, CRYPTO
        private String exchange;
        private String currency;
        private String country;
        private String sector;
        private String industry;
        private String description;
        private Boolean isActive;
        private LocalDate listingDate;
        private LocalDate delistingDate;
        private String isin;
        private String cusip;
        private String dataSourceId;
        private Map<String, Object> additionalInfo;
        
        public InstrumentInfo(String symbol, String name, String type) {
            this.symbol = symbol;
            this.name = name;
            this.type = type;
            this.additionalInfo = new HashMap<>();
            this.isActive = true;
        }
        
        public boolean isStock() {
            return "STOCK".equals(type);
        }
        
        public boolean isETF() {
            return "ETF".equals(type);
        }
        
        public boolean isCrypto() {
            return "CRYPTO".equals(type);
        }
        
        public void setRegion(String region) {
            if (this.additionalInfo == null) {
                this.additionalInfo = new HashMap<>();
            }
            this.additionalInfo.put("region", region);
        }
    }
    
    /**
     * Represents data source performance metrics.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataSourceMetrics {
        private String dataSourceId;
        private LocalDateTime timestamp;
        private Long totalRequests;
        private Long successfulRequests;
        private Long failedRequests;
        private Double successRate;
        private Double averageResponseTime;
        private Long cacheHits;
        private Long cacheMisses;
        private Double cacheHitRate;
        private Integer rateLimitRemaining;
        private LocalDateTime rateLimitReset;
        private Boolean isHealthy;
        private String lastError;
        private LocalDateTime lastErrorTime;
        private Map<String, Object> customMetrics;
        
        public DataSourceMetrics(String dataSourceId) {
            this.dataSourceId = dataSourceId;
            this.timestamp = LocalDateTime.now();
            this.customMetrics = new HashMap<>();
            this.totalRequests = 0L;
            this.successfulRequests = 0L;
            this.failedRequests = 0L;
            this.cacheHits = 0L;
            this.cacheMisses = 0L;
            this.isHealthy = true;
        }
        
        public void recordRequest(boolean success, long responseTime) {
            totalRequests++;
            if (success) {
                successfulRequests++;
            } else {
                failedRequests++;
            }
            
            // Update success rate
            successRate = (double) successfulRequests / totalRequests;
            
            // Update average response time (simple moving average)
            if (averageResponseTime == null) {
                averageResponseTime = (double) responseTime;
            } else {
                averageResponseTime = (averageResponseTime + responseTime) / 2.0;
            }
        }
        
        public void recordCacheHit() {
            cacheHits++;
            updateCacheHitRate();
        }
        
        public void recordCacheMiss() {
            cacheMisses++;
            updateCacheHitRate();
        }
        
        private void updateCacheHitRate() {
            long totalCacheRequests = cacheHits + cacheMisses;
            if (totalCacheRequests > 0) {
                cacheHitRate = (double) cacheHits / totalCacheRequests;
            }
        }
        
        public boolean isPerformingWell() {
            return isHealthy && 
                   (successRate == null || successRate > 0.95) &&
                   (averageResponseTime == null || averageResponseTime < 2000);
        }
    }
    
    /**
     * Base class for data requests.
     */
    @Data
    public abstract static class DataRequest {
        protected String requestId;
        protected String symbol;
        protected LocalDateTime timestamp;
        protected String dataSourceId;
        protected Map<String, Object> parameters;
        protected Integer timeoutSeconds;
        protected Boolean useCache;
        
        public DataRequest(String symbol) {
            this.requestId = java.util.UUID.randomUUID().toString();
            this.symbol = symbol;
            this.timestamp = LocalDateTime.now();
            this.parameters = new HashMap<>();
            this.timeoutSeconds = 30;
            this.useCache = true;
        }
    }
    
    /**
     * Request for current price data.
     */
    @Data
    public static class PriceDataRequest extends DataRequest {
        private Boolean includeExtendedHours;
        private List<String> fields;
        
        public PriceDataRequest(String symbol) {
            super(symbol);
            this.includeExtendedHours = false;
        }
    }
    
    /**
     * Request for historical data.
     */
    @Data
    public static class HistoricalDataRequest extends DataRequest {
        private LocalDate startDate;
        private LocalDate endDate;
        private String interval; // 1d, 1wk, 1mo
        private Boolean adjustForSplits;
        private Boolean adjustForDividends;
        
        public HistoricalDataRequest(String symbol, LocalDate startDate, LocalDate endDate) {
            super(symbol);
            this.startDate = startDate;
            this.endDate = endDate;
            this.interval = "1d";
            this.adjustForSplits = true;
            this.adjustForDividends = true;
        }
    }
    
    /**
     * Request for fundamental data.
     */
    @Data
    public static class FundamentalDataRequest extends DataRequest {
        private List<String> metrics;
        private String reportType; // ANNUAL, QUARTERLY, TTM
        private Integer yearsBack;
        
        public FundamentalDataRequest(String symbol) {
            super(symbol);
            this.reportType = "TTM";
            this.yearsBack = 5;
        }
    }
    
    /**
     * Request for news data.
     */
    @Data
    public static class NewsDataRequest extends DataRequest {
        private List<String> symbols;
        private LocalDateTime fromDate;
        private LocalDateTime toDate;
        private List<String> categories;
        private List<String> sources;
        private String language;
        private Integer maxResults;
        private Boolean includeSentiment;
        
        public NewsDataRequest() {
            super(null);
            this.maxResults = 50;
            this.includeSentiment = true;
            this.language = "en";
        }
    }
    
    /**
     * Request for economic data.
     */
    @Data
    public static class EconomicDataRequest extends DataRequest {
        private List<String> indicators;
        private String country;
        private LocalDate fromDate;
        private LocalDate toDate;
        private String frequency;
        
        public EconomicDataRequest() {
            super(null);
            this.country = "US";
            this.frequency = "MONTHLY";
        }
        
        public List<String> getIndicator() {
            return this.indicators;
        }
    }
}