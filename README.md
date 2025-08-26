# Gielinor Gains RuneLite Plugin

A RuneLite plugin that integrates Gielinor Gains trading data directly into the OSRS client.

![Plugin Version](https://img.shields.io/badge/version-1.0.0-blue)
![Java](https://img.shields.io/badge/java-11-orange)
![RuneLite](https://img.shields.io/badge/runelite-compatible-green)

## Features
- Real-time trading opportunities from Gielinor Gains API
- Card-based grid interface with individual item displays
- Score-based ranking (0-5 scale) with color-coded indicators
- Buy/sell prices, profit margins, and recommended quantities
- Click item names to open OSRS Wiki pages
- Configurable refresh intervals, score thresholds, and icon display
- Item icon caching with loading animations
- 90-second cache synchronization with web app

## Installation

### Quick Install
1. Download `gielinor-gains-runelite-1.0.0-all.jar` from releases
2. Open RuneLite and enable Developer mode in Settings > Configuration > RuneLite
3. Go to Plugin Hub and click "Load Plugin"
4. Select the downloaded JAR file
5. Plugin appears in sidebar with blue "GG" icon

### Build from Source
```bash
git clone <repository-url>
cd gielinor-gains-runelite
./gradlew clean shadowJar
# Load build/libs/gielinor-gains-runelite-1.0.0-all.jar in RuneLite
```

## Usage

1. Click the "GG" icon in RuneLite sidebar to open the panel
2. View trading opportunities in card format
3. Click item names to open OSRS Wiki pages
4. Use "Refresh" button or wait for automatic updates (90s)
5. Configure settings through RuneLite Configuration panel

## Configuration

| Setting | Default | Description |
|---------|---------|-------------|
| Refresh Interval | 90 seconds | Data fetch frequency |
| Minimum Score | 0.0 | Filter items below this score |
| Show Item Icons | Enabled | Display item icons in cards |

## Project Structure

```
src/main/java/com/gielinorgains/
├── GielinorGainsPlugin.java      # Main plugin class
├── GielinorGainsConfig.java      # Configuration interface
├── api/
│   └── GainsApiClient.java       # HTTP client with caching
├── model/
│   ├── GainsItem.java           # Item data model
│   └── ApiResponse.java         # API response wrapper
├── ui/
│   ├── GainsPanel.java          # Main UI panel
│   ├── CardGridPanel.java       # Card grid container
│   ├── ItemCardPanel.java       # Individual item cards
│   ├── IconCache.java           # Icon loading and caching
│   └── LogoLoader.java          # Logo loading utilities
└── util/
    └── ScoreFormatter.java      # Score display utilities
```

## API Integration

**Endpoint**: `https://gielinorgains.com/api/items`

Data includes:
- Item names and OSRS Wiki icons
- Quality scores (0-5 scale using technical indicators)
- Buy/sell price recommendations
- Profit calculations and ROI analysis
- Trading quantities based on volume analysis
- Market volume and liquidity data

## Development

### Prerequisites
- Java 11+
- Gradle 8.0+
- RuneLite development environment

### Tech Stack
- Java 11 (RuneLite compatibility)
- Gradle with shadow plugin
- OkHttp 4.12.0 for HTTP requests
- Gson 2.10.1 for JSON parsing
- Lombok for code generation
- Swing UI framework

### Commands
```bash
./gradlew clean build          # Standard build
./gradlew shadowJar           # Build with dependencies
./gradlew test                # Run tests
./gradlew --continuous build  # Development mode
```

## Troubleshooting

### Plugin Not Appearing
- Enable Developer mode in RuneLite settings
- Load the `-all.jar` file (includes dependencies)
- Restart RuneLite after installation
- Check RuneLite console for errors

### Network Issues
- Check internet connection
- Verify https://gielinorgains.com accessibility
- Check firewall/antivirus blocking RuneLite network access

### Data Not Loading
- Click "Refresh" button manually
- Verify Gielinor Gains website is online
- Check minimum score filter setting
- Look for error messages in status bar

### Performance Issues
- Disable item icons if experiencing lag
- Increase refresh interval to reduce API calls

## Known Issues

- Some item icons may load slowly on first viewing (cached afterwards)
- API rate limiting may occur with frequent manual refreshes
- Requires internet connection for data

## License

This project is licensed under the BSD 2-Clause License. See the [LICENSE](LICENSE) file for details.

Copyright (c) 2024, Cameron Spears. All rights reserved.