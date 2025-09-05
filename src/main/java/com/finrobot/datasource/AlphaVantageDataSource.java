package com.finrobot.datasource;

import com.finrobot.datasource.FinancialData.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Alpha Vantage data source implementation.
 * 
 * This implementation provides access to Alpha Vantage's comprehensive financial data API,
 * including real-time quotes, historical data, fundamental data, technical indicators,
 * economic data, and news sentiment. Requires API key for access.
 */
@Slf4j
@Component
public class AlphaVantageDataSource implements DataSource {
    
    private static final String DATA_SOURCE_ID = "alpha-vantage";
    private static final String BASE_URL = "https://www.alphavantage.co/query";
    
    @Value("${finrobot.datasources.alpha-vantage.api-key:}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final Map<String, Object> cache;
    private final DataSourceProvider provider;
    private final RateLimit rateLimit;
    private final DataSourceMetrics metrics;
    private boolean initialized = false;
    
    public AlphaVantageDataSource() {
        this.restTemplate = new RestTemplate();
        this.cache = new ConcurrentHashMap<>();
        this.provider = new DataSourceProvider("Alpha Vantage", "https://www.alphavantage.co");
        this.provider.setApiVersion("1.0");
        this.provider.setRequiresApiKey(true);
        this.provider.setIsPremium(true);
        this.provider.setDocumentation("https://www.alphavantage.co/documentation/");
        
        // Alpha Vantage rate limits: 5 calls per minute for free tier
        this.rateLimit = new RateLimit(5, 60, 500);
        this.metrics = new DataSourceMetrics(DATA_SOURCE_ID);
    }
    
    @Override
    public String getDataSourceId() {
        return DATA_SOURCE_ID;
    }
    
    @Override
    public String getDataSourceName() {
        return "Alpha Vantage";
    }
    
    @Override
    public DataSourceProvider getProvider() {
        return provider;
    }
    
    @Override
    public boolean isAvailable() {
        if (!initialized || apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Simple health check
            String url = BASE_URL + "?function=GLOBAL_QUOTE&symbol=IBM&apikey=" + apiKey;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            // Check if response contains error or rate limit message
            return response != null && !response.containsKey("Error Message") && 
                   !response.containsKey("Note");
        } catch (Exception e) {
            log.warn("Alpha Vantage health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public List<DataType> getSupportedDataTypes() {
        return Arrays.asList(
            DataType.REAL_TIME_PRICE,
            DataType.HISTORICAL_PRICE,
            DataType.INTRADAY_PRICE,
            DataType.FUNDAMENTAL_DATA,
            DataType.NEWS_SENTIMENT,
            DataType.ECONOMIC_INDICATORS
        );
    }
    
    @Override
    public RateLimit getRateLimit() {
        return rateLimit;
    }
    
    @Override
    public void initialize(Map<String, Object> configuration) {
        try {
            log.info("Initializing Alpha Vantage data source...");
            
            // Override API key from configuration if provided
            if (configuration.containsKey("apiKey")) {
                this.apiKey = (String) configuration.get("apiKey");
            }
            
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new RuntimeException("Alpha Vantage API key is required");
            }
            
            // Test connectivity
            if (!isAvailable()) {
                throw new RuntimeException("Cannot connect to Alpha Vantage API or invalid API key");
            }
            
            initialized = true;
            log.info("Alpha Vantage data source initialized successfully");
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Alpha Vantage data source: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void shutdown() {
        log.info("Shutting down Alpha Vantage data source...");
        cache.clear();
        initialized = false;
    }
    
    @Override
    public CompletableFuture<PriceData> getPriceData(PriceDataRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                if (!rateLimit.canMakeRequest()) {
                    throw new DataSourceException(DATA_SOURCE_ID, 
                        DataSourceException.ErrorCode.RATE_LIMIT_EXCEEDED, 
                        "Rate limit exceeded");
                }
                
                String cacheKey = "price_" + request.getSymbol();
                if (request.getUseCache() && cache.containsKey(cacheKey)) {
                    PriceData cachedData = (PriceData) cache.get(cacheKey);
                    if (cachedData.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(5))) {
                        metrics.recordCacheHit();
                        return cachedData;
                    }
                }
                
                metrics.recordCacheMiss();
                rateLimit.incrementUsage();
                
                String url = BASE_URL + "?function=GLOBAL_QUOTE&symbol=" + request.getSymbol() + "&apikey=" + apiKey;
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                
                checkForErrors(response);
                
                PriceData priceData = parseGlobalQuoteResponse(response, request.getSymbol());
                
                if (request.getUseCache()) {
                    cache.put(cacheKey, priceData);
                }
                
                metrics.recordRequest(true, System.currentTimeMillis() - startTime);
                return priceData;
                
            } catch (Exception e) {
                metrics.recordRequest(false, System.currentTimeMillis() - startTime);
                log.error("Failed to get price data for {}: {}", request.getSymbol(), e.getMessage());
                throw new RuntimeException("Failed to get price data", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<HistoricalData>> getHistoricalData(HistoricalDataRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                if (!rateLimit.canMakeRequest()) {
                    throw new DataSourceException(DATA_SOURCE_ID, 
                        DataSourceException.ErrorCode.RATE_LIMIT_EXCEEDED, 
                        "Rate limit exceeded");
                }
                
                String cacheKey = String.format("historical_%s_%s_%s", 
                    request.getSymbol(), request.getStartDate(), request.getEndDate());
                
                if (request.getUseCache() && cache.containsKey(cacheKey)) {
                    @SuppressWarnings("unchecked")
                    List<HistoricalData> cachedData = (List<HistoricalData>) cache.get(cacheKey);
                    metrics.recordCacheHit();
                    return cachedData;
                }
                
                metrics.recordCacheMiss();
                rateLimit.incrementUsage();
                
                // Alpha Vantage uses different functions for different time ranges
                String function = determineHistoricalFunction(request);
                String url = BASE_URL + "?function=" + function + "&symbol=" + request.getSymbol() + 
                           "&outputsize=full&apikey=" + apiKey;
                
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                checkForErrors(response);
                
                List<HistoricalData> historicalData = parseHistoricalResponse(response, request);
                
                if (request.getUseCache()) {
                    cache.put(cacheKey, historicalData);
                }
                
                metrics.recordRequest(true, System.currentTimeMillis() - startTime);
                return historicalData;
                
            } catch (Exception e) {
                metrics.recordRequest(false, System.currentTimeMillis() - startTime);
                log.error("Failed to get historical data for {}: {}", request.getSymbol(), e.getMessage());
                throw new RuntimeException("Failed to get historical data", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<FundamentalData> getFundamentalData(FundamentalDataRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                if (!rateLimit.canMakeRequest()) {
                    throw new DataSourceException(DATA_SOURCE_ID, 
                        DataSourceException.ErrorCode.RATE_LIMIT_EXCEEDED, 
                        "Rate limit exceeded");
                }
                
                String cacheKey = "fundamental_" + request.getSymbol();
                if (request.getUseCache() && cache.containsKey(cacheKey)) {
                    FundamentalData cachedData = (FundamentalData) cache.get(cacheKey);
                    if (cachedData.getTimestamp().isAfter(LocalDateTime.now().minusHours(24))) {
                        metrics.recordCacheHit();
                        return cachedData;
                    }
                }
                
                metrics.recordCacheMiss();
                rateLimit.incrementUsage();
                
                String url = BASE_URL + "?function=OVERVIEW&symbol=" + request.getSymbol() + "&apikey=" + apiKey;
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                
                checkForErrors(response);
                
                FundamentalData fundamentalData = parseFundamentalResponse(response, request.getSymbol());
                
                if (request.getUseCache()) {
                    cache.put(cacheKey, fundamentalData);
                }
                
                metrics.recordRequest(true, System.currentTimeMillis() - startTime);
                return fundamentalData;
                
            } catch (Exception e) {
                metrics.recordRequest(false, System.currentTimeMillis() - startTime);
                log.error("Failed to get fundamental data for {}: {}", request.getSymbol(), e.getMessage());
                throw new RuntimeException("Failed to get fundamental data", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<NewsData>> getNewsData(NewsDataRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                if (!rateLimit.canMakeRequest()) {
                    throw new DataSourceException(DATA_SOURCE_ID, 
                        DataSourceException.ErrorCode.RATE_LIMIT_EXCEEDED, 
                        "Rate limit exceeded");
                }
                
                rateLimit.incrementUsage();
                
                String url = BASE_URL + "?function=NEWS_SENTIMENT";
                if (request.getSymbols() != null && !request.getSymbols().isEmpty()) {
                    url += "&tickers=" + String.join(",", request.getSymbols());
                }
                if (request.getCategories() != null && !request.getCategories().isEmpty()) {
                    url += "&topics=" + String.join(",", request.getCategories());
                }
                url += "&limit=" + Math.min(request.getMaxResults(), 1000);
                url += "&apikey=" + apiKey;
                
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                checkForErrors(response);
                
                List<NewsData> newsData = parseNewsResponse(response);
                
                metrics.recordRequest(true, System.currentTimeMillis() - startTime);
                return newsData;
                
            } catch (Exception e) {
                metrics.recordRequest(false, System.currentTimeMillis() - startTime);
                log.error("Failed to get news data: {}", e.getMessage());
                throw new RuntimeException("Failed to get news data", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<EconomicData>> getEconomicData(EconomicDataRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                if (!rateLimit.canMakeRequest()) {
                    throw new DataSourceException(DATA_SOURCE_ID, 
                        DataSourceException.ErrorCode.RATE_LIMIT_EXCEEDED, 
                        "Rate limit exceeded");
                }
                
                rateLimit.incrementUsage();
                
                String indicator = request.getIndicator() != null && !request.getIndicator().isEmpty() 
                    ? request.getIndicator().get(0) : "GDP";
                
                String function = mapEconomicIndicator(indicator);
                String url = BASE_URL + "?function=" + function + "&interval=" + request.getFrequency() + 
                           "&apikey=" + apiKey;
                
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                checkForErrors(response);
                List<EconomicData> economicData = parseEconomicResponse(response, indicator);
                
                metrics.recordRequest(true, System.currentTimeMillis() - startTime);
                return economicData;
                
            } catch (Exception e) {
                metrics.recordRequest(false, System.currentTimeMillis() - startTime);
                log.error("Failed to get economic data for {}: {}", request.getIndicator(), e.getMessage());
                throw new RuntimeException("Failed to get economic data", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<InstrumentInfo>> searchInstruments(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = BASE_URL + "?function=SYMBOL_SEARCH&keywords=" + query + "&apikey=" + apiKey;
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                
                checkForErrors(response);
                
                return parseSearchResponse(response);
                
            } catch (Exception e) {
                log.error("Failed to search instruments for query {}: {}", query, e.getMessage());
                return new ArrayList<>();
            }
        });
    }
    
    @Override
    public StreamSubscription subscribeToRealTimeData(List<String> symbols, RealTimeDataCallback callback) {
        // Alpha Vantage doesn't provide WebSocket streaming in their standard API
        throw new UnsupportedOperationException("Real-time streaming not supported in Alpha Vantage standard API");
    }
    
    @Override
    public boolean isSymbolSupported(String symbol) {
        try {
            String url = BASE_URL + "?function=SYMBOL_SEARCH&keywords=" + symbol + "&apikey=" + apiKey;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> bestMatches = (List<Map<String, Object>>) response.get("bestMatches");
            
            return bestMatches != null && bestMatches.stream()
                .anyMatch(match -> symbol.equalsIgnoreCase(getStringValue(match, "1. symbol")));
            
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public LocalDateTime getLastUpdateTime(String symbol, DataType dataType) {
        String cacheKey = dataType.name().toLowerCase() + "_" + symbol;
        if (cache.containsKey(cacheKey)) {
            Object cachedData = cache.get(cacheKey);
            if (cachedData instanceof PriceData) {
                return ((PriceData) cachedData).getTimestamp();
            } else if (cachedData instanceof FundamentalData) {
                return ((FundamentalData) cachedData).getTimestamp();
            }
        }
        return null;
    }
    
    @Override
    public void clearCache(String symbol, DataType dataType) {
        if (symbol == null && dataType == null) {
            cache.clear();
        } else if (symbol == null) {
            cache.entrySet().removeIf(entry -> entry.getKey().startsWith(dataType.name().toLowerCase()));
        } else if (dataType == null) {
            cache.entrySet().removeIf(entry -> entry.getKey().endsWith(symbol));
        } else {
            cache.remove(dataType.name().toLowerCase() + "_" + symbol);
        }
    }
    
    @Override
    public DataSourceMetrics getMetrics() {
        return metrics;
    }
    
    /**
     * Checks for API errors in Alpha Vantage response.
     */
    private void checkForErrors(Map<String, Object> response) {
        if (response.containsKey("Error Message")) {
            throw new RuntimeException("Invalid symbol: " + response.get("Error Message").toString());
        }
        
        if (response.containsKey("Note")) {
            throw new RuntimeException("API call frequency exceeded");
        }
    }
    
    /**
     * Determines the appropriate Alpha Vantage function for historical data.
     */
    private String determineHistoricalFunction(HistoricalDataRequest request) {
        if ("1min".equals(request.getInterval()) || "5min".equals(request.getInterval()) ||
            "15min".equals(request.getInterval()) || "30min".equals(request.getInterval()) ||
            "60min".equals(request.getInterval())) {
            return "TIME_SERIES_INTRADAY";
        } else if ("daily".equals(request.getInterval())) {
            return "TIME_SERIES_DAILY";
        } else if ("weekly".equals(request.getInterval())) {
            return "TIME_SERIES_WEEKLY";
        } else if ("monthly".equals(request.getInterval())) {
            return "TIME_SERIES_MONTHLY";
        }
        return "TIME_SERIES_DAILY"; // default
    }
    
    /**
     * Maps economic indicators to Alpha Vantage function names.
     */
    private String mapEconomicIndicator(String indicator) {
        switch (indicator.toUpperCase()) {
            case "GDP": return "REAL_GDP";
            case "INFLATION": return "CPI";
            case "UNEMPLOYMENT": return "UNEMPLOYMENT";
            case "INTEREST_RATE": return "FEDERAL_FUNDS_RATE";
            case "RETAIL_SALES": return "RETAIL_SALES";
            case "CONSUMER_SENTIMENT": return "CONSUMER_SENTIMENT";
            default: return indicator;
        }
    }
    
    /**
     * Parses Alpha Vantage global quote response.
     */
    private PriceData parseGlobalQuoteResponse(Map<String, Object> response, String symbol) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> globalQuote = (Map<String, Object>) response.get("Global Quote");
            
            if (globalQuote == null || globalQuote.isEmpty()) {
                throw new DataSourceException(DATA_SOURCE_ID, 
                    DataSourceException.ErrorCode.INVALID_SYMBOL, 
                    "No data found for symbol: " + symbol);
            }
            
            return PriceData.builder()
                .symbol(symbol)
                .currentPrice(getBigDecimalValue(globalQuote, "05. price"))
                .previousClose(getBigDecimalValue(globalQuote, "08. previous close"))
                .change(getBigDecimalValue(globalQuote, "09. change"))
                .changePercent(parsePercentage(getStringValue(globalQuote, "10. change percent")))
                .dayHigh(getBigDecimalValue(globalQuote, "03. high"))
                .dayLow(getBigDecimalValue(globalQuote, "04. low"))
                .volume(getLongValue(globalQuote, "06. volume"))
                .timestamp(LocalDateTime.now())
                .dataSourceId(DATA_SOURCE_ID)
                .build();
                
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse global quote response", e);
        }
    }
    
    /**
     * Parses Alpha Vantage historical data response.
     */
    private List<HistoricalData> parseHistoricalResponse(Map<String, Object> response, HistoricalDataRequest request) {
        try {
            String timeSeriesKey = findTimeSeriesKey(response);
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> timeSeries = (Map<String, Map<String, Object>>) response.get(timeSeriesKey);
            
            if (timeSeries == null) {
                return new ArrayList<>();
            }
            
            List<HistoricalData> historicalData = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            for (Map.Entry<String, Map<String, Object>> entry : timeSeries.entrySet()) {
                LocalDate date = LocalDate.parse(entry.getKey(), formatter);
                
                // Filter by date range if specified
                if (request.getStartDate() != null && date.isBefore(request.getStartDate())) {
                    continue;
                }
                if (request.getEndDate() != null && date.isAfter(request.getEndDate())) {
                    continue;
                }
                
                Map<String, Object> dailyData = entry.getValue();
                
                HistoricalData data = HistoricalData.builder()
                    .symbol(request.getSymbol())
                    .date(date)
                    .open(getBigDecimalValue(dailyData, "1. open"))
                    .high(getBigDecimalValue(dailyData, "2. high"))
                    .low(getBigDecimalValue(dailyData, "3. low"))
                    .close(getBigDecimalValue(dailyData, "4. close"))
                    .volume(getLongValue(dailyData, "5. volume"))
                    .dataSourceId(DATA_SOURCE_ID)
                    .build();
                
                historicalData.add(data);
            }
            
            // Sort by date (newest first)
            historicalData.sort((a, b) -> b.getDate().compareTo(a.getDate()));
            
            return historicalData;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse historical response", e);
        }
    }
    
    /**
     * Finds the time series key in Alpha Vantage response.
     */
    private String findTimeSeriesKey(Map<String, Object> response) {
        for (String key : response.keySet()) {
            if (key.contains("Time Series")) {
                return key;
            }
        }
        return null;
    }
    
    /**
     * Parses Alpha Vantage fundamental data response.
     */
    private FundamentalData parseFundamentalResponse(Map<String, Object> response, String symbol) {
        try {
            return FundamentalData.builder()
                .symbol(symbol)
                .companyName(getStringValue(response, "Name"))
                .sector(getStringValue(response, "Sector"))
                .industry(getStringValue(response, "Industry"))
                .currency(getStringValue(response, "Currency"))
                .sharesOutstanding(getLongValue(response, "SharesOutstanding"))
                .marketCap(getBigDecimalValue(response, "MarketCapitalization"))
                .bookValue(getBigDecimalValue(response, "BookValue"))
                .peRatio(getBigDecimalValue(response, "PERatio"))
                .pbRatio(getBigDecimalValue(response, "PriceToBookRatio"))
                .beta(getBigDecimalValue(response, "Beta"))
                .dividendYield(getBigDecimalValue(response, "DividendYield"))
                .netIncome(getBigDecimalValue(response, "EPS"))
                .revenue(getBigDecimalValue(response, "RevenueTTM"))
                .grossProfit(getBigDecimalValue(response, "GrossProfitTTM"))
                .dataSourceId(DATA_SOURCE_ID)
                .timestamp(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse fundamental response", e);
        }
    }
    
    /**
     * Parses Alpha Vantage news sentiment response.
     */
    private List<NewsData> parseNewsResponse(Map<String, Object> response) {
        List<NewsData> newsList = new ArrayList<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> feed = (List<Map<String, Object>>) response.get("feed");
            
            if (feed != null) {
                for (Map<String, Object> article : feed) {
                    NewsData newsData = NewsData.builder()
                        .title(getStringValue(article, "title"))
                        .summary(getStringValue(article, "summary"))
                        .url(getStringValue(article, "url"))
                        .source(getStringValue(article, "source"))
                        .publishedAt(parseDateTime(getStringValue(article, "time_published")))
                        .dataSourceId(DATA_SOURCE_ID)
                        .build();
                    
                    // Parse sentiment scores
                    @SuppressWarnings("unchecked")
                    Map<String, Object> sentiment = (Map<String, Object>) article.get("overall_sentiment_score");
                    if (sentiment != null) {
                        newsData.setSentimentScore(getBigDecimalValue(sentiment, "score"));
                        newsData.setSentimentLabel(getStringValue(sentiment, "label"));
                    }
                    
                    newsData.setDataSourceId(DATA_SOURCE_ID);
                    newsList.add(newsData);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to parse news response: {}", e.getMessage());
        }
        
        return newsList;
    }
    
    /**
     * Parses Alpha Vantage economic data response.
     */
    private List<EconomicData> parseEconomicResponse(Map<String, Object> response, String indicator) {
        List<EconomicData> economicList = new ArrayList<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            
            if (data != null) {
                for (Map<String, Object> dataPoint : data) {
                    EconomicData economicData = EconomicData.builder()
                        .indicatorId(indicator)
                        .name(indicator)
                        .value(getBigDecimalValue(dataPoint, "value"))
                        .date(parseDate(getStringValue(dataPoint, "date")))
                        .unit(getStringValue(dataPoint, "unit"))
                        .dataSourceId(DATA_SOURCE_ID)
                        .timestamp(LocalDateTime.now())
                        .build();
                    
                    economicData.setDataSourceId(DATA_SOURCE_ID);
                    economicList.add(economicData);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to parse economic response: {}", e.getMessage());
        }
        
        return economicList;
    }
    
    /**
     * Parses Alpha Vantage symbol search response.
     */
    private List<InstrumentInfo> parseSearchResponse(Map<String, Object> response) {
        List<InstrumentInfo> instruments = new ArrayList<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> bestMatches = (List<Map<String, Object>>) response.get("bestMatches");
            
            if (bestMatches != null) {
                for (Map<String, Object> match : bestMatches) {
                    InstrumentInfo instrument = new InstrumentInfo(
                        getStringValue(match, "1. symbol"),
                        getStringValue(match, "2. name"),
                        getStringValue(match, "3. type")
                    );
                    
                    instrument.setRegion(getStringValue(match, "4. region"));
                    instrument.setCurrency(getStringValue(match, "8. currency"));
                    instrument.setDataSourceId(DATA_SOURCE_ID);
                    
                    instruments.add(instrument);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to parse search response: {}", e.getMessage());
        }
        
        return instruments;
    }
    
    // Helper methods for safe value extraction and parsing
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    
    private BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        
        try {
            if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            }
            String strValue = value.toString().replaceAll("[^0-9.-]", "");
            return new BigDecimal(strValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        
        try {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            String strValue = value.toString().replaceAll("[^0-9]", "");
            return Long.parseLong(strValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private BigDecimal parsePercentage(String percentStr) {
        if (percentStr == null) return null;
        
        try {
            String cleanStr = percentStr.replaceAll("[^0-9.-]", "");
            return new BigDecimal(cleanStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null) return null;
        
        try {
            // Alpha Vantage format: 20231201T120000
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            return null;
        }
    }
    
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null) return null;
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            return null;
        }
    }
}