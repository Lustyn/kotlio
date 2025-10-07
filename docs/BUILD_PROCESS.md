# Build Process - Resource Generation

## Overview

The Kotlio build process automatically generates and bundles all static resources from the JS build into the JVM library. This ensures:

✅ **No bloat in source control** - Generated files are gitignored  
✅ **Single source of truth** - HTML lives in `jsMain/resources`  
✅ **Automatic bundling** - JS compilation triggers resource copy  
✅ **Zero configuration** - Works out of the box  

## Source Layout

### What's in Source Control

```
kotlio-core/
├── src/
│   ├── jsMain/
│   │   ├── kotlin/                # JS client code
│   │   └── resources/
│   │       └── index.html        # ✓ SOURCE - In git
│   └── jvmMain/
│       ├── kotlin/                # JVM server code
│       └── resources/             # JVM source resources (no static assets)
└── build/
    └── processedResources/jvm/main/
        └── kotlio/static/         # ✗ GENERATED - In build directory
            ├── index.html         # Copied from jsMain
            ├── kotlio-core.js     # Webpack bundle
            └── kotlio-core.js.map # Source map
```

**Key Points:**
- Static assets are **never** in `src/jvmMain/resources/`
- Generated files only exist in `build/` directory
- No `.gitignore` rules needed for static assets (build/ is already ignored)

## Build Flow

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Kotlin/JS Compilation                                     │
│    src/jsMain/kotlin → build/js/                            │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. Webpack Bundling                                          │
│    build/js → build/kotlin-webpack/js/productionExecutable/ │
│    Creates: kotlio-core.js, kotlio-core.js.map             │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. jvmProcessResources Task                                  │
│    • Processes jvmMain/resources (if any)                   │
│    • Copies webpack output → build/processedResources/      │
│    • Copies index.html → build/processedResources/          │
│    Output: build/processedResources/jvm/main/kotlio/static/ │
└────────────────────┬────────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. JAR Packaging                                             │
│    build/libs/kotlio-core.jar                               │
│    Contains: /kotlio/static/index.html, *.js, *.js.map     │
└─────────────────────────────────────────────────────────────┘
```

## Gradle Task Configuration

### kotlio-core/build.gradle.kts

```kotlin
import org.gradle.language.jvm.tasks.ProcessResources

// Extend jvmProcessResources to include JS webpack bundle and index.html
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
    
    doLast {
        println("✓ Bundled JS webpack output into kotlio-core JAR resources")
        println("✓ Bundled index.html into kotlio-core JAR resources")
    }
}
```

**How it works:**
- Extends the standard `jvmProcessResources` task
- Adds webpack output and index.html as additional input sources
- Files copied to `build/processedResources/jvm/main/kotlio/static/`
- JAR packaging automatically includes processed resources

### Task Dependencies

```
jsBrowserProductionWebpack
         ↓
jvmProcessResources (extends to include webpack output)
         ↓
jvmJar
         ↓
assemble
```

## Build Commands

### Clean Build

```bash
./gradlew clean build
```

**What happens:**
1. Cleans all build directories and generated resources
2. Compiles Kotlin/JS
3. Runs webpack bundler
4. Copies resources to JVM
5. Packages JAR

### Incremental Build

```bash
./gradlew build
```

**What happens:**
- Gradle detects changes via `inputs`/`outputs`
- Only reprocesses changed files
- Faster for development

### Check Generated Resources

```bash
# Source directory should be empty
ls -lh kotlio-core/src/jvmMain/resources/kotlio/static/

# Build directory should have the files
ls -lh kotlio-core/build/processedResources/jvm/main/kotlio/static/
```

Should show (in build directory):
```
index.html
kotlio-core.js
kotlio-core.js.map
```

### Verify JAR Contents

```bash
jar tf kotlio-core/build/libs/kotlio-core-jvm.jar | grep kotlio/static
```

Should show:
```
kotlio/static/
kotlio/static/index.html
kotlio/static/kotlio-core.js
kotlio/static/kotlio-core.js.map
```

## Development Workflow

### Making HTML Changes

1. Edit `kotlio-core/src/jsMain/resources/index.html`
2. Run `./gradlew kotlio-core:build`
3. Resource automatically copied to JVM
4. Changes included in JAR

### Making JS Client Changes

1. Edit `kotlio-core/src/jsMain/kotlin/kotlio/client/KotlioClient.kt`
2. Run `./gradlew kotlio-core:build`
3. Webpack bundles new JS
4. Bundle automatically copied to JVM
5. Changes included in JAR

### Testing Changes

```bash
# Build the library
./gradlew kotlio-core:build

# Run the example app
./gradlew example:run

# Visit http://localhost:8080
```

## Benefits

### 1. Clean Source Tree

**Before:**
- Generated `.js` files committed to git
- Merge conflicts on binary bundles
- Large diffs for minified code

**After:**
- Only source files in git
- No generated files tracked
- Clean, readable diffs

### 2. Single Source of Truth

**Before:**
- HTML might exist in multiple places
- Risk of serving stale version
- Manual copy required

**After:**
- `index.html` lives in `jsMain/resources` only
- Build guarantees fresh copy
- Automatic synchronization

### 3. Easier Maintenance

- Edit HTML once in `jsMain/resources`
- Build system handles the rest
- No manual file copying
- Consistent build output

### 4. Better Collaboration

- Cleaner git history
- No generated file conflicts
- Reviewers see only source changes
- CI/CD builds from source

## Troubleshooting

### Generated files missing after build

```bash
# Clean and rebuild
./gradlew clean kotlio-core:build

# Check task ran
./gradlew kotlio-core:build | grep "Bundled"
```

Should see:
```
✓ Bundled kotlio-core.js into kotlio-core resources
✓ Bundled kotlio-core.js.map into kotlio-core resources
✓ Bundled index.html into kotlio-core resources
```

### index.html not copied

Check source exists:
```bash
ls -la kotlio-core/src/jsMain/resources/index.html
```

### Webpack bundle missing

```bash
# Run webpack explicitly
./gradlew jsBrowserProductionWebpack

# Check output
ls -la kotlio-core/build/kotlin-webpack/js/productionExecutable/
```

### Resources not in JAR

```bash
# Rebuild JAR
./gradlew kotlio-core:jvmJar --rerun-tasks

# Verify contents
jar tf kotlio-core/build/libs/kotlio-core-jvm.jar | grep static
```

## CI/CD Considerations

### GitHub Actions Example

```yaml
- name: Build Kotlio
  run: ./gradlew build
  
- name: Verify Resources
  run: |
    test -f kotlio-core/src/jvmMain/resources/kotlio/static/index.html
    test -f kotlio-core/src/jvmMain/resources/kotlio/static/kotlio-core.js
    
- name: Package
  run: ./gradlew assemble
```

### GitLab CI Example

```yaml
build:
  script:
    - ./gradlew clean build
    - ls -la kotlio-core/src/jvmMain/resources/kotlio/static/
  artifacts:
    paths:
      - kotlio-core/build/libs/*.jar
```

## Summary

The Kotlio build process:

1. **Keeps source clean** - No generated files in git
2. **Automates bundling** - JS + HTML → JVM resources
3. **Single source** - HTML lives in `jsMain/resources`
4. **Zero config** - Works out of the box
5. **Reproducible** - Same source = same output

**For developers:**
- Edit `jsMain/resources/index.html` for HTML changes
- Run `./gradlew build` - everything is automatic
- Generated files in `jvmMain/resources/kotlio/static/` are gitignored

**For users:**
- Just add `kotlio-core` dependency
- All resources bundled in JAR
- No manual setup required

🎯 **Result:** Clean source tree, automatic builds, zero configuration!
