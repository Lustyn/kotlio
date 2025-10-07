# Ktor Routing Configuration

## Route Order Matters!

In Ktor, routes are matched in the order they are defined. This is critical for serving static resources correctly.

## Current Configuration

```kotlin
routing {
    // 1. Define specific API routes FIRST (they take precedence)
    get("/schema") { ... }
    get("/health") { ... }
    post("/action") { ... }
    
    // 2. Serve static resources LAST (catch-all)
    staticResources("/", "kotlio/static")
}
```

### Why This Order?

**Correct Order (Current):**
```kotlin
get("/schema") { ... }           // ✅ Matches /schema
staticResources("/", "...")       // ✅ Then checks static files
```
- Request to `/schema` → Matches specific route ✅
- Request to `/` → Falls through to staticResources, serves `index.html` ✅
- Request to `/kotlio-core.js` → Serves from static resources ✅

**Wrong Order (Don't Do This):**
```kotlin
staticResources("/", "...")       // ❌ Too early!
get("/schema") { ... }            // ❌ Never reached!
```
- Request to `/schema` → staticResources tries to find `schema` file, fails ❌
- API endpoints would be shadowed by static file lookup ❌

## How StaticResources Works

`staticResources("/", "kotlio/static")` does the following:

1. **Takes the request path** (e.g., `/`, `/kotlio-core.js`, `/schema`)
2. **Appends it to the classpath resource path** → `kotlio/static{path}`
3. **Looks for a file at that path** in the classpath
4. **If found**, serves it with appropriate Content-Type
5. **If not found**, continues to next route

### Examples

| Request | Classpath Lookup | Result |
|---------|-----------------|--------|
| `GET /` | `kotlio/static/index.html` | ✅ Served (exists) |
| `GET /kotlio-core.js` | `kotlio/static/kotlio-core.js` | ✅ Served (exists) |
| `GET /schema` | `kotlio/static/schema` | ❌ Not found → Falls through to `get("/schema")` |
| `GET /action` | `kotlio/static/action` | ❌ Not found → Falls through to `post("/action")` |

## Best Practices

### ✅ DO

1. **Define specific routes first**
   ```kotlin
   get("/api/users") { ... }
   post("/api/login") { ... }
   staticResources("/", "public")  // Last
   ```

2. **Use specific paths for APIs**
   ```kotlin
   get("/api/schema") { ... }      // Good - specific prefix
   staticResources("/", "static")
   ```

3. **Group static resources under a path**
   ```kotlin
   get("/api/data") { ... }
   staticResources("/assets", "public")  // Serves from /assets/*
   ```

### ❌ DON'T

1. **Don't put staticResources before specific routes**
   ```kotlin
   staticResources("/", "...")     // ❌ Wrong order!
   get("/api/data") { ... }
   ```

2. **Don't rely on staticResources to handle API routes**
   ```kotlin
   // Bad - hoping 404 will fall through
   staticResources("/", "...")
   get("/schema") { ... }  // May never be reached
   ```

## Testing the Setup

### Verify Routes Work

```bash
# Start server
./gradlew example:run

# Test in another terminal:
curl http://localhost:8080/          # Should return HTML
curl http://localhost:8080/schema    # Should return JSON
curl http://localhost:8080/health    # Should return {"status":"ok"}
```

### Expected Responses

```bash
# Root path (/)
curl http://localhost:8080/
# Returns: <!DOCTYPE html><html>...</html>
# Content-Type: text/html

# API endpoint
curl http://localhost:8080/schema
# Returns: {"pages":[...]}
# Content-Type: application/json

# Static resource
curl http://localhost:8080/kotlio-core.js
# Returns: JavaScript code
# Content-Type: application/javascript
```

## Debugging

### Route Not Working?

1. **Check the order** - Specific routes before staticResources?
2. **Check the path** - Does the file exist in `src/jvmMain/resources/kotlio/static/`?
3. **Check the JAR** - `jar tf kotlio-core-jvm.jar | grep static`
4. **Check logs** - Enable Ktor's call logging

### Common Issues

**Issue:** API returns 404  
**Cause:** staticResources defined before the API route  
**Fix:** Move staticResources to end of routing block

**Issue:** Root path (/) returns 404  
**Cause:** No index.html in static resources  
**Fix:** Ensure `index.html` exists in `kotlio/static/`

**Issue:** JavaScript not loading  
**Cause:** Build didn't copy webpack bundle  
**Fix:** Run `./gradlew kotlio-core:build` to trigger `copyJsBundleToResources`

## Summary

✨ **Key Takeaway:** Order routes from most specific to least specific:
1. Exact API routes (`get("/schema")`)
2. Parameterized routes (`get("/user/{id}")`)
3. Catch-all static resources (`staticResources("/", "...")`)

This ensures API endpoints are never shadowed by static file lookups! 🎯
