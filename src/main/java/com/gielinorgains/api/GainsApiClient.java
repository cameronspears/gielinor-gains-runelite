package com.gielinorgains.api;

import com.gielinorgains.model.ApiResponse;
import com.gielinorgains.model.GainsItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Singleton
public class GainsApiClient {
    private static final String API_BASE_URL = "https://gielinorgains.com/api";
    private static final String ITEMS_ENDPOINT = "/items";
    private static final int CACHE_TTL_SECONDS = 90;
    
    private final OkHttpClient httpClient;
    private final Gson gson;
    private ApiResponse cachedResponse;
    private long lastFetchTime;
    private boolean lastRequestWasCached;
    
    @Inject
    public GainsApiClient() {
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();
            
        this.gson = new GsonBuilder()
            .setLenient()
            .create();
    }
    
    public CompletableFuture<ApiResponse> fetchItems(int limit, double minScore) {
        return fetchItems(limit, minScore, false);
    }
    
    public CompletableFuture<ApiResponse> fetchItems(int limit, double minScore, boolean forceRefresh) {
        // Check cache first (unless force refresh is requested)
        if (!forceRefresh && isCacheValid()) {
            log.debug("Returning cached items data");
            lastRequestWasCached = true;
            return CompletableFuture.completedFuture(filterResponse(cachedResponse, limit, minScore));
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = API_BASE_URL + ITEMS_ENDPOINT + "?limit=" + limit;
                
                Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Gielinor-Gains-RuneLite-Plugin/1.0.0")
                    .addHeader("Accept", "application/json")
                    .build();
                
                log.info("Fetching items from: {}", url);
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        log.error("API request failed with status: {}", response.code());
                        return createErrorResponse("API request failed: " + response.code());
                    }
                    
                    String responseBody = response.body().string();
                    log.debug("Received response: {} chars", responseBody.length());
                    
                    // Parse the response - it should be an object with 'data' and 'totalItems'
                    Type responseType = new TypeToken<ApiResponseDto>(){}.getType();
                    ApiResponseDto dto = gson.fromJson(responseBody, responseType);
                    
                    if (dto == null || dto.data == null) {
                        log.error("Invalid response structure");
                        return createErrorResponse("Invalid response structure");
                    }
                    
                    ApiResponse apiResponse = new ApiResponse();
                    apiResponse.setData(dto.data);
                    apiResponse.setTotalItems(dto.totalItems);
                    apiResponse.setSuccess(true);
                    
                    // Cache the response
                    cachedResponse = apiResponse;
                    lastFetchTime = System.currentTimeMillis();
                    lastRequestWasCached = false;
                    
                    log.info("Successfully fetched {} items", dto.data.size());
                    return filterResponse(apiResponse, limit, minScore);
                    
                } catch (IOException e) {
                    log.error("Network error fetching items from {}: {}", url, e.getMessage(), e);
                    return createErrorResponse("Network error: " + e.getMessage());
                }
                
            } catch (Exception e) {
                log.error("Unexpected error fetching items", e);
                return createErrorResponse("Unexpected error: " + e.getMessage());
            }
        });
    }
    
    private boolean isCacheValid() {
        return cachedResponse != null && 
               (System.currentTimeMillis() - lastFetchTime) < (CACHE_TTL_SECONDS * 1000);
    }
    
    private ApiResponse filterResponse(ApiResponse response, int limit, double minScore) {
        if (response == null || response.getData() == null) {
            return response;
        }
        
        List<GainsItem> filteredItems = response.getData().stream()
            .filter(item -> item.getScore() >= minScore)
            .limit(limit)
            .collect(java.util.stream.Collectors.toList());
            
        ApiResponse filtered = new ApiResponse();
        filtered.setData(filteredItems);
        filtered.setTotalItems(filteredItems.size());
        filtered.setSuccess(true);
        
        return filtered;
    }
    
    private ApiResponse createErrorResponse(String error) {
        ApiResponse response = new ApiResponse();
        response.setSuccess(false);
        response.setError(error);
        return response;
    }
    
    // DTO class for parsing the API response
    private static class ApiResponseDto {
        public List<GainsItem> data;
        public int totalItems;
    }
    
    public void clearCache() {
        cachedResponse = null;
        lastFetchTime = 0;
        lastRequestWasCached = false;
    }
    
    public boolean wasLastRequestCached() {
        return lastRequestWasCached;
    }
    
    public long getLastFetchTime() {
        return lastFetchTime;
    }
    
    public boolean hasCachedData() {
        return cachedResponse != null;
    }
}