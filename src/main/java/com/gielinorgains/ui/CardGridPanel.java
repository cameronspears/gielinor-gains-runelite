package com.gielinorgains.ui;

import com.gielinorgains.GielinorGainsConfig;
import com.gielinorgains.model.GainsItem;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class CardGridPanel extends JPanel implements Scrollable {
    private static final int CARD_SPACING = 6;
    private static final int CARD_WIDTH = 190;
    private static final int CARD_HEIGHT = 180;
    private final IconCache iconCache;
    private List<GainsItem> items = new ArrayList<>();
    private final List<ItemCardPanel> cardPanels = new ArrayList<>();
    private String sortBy = "score";
    private boolean ascending = false;
    private JComponent headerComponent;
    private JComponent statusComponent;
    private boolean loading = true;
    private Timer loadingTipTimer;
    private JLabel loadingTipLabel;
    
    public CardGridPanel(IconCache iconCache, GielinorGainsConfig config) {
        this.iconCache = iconCache;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        
        // Enable touch scrolling on macOS by ensuring this panel doesn't consume events
        setFocusable(false);
    }

    /**
     * Optionally provides a header and status component to render above and below the cards.
     * This allows the entire sidebar (header + cards + status) to scroll together.
     */
    public void setHeaderAndStatus(JComponent header, JComponent status) {
        this.headerComponent = header;
        this.statusComponent = status;
        updateLayout();
    }

    
    @Override
    public Dimension getPreferredSize() {
        // Calculate height based on actual card panels
        int height = cardPanels.size() * (CARD_HEIGHT + CARD_SPACING) + CARD_SPACING;
        
        // Account for optional header and status components and surrounding spacing
        if (headerComponent != null) {
            height += headerComponent.getPreferredSize().height + CARD_SPACING;
        }
        if (statusComponent != null) {
            height += statusComponent.getPreferredSize().height + CARD_SPACING;
        }
        return new Dimension(CARD_WIDTH + CARD_SPACING * 2, height);
    }
    
    /**
     * Updates the items displayed in the grid.
     */
    public void setItems(List<GainsItem> newItems) {
        this.items = new ArrayList<>(newItems);
        this.loading = false;
        sortItems();
        createCardPanels();
        updateLayout();
        
        log.debug("Set {} items, created {} card panels", items.size(), cardPanels.size());
    }
    
    /**
     * Sets the sort criteria and updates the display.
     */
    public void setSorting(String sortBy, boolean ascending) {
        this.sortBy = sortBy;
        this.ascending = ascending;
        sortItems();
        createCardPanels();
        updateLayout();
    }

    /**
     * Sets loading state.
     */
    public void setLoading(boolean loading) {
        this.loading = loading;
        if (!loading && loadingTipTimer != null) {
            loadingTipTimer.stop();
        }
        updateLayout();
    }
    
    private void sortItems() {
        if (items.isEmpty()) return;
        
        Comparator<GainsItem> comparator = getComparator();
        if (!ascending) {
            comparator = comparator.reversed();
        }
        
        items.sort(comparator);
    }
    
    private Comparator<GainsItem> getComparator() {
        switch (sortBy.toLowerCase()) {
            case "profit":
                return Comparator.comparing(GainsItem::getProfit);
            case "roi":
                return Comparator.comparing(GainsItem::getAdjustedRoi);
            case "volume":
                return Comparator.comparing(GainsItem::getDailyVolume);
            case "name":
                return Comparator.comparing(GainsItem::getName, String.CASE_INSENSITIVE_ORDER);
            default:
                return Comparator.comparing(GainsItem::getScore); // "score" or default
        }
    }
    
    /**
     * Creates card panels for all items
     */
    private void createCardPanels() {
        // Clear existing panels
        cardPanels.clear();
        
        // Create new card panels for all items
        for (GainsItem item : items) {
            ItemCardPanel cardPanel = new ItemCardPanel(item, iconCache);
            cardPanels.add(cardPanel);
        }
        
        log.debug("Created {} card panels", cardPanels.size());
    }
    
    private void addComponentWithSpacing(JComponent component) {
        component.setAlignmentX(Component.CENTER_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
        add(component);
        add(Box.createRigidArea(new Dimension(0, CARD_SPACING)));
    }
    
    private void updateLayout() {
        removeAll();
        
        if (loading) {
            log.debug("Showing loading state");
            showLoadingState();
            revalidate();
            repaint();
            return;
        }

        if (cardPanels.isEmpty()) {
            log.debug("No card panels to display, showing empty state");
            showEmptyState();
            revalidate();
            repaint();
            return;
        }
        
        log.debug("Updating layout with {} card panels", cardPanels.size());
        
        // Add spacing at top
        add(Box.createRigidArea(new Dimension(0, CARD_SPACING)));

        // Optional header
        if (headerComponent != null) {
            addComponentWithSpacing(headerComponent);
        }
        
        // Add all cards with spacing between them
        for (int i = 0; i < cardPanels.size(); i++) {
            ItemCardPanel card = cardPanels.get(i);
            card.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(card);
            
            // Add spacing between cards (but not after the last one)
            if (i < cardPanels.size() - 1) {
                add(Box.createRigidArea(new Dimension(0, CARD_SPACING)));
            }
        }
        
        // Optional status/footer
        add(Box.createRigidArea(new Dimension(0, CARD_SPACING)));
        if (statusComponent != null) {
            addComponentWithSpacing(statusComponent);
        }

        // Add spacing at bottom
        add(Box.createRigidArea(new Dimension(0, CARD_SPACING)));
        
        revalidate();
        repaint();
        
        log.debug("Updated layout with {} cards", cardPanels.size());
    }
    
    
    private void showEmptyState() {
        // Add spacing at top
        add(Box.createRigidArea(new Dimension(0, 40)));
        
        // Empty state message
        JLabel emptyLabel = new JLabel("No trading opportunities available");
        emptyLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        emptyLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(emptyLabel);
        
        add(Box.createRigidArea(new Dimension(0, 8)));
        
        JLabel hintLabel = new JLabel("Try refreshing or adjusting your score filter");
        hintLabel.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
        hintLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(hintLabel);
        
        // Add flexible space to push content up
        add(Box.createVerticalGlue());
    }

    private void showLoadingState() {
        // Add spacing at top
        add(Box.createRigidArea(new Dimension(0, 30)));

        // Gielinor Gains logo (clickable)
        ImageIcon logoIcon = LogoLoader.getLogoIcon();
        if (logoIcon != null) {
            JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            logoPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
            logoPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            logoPanel.setToolTipText("Visit GielinorGains.com");
            logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            // Add click listener to logo panel
            logoPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    openWebsite();
                }
            });
            
            JLabel logoLabel = new JLabel(logoIcon);
            logoPanel.add(logoLabel);
            add(logoPanel);
            add(Box.createRigidArea(new Dimension(0, 20)));
        }

        // Skip the static loading message - using rotating tips instead

        // Main rotating loading message
        loadingTipLabel = new JLabel("Loading latest prices and volumes...");
        loadingTipLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        loadingTipLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        loadingTipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(loadingTipLabel);

        add(Box.createRigidArea(new Dimension(0, 16)));

        // Progress bar
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setAlignmentX(Component.CENTER_ALIGNMENT);
        bar.setMaximumSize(new Dimension(180, 10));
        bar.setPreferredSize(new Dimension(180, 10));
        add(bar);

        // Start rotating tips
        startLoadingTips();

        // Flexible space to center content
        add(Box.createVerticalGlue());
    }
    
    private void startLoadingTips() {
        if (loadingTipTimer != null) {
            loadingTipTimer.stop();
        }
        
        String[] tips = {
            "Loading latest prices and volumes...",
            "Loading timeseries data...",
            "Calculating scores...",
            "Determining quantities and offers..."
        };
        
        // Cumulative timing: when each message should appear
        int[] showAtMs = {0, 3000, 9000, 15000}; // 0s, 3s, 9s, 15s
        
        loadingTipTimer = new Timer(500, new java.awt.event.ActionListener() {
            private final long startTime = System.currentTimeMillis();
            private int currentTip = 0;
            
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (loadingTipLabel == null || !loading) {
                    loadingTipTimer.stop();
                    return;
                }
                
                long elapsed = System.currentTimeMillis() - startTime;
                
                // Check if we should advance to the next tip
                int nextTip = currentTip;
                for (int i = currentTip + 1; i < showAtMs.length; i++) {
                    if (elapsed >= showAtMs[i]) {
                        nextTip = i;
                    } else {
                        break;
                    }
                }
                
                // Update the message if we've advanced
                if (nextTip != currentTip) {
                    currentTip = nextTip;
                    loadingTipLabel.setText(tips[currentTip]);
                }
                
                // Stop after showing the final message for a bit
                if (currentTip == tips.length - 1 && elapsed >= 19000) {
                    loadingTipTimer.stop();
                }
            }
        });
        
        loadingTipTimer.start();
    }
    
    private void openWebsite() {
        try {
            Desktop.getDesktop().browse(URI.create("https://gielinorgains.com"));
            log.debug("Opened GielinorGains.com website");
        } catch (Exception e) {
            log.error("Failed to open website", e);
        }
    }
    
    /**
     * Cleanup resources when the panel is destroyed
     */
    public void shutdown() {
        log.debug("Shutting down CardGridPanel");
        
        // Stop any running loading tip timer
        if (loadingTipTimer != null && loadingTipTimer.isRunning()) {
            loadingTipTimer.stop();
            loadingTipTimer = null;
        }
        
        // Clear references
        loadingTipLabel = null;
        cardPanels.clear();
        items.clear();
        
        log.debug("CardGridPanel shutdown completed");
    }
    
    
    // Scrollable interface implementation for better scrolling behavior
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }
    
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.VERTICAL) {
            return CARD_HEIGHT + CARD_SPACING; // Scroll one card at a time
        }
        return 10;
    }
    
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.VERTICAL) {
            return visibleRect.height - (CARD_HEIGHT + CARD_SPACING); // Scroll almost a full screen
        }
        return visibleRect.width;
    }
    
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true; // Panel should match viewport width
    }
    
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false; // Panel height should be independent of viewport height
    }
    
}
