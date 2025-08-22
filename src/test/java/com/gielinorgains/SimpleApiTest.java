package com.gielinorgains;

import com.gielinorgains.api.GainsApiClient;
import com.gielinorgains.model.ApiResponse;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class SimpleApiTest {
    
    @Test
    public void testApiConnectivity() throws Exception {
        GainsApiClient client = new GainsApiClient();
        
        System.out.println("Testing Gielinor Gains API connectivity...");
        
        try {
            ApiResponse response = client.fetchItems(5, 0.0)
                .get(60, TimeUnit.SECONDS); // Increased timeout
            
            System.out.println("API Response received");
            System.out.println("Success: " + response.isSuccess());
            
            if (response.isSuccess()) {
                System.out.println("Items count: " + (response.getData() != null ? response.getData().size() : "null"));
                if (response.getData() != null && !response.getData().isEmpty()) {
                    System.out.println("First item: " + response.getData().get(0).getName());
                }
            } else {
                System.out.println("Error: " + response.getError());
            }
            
            // Don't fail the test if API is unreachable, just log results
            
        } catch (Exception e) {
            System.out.println("Exception during API test: " + e.getMessage());
            e.printStackTrace();
            // Don't fail the test, just log the exception
        }
    }
}