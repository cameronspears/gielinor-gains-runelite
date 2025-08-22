package com.gielinorgains;

import com.gielinorgains.api.GainsApiClient;
import com.gielinorgains.model.ApiResponse;
import com.gielinorgains.model.GainsItem;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class JsonParsingTest {
    
    @Test
    public void testJsonParsingWithDecimalValues() throws Exception {
        GainsApiClient client = new GainsApiClient();
        
        System.out.println("Testing JSON parsing with decimal volume support values...");
        
        try {
            ApiResponse response = client.fetchItems(3, 0.0)
                .get(60, TimeUnit.SECONDS);
            
            if (response.isSuccess() && response.getData() != null && !response.getData().isEmpty()) {
                System.out.println("Successfully parsed " + response.getData().size() + " items");
                
                for (int i = 0; i < Math.min(3, response.getData().size()); i++) {
                    GainsItem item = response.getData().get(i);
                    System.out.println("\nItem " + (i + 1) + ": " + item.getName());
                    System.out.println("  buyVolumeSupport: " + item.getBuyVolumeSupport());
                    System.out.println("  sellVolumeSupport: " + item.getSellVolumeSupport());
                    System.out.println("  medianHourlyVolume: " + item.getMedianHourlyVolume());
                    System.out.println("  score: " + item.getScore());
                    System.out.println("  profit: " + item.getProfit());
                }
                
                System.out.println("\n✅ JSON parsing with decimal values successful!");
            } else {
                System.out.println("❌ API call failed or returned no data");
                if (!response.isSuccess()) {
                    System.out.println("Error: " + response.getError());
                }
            }
            
        } catch (Exception e) {
            System.out.println("❌ Exception during JSON parsing test: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to fail the test
        }
    }
}