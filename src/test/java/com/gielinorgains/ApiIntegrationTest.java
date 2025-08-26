package com.gielinorgains;

import com.gielinorgains.api.GainsApiClient;
import com.gielinorgains.model.ApiResponse;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class ApiIntegrationTest {
    
    @Test
    public void testApiConnection() throws Exception {
        GainsApiClient client = new GainsApiClient(new OkHttpClient(), new Gson());
        
        System.out.println("Testing connection to Gielinor Gains API...");
        
        ApiResponse response = client.fetchItems(10, 0.0)
            .get(30, TimeUnit.SECONDS);
        
        if (response.isSuccess()) {
            System.out.println("API test successful! Received " + response.getData().size() + " items");
            
            if (!response.getData().isEmpty()) {
                System.out.println("First item: " + response.getData().get(0).getName() + 
                    " (Score: " + response.getData().get(0).getScore() + ")");
            }
        } else {
            System.err.println("API test failed: " + response.getError());
        }
        
        assert response.isSuccess() : "API should return successful response";
        assert response.getData() != null : "API should return data";
        assert !response.getData().isEmpty() : "API should return at least one item";
    }
}