package com.finrobot.datasource;

import com.finrobot.datasource.FinancialData.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Yahoo Finance data source implementation.
 * 
 * This implementation provides access to Yahoo Finance's free financial data API,
 * including real-time quotes, historical data, and basic fundamental information.
 * It handles rate limiting, caching, and error recovery automatically.
 */
@Slf4j
@Component
public class YahooFinanceDataSource implements DataSource {
    
    private static final String DATA_SOURCE_ID = "yahoo-finance";
    private static final String BASE_URL = "https://query1.finance.yahoo.com/v8/finance";
    private static final String CHART_URL = BASE_URL + "/chart";
    private static final String QUOTE_URL = BASE_URL + "/quote";
    private static final String SEARCH_URL = BASE_URL + "/search";
    
    private final RestTemplate restTemplate;
    private final Map<String, Object> cache;
    private final DataSourceProvider provider;
    private final RateLimit rateLimit;
    private final DataSourceMetrics metrics;
    private boolean initialized = false;
    
    public YahooFinanceDataSource() {
        this.restTemplate = new RestTemplate();
        this.cache = new ConcurrentHashMap<>();
        this.provider = new DataSourceProvider("Yahoo Finance", "https://finance.yahoo.com");
        this.provider.setApiVersion("v8");
        this.provider.setRequiresApiKey(false);
        this.provider.setIsPremium(false);
        this.provider.setDocumentation("https://github.com/ranaroussi/yfinance");
        
        // Yahoo Finance has informal rate limits
        this.rateLimit = new RateLimit(60, 1000, 10000);
        this.metrics = new DataSourceMetrics(DATA_SOURCE_ID);
    }
    
    @Override
    public String getDataSourceId() {
        return DATA_SOURCE_ID;
    }
    
    @Override
    public String getDataSourceName() {
        return "Yahoo Finance";
    }
    
    @Override
    public DataSourceProvider getProvider() {
        return provider;
    }
    
    @Override
    public boolean isAvailable() {
        if (!initialized) {
            return false;
        }
        
        try {
            // Simple health check by requesting a well-known symbol
            String url = QUOTE_URL + "?symbols=AAPL";
            restTemplate.getForObject(url, Map.class);
            return true;
        } catch (Exception e) {
            log.warn("Yahoo Finance health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public List<DataType> getSupportedDataTypes() {
        return Arrays.asList(
            DataType.REAL_TIME_PRICE,
            DataType.HISTORICAL_PRICE,
            DataType.INTRADAY_PRICE,
            DataType.FUNDAMENTAL_DATA
        );
    }
    
    @Override
    public RateLimit getRateLimit() {
        return rateLimit;
    }
    
    @Override
    public void initialize(Map<String, Object> configuration) {
        try {
            log.info("Initializing Yahoo Finance data source...");
            
            // Configure RestTemplate if needed
            if (configuration.containsKey("timeout")) {
                // Set timeout configuration
            }
            
            // Test connectivity
            if (!isAvailable()) {
                throw new RuntimeException("Cannot connect to Yahoo Finance API");
            }
            
            initialized = true;
            log.info("Yahoo Finance data source initialized successfully");
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Yahoo Finance data source: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void shutdown() {
        log.info("Shutting down Yahoo Finance data source...");
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
                    if (cachedData.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(1))) {
                        metrics.recordCacheHit();
                        return cachedData;
                    }
                }
                
                metrics.recordCacheMiss();
                rateLimit.incrementUsage();
                
                String url = QUOTE_URL + "?symbols=" + request.getSymbol();
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                
                PriceData priceData = parseQuoteResponse(response, request.getSymbol());
                
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
                
                String cacheKey = String.format("historical_%s_%s_%s_%s", 
                    request.getSymbol(), request.getStartDate(), request.getEndDate(), request.getInterval());
                
                if (request.getUseCache() && cache.containsKey(cacheKey)) {
                    @SuppressWarnings("unchecked")
                    List<HistoricalData> cachedData = (List<HistoricalData>) cache.get(cacheKey);
                    metrics.recordCacheHit();
                    return cachedData;
                }
                
                metrics.recordCacheMiss();
                rateLimit.incrementUsage();
                
                long period1 = request.getStartDate().toEpochDay() * 86400;
                long period2 = request.getEndDate().toEpochDay() * 86400;
                
                String url = String.format("%s/%s?period1=%d&period2=%d&interval=%s",
                    CHART_URL, request.getSymbol(), period1, period2, request.getInterval());
                
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                List<HistoricalData> historicalData = parseChartResponse(response, request.getSymbol());
                
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
                // Yahoo Finance provides limited fundamental data in quote response
                String url = QUOTE_URL + "?symbols=" + request.getSymbol();
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                
                FundamentalData fundamentalData = parseFundamentalResponse(response, request.getSymbol());
                
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
        // Yahoo Finance doesn't provide a direct news API in their free tier
        return CompletableFuture.completedFuture(new ArrayList<>());
    }
    
    @Override
    public CompletableFuture<List<EconomicData>> getEconomicData(EconomicDataRequest request) {
        // Yahoo Finance doesn't provide economic indicators in their free API
        return CompletableFuture.completedFuture(new ArrayList<>());
    }
    
    @Override
    public CompletableFuture<List<InstrumentInfo>> searchInstruments(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = SEARCH_URL + "?q=" + query;
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                
                return parseSearchResponse(response);
                
            } catch (Exception e) {
                log.error("Failed to search instruments for query {}: {}", query, e.getMessage());
                return new ArrayList<>();
            }
        });
    }
    
    @Override
    public StreamSubscription subscribeToRealTimeData(List<String> symbols, RealTimeDataCallback callback) {
        // Yahoo Finance free API doesn't support real-time streaming
        // This would require WebSocket implementation or polling
        throw new UnsupportedOperationException("Real-time streaming not supported in Yahoo Finance free tier");
    }
    
    @Override
    public boolean isSymbolSupported(String symbol) {
        try {
            String url = QUOTE_URL + "?symbols=" + symbol;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> quoteResponse = (Map<String, Object>) response.get("quoteResponse");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>) quoteResponse.get("result");
            
            return result != null && !result.isEmpty();
            
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
     * Parses Yahoo Finance quote response into PriceData.
     */
    private PriceData parseQuoteResponse(Map<String, Object> response, String symbol) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> quoteResponse = (Map<String, Object>) response.get("quoteResponse");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>) quoteResponse.get("result");
            
            if (result == null || result.isEmpty()) {
                throw new DataSourceException(DATA_SOURCE_ID, 
                    DataSourceException.ErrorCode.INVALID_SYMBOL, 
                    "Symbol not found: " + symbol);
            }
            
            Map<String, Object> quote = result.get(0);
            
            return PriceData.builder()
                .symbol(symbol)
                .name(getStringValue(quote, "longName"))
                .currentPrice(getBigDecimalValue(quote, "regularMarketPrice"))
                .previousClose(getBigDecimalValue(quote, "regularMarketPreviousClose"))
                .change(getBigDecimalValue(quote, "regularMarketChange"))
                .changePercent(getBigDecimalValue(quote, "regularMarketChangePercent"))
                .dayHigh(getBigDecimalValue(quote, "regularMarketDayHigh"))
                .dayLow(getBigDecimalValue(quote, "regularMarketDayLow"))
                .volume(getLongValue(quote, "regularMarketVolume"))
                .marketCap(getBigDecimalValue(quote, "marketCap"))
                .peRatio(getBigDecimalValue(quote, "trailingPE"))
                .eps(getBigDecimalValue(quote, "epsTrailingTwelveMonths"))
                .dividendYield(getBigDecimalValue(quote, "dividendYield"))
                .currency(getStringValue(quote, "currency"))
                .exchange(getStringValue(quote, "fullExchangeName"))
                .timestamp(LocalDateTime.now())
                .dataSourceId(DATA_SOURCE_ID)
                .build();
                
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse quote response", e);
        }
    }
    
    /**
     * Parses Yahoo Finance chart response into HistoricalData list.
     */
    private List<HistoricalData> parseChartResponse(Map<String, Object> response, String symbol) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> chart = (Map<String, Object>) response.get("chart");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>) chart.get("result");
            
            if (result == null || result.isEmpty()) {
                return new ArrayList<>();
            }
            
            Map<String, Object> data = result.get(0);
            @SuppressWarnings("unchecked")
            List<Long> timestamps = (List<Long>) data.get("timestamp");
            @SuppressWarnings("unchecked")
            Map<String, Object> indicators = (Map<String, Object>) data.get("indicators");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> quote = (List<Map<String, Object>>) indicators.get("quote");
            
            if (quote == null || quote.isEmpty()) {
                return new ArrayList<>();
            }
            
            Map<String, Object> ohlcv = quote.get(0);
            @SuppressWarnings("unchecked")
            List<Double> open = (List<Double>) ohlcv.get("open");
            @SuppressWarnings("unchecked")
            List<Double> high = (List<Double>) ohlcv.get("high");
            @SuppressWarnings("unchecked")
            List<Double> low = (List<Double>) ohlcv.get("low");
            @SuppressWarnings("unchecked")
            List<Double> close = (List<Double>) ohlcv.get("close");
            @SuppressWarnings("unchecked")
            List<Long> volume = (List<Long>) ohlcv.get("volume");
            
            List<HistoricalData> historicalData = new ArrayList<>();
            
            for (int i = 0; i < timestamps.size(); i++) {
                LocalDate date = LocalDate.ofEpochDay(timestamps.get(i) / 86400);
                
                HistoricalData data_point = HistoricalData.builder()
                    .symbol(symbol)
                    .date(date)
                    .open(open.get(i) != null ? BigDecimal.valueOf(open.get(i)) : null)
                    .high(high.get(i) != null ? BigDecimal.valueOf(high.get(i)) : null)
                    .low(low.get(i) != null ? BigDecimal.valueOf(low.get(i)) : null)
                    .close(close.get(i) != null ? BigDecimal.valueOf(close.get(i)) : null)
                    .volume(volume.get(i))
                    .dataSourceId(DATA_SOURCE_ID)
                    .build();
                
                historicalData.add(data_point);
            }
            
            return historicalData;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse chart response", e);
        }
    }
    
    /**
     * Parses fundamental data from quote response.
     */
    private FundamentalData parseFundamentalResponse(Map<String, Object> response, String symbol) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> quoteResponse = (Map<String, Object>) response.get("quoteResponse");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> result = (List<Map<String, Object>>) quoteResponse.get("result");
            
            if (result == null || result.isEmpty()) {
                throw new DataSourceException(DATA_SOURCE_ID, 
                    DataSourceException.ErrorCode.INVALID_SYMBOL, 
                    "Symbol not found: " + symbol);
            }
            
            Map<String, Object> quote = result.get(0);
            
            return FundamentalData.builder()
                .symbol(symbol)
                .companyName(getStringValue(quote, "longName"))
                .sector(getStringValue(quote, "sector"))
                .industry(getStringValue(quote, "industry"))
                .currency(getStringValue(quote, "currency"))
                .sharesOutstanding(getLongValue(quote, "sharesOutstanding"))
                .marketCap(getBigDecimalValue(quote, "marketCap"))
                .bookValue(getBigDecimalValue(quote, "bookValue"))
                .peRatio(getBigDecimalValue(quote, "trailingPE"))
                .pbRatio(getBigDecimalValue(quote, "priceToBook"))
                .beta(getBigDecimalValue(quote, "beta"))
                .dividendYield(getBigDecimalValue(quote, "dividendYield"))
                .dataSourceId(DATA_SOURCE_ID)
                .timestamp(LocalDateTime.now())
                .build();
                
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse fundamental response", e);
        }
    }
    
    /**
     * Parses search response into InstrumentInfo list.
     */
    private List<InstrumentInfo> parseSearchResponse(Map<String, Object> response) {
        List<InstrumentInfo> instruments = new ArrayList<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> quotes = (List<Map<String, Object>>) response.get("quotes");
            
            if (quotes != null) {
                for (Map<String, Object> quote : quotes) {
                    InstrumentInfo instrument = new InstrumentInfo(
                        getStringValue(quote, "symbol"),
                        getStringValue(quote, "longname"),
                        getStringValue(quote, "quoteType")
                    );
                    
                    instrument.setExchange(getStringValue(quote, "exchange"));
                    instrument.setDataSourceId(DATA_SOURCE_ID);
                    
                    instruments.add(instrument);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to parse search response: {}", e.getMessage());
        }
        
        return instruments;
    }
    
    // Helper methods for safe value extraction
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
            return new BigDecimal(value.toString());
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
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}