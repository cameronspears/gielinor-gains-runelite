package com.gielinorgains.ui;

import com.gielinorgains.GielinorGainsConfig;
import com.gielinorgains.api.GainsApiClient;
import com.gielinorgains.model.GainsItem;
import com.gielinorgains.model.ApiResponse;
import com.gielinorgains.util.ScoreFormatter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class GainsPanel extends PluginPanel {
    private static final String[] COLUMN_NAMES = {"Item", "Score", "Buy/Sell", "Profit", "Qty"};
    private static final int[] COLUMN_WIDTHS = {140, 60, 90, 70, 50};
    
    private final GainsApiClient apiClient;
    private final GielinorGainsConfig config;
    private final IconCache iconCache;
    
    private JTable table;
    private GainsTableModel tableModel;
    private JButton refreshButton;
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
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JLabel titleLabel = new JLabel("Gielinor Gains");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        
        refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(61, 125, 223));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> refreshData());
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshButton, BorderLayout.EAST);
        
        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(statusLabel.getFont().deriveFont(11f));
        
        loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(true);
        loadingBar.setVisible(false);
        loadingBar.setPreferredSize(new Dimension(0, 15));
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(loadingBar, BorderLayout.SOUTH);
        
        // Table
        tableModel = new GainsTableModel();
        table = new JTable(tableModel);
        setupTable();
        
        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void layoutComponents() {
        // Table is already added in initializeComponents
    }
    
    private void setupTable() {
        table.setBackground(ColorScheme.DARK_GRAY_COLOR);
        table.setForeground(Color.WHITE);
        table.setGridColor(ColorScheme.MEDIUM_GRAY_COLOR);
        table.setSelectionBackground(new Color(61, 125, 223));
        table.setSelectionForeground(Color.WHITE);
        table.setRowHeight(32);
        table.setShowGrid(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Set column widths
        for (int i = 0; i < COLUMN_WIDTHS.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(COLUMN_WIDTHS[i]);
            table.getColumnModel().getColumn(i).setMinWidth(COLUMN_WIDTHS[i]);
        }
        
        // Custom cell renderers
        table.getColumnModel().getColumn(0).setCellRenderer(new ItemCellRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new ScoreCellRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new PriceCellRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new ProfitCellRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new CenteredCellRenderer());
        
        // Add click listener for opening wiki pages
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        GainsItem item = tableModel.getItemAt(row);
                        if (item != null) {
                            openWikiPage(item.getName());
                        }
                    }
                }
            }
        });
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
                tableModel.setItems(response.getData());
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
    
    private void openWikiPage(String itemName) {
        try {
            String encodedName = itemName.replace(" ", "_");
            String url = "https://oldschool.runescape.wiki/w/" + encodedName;
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            log.error("Failed to open wiki page for item: {}", itemName, e);
        }
    }
    
    // Table Model
    private static class GainsTableModel extends AbstractTableModel {
        private List<GainsItem> items = new ArrayList<>();
        
        public void setItems(List<GainsItem> items) {
            this.items = new ArrayList<>(items);
            fireTableDataChanged();
        }
        
        public GainsItem getItemAt(int row) {
            if (row >= 0 && row < items.size()) {
                return items.get(row);
            }
            return null;
        }
        
        @Override
        public int getRowCount() {
            return items.size();
        }
        
        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= items.size()) return null;
            
            GainsItem item = items.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return item; // Item (name + icon)
                case 1:
                    return item.getScore(); // Score
                case 2:
                    return item.getPriceRange(); // Buy/Sell
                case 3:
                    return item.getProfit(); // Profit
                case 4:
                    return item.getQuantity(); // Quantity
                default:
                    return null;
            }
        }
    }
    
    // Cell Renderers
    private class ItemCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            
            if (value instanceof GainsItem) {
                GainsItem item = (GainsItem) value;
                JPanel panel = new JPanel(new BorderLayout());
                panel.setOpaque(true);
                panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                
                if (config.showIcons()) {
                    ImageIcon icon = iconCache.getIcon(item.getIcon());
                    if (icon != null) {
                        JLabel iconLabel = new JLabel(icon);
                        iconLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                        panel.add(iconLabel, BorderLayout.WEST);
                    }
                }
                
                JLabel nameLabel = new JLabel(item.getName());
                nameLabel.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                nameLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                panel.add(nameLabel, BorderLayout.CENTER);
                
                return panel;
            }
            
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
    
    private static class ScoreCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Double) {
                Double score = (Double) value;
                setText(ScoreFormatter.getScoreText(score));
                if (!isSelected) {
                    setForeground(ScoreFormatter.getScoreColor(score));
                }
                setHorizontalAlignment(SwingConstants.CENTER);
            }
            
            return this;
        }
    }
    
    private static class PriceCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            
            return this;
        }
    }
    
    private static class ProfitCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Integer) {
                Integer profit = (Integer) value;
                String formattedProfit = formatPrice(profit);
                setText(formattedProfit);
                
                if (!isSelected) {
                    setForeground(profit > 0 ? new Color(34, 197, 94) : new Color(239, 68, 68));
                }
            }
            
            setHorizontalAlignment(SwingConstants.RIGHT);
            return this;
        }
        
        private String formatPrice(int price) {
            if (price >= 1000000) {
                return String.format("%.1fM", price / 1000000.0);
            } else if (price >= 1000) {
                return String.format("%.1fK", price / 1000.0);
            }
            return String.valueOf(price);
        }
    }
    
    private static class CenteredCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            
            return this;
        }
    }
}