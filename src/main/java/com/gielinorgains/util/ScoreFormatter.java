package com.gielinorgains.util;

import java.awt.Color;

public class ScoreFormatter {
    
    public static Color getScoreColor(double score) {
        if (score >= 4.5) {
            return new Color(34, 197, 94); // green-500
        } else if (score >= 3.5) {
            return new Color(132, 204, 22); // lime-500
        } else if (score >= 2.5) {
            return new Color(234, 179, 8); // yellow-500
        } else if (score >= 1.5) {
            return new Color(249, 115, 22); // orange-500
        } else {
            return new Color(239, 68, 68); // red-500
        }
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