# CLAUDE.md

This file provides AI-friendly guidance for working with the Gielinor Gains RuneLite Plugin project. It contains essential information for Claude Code and other AI assistants to understand and maintain this codebase effectively.

## Project Overview

This is a RuneLite plugin that integrates Gielinor Gains trading data directly into the OSRS client. It's a companion to the main Gielinor Gains web application (https://gielinorgains.com).

### Key Architecture Principles
- **Read-only data access**: Plugin only consumes data, never modifies it
- **Cache-first approach**: 90-second TTL matching web app for consistency
- **Asynchronous operations**: All network calls and UI updates are non-blocking
- **Error resilience**: Graceful handling of network failures and malformed data
- **Memory efficiency**: Bounded caches with automatic cleanup

## Essential Commands

### Development
- `./gradlew build` - Standard build (no dependencies bundled)
- `./gradlew shadowJar` - Build with all dependencies (for plugin distribution)
- `./gradlew clean build` - Clean build from scratch
- `./gradlew test` - Run all tests
- `./gradlew test --tests "com.gielinorgains.JsonParsingTest"` - Run specific test

### Debugging
- `./gradlew build --debug` - Debug build issues
- `./gradlew test --info` - Verbose test output
- `./gradlew dependencies` - Show dependency tree

## Plugin Hub Submission & Updates

This plugin is distributed through the RuneLite Plugin Hub. Understanding the submission and update process is critical for maintaining the plugin.

### Repository Structure
- **Main Plugin Repo**: `gielinor-gains-runelite` - Contains the actual plugin code
- **Plugin Hub Fork**: `plugin-hub` - Fork of `runelite/plugin-hub` for submissions
- **Plugin Definition**: `plugin-hub/plugins/gielinor-gains` - Contains repository URL and commit hash

### Plugin Hub Requirements & Compliance

**Licensing**: Must use BSD 2-Clause License (✅ already in place)

**Code Standards**:
- No malicious code or Jagex rule-breaking features
- Java 11 compatibility required (avoid Java 14+ features)
- Clean builds must pass CI checks
- Dependencies require cryptographic verification

**Plugin Metadata** (in `runelite-plugin.properties`):
```
displayName=Gielinor Gains
author=cameronspears
description=Live trading opportunities from GielinorGains.com
tags=trading,flipping,profit,osrs,gp
plugins=com.gielinorgains.GielinorGainsPlugin
```

**Icon**: Maximum 48x72px PNG at repository root (✅ already optimized)

### Update Process Workflow

**When making changes to the plugin:**

1. **Develop & Test** in main plugin repository:
   ```bash
   ./gradlew test -ea           # Run tests with assertions
   ./gradlew shadowJar          # Build for distribution  
   ./gradlew clean build        # Verify clean build passes
   ```

2. **Commit & Push** changes to main repo:
   ```bash
   git add .
   git commit -m "description of changes"
   git push origin master
   ```

3. **Get Full Commit Hash**:
   ```bash
   git rev-parse HEAD    # Returns 40-character hash
   ```

4. **Update Plugin Hub Definition**:
   - Navigate to plugin-hub repository: `/private/tmp/plugin-hub`
   - Edit `plugins/gielinor-gains` file:
     ```
     repository=https://github.com/cameronspears/gielinor-gains-runelite.git
     commit=[40-character-hash-from-step-3]
     ```

5. **Commit & Push Plugin Hub Update**:
   ```bash
   cd /private/tmp/plugin-hub
   git add plugins/gielinor-gains
   git commit -m "update gielinor-gains to [short-hash]"
   git push origin add-gielinor-gains-plugin
   ```

6. **Monitor PR**: Check CI status and Plugin Hub validation

### Testing Commands for Plugin Hub
- `./gradlew test -ea` - Run with assertions enabled (required for plugin tests)
- `./gradlew shadowJar` - Build JAR with all dependencies for distribution
- `./gradlew --write-verification-metadata sha256` - Update dependency verification (if adding deps)

### CI/Build Validation
- ✅ Build must pass: `.github/workflows/build.yml`
- ✅ Plugin Hub checks must pass (no "Changes are needed")  
- ✅ No build errors or dependency issues
- ✅ Java 11 compatibility maintained

### Common Issues & Solutions
- **Build failures**: Check for Java 11 compatibility issues
- **Dependency verification**: New dependencies need cryptographic verification
- **Plugin Hub checks**: Ensure no rule-breaking or malicious code patterns
- **Client version outdated**: Set `runeLiteVersion = 'latest.release'` in build.gradle

**IMPORTANT**: Always update the commit hash in plugin-hub after ANY changes to the main repository. The Plugin Hub builds from the exact commit specified, not the latest commit.

## Project Structure Deep Dive

### Core Plugin Files
```
GielinorGainsPlugin.java       # Main entry point, RuneLite integration
├─ startUp()                   # Plugin initialization
├─ shutDown()                  # Cleanup on plugin disable
├─ createDefaultIcon()         # Programmatic icon generation
└─ provideConfig()             # Dependency injection for config

GielinorGainsConfig.java       # Configuration interface
├─ refreshInterval()           # API call frequency (default: 90s)
├─ minScore()                 # Score filter threshold (default: 0.0)
└─ showIcons()                # Icon display toggle (default: true)
```

### API Layer
```
GainsApiClient.java            # HTTP client with caching
├─ fetchItems()               # Main API call with async CompletableFuture
├─ isCacheValid()             # TTL-based cache validation
├─ filterResponse()           # Client-side filtering by score/limit
└─ createErrorResponse()      # Standardized error handling

ApiResponse.java              # Response wrapper
├─ data: List<GainsItem>      # Parsed items from API
├─ totalItems: int            # Total count from server
├─ success: boolean           # Success/failure flag
└─ error: String              # Error message if failed
```

### Data Models
```
GainsItem.java                # Core item data model
├─ Basic Info                 # id, name, icon, detailIcon
├─ Pricing                    # latestLowPrice, latestHighPrice, adjustedLowPrice, adjustedHighPrice
├─ Trading                    # quantity, profit, score, adjustedRoi
├─ Volume Data                # dailyVolume, buyVolumeSupport, sellVolumeSupport (Double!)
├─ Technical                  # rsi, roc, sparklineData
└─ Metadata                   # quantityConfidence, limitingFactor, sDataCompleteness
```

### UI Components
```
GainsPanel.java               # Main UI panel
├─ initializeComponents()     # Set up header, table, status bar
├─ setupTable()              # Configure table columns and renderers
├─ refreshData()             # Trigger API refresh
├─ handleApiResponse()       # Process successful API calls
└─ Custom Renderers          # ItemCellRenderer, ScoreCellRenderer, etc.

IconCache.java               # Icon management system
├─ getIcon()                 # Synchronous icon retrieval
├─ loadIconAsync()           # Background icon loading
├─ cleanupExpiredEntries()   # Memory management
└─ Cache Management          # LRU eviction, TTL expiry
```

## Critical Implementation Details

### JSON Parsing Considerations
**IMPORTANT**: The API returns decimal values for volume fields that must be parsed as `Double`, not `Long`:
```java
// ✅ Correct (current implementation)
private Double buyVolumeSupport;
private Double sellVolumeSupport; 
private Double medianHourlyVolume;

// ❌ Wrong (causes NumberFormatException)
private Long buyVolumeSupport;    // API returns 493.6666666666667
```

### Network Configuration
```java
// Current timeout settings (working well)
.connectTimeout(30, TimeUnit.SECONDS)    // Increased from 10s
.readTimeout(45, TimeUnit.SECONDS)       // Increased from 15s  
.writeTimeout(30, TimeUnit.SECONDS)      // Increased from 10s
.retryOnConnectionFailure(true)          // Enable automatic retries
```

### Dependency Management
**Critical**: Use `implementation` not `compileOnly` for runtime dependencies:
```gradle
// ✅ Correct - bundles dependencies with plugin
implementation 'com.squareup.okhttp3:okhttp:4.12.0'
implementation 'com.google.code.gson:gson:2.10.1'

// ❌ Wrong - causes ClassNotFoundException at runtime
compileOnly 'com.squareup.okhttp3:okhttp:4.12.0'
```

### Java 11 Compatibility
This project MUST maintain Java 11 compatibility for RuneLite. Avoid newer language features:
```java
// ❌ Avoid (Java 14+)
return switch (columnIndex) {
    case 0 -> item;
    case 1 -> item.getScore();
};

// ✅ Use instead (Java 11)
switch (columnIndex) {
    case 0: return item;
    case 1: return item.getScore();
    default: return null;
}

// ❌ Avoid (Java 16+)
if (value instanceof GainsItem item) {

// ✅ Use instead (Java 11)
if (value instanceof GainsItem) {
    GainsItem item = (GainsItem) value;
}
```

## API Integration Details

### Endpoint Information
- **Base URL**: `https://gielinorgains.com/api`
- **Main Endpoint**: `/items?limit={limit}`
- **Response Format**: `{"data": [...], "totalItems": number}`
- **Cache TTL**: 90 seconds (server-side), matches web app
- **Rate Limiting**: Gentle - 1 request per 90s per client is acceptable

### Response Schema Mapping
```typescript
// API Response (TypeScript)
interface TableDataItem {
  buyVolumeSupport?: number;      // Can be decimal!
  sellVolumeSupport?: number;     // Can be decimal!
  medianHourlyVolume?: number;    // Can be decimal!
  // ... other fields
}

// Java Model
public class GainsItem {
  private Double buyVolumeSupport;   // Must be Double for decimals
  private Double sellVolumeSupport;  // Must be Double for decimals 
  private Double medianHourlyVolume; // Must be Double for decimals
  // ... other fields
}
```

## Common Development Patterns

### Adding New Configuration Options
1. Add to `GielinorGainsConfig.java`:
```java
@ConfigItem(
    keyName = "newSetting",
    name = "Display Name",
    description = "Description for users"
)
default boolean newSetting() {
    return true;
}
```

2. Use in code:
```java
if (config.newSetting()) {
    // Implementation
}
```

**Note**: The item count is hardcoded to 200 items to match the website behavior and is not configurable.

### Adding New API Fields
1. Update `GainsItem.java` with new field
2. Ensure correct data type (check API response manually)
3. Add to UI if needed (table renderer, formatter)
4. Test with `JsonParsingTest`

### Error Handling Pattern
```java
// API Client Pattern
try {
    // Network operation
} catch (IOException e) {
    log.error("Network error: {}", e.getMessage(), e);
    return createErrorResponse("Network error: " + e.getMessage());
} catch (Exception e) {
    log.error("Unexpected error: {}", e.getMessage(), e);
    return createErrorResponse("Unexpected error: " + e.getMessage());
}

// UI Pattern
SwingUtilities.invokeLater(() -> {
    if (response.isSuccess()) {
        // Update UI with data
    } else {
        // Show error to user
        statusLabel.setText("Error: " + response.getError());
    }
});
```

## Testing Strategy

### Test Files Overview
- `ApiIntegrationTest.java` - Live API connectivity (may fail if offline)
- `JsonParsingTest.java` - Validates JSON parsing with real data
- `SimpleApiTest.java` - Basic connectivity test (non-failing)

### Running Tests Safely
```bash
# Skip integration tests if offline
./gradlew test -x ApiIntegrationTest

# Run only parsing tests
./gradlew test --tests "*JsonParsing*"

# Test with verbose output
./gradlew test --info
```

## Known Issues & Solutions

### Issue: Plugin not appearing in sidebar
**Cause**: Missing icon or null NavigationButton
**Solution**: `createDefaultIcon()` method generates programmatic icon

### Issue: NumberFormatException on volume fields  
**Cause**: API returns decimals, Java expects Long
**Solution**: Use `Double` types for volume support fields

### Issue: Network timeouts
**Cause**: Default timeouts too short for API response
**Solution**: Extended timeouts (30s/45s) with retry enabled

### Issue: JSON parsing errors
**Cause**: Schema mismatch between API and Java model
**Solution**: Check API response manually, update data types

## Future Development Guidelines

### Adding Planner Integration
The next major feature will integrate with Gielinor Gains Planner:
- New API endpoint: `/api/planner` 
- Additional UI panel or modal dialog
- User portfolio management
- Trade execution recommendations

### Performance Considerations
- Icon cache is bounded (500 items max)
- API responses cached for 90s
- Table rendering optimized for up to 200 items (hardcoded limit, typically returns ~110)
- Background threads for all network operations

### Code Style
- Follow existing patterns for consistency
- Use Lombok for boilerplate reduction
- Comprehensive logging with appropriate levels
- Prefer composition over inheritance
- Keep methods focused and testable

## Troubleshooting for AI Assistants

### When Build Fails
1. Check Java version compatibility (must be 11)
2. Verify dependency declarations (`implementation` vs `compileOnly`)
3. Look for unsupported language features
4. Check for missing imports

### When API Calls Fail
1. Test endpoint manually: `curl https://gielinorgains.com/api/items?limit=5`
2. Check network connectivity
3. Verify JSON structure hasn't changed
4. Look for new fields causing parsing errors

### When UI Issues Occur
1. Check SwingUtilities.invokeLater() usage
2. Verify icon loading and caching
3. Test with different table sizes
4. Check color scheme compatibility

## Integration Points

### With Gielinor Gains Web App
- Shared 90-second cache TTL
- Identical scoring algorithm results
- Same item filtering and sorting
- Consistent API schema

### With RuneLite
- Standard plugin lifecycle (startUp/shutDown)
- Configuration through RuneLite settings
- Navigation button in sidebar
- Swing UI components
- Proper dependency injection

### With OSRS Wiki
- Item icon URLs from wiki
- Double-click opens wiki pages
- Respects wiki's rate limits and terms

This documentation should provide comprehensive guidance for maintaining and extending the Gielinor Gains RuneLite Plugin. Always test thoroughly after making changes, especially to API integration or data parsing logic.