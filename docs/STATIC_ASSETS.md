# Static Assets Architecture

## Overview

All static assets (HTML, CSS, JavaScript bundle) are automatically bundled into the `kotlio-core` JVM library. This means:

✅ **Zero configuration** - Users just add `kotlio-core` dependency  
✅ **No manual resource management** - Everything is packaged automatically  
✅ **No custom Gradle tasks** - Standard Kotlin multiplatform build  
✅ **Classpath resources** - Assets served directly from the JAR  

## Architecture

### Build Process

```
1. Kotlin/JS → Webpack → kotlio-core.js bundle
                    ↓
2. Copy to JVM resources (kotlio/static/)
                    ↓
3. Package into kotlio-core JAR
                    ↓
4. User apps automatically include everything
```

### Directory Structure

```
kotlio-core/
├── src/
│   ├── commonMain/kotlin/          # Shared Kotlin code
│   ├── jvmMain/
│   │   ├── kotlin/                 # JVM server code
│   │   └── resources/              # JVM source resources (empty for static assets)
│   └── jsMain/
│       ├── kotlin/                 # JS client code
│       └── resources/
│           └── index.html          # Source HTML template
├── build/
│   └── processedResources/jvm/main/
│       └── kotlio/static/          # Generated at build time
│           ├── index.html          # Copied from jsMain
│           ├── kotlio-core.js      # Webpack bundle
│           └── kotlio-core.js.map  # Source map
└── build.gradle.kts                # Automatic bundling configuration
```

**Key Points:**
- `index.html` source lives in `jsMain/resources/`
- Generated files go to `build/processedResources/` (never in source tree)
- Build task automatically copies everything during resource processing
- JAR packages the processed resources from build directory

## Gradle Build Configuration

### kotlio-core/build.gradle.kts

```kotlin
// Extend jvmProcessResources to include JS bundle and index.html
tasks.named<ProcessResources>("jvmProcessResources") {
    dependsOn("jsBrowserProductionWebpack")
    
    val webpackDir = layout.buildDirectory.dir("kotlin-webpack/js/productionExecutable")
    val jsResourcesDir = layout.projectDirectory.dir("src/jsMain/resources")
    
    // Copy webpack output (.js, .js.map)
    from(webpackDir) {
        include("*.js", "*.js.map")
        into("kotlio/static")
    }
    
    // Copy index.html from JS resources
    from(jsResourcesDir) {
        include("index.html")
        into("kotlio/static")
    }
}
```

**What it does:**
1. Builds the JS webpack bundle
2. During `jvmProcessResources`, copies `.js` and `.js.map` from webpack output
3. Also copies `index.html` from `jsMain/resources`
4. All files go to `build/processedResources/jvm/main/kotlio/static/`
5. JAR packages the processed resources

**Key Benefits:**
- ✅ No files generated in source tree
- ✅ Standard Gradle resource processing
- ✅ Proper incremental build support
- ✅ Clean `git status`

This runs automatically during `./gradlew build` - no manual steps!

## Ktor Server Configuration

### KotlioServer.kt

```kotlin
routing {
    // Serve bundled static resources from classpath
    staticResources("/", "kotlio/static")
    
    get("/") {
        // index.html is automatically served
    }
    
    get("/schema") { /* API endpoint */ }
    post("/action") { /* API endpoint */ }
}
```

The Ktor server automatically finds and serves resources from the JAR's classpath.

## User Experience

### Before (Complex)

```kotlin
// User had to:
// 1. Create custom Gradle tasks
// 2. Manage static resource directories
// 3. Copy files during build
// 4. Configure Ktor to serve from local filesystem

example/
├── build.gradle.kts (100+ lines of custom tasks)
├── src/main/resources/static/
│   ├── index.html
│   ├── kotlio-core.js (manually copied)
│   └── kotlio-core.js.map
```

### After (Simple)

```kotlin
// User just writes:
fun main() {
    runKotlioApp(port = 8080) {
        page("Hello") {
            // Your app code
        }
    }.start(wait = false)
}

// That's it! Everything is bundled in kotlio-core.
```

```
example/
├── build.gradle.kts (25 lines - standard config only)
└── src/main/kotlin/example/SimpleGreeterApp.kt
```

## Benefits

### For Library Developers

- ✅ Single source of truth for UI assets
- ✅ Assets versioned with library code
- ✅ Automatic bundling during CI/CD
- ✅ No risk of version mismatches

### For Library Users

- ✅ Zero configuration required
- ✅ No manual file copying
- ✅ No custom Gradle tasks
- ✅ Just `implementation(kotlio-core)` and go!

### For Distribution

- ✅ Single JAR contains everything
- ✅ Works with Maven Central
- ✅ No external dependencies
- ✅ Portable across projects

## Testing

### Verify Bundled Resources

```bash
# Build kotlio-core
./gradlew kotlio-core:build

# Check bundled resources
ls -lh kotlio-core/src/jvmMain/resources/kotlio/static/
# Should see: index.html, kotlio-core.js, kotlio-core.js.map

# Run example
./gradlew example:run

# Open http://localhost:8080 - everything just works!
```

## Future Enhancements

### Potential Improvements

1. **Production vs Development Mode**
   - Dev: Serve from webpack dev server (hot reload)
   - Prod: Serve from bundled resources

2. **Custom Themes**
   - Allow users to override default HTML/CSS
   - Provide theme extension points

3. **Asset Optimization**
   - Minification
   - Gzip compression
   - Cache headers

4. **CDN Support**
   - Optional external asset loading
   - Fallback to bundled resources

## Migration Guide

If you have an existing Kotlio app with custom static resources:

### Step 1: Remove Custom Tasks

Delete from your `build.gradle.kts`:
- `prepareStaticResources` task
- `cleanStaticResources` task
- `copyJsBundleToResources` task
- Any `dependsOn` references to these

### Step 2: Remove Static Resources Directory

```bash
rm -rf src/main/resources/static/
```

### Step 3: Update to Latest kotlio-core

```kotlin
dependencies {
    implementation("com.kotlio:kotlio-core:1.0.0") // Latest version
}
```

### Step 4: Run!

```bash
./gradlew run
```

Everything now works automatically! 🎉

## Technical Details

### Resource Loading

Ktor's `staticResources` uses `ClassLoader.getResource()` to load bundled assets:

```kotlin
staticResources("/", "kotlio/static")
// Looks for: classpath:kotlio/static/index.html
// Found in: kotlio-core JAR
```

### JAR Structure

```
kotlio-core.jar
├── META-INF/
├── kotlio/
│   ├── schema/ (compiled Kotlin classes)
│   ├── server/ (compiled Kotlin classes)
│   ├── client/ (compiled Kotlin classes)
│   └── static/ (bundled web assets)
│       ├── index.html
│       ├── kotlio-core.js
│       └── kotlio-core.js.map
```

### Build Order

1. `compileKotlinJs` → JS/IR output
2. `jsBrowserProductionWebpack` → Webpack bundle
3. `copyJsBundleToResources` → Copy to jvmMain/resources
4. `jvmProcessResources` → Include in JAR
5. `jvmJar` → Final JAR with everything

## Summary

The new architecture eliminates all build system complexity for users while maintaining full functionality. Static assets are seamlessly bundled into the library, making Kotlio truly zero-config for end users.

Just add the dependency, write your app, and run! 🚀
