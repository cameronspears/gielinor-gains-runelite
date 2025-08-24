package com.gielinorgains.ui;

import com.gielinorgains.GielinorGainsConfig;
import com.gielinorgains.model.GainsItem;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class CardGridPanel extends JPanel implements Scrollable {
    private static final int CARD_SPACING = 6;
    private static final int CARD_WIDTH = 190;
    private static final int CARD_HEIGHT = 180;
    
    private final IconCache iconCache;
    private final GielinorGainsConfig config;
    private List<GainsItem> items = new ArrayList<>();
    private List<ItemCardPanel> cardPanels = new ArrayList<>();
    private String sortBy = "score";
    private boolean ascending = false;
    private JComponent headerComponent;
    private JComponent statusComponent;
    private boolean loading = true;
    
    public CardGridPanel(IconCache iconCache, GielinorGainsConfig config) {
        this.iconCache = iconCache;
        this.config = config;
        
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
    }
    
    /**
     * Sets the sort criteria and updates the display.
     */
    public void setSorting(String sortBy, boolean ascending) {
        this.sortBy = sortBy;
        this.ascending = ascending;
        sortItems();
        createCardPanels(); // Re-create panels after sorting
        updateLayout();
    }

    /**
     * Sets loading state to control whether to show a loading indicator
     * instead of the empty-state message while data is being fetched.
     */
    public void setLoading(boolean loading) {
        this.loading = loading;
        updateLayout();
    }
    
    /**
     * Gets the current sort field.
     */
    public String getSortBy() {
        return sortBy;
    }
    
    /**
     * Gets the current sort direction.
     */
    public boolean isAscending() {
        return ascending;
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
    
    private void createCardPanels() {
        // Clear existing panels
        cardPanels.clear();
        
        // Create new card panels
        for (GainsItem item : items) {
            ItemCardPanel cardPanel = new ItemCardPanel(item, iconCache);
            cardPanels.add(cardPanel);
        }
        
        log.debug("Created {} card panels", cardPanels.size());
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
            // Show empty state
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
            headerComponent.setAlignmentX(Component.CENTER_ALIGNMENT);
            // Ensure header stretches to viewport width
            headerComponent.setMaximumSize(new Dimension(Integer.MAX_VALUE, headerComponent.getPreferredSize().height));
            add(headerComponent);
            add(Box.createRigidArea(new Dimension(0, CARD_SPACING)));
        }
        
        // Add cards with spacing between them
        for (int i = 0; i < cardPanels.size(); i++) {
            ItemCardPanel card = cardPanels.get(i);
            card.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(card);
            
            log.debug("Added card {} for item: {}", i, card.getItem().getName());
            
            // Add spacing between cards (but not after the last one)
            if (i < cardPanels.size() - 1) {
                add(Box.createRigidArea(new Dimension(0, CARD_SPACING)));
            }
        }
        
        // Optional status/footer
        add(Box.createRigidArea(new Dimension(0, CARD_SPACING)));
        if (statusComponent != null) {
            statusComponent.setAlignmentX(Component.CENTER_ALIGNMENT);
            statusComponent.setMaximumSize(new Dimension(Integer.MAX_VALUE, statusComponent.getPreferredSize().height));
            add(statusComponent);
            add(Box.createRigidArea(new Dimension(0, CARD_SPACING)));
        }

        // Add spacing at bottom
        add(Box.createRigidArea(new Dimension(0, CARD_SPACING)));
        
        revalidate();
        repaint();
        
        log.info("Updated layout with {} cards in single column", cardPanels.size());
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
        add(Box.createRigidArea(new Dimension(0, 20)));

        // Gielinor Gains logo
        ImageIcon logoIcon = LogoLoader.getLogoIcon();
        if (logoIcon != null) {
            JLabel logoLabel = new JLabel(logoIcon);
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(logoLabel);
            add(Box.createRigidArea(new Dimension(0, 16)));
        }

        // Loading indicator centered
        JPanel loadingPanel = new JPanel();
        loadingPanel.setOpaque(false);
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.Y_AXIS));

        JLabel loadingLabel = new JLabel("Loading trading opportunities...");
        loadingLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
        loadingLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setAlignmentX(Component.CENTER_ALIGNMENT);
        bar.setMaximumSize(new Dimension(200, 12));

        loadingPanel.add(loadingLabel);
        loadingPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        loadingPanel.add(bar);

        loadingPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(loadingPanel);

        // Flexible space
        add(Box.createVerticalGlue());
    }
    
    /**
     * Returns the number of items currently displayed.
     */
    public int getItemCount() {
        return items.size();
    }
    
    /**
     * Clears all items and cards.
     */
    public void clearItems() {
        items.clear();
        cardPanels.clear();
        removeAll();
        revalidate();
        repaint();
    }
    
    /**
     * Gets a copy of the current items list.
     */
    public List<GainsItem> getItems() {
        return new ArrayList<>(items);
    }
    
    /**
     * Refreshes the display by recreating card panels.
     * Useful when configuration changes affect card rendering.
     */
    public void refreshDisplay() {
        if (!items.isEmpty()) {
            createCardPanels();
            updateLayout();
        }
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
