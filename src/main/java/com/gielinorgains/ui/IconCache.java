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
import java.util.concurrent.Executor;
import java.util.Set;

@Slf4j
public class IconCache {
    private static final int ICON_SIZE = 24;
    private static final int MAX_CACHE_SIZE = 500;
    private static final long CACHE_EXPIRY_HOURS = 24;
    private static final int BATCH_REPAINT_DELAY_MS = 50; // Batch repaints within 50ms
    
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    // Batch repaint optimization
    private final Set<Runnable> pendingCallbacks = ConcurrentHashMap.newKeySet();
    private final Object batchLock = new Object();
    private volatile boolean batchRepaintScheduled = false;
    
    public IconCache() {
        // Schedule cleanup every hour
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 1, 1, TimeUnit.HOURS);
    }
    
    public ImageIcon getIcon(String iconUrl) {
        return getIcon(iconUrl, null);
    }
    
    public ImageIcon getIcon(String iconUrl, Runnable onLoadCallback) {
        return getIcon(iconUrl, onLoadCallback, false);
    }
    
    /**
     * Gets an icon with priority support for visible items
     */
    public ImageIcon getIcon(String iconUrl, Runnable onLoadCallback, boolean highPriority) {
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
            loadIconAsync(iconUrl, onLoadCallback, highPriority);
        }
        
        // Return existing icon if available, null otherwise
        return entry != null ? entry.icon : null;
    }
    
    private void loadIconAsync(String iconUrl, Runnable onLoadCallback) {
        loadIconAsync(iconUrl, onLoadCallback, false);
    }
    
    private void loadIconAsync(String iconUrl, Runnable onLoadCallback, boolean highPriority) {
        CompletableFuture<ImageIcon> future;
        
        if (highPriority) {
            // High priority - load immediately
            future = CompletableFuture.supplyAsync(() -> loadIcon(iconUrl));
        } else {
            // Low priority - add small delay to batch with other requests
            future = CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(5); // Small delay for low priority items
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return loadIcon(iconUrl);
            });
        }
        
        future.thenAccept(icon -> {
            if (icon != null && onLoadCallback != null) {
                scheduleBatchedCallback(onLoadCallback);
            }
        });
    }
    
    private ImageIcon loadIcon(String iconUrl) {
        try {
            log.debug("Loading icon from: {}", iconUrl);
            
            URL url = new URL(iconUrl);
            BufferedImage image;
            synchronized (ImageIO.class) {
                image = ImageIO.read(url);
            }
            
            if (image != null) {
                // Calculate dimensions that preserve aspect ratio within ICON_SIZE bounds
                int originalWidth = image.getWidth();
                int originalHeight = image.getHeight();
                
                double aspectRatio = (double) originalWidth / originalHeight;
                int scaledWidth, scaledHeight;
                
                if (originalWidth > originalHeight) {
                    // Wider than tall - constrain by width
                    scaledWidth = ICON_SIZE;
                    scaledHeight = (int) (ICON_SIZE / aspectRatio);
                } else {
                    // Taller than wide or square - constrain by height
                    scaledHeight = ICON_SIZE;
                    scaledWidth = (int) (ICON_SIZE * aspectRatio);
                }
                
                // Create transparent canvas at standard icon size
                BufferedImage resized = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = resized.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Center the scaled image within the canvas
                int x = (ICON_SIZE - scaledWidth) / 2;
                int y = (ICON_SIZE - scaledHeight) / 2;
                
                g2d.drawImage(image, x, y, scaledWidth, scaledHeight, null);
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
    }
    
    /**
     * Schedule a callback to be executed in a batch to reduce repaint frequency
     */
    private void scheduleBatchedCallback(Runnable callback) {
        if (callback == null) return;
        
        pendingCallbacks.add(callback);
        
        synchronized (batchLock) {
            if (!batchRepaintScheduled) {
                batchRepaintScheduled = true;
                
                // Schedule the batch execution using existing executor
                cleanupExecutor.schedule(() -> {
                    SwingUtilities.invokeLater(() -> {
                        synchronized (batchLock) {
                            // Execute all pending callbacks
                            Set<Runnable> callbacks = Set.copyOf(pendingCallbacks);
                            pendingCallbacks.clear();
                            batchRepaintScheduled = false;
                            
                            // Execute callbacks
                            for (Runnable callback_ : callbacks) {
                                try {
                                    callback_.run();
                                } catch (Exception ex) {
                                    log.warn("Error executing icon load callback", ex);
                                }
                            }
                            
                            log.debug("Executed batch of {} icon load callbacks", callbacks.size());
                        }
                    });
                }, BATCH_REPAINT_DELAY_MS, TimeUnit.MILLISECONDS);
            }
        }
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