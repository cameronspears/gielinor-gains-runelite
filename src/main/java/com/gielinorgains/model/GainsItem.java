package com.gielinorgains.model;

import lombok.Data;
import lombok.Builder;
import java.util.List;

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
    
    public String getFormattedProfit() {
        return getFormattedPrice(profit);
    }
    
    public String getScoreDisplay() {
        return String.format("%.1f", score);
    }
    
    public String getPriceRange() {
        return getFormattedPrice(adjustedLowPrice) + " - " + getFormattedPrice(adjustedHighPrice);
    }
}