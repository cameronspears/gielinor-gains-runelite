package com.gielinorgains;

import com.gielinorgains.api.GainsApiClient;
import com.gielinorgains.ui.GainsPanel;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
	name = "Gielinor Gains",
	description = "Trade analysis and profit opportunities from Gielinor Gains",
	tags = {"trading", "flipping", "ge", "profit", "gielinor", "gains"}
)
public class GielinorGainsPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private GielinorGainsConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private GainsApiClient apiClient;

	private GainsPanel panel;
	private NavigationButton navButton;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Gielinor Gains plugin started!");
		
		// Create the panel
		log.info("Creating Gielinor Gains panel...");
		panel = new GainsPanel(apiClient, config);
		log.info("Gielinor Gains panel created successfully");
		
		// Create navigation button
		BufferedImage icon = null;
		try {
			icon = ImageUtil.loadImageResource(getClass(), "/icon.png");
			log.debug("Loaded plugin icon from resource");
		} catch (Exception e) {
			log.info("Could not load plugin icon resource, creating default icon");
			icon = createDefaultIcon();
		}
		
		if (icon == null) {
			log.warn("Failed to create icon, using null icon");
		}
		
		navButton = NavigationButton.builder()
			.tooltip("Gielinor Gains")
			.icon(icon)
			.priority(5)
			.panel(panel)
			.build();
		
		log.info("Adding Gielinor Gains navigation button to toolbar");
		// Add to toolbar
		clientToolbar.addNavigation(navButton);
		log.info("Gielinor Gains navigation button added successfully");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Gielinor Gains plugin stopped!");
		
		// Remove from toolbar
		if (navButton != null) {
			clientToolbar.removeNavigation(navButton);
		}
		
		// Cleanup
		if (panel != null) {
			panel.shutdown();
			panel = null;
		}
	}

	@Provides
    GielinorGainsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GielinorGainsConfig.class);
	}
	
	private BufferedImage createDefaultIcon() {
		try {
			// Create a 16x16 icon with "GG" text
			BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			java.awt.Graphics2D g2d = icon.createGraphics();
			
			// Enable antialiasing
			g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
								 java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
			
			// Set background to transparent
			g2d.setComposite(java.awt.AlphaComposite.Clear);
			g2d.fillRect(0, 0, 16, 16);
			g2d.setComposite(java.awt.AlphaComposite.Src);
			
			// Draw background circle
			g2d.setColor(new java.awt.Color(61, 125, 223)); // Blue background
			g2d.fillOval(1, 1, 14, 14);
			
			// Draw border
			g2d.setColor(new java.awt.Color(255, 255, 255)); // White border
			g2d.drawOval(1, 1, 14, 14);
			
			// Draw "GG" text
			g2d.setColor(java.awt.Color.WHITE);
			g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 8));
			java.awt.FontMetrics fm = g2d.getFontMetrics();
			String text = "GG";
			int textWidth = fm.stringWidth(text);
			int textHeight = fm.getAscent();
			int x = (16 - textWidth) / 2;
			int y = (16 + textHeight) / 2 - 1;
			g2d.drawString(text, x, y);
			
			g2d.dispose();
			log.info("Created default plugin icon successfully");
			return icon;
		} catch (Exception e) {
			log.error("Failed to create default icon", e);
			return null;
		}
	}
}
