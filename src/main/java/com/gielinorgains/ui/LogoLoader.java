package com.gielinorgains.ui;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class LogoLoader {
    private static BufferedImage cachedLogo = null;
    private static final int LOGO_WIDTH = 90;
    private static final int LOGO_HEIGHT = 32;
    
    /**
     * Loads and returns the Gielinor Gains logo, scaled appropriately for the UI.
     * The logo is cached after first load for performance.
     */
    public static BufferedImage getLogo() {
        if (cachedLogo == null) {
            cachedLogo = loadLogoFromResources();
        }
        return cachedLogo;
    }
    
    /**
     * Returns the logo as an ImageIcon for use in Swing components.
     */
    public static ImageIcon getLogoIcon() {
        BufferedImage logo = getLogo();
        return logo != null ? new ImageIcon(logo) : null;
    }
    
    private static BufferedImage loadLogoFromResources() {
        try {
            // Try to load the logo from resources
            InputStream logoStream = LogoLoader.class.getResourceAsStream("/logo-white.png");
            if (logoStream != null) {
                BufferedImage originalLogo = ImageIO.read(logoStream);
                logoStream.close();
                
                if (originalLogo != null) {
                    // Scale the logo to appropriate size for the header
                    BufferedImage scaledLogo = new BufferedImage(LOGO_WIDTH, LOGO_HEIGHT, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = scaledLogo.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    
                    // Calculate scaling to preserve aspect ratio
                    double scaleX = (double) LOGO_WIDTH / originalLogo.getWidth();
                    double scaleY = (double) LOGO_HEIGHT / originalLogo.getHeight();
                    double scale = Math.min(scaleX, scaleY);
                    
                    int scaledWidth = (int) (originalLogo.getWidth() * scale);
                    int scaledHeight = (int) (originalLogo.getHeight() * scale);
                    int x = (LOGO_WIDTH - scaledWidth) / 2;
                    int y = (LOGO_HEIGHT - scaledHeight) / 2;
                    
                    // Draw the logo scaled to fit with preserved aspect ratio
                    g2d.drawImage(originalLogo, x, y, scaledWidth, scaledHeight, null);
                    g2d.dispose();
                    
                    log.debug("Successfully loaded and scaled Gielinor Gains logo");
                    return scaledLogo;
                }
            }
        } catch (IOException e) {
            log.warn("Failed to load logo from resources: {}", e.getMessage());
        }
        
        // Fallback: create a simple text-based logo
        log.debug("Creating fallback text logo");
        return createFallbackLogo();
    }
    
    /**
     * Creates a simple text-based logo as fallback when the image can't be loaded.
     */
    private static BufferedImage createFallbackLogo() {
        BufferedImage fallbackLogo = new BufferedImage(LOGO_WIDTH, LOGO_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = fallbackLogo.createGraphics();
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Clear background
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, LOGO_WIDTH, LOGO_HEIGHT);
        g2d.setComposite(AlphaComposite.Src);
        
        // Draw "Gielinor Gains" text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        FontMetrics fm = g2d.getFontMetrics();
        String text = "Gielinor Gains";
        int textWidth = fm.stringWidth(text);
        int x = (LOGO_WIDTH - textWidth) / 2;
        int y = (LOGO_HEIGHT + fm.getAscent()) / 2 - 2;
        
        // Add a subtle background for better visibility
        g2d.setColor(new Color(61, 125, 223, 80)); // Semi-transparent blue
        g2d.fillRoundRect(x - 4, y - fm.getAscent() - 2, textWidth + 8, fm.getHeight() + 2, 4, 4);
        
        // Draw the text
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, x, y);
        
        g2d.dispose();
        return fallbackLogo;
    }
    
    /**
     * Gets the preferred width of the logo for layout purposes.
     */
    public static int getLogoWidth() {
        return LOGO_WIDTH;
    }
    
    /**
     * Gets the preferred height of the logo for layout purposes.
     */
    public static int getLogoHeight() {
        return LOGO_HEIGHT;
    }
    
    /**
     * Clears the cached logo, forcing it to be reloaded on next access.
     * Useful for testing or if logo resources change.
     */
    public static void clearCache() {
        cachedLogo = null;
    }
}