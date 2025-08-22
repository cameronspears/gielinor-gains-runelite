# Gielinor Gains RuneLite Plugin

A companion RuneLite plugin that brings the powerful Gielinor Gains trading analysis directly into your OSRS client. View real-time profitable trading opportunities without switching between applications.

![Plugin Version](https://img.shields.io/badge/version-1.0.0-blue)
![Java](https://img.shields.io/badge/java-11-orange)
![RuneLite](https://img.shields.io/badge/runelite-compatible-green)

## 🚀 Features

- **📊 Live Trading Data**: Real-time profitable trading opportunities from Gielinor Gains
- **⭐ Score-Based Ranking**: Items sorted by quality score (0-5 scale) with intuitive color coding
- **💰 Profit Analysis**: Instant buy/sell prices, profit margins, and recommended quantities
- **🎯 Interactive Interface**: Double-click items to open their OSRS Wiki pages
- **⚙️ Highly Configurable**: Customize refresh intervals, item limits, and score thresholds
- **🖼️ Smart Icon System**: Displays item icons with intelligent caching for smooth performance
- **🔄 Auto-Refresh**: Synchronized with web app's 90-second cache for consistent data

## 📋 Table Columns

| Column | Description |
|--------|-------------|
| **Item** | Item name with icon (toggleable) |
| **Score** | Quality score (0-5) with color coding |
| **Buy/Sell** | Price range for trading |
| **Profit** | Expected profit per item |
| **Qty** | Recommended quantity to trade |

## 📦 Installation

### Method 1: Quick Install (Recommended)
1. Download the latest `gielinor-gains-runelite-1.0.0-all.jar` from releases
2. Open RuneLite
3. Navigate to Settings (⚙️) → Configuration → RuneLite → Enable "Developer mode"
4. Go to the Plugin Hub and click "Load Plugin"
5. Select the downloaded JAR file
6. The plugin will appear in your sidebar with a blue "GG" icon

### Method 2: Build from Source
```bash
# Clone the repository
git clone <repository-url>
cd gielinor-gains-runelite

# Build the plugin
./gradlew clean shadowJar

# The plugin JAR will be in build/libs/
# Load gielinor-gains-runelite-1.0.0-all.jar in RuneLite
```

## 🎮 Usage

1. **Launch**: After installation, look for the blue "GG" icon in your RuneLite sidebar
2. **View Data**: Click the icon to open the trading opportunities panel
3. **Interact**: Double-click any item name to open its OSRS Wiki page
4. **Refresh**: Use the "Refresh" button or wait for automatic updates (90s interval)
5. **Configure**: Right-click the plugin in settings to customize behavior

## ⚙️ Configuration Options

Access these settings through RuneLite's Configuration panel:

| Setting | Default | Description |
|---------|---------|-------------|
| **Refresh Interval** | 90 seconds | How often to fetch new data from API |
| **Items to Display** | 50 | Maximum number of items to show in table |
| **Minimum Score** | 0.0 | Only show items with score >= this value |
| **Show Item Icons** | Enabled | Display item icons in the table |

## 🏗️ Project Structure

```
src/main/java/com/gielinorgains/
├── GielinorGainsPlugin.java      # Main plugin class & RuneLite integration
├── GielinorGainsConfig.java      # Configuration interface
├── api/
│   └── GainsApiClient.java       # HTTP client with caching & error handling
├── model/
│   ├── GainsItem.java           # Item data model matching API schema
│   └── ApiResponse.java         # API response wrapper with success/error states
├── ui/
│   ├── GainsPanel.java          # Main UI panel with sortable table
│   └── IconCache.java           # Async icon loading with memory management
└── util/
    └── ScoreFormatter.java      # Score display utilities & color coding
```

## 🌐 Data Source & API

**Endpoint**: `https://gielinorgains.com/api/items`

The plugin fetches comprehensive trading data including:
- Item names and OSRS Wiki icons
- AI-generated quality scores (0-5 scale using technical indicators)
- Buy/sell price recommendations with tax considerations
- Profit calculations and ROI analysis
- Recommended trading quantities based on volume analysis
- Market volume and liquidity data

**Data Flow**: OSRS Wiki API → Gielinor Gains Analysis → RuneLite Plugin

## 🛠️ Development

### Prerequisites
- Java 11 or higher
- Gradle 8.0+
- RuneLite development environment

### Tech Stack
- **Language**: Java 11 (compatible with RuneLite)
- **Build System**: Gradle with shadow plugin
- **HTTP Client**: OkHttp 4.12.0 for API requests
- **JSON Parsing**: Gson 2.10.1 for response handling
- **Code Generation**: Lombok for boilerplate reduction
- **UI Framework**: Swing (RuneLite standard)

### Building
```bash
# Clean build
./gradlew clean build

# Build with dependencies (for plugin distribution)
./gradlew shadowJar

# Run tests
./gradlew test

# Continuous development
./gradlew --continuous build
```

### Testing
```bash
# Run all tests
./gradlew test

# Run specific test suites
./gradlew test --tests "com.gielinorgains.ApiIntegrationTest"
./gradlew test --tests "com.gielinorgains.JsonParsingTest"
```

## 🐛 Troubleshooting

### Plugin Not Appearing in Sidebar
- ✅ Ensure "Developer mode" is enabled in RuneLite settings
- ✅ Check that you loaded the `-all.jar` file (includes dependencies)
- ✅ Restart RuneLite after installing the plugin
- ✅ Look for errors in RuneLite's console/logs

### Network/Timeout Errors
- ✅ Check your internet connection
- ✅ Verify https://gielinorgains.com is accessible
- ✅ Increase timeout in configuration if on slow connection
- ✅ Check firewall/antivirus blocking RuneLite's network access

### Data Not Loading
- ✅ Click the "Refresh" button manually
- ✅ Check if Gielinor Gains website is online
- ✅ Verify minimum score filter isn't too restrictive
- ✅ Look for error messages in the status bar

### Performance Issues
- ✅ Disable item icons if experiencing lag
- ✅ Reduce "Items to Display" count
- ✅ Increase refresh interval to reduce API calls

## 🔄 Version History

### v1.0.0 (Current)
- ✅ Initial release with core functionality
- ✅ API integration with proper error handling
- ✅ Configurable table with color-coded scores
- ✅ Icon caching system for optimal performance
- ✅ Fixed JSON parsing for decimal volume values
- ✅ Improved network timeouts and SSL handling

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Follow Java 11 compatibility requirements
4. Add tests for new functionality
5. Ensure `./gradlew build` passes
6. Submit a pull request

## 📝 Known Issues

- Some item icons may load slowly on first viewing (cached afterwards)
- API rate limiting may occur with very frequent manual refreshes
- Plugin requires internet connection for real-time data

## 🔮 Future Enhancements

- 📊 Integration with Gielinor Gains Planner feature
- 📈 Price history charts and trend analysis
- 🔔 Profit alerts and notifications
- 📱 Mobile-friendly companion features
- 🎨 Customizable themes and layouts

## 📄 License

This plugin is for educational and personal use only. It connects to the free Gielinor Gains service and respects all rate limits and terms of service.

## 🙏 Acknowledgments

- **Gielinor Gains Team** for the excellent trading analysis platform
- **RuneLite Developers** for the robust plugin framework
- **OSRS Wiki** for providing comprehensive item data
- **OSRS Community** for feedback and support

---

*Happy trading! 💰*