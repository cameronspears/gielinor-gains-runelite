package com.gielinorgains.ui;

import com.gielinorgains.model.GainsItem;
import com.gielinorgains.util.ScoreFormatter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.SwingUtilities;
import java.awt.geom.RoundRectangle2D;
import java.net.URI;

@Slf4j
public class ItemCardPanel extends JPanel {
    private static final int CARD_WIDTH = 190;
    private static final int CARD_HEIGHT = 180;
    private static final int CORNER_RADIUS = 8;
    private static final int PADDING = 8;
    
    // Improved color palette for better visibility in RuneLite
    private static final Color CARD_BG = new Color(45, 45, 45);        // Lighter gray for better contrast
    private static final Color CARD_BORDER = new Color(80, 80, 90);    // Lighter border
    private static final Color CARD_HOVER = new Color(60, 60, 70);     // Lighter hover state
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255);   // Pure white text
    private static final Color TEXT_SECONDARY = new Color(200, 200, 200); // Lighter gray text
    private static final Color PRICE_BG = new Color(55, 55, 65);       // Slightly lighter background
    private static final Color STATS_BG = new Color(55, 55, 65);       // Slightly lighter background
    private static final Color PROFIT_GOLD = new Color(255, 215, 0);   // Brighter gold
    
    private final GainsItem item;
    private final IconCache iconCache;
    private boolean isHovered = false;
    
    public ItemCardPanel(GainsItem item, IconCache iconCache) {
        this.item = item;
        this.iconCache = iconCache;
        
        setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Disable event consumption to let scroll events bubble up on macOS
        setRequestFocusEnabled(false);
        setVerifyInputWhenFocusTarget(false);
        
        setupMouseListeners();
    }
    
    private void setupMouseListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    openWikiPage();
                }
            }
            
        };
        
        addMouseListener(mouseAdapter);
        
        // Ensure mouse wheel events can bubble up for scrolling
        setFocusable(false);
    }
    
    private void openWikiPage() {
        try {
            String encodedName = item.getName().replace(" ", "_");
            String url = "https://oldschool.runescape.wiki/w/" + encodedName;
            Desktop.getDesktop().browse(URI.create(url));
            log.debug("Opened wiki page for: {}", item.getName());
        } catch (Exception e) {
            log.error("Failed to open wiki page for item: {}", item.getName(), e);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        
        try {
            // Enable antialiasing for smooth rendering
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // Draw card background with rounded corners
            RoundRectangle2D cardShape = new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, CORNER_RADIUS, CORNER_RADIUS);
            
            // Background
            g2.setColor(CARD_BG);
            g2.fill(cardShape);
            
            // Border with hover effect
            g2.setColor(isHovered ? CARD_HOVER : CARD_BORDER);
            g2.setStroke(new BasicStroke(2f)); // Make border more visible
            g2.draw(cardShape);
            
            // Draw hover effect (slight elevation simulation)
            if (isHovered) {
                g2.setColor(new Color(255, 255, 255, 5));
                g2.fill(cardShape);
            }
            
            // Draw content
            drawContent(g2);
            
        } finally {
            g2.dispose();
        }
    }
    
    private void drawContent(Graphics2D g2) {
        int y = PADDING + 5;
        
        // Header section (Item icon + name + score)
        y = drawHeader(g2, y);
        
        // Price section
        y = drawPriceSection(g2, y);
        
        // Stats section (profit + quantity)
        drawStatsSection(g2, y);
    }
    
    private int drawHeader(Graphics2D g2, int startY) {
        int iconSize = 20;
        int x = PADDING;
        int y = startY;
        
        // Draw item icon
        ImageIcon icon = iconCache.getIcon(item.getIcon());
        if (icon != null) {
            g2.drawImage(icon.getImage(), x, y, iconSize, iconSize, null);
        } else {
            // Placeholder if icon not loaded
            g2.setColor(ColorScheme.MEDIUM_GRAY_COLOR);
            g2.fillRect(x, y, iconSize, iconSize);
        }
        
        // Item name with wrapping
        g2.setColor(TEXT_PRIMARY);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        
        // Score indicator (colored dot + score text) - position first to reserve space
        int scoreX = CARD_WIDTH - PADDING - 35;
        
        // Score dot
        Color scoreColor = ScoreFormatter.getScoreColor(item.getScore());
        g2.setColor(scoreColor);
        g2.fillOval(scoreX, y + 4, 8, 8);
        
        // Score text
        g2.setColor(TEXT_SECONDARY);
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        g2.drawString(String.format("%.1f", item.getScore()), scoreX + 12, y + 12);
        
        // Draw item name with wrapping support
        g2.setColor(TEXT_PRIMARY);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        fm = g2.getFontMetrics();
        
        String name = item.getName();
        int maxWidth = scoreX - (x + iconSize + 8) - 8; // Space between icon and score
        int nameY = drawWrappedText(g2, name, x + iconSize + 8, y + 14, maxWidth, fm);
        
        // Return position after the name area, allowing more space for wrapped text
        return Math.max(y + iconSize + 12, nameY + 8);
    }
    
    private int drawPriceSection(Graphics2D g2, int startY) {
        int x = PADDING;
        int y = startY + 8; // Add more space from header
        int sectionHeight = 40;
        int sectionWidth = CARD_WIDTH - PADDING * 2;
        
        // Price background
        g2.setColor(PRICE_BG);
        g2.fillRoundRect(x, y, sectionWidth, sectionHeight, 8, 8);
        
        // Buy price
        g2.setColor(TEXT_SECONDARY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.drawString("Buy", x + 8, y + 14);
        
        g2.setColor(TEXT_PRIMARY);
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        String buyPrice = formatFullPrice(item.getAdjustedLowPrice());
        g2.drawString(buyPrice, x + sectionWidth - 8 - g2.getFontMetrics().stringWidth(buyPrice), y + 14);
        
        // Sell price
        g2.setColor(TEXT_SECONDARY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.drawString("Sell", x + 8, y + 30);
        
        g2.setColor(TEXT_PRIMARY);
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        String sellPrice = formatFullPrice(item.getAdjustedHighPrice());
        g2.drawString(sellPrice, x + sectionWidth - 8 - g2.getFontMetrics().stringWidth(sellPrice), y + 30);
        
        return y + sectionHeight + 8;
    }
    
    private int drawStatsSection(Graphics2D g2, int startY) {
        int x = PADDING;
        int y = startY + 4; // Add a bit more space from price section
        int sectionHeight = 55;
        int sectionWidth = CARD_WIDTH - PADDING * 2;
        
        // Stats background
        g2.setColor(STATS_BG);
        g2.fillRoundRect(x, y, sectionWidth, sectionHeight, 8, 8);
        
        // Profit
        g2.setColor(TEXT_PRIMARY);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.drawString("Profit", x + 8, y + 18);
        
        g2.setColor(TEXT_PRIMARY);
        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        String profitText = formatPrice(item.getProfit());
        g2.drawString(profitText, x + sectionWidth - 8 - g2.getFontMetrics().stringWidth(profitText), y + 18);
        
        // Quantity
        g2.setColor(TEXT_SECONDARY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.drawString("Quantity", x + 8, y + 38);
        
        g2.setColor(TEXT_PRIMARY);
        g2.setFont(new Font("Monospaced", Font.BOLD, 10));
        String qtyText = String.valueOf(item.getQuantity());
        g2.drawString(qtyText, x + sectionWidth - 8 - g2.getFontMetrics().stringWidth(qtyText), y + 38);
        
        return y + sectionHeight;
    }
    
    private String formatPrice(int price) {
        if (price >= 1000000) {
            return String.format("%.1fM", price / 1000000.0);
        } else if (price >= 1000) {
            return String.format("%.1fK", price / 1000.0);
        }
        return String.valueOf(price);
    }

    private String formatFullPrice(int price) {
        java.text.NumberFormat nf = java.text.NumberFormat.getIntegerInstance(java.util.Locale.US);
        nf.setGroupingUsed(true);
        return nf.format(price);
    }
    
    private String truncateText(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        
        String ellipsis = "...";
        int ellipsisWidth = fm.stringWidth(ellipsis);
        
        for (int i = text.length() - 1; i > 0; i--) {
            String truncated = text.substring(0, i);
            if (fm.stringWidth(truncated) + ellipsisWidth <= maxWidth) {
                return truncated + ellipsis;
            }
        }
        
        return ellipsis;
    }
    
    /**
     * Draws text with word wrapping support, returns the Y position after the last line
     */
    private int drawWrappedText(Graphics2D g2, String text, int x, int startY, int maxWidth, FontMetrics fm) {
        if (fm.stringWidth(text) <= maxWidth) {
            // Text fits on one line
            g2.drawString(text, x, startY);
            return startY;
        }
        
        // Need to wrap text
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int currentY = startY;
        int lineHeight = fm.getHeight();
        
        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            
            if (fm.stringWidth(testLine) <= maxWidth) {
                // Word fits on current line
                currentLine = new StringBuilder(testLine);
            } else {
                // Need to start new line
                if (currentLine.length() > 0) {
                    g2.drawString(currentLine.toString(), x, currentY);
                    currentY += lineHeight;
                }
                currentLine = new StringBuilder(word);
                
                // Check if single word is too long
                if (fm.stringWidth(word) > maxWidth) {
                    String truncated = truncateText(word, fm, maxWidth);
                    g2.drawString(truncated, x, currentY);
                    return currentY;
                }
            }
        }
        
        // Draw the last line
        if (currentLine.length() > 0) {
            g2.drawString(currentLine.toString(), x, currentY);
        }
        
        return currentY;
    }
    
    public GainsItem getItem() {
        return item;
    }
    
    @Override
    public boolean contains(int x, int y) {
        // For macOS touch scrolling, we want to be transparent to scroll events
        // but still handle clicks. Return false for areas that shouldn't consume scroll events.
        return super.contains(x, y);
    }
    
    @Override
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        // Forward wheel events to the nearest JScrollPane so hovering over cards scrolls the sidebar
        java.awt.Component sp = SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
        if (sp != null) {
            sp.dispatchEvent(SwingUtilities.convertMouseEvent(this, e, sp));
            e.consume();
            return;
        }
        super.processMouseWheelEvent(e);
    }
}
