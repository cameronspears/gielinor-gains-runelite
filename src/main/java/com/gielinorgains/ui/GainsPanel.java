package com.gielinorgains.ui;

import com.gielinorgains.GielinorGainsConfig;
import com.gielinorgains.api.GainsApiClient;
import com.gielinorgains.model.GainsItem;
import com.gielinorgains.model.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class GainsPanel extends PluginPanel {
    private static final String[] SORT_OPTIONS = {"Score", "Profit", "ROI", "Volume", "Name"};
    
    private final GainsApiClient apiClient;
    private final GielinorGainsConfig config;
    private final IconCache iconCache;
    
    private CardGridPanel cardGridPanel;
    private JButton refreshButton;
    private JComboBox<String> sortComboBox;
    private JButton sortOrderButton;
    private JLabel statusLabel;
    private JProgressBar loadingBar;
    
    @Inject
    public GainsPanel(GainsApiClient apiClient, GielinorGainsConfig config) {
        this.apiClient = apiClient;
        this.config = config;
        this.iconCache = new IconCache();
        
        setLayout(new BorderLayout());
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        
        initializeComponents();
        layoutComponents();
        loadData();
    }
    
    private void initializeComponents() {
        // Header and status panels
        JPanel headerPanel = createHeaderPanel();
        JPanel statusPanel = createStatusPanel();

        // Card grid panel (will host header + cards + status and scroll as one)
        cardGridPanel = new CardGridPanel(iconCache, config);
        cardGridPanel.setHeaderAndStatus(headerPanel, statusPanel);

        JScrollPane scrollPane = new JScrollPane(cardGridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(50);
        scrollPane.getVerticalScrollBar().setBlockIncrement(150);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

        // Ensure scroll works when hovering over header/status components
        java.awt.event.MouseWheelListener wheelForwarder = e -> scrollPane.dispatchEvent(SwingUtilities.convertMouseEvent((Component) e.getSource(), e, scrollPane));
        headerPanel.addMouseWheelListener(wheelForwarder);
        statusPanel.addMouseWheelListener(wheelForwarder);

        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        // Top section: Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        
        ImageIcon logoIcon = LogoLoader.getLogoIcon();
        if (logoIcon != null) {
            JLabel logoLabel = new JLabel(logoIcon);
            logoPanel.add(logoLabel);
        } else {
            JLabel titleLabel = new JLabel("Gielinor Gains");
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
            logoPanel.add(titleLabel);
        }
        
        // Bottom section: Controls stacked vertically
        JPanel controlsPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        controlsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        
        // First row: Sort controls
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        sortPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        
        JLabel sortLabel = new JLabel("Sort:");
        sortLabel.setForeground(Color.WHITE);
        sortLabel.setFont(sortLabel.getFont().deriveFont(10f));
        sortPanel.add(sortLabel);
        
        sortComboBox = new JComboBox<>(SORT_OPTIONS);
        sortComboBox.setSelectedItem("Score");
        sortComboBox.setPreferredSize(new Dimension(70, 20));
        sortComboBox.setFont(sortComboBox.getFont().deriveFont(10f));
        sortComboBox.addActionListener(e -> updateSorting());
        sortPanel.add(sortComboBox);
        
        sortOrderButton = new JButton("↓");
        sortOrderButton.setPreferredSize(new Dimension(20, 20));
        sortOrderButton.setFont(sortOrderButton.getFont().deriveFont(10f));
        sortOrderButton.setFocusPainted(false);
        sortOrderButton.setToolTipText("Sort order: Descending");
        sortOrderButton.addActionListener(e -> toggleSortOrder());
        sortPanel.add(sortOrderButton);
        
        // Second row: Refresh button
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        refreshPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        
        refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(61, 125, 223));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setPreferredSize(new Dimension(80, 20));
        refreshButton.setFont(refreshButton.getFont().deriveFont(10f));
        refreshButton.addActionListener(e -> refreshData());
        refreshPanel.add(refreshButton);
        
        controlsPanel.add(sortPanel);
        controlsPanel.add(refreshPanel);
        
        headerPanel.add(logoPanel, BorderLayout.NORTH);
        headerPanel.add(controlsPanel, BorderLayout.CENTER);
        
        return headerPanel;
    }
    
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(statusLabel.getFont().deriveFont(11f));
        
        loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(true);
        loadingBar.setVisible(false);
        loadingBar.setPreferredSize(new Dimension(0, 15));
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(loadingBar, BorderLayout.SOUTH);
        
        return statusPanel;
    }
    
    private void layoutComponents() {
        // Components are already added in initializeComponents
    }
    
    private void updateSorting() {
        String selectedSort = (String) sortComboBox.getSelectedItem();
        if (selectedSort != null) {
            String sortField = selectedSort.toLowerCase();
            boolean ascending = sortOrderButton.getText().equals("↑");
            cardGridPanel.setSorting(sortField, ascending);
            log.debug("Updated sorting to: {} ({})", sortField, ascending ? "ascending" : "descending");
        }
    }
    
    private void toggleSortOrder() {
        boolean currentAscending = sortOrderButton.getText().equals("↑");
        boolean newAscending = !currentAscending;
        
        sortOrderButton.setText(newAscending ? "↑" : "↓");
        sortOrderButton.setToolTipText("Sort order: " + (newAscending ? "Ascending" : "Descending"));
        
        updateSorting();
    }
    
    private void loadData() {
        refreshData();
    }
    
    private void refreshData() {
        setLoading(true);
        statusLabel.setText("Fetching data...");
        refreshButton.setEnabled(false);
        
        apiClient.fetchItems(config.itemLimit(), config.minScore())
            .thenAccept(this::handleApiResponse)
            .exceptionally(this::handleApiError);
    }
    
    private void handleApiResponse(ApiResponse response) {
        SwingUtilities.invokeLater(() -> {
            setLoading(false);
            refreshButton.setEnabled(true);
            
            if (response.isSuccess() && response.getData() != null) {
                cardGridPanel.setItems(response.getData());
                statusLabel.setText(String.format("Loaded %d items", response.getData().size()));
                log.info("Successfully loaded {} items", response.getData().size());
            } else {
                String error = response.getError() != null ? response.getError() : "Unknown error";
                statusLabel.setText("Error: " + error);
                log.error("Failed to load items: {}", error);
                showErrorDialog("Failed to load data: " + error);
            }
        });
    }
    
    private Void handleApiError(Throwable throwable) {
        SwingUtilities.invokeLater(() -> {
            setLoading(false);
            refreshButton.setEnabled(true);
            String error = throwable.getMessage() != null ? throwable.getMessage() : "Unknown error";
            statusLabel.setText("Error: " + error);
            log.error("API request failed", throwable);
            showErrorDialog("Network error: " + error);
        });
        return null;
    }
    
    private void setLoading(boolean loading) {
        loadingBar.setVisible(loading);
        if (loading) {
            loadingBar.setIndeterminate(true);
        }
    }
    
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
}
