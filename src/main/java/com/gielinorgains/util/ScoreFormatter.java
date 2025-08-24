package com.gielinorgains.util;

import java.awt.Color;

public class ScoreFormatter {
    
    // Color stops matching the website's exact color scheme (from ItemCard.tsx)
    private static final ColorStop[] COLOR_STOPS = {
        new ColorStop(0, new Color(0xdc, 0x26, 0x26)), // red-600 #dc2626
        new ColorStop(1, new Color(0xea, 0x58, 0x0c)), // orange-500 #ea580c
        new ColorStop(2, new Color(0xf5, 0x9e, 0x0b)), // amber-500 #f59e0b
        new ColorStop(3, new Color(0x84, 0xcc, 0x16)), // lime-500 #84cc16
        new ColorStop(4, new Color(0x22, 0xc5, 0x5e)), // green-500 #22c55e
        new ColorStop(5, new Color(0x16, 0xa3, 0x4a))  // green-600 #16a34a
    };
    
    private static final Color ZERO_SCORE_COLOR = new Color(0xa8, 0xa2, 0x9e); // stone-400 #a8a29e
    
    private static class ColorStop {
        final double value;
        final Color color;
        
        ColorStop(double value, Color color) {
            this.value = value;
            this.color = color;
        }
    }
    
    public static Color getScoreColor(double score) {
        // Clamp score to valid range
        double clampedScore = Math.max(0, Math.min(5, score));
        
        // Return gray color for exactly 0 score
        if (clampedScore == 0) {
            return ZERO_SCORE_COLOR;
        }
        
        // Find the appropriate color stops for interpolation
        if (clampedScore <= COLOR_STOPS[0].value) {
            return COLOR_STOPS[0].color;
        }
        
        if (clampedScore >= COLOR_STOPS[COLOR_STOPS.length - 1].value) {
            return COLOR_STOPS[COLOR_STOPS.length - 1].color;
        }
        
        // Find the two stops to interpolate between
        for (int i = 0; i < COLOR_STOPS.length - 1; i++) {
            ColorStop current = COLOR_STOPS[i];
            ColorStop next = COLOR_STOPS[i + 1];
            
            if (clampedScore >= current.value && clampedScore <= next.value) {
                return interpolateColor(clampedScore, current, next);
            }
        }
        
        // Fallback (should never reach here)
        return COLOR_STOPS[0].color;
    }
    
    private static Color interpolateColor(double value, ColorStop lower, ColorStop upper) {
        double range = upper.value - lower.value;
        double fraction = range == 0 ? 0 : (value - lower.value) / range;
        
        int r = (int) Math.round(lower.color.getRed() + (upper.color.getRed() - lower.color.getRed()) * fraction);
        int g = (int) Math.round(lower.color.getGreen() + (upper.color.getGreen() - lower.color.getGreen()) * fraction);
        int b = (int) Math.round(lower.color.getBlue() + (upper.color.getBlue() - lower.color.getBlue()) * fraction);
        
        // Ensure values are within valid range
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        
        return new Color(r, g, b);
    }
    
    public static String getScoreText(double score) {
        return String.format("%.1f", Math.min(5.0, Math.max(0.0, score)));
    }
    
    public static String getScoreStars(double score) {
        int fullStars = (int) score;
        boolean halfStar = (score - fullStars) >= 0.5;
        
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }
        if (halfStar && fullStars < 5) {
            stars.append("☆");
        }
        
        return stars.toString();
    }
}