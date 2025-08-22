package com.gielinorgains.ui;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class IconCache {
    private static final int ICON_SIZE = 24;
    private static final int MAX_CACHE_SIZE = 500;
    private static final long CACHE_EXPIRY_HOURS = 24;
    
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    public IconCache() {
        // Schedule cleanup every hour
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 1, 1, TimeUnit.HOURS);
    }
    
    public ImageIcon getIcon(String iconUrl) {
        if (iconUrl == null || iconUrl.isEmpty()) {
            return null;
        }
        
        CacheEntry entry = cache.get(iconUrl);
        
        // Check if we have a valid cached entry
        if (entry != null && !entry.isExpired()) {
            return entry.icon;
        }
        
        // Start loading the icon asynchronously if not already loading
        if (entry == null || entry.isExpired()) {
            loadIconAsync(iconUrl);
        }
        
        // Return existing icon if available, null otherwise
        return entry != null ? entry.icon : null;
    }
    
    private void loadIconAsync(String iconUrl) {
        CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Loading icon from: {}", iconUrl);
                
                URL url = new URL(iconUrl);
                BufferedImage image = ImageIO.read(url);
                
                if (image != null) {
                    // Resize image to standard icon size
                    BufferedImage resized = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = resized.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(image, 0, 0, ICON_SIZE, ICON_SIZE, null);
                    g2d.dispose();
                    
                    ImageIcon icon = new ImageIcon(resized);
                    
                    // Cache the icon
                    cache.put(iconUrl, new CacheEntry(icon));
                    
                    // Ensure cache doesn't grow too large
                    if (cache.size() > MAX_CACHE_SIZE) {
                        cleanupOldestEntries();
                    }
                    
                    log.debug("Successfully cached icon for: {}", iconUrl);
                    return icon;
                }
            } catch (IOException e) {
                log.warn("Failed to load icon from: {}", iconUrl, e);
            } catch (Exception e) {
                log.error("Unexpected error loading icon from: {}", iconUrl, e);
            }
            
            return null;
        });
    }
    
    private void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> 
            now - entry.getValue().timestamp > TimeUnit.HOURS.toMillis(CACHE_EXPIRY_HOURS));
        
        log.debug("Cache cleanup completed. Current size: {}", cache.size());
    }
    
    private void cleanupOldestEntries() {
        // Remove 20% of oldest entries when cache is full
        int entriesToRemove = MAX_CACHE_SIZE / 5;
        
        cache.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e1.getValue().timestamp, e2.getValue().timestamp))
            .limit(entriesToRemove)
            .forEach(entry -> cache.remove(entry.getKey()));
        
        log.debug("Removed {} oldest cache entries. Current size: {}", entriesToRemove, cache.size());
    }
    
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        cache.clear();
    }
    
    private static class CacheEntry {
        final ImageIcon icon;
        final long timestamp;
        
        CacheEntry(ImageIcon icon) {
            this.icon = icon;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > TimeUnit.HOURS.toMillis(CACHE_EXPIRY_HOURS);
        }
    }
}