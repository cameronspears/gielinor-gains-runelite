package com.gielinorgains.model;

import lombok.Data;
import lombok.Builder;
import java.util.List;

/**
 * Model for trading opportunities from Gielinor Gains API.
 *
 * SECURITY NOTE: All string fields from the API are used safely:
 * - 'name' and 'id': Only displayed in UI or URL-encoded for wiki links (no HTML rendering)
 * - 'icon' and 'detailIcon': Only used as URLs for image loading 
 * - No string values are used in SQL queries, command execution, or script evaluation
 * - All numeric values are properly typed (Double for decimal values from API)
 */
@Data
@Builder
public class GainsItem {
    private String id;
    private String name;
    private String icon;
    private String detailIcon;
    private int quantity;
    private Integer limit;
    private long dailyVolume;
    private int latestLowPrice;
    private int latestHighPrice;
    private int adjustedLowPrice;
    private int adjustedHighPrice;
    private int profit;
    private double adjustedRoi;
    private double score;
    private Double rsi;
    private Double roc;
    private String timeframe;
    private List<Double> sparklineData;
    private String quantityConfidence;
    private String quantityReasoning;
    private Double buyVolumeSupport;
    private Double sellVolumeSupport;
    private String limitingFactor;
    private double sDataCompleteness;
    private Double medianHourlyVolume;
    
    public String getFormattedPrice(int price) {
        if (price >= 1000000) {
            return String.format("%.1fM", price / 1000000.0);
        } else if (price >= 1000) {
            return String.format("%.1fK", price / 1000.0);
        }
        return String.valueOf(price);
    }
    
}