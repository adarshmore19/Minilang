// ==============================================================================
// MiniLang 2.0: Advanced Capabilities Showcase
// 1. Native Dictionaries & HashMaps
// 2. Triple-Quoted Multi-line Strings
// 3. Robust Exception Handling (Try / Catch)
// 4. File I/O Standard Library
// ==============================================================================

// ------------------------------------------------------------------------------
// Feature 1: Triple-Quoted Multi-line Raw Strings
// ------------------------------------------------------------------------------
let banner = """
╔═══════════════════════════════════════════════════════════╗
║          MiniLang 2.0 Advanced System Showcase            ║
║  Dictionaries • Safe Exceptions • File I/O • Multi-Strings║
╚═══════════════════════════════════════════════════════════╝
""";
print(banner);

// ------------------------------------------------------------------------------
// Feature 2: Native Dictionaries (HashMaps)
// ------------------------------------------------------------------------------
print("=== 1. Native Dictionaries / HashMaps ===");

let serverConfig = {
    "host": "127.0.0.1",
    "port": 8080,
    "environment": "production",
    "sslEnabled": true
};

print("Initial Server Host: " + serverConfig["host"]);
print("Initial Port: " + str(serverConfig["port"]));

// Mutating existing key and adding new keys
serverConfig["port"] = 9000;
serverConfig["maxConnections"] = 5000;

print("Updated Server Config: " + str(serverConfig));
print("Dictionary Length: " + str(len(serverConfig)));
print("Has 'sslEnabled'? " + str(has(serverConfig, "sslEnabled")));
print("Has 'databaseUrl'? " + str(has(serverConfig, "databaseUrl")));
print("Config Keys: " + str(keys(serverConfig)));
print("Config Values: " + str(values(serverConfig)));

// ------------------------------------------------------------------------------
// Feature 3: File I/O Standard Library
// ------------------------------------------------------------------------------
print("\n=== 2. File I/O Standard Library ===");

let logFile = "minilang_system_audit.log";

// Write initial log
writeFile(logFile, "[2026-10-01 10:00:00] MiniLang 2.0 VM Initialized.\n");
print("Audit log created. Exists on disk? " + str(fileExists(logFile)));

// Append subsequent events
appendFile(logFile, "[2026-10-01 10:00:01] Loaded config for port " + str(serverConfig["port"]) + ".\n");
appendFile(logFile, "[2026-10-01 10:00:02] Security sandbox enabled. 0 leaks detected.\n");

// Read entire log
let logContents = readFile(logFile);
print("Read from disk (" + logFile + "):\n" + logContents);

// Clean up
deleteFile(logFile);
print("Cleaned up audit log. Exists now? " + str(fileExists(logFile)));

// ------------------------------------------------------------------------------
// Feature 4: Safe Exception Handling (Try / Catch)
// ------------------------------------------------------------------------------
print("\n=== 3. Try / Catch Exception Handling ===");

// 1. Handling division by zero
try {
    print("Executing potentially unsafe calculation...");
    let x = 100 / 0;
    print("This line will never be reached!");
} catch (mathError) {
    print(">>> Successfully caught runtime error: " + mathError);
}

// 2. Handling reading a file that doesn't exist
try {
    print("Attempting to read nonexistent file 'missing.txt'...");
    let missingData = readFile("missing.txt");
    print("Read succeeded? " + missingData);
} catch (fileError) {
    print(">>> Caught file error gracefully: " + fileError);
}

// 3. Nested Try-Catch inside a function
fn safeDivide(a, b) {
    try {
        return a / b;
    } catch (e) {
        print("safeDivide caught: " + e);
        return 0;
    }
}

print("safeDivide(40, 4) = " + str(safeDivide(40, 4)));
print("safeDivide(40, 0) = " + str(safeDivide(40, 0)));

print("\n=== All Advanced Features Executed Successfully! ===");
