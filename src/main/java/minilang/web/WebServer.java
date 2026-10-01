package minilang.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import minilang.compiler.Compiler;
import minilang.compiler.Program;
import minilang.lexer.Lexer;
import minilang.lexer.Token;
import minilang.parser.Parser;
import minilang.parser.Stmt;
import minilang.vm.VM;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP server for MiniLang 2.0 Web Studio & Interactive Playground.
 * Provides static asset serving and execution REST APIs.
 */
public class WebServer {
    private final int port;
    private HttpServer server;

    public WebServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        server.createContext("/api/run", new RunHandler());
        server.createContext("/api/disassemble", new DisassembleHandler());
        server.createContext("/api/ast", new AstHandler());
        server.createContext("/api/tokens", new TokensHandler());
        server.createContext("/", new StaticFileHandler());

        server.start();
        System.out.println("=========================================================");
        System.out.println("  🚀 MiniLang 2.0 Web Studio is running!");
        System.out.println("  👉 Open in your browser: http://localhost:" + port);
        System.out.println("=========================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            byte[] bytes = loadResource(path);
            if (bytes == null) {
                String notFound = "404 Not Found";
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes(StandardCharsets.UTF_8));
                }
                return;
            }

            String contentType = "text/plain";
            if (path.endsWith(".html")) contentType = "text/html; charset=utf-8";
            else if (path.endsWith(".css")) contentType = "text/css; charset=utf-8";
            else if (path.endsWith(".js")) contentType = "application/javascript; charset=utf-8";
            else if (path.endsWith(".svg")) contentType = "image/svg+xml";
            else if (path.endsWith(".json")) contentType = "application/json";

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private byte[] loadResource(String requestPath) {
            if (requestPath.startsWith("/")) {
                requestPath = requestPath.substring(1);
            }

            // 1. Try file on disk (development hot-reload)
            Path[] candidates = new Path[]{
                    Paths.get("src/main/resources/web", requestPath),
                    Paths.get("web", requestPath),
                    Paths.get("target/classes/web", requestPath)
            };

            for (Path p : candidates) {
                if (Files.exists(p) && !Files.isDirectory(p)) {
                    try {
                        return Files.readAllBytes(p);
                    } catch (IOException ignored) {}
                }
            }

            // 2. Try classpath resource
            try (InputStream is = getClass().getResourceAsStream("/web/" + requestPath)) {
                if (is != null) {
                    return is.readAllBytes();
                }
            } catch (IOException ignored) {}

            return null;
        }
    }

    private static class RunHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String code = extractJsonField(body, "code");

            if (code == null) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Missing 'code' field\"}");
                return;
            }

            long startNano = System.nanoTime();
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
            ByteArrayOutputStream outCapture = new ByteArrayOutputStream();
            PrintStream customPrint = new PrintStream(outCapture, true, StandardCharsets.UTF_8);

            boolean success = false;
            String errorMsg = null;
            String bytecode = "";
            String ast = "";

            synchronized (WebServer.class) {
                try {
                    System.setOut(customPrint);
                    System.setErr(customPrint);

                    Lexer lexer = new Lexer(code);
                    Parser parser = new Parser(lexer);
                    List<Stmt> statements = parser.parse();

                    Compiler compiler = new Compiler(true);
                    Program program = compiler.compile(statements);
                    bytecode = program.disassemble();

                    VM vm = new VM(program);
                    vm.run();
                    success = true;
                } catch (Throwable t) {
                    success = false;
                    errorMsg = t.getMessage();
                    if (errorMsg == null || errorMsg.isBlank()) {
                        errorMsg = t.getClass().getSimpleName();
                    }
                } finally {
                    System.setOut(originalOut);
                    System.setErr(originalErr);
                }
            }

            long elapsedNanos = System.nanoTime() - startNano;
            double elapsedMs = elapsedNanos / 1_000_000.0;

            String output = outCapture.toString(StandardCharsets.UTF_8);

            StringBuilder json = new StringBuilder("{");
            json.append("\"success\":").append(success).append(",");
            json.append("\"output\":").append(escapeJson(output)).append(",");
            json.append("\"error\":").append(errorMsg == null ? "null" : escapeJson(errorMsg)).append(",");
            json.append("\"bytecode\":").append(escapeJson(bytecode)).append(",");
            json.append("\"timeMs\":").append(String.format("%.2f", elapsedMs));
            json.append("}");

            sendJsonResponse(exchange, 200, json.toString());
        }
    }

    private static class DisassembleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String code = extractJsonField(body, "code");

            try {
                Lexer lexer = new Lexer(code != null ? code : "");
                Parser parser = new Parser(lexer);
                List<Stmt> statements = parser.parse();
                Compiler compiler = new Compiler(true);
                Program program = compiler.compile(statements);

                String result = "{\"success\":true,\"bytecode\":" + escapeJson(program.disassemble()) + "}";
                sendJsonResponse(exchange, 200, result);
            } catch (Throwable t) {
                String err = "{\"success\":false,\"error\":" + escapeJson(t.getMessage()) + "}";
                sendJsonResponse(exchange, 200, err);
            }
        }
    }

    private static class AstHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String code = extractJsonField(body, "code");

            try {
                Lexer lexer = new Lexer(code != null ? code : "");
                Parser parser = new Parser(lexer);
                List<Stmt> statements = parser.parse();

                StringBuilder sb = new StringBuilder();
                for (Stmt s : statements) {
                    sb.append(s.toString()).append("\n");
                }

                String result = "{\"success\":true,\"ast\":" + escapeJson(sb.toString()) + "}";
                sendJsonResponse(exchange, 200, result);
            } catch (Throwable t) {
                String err = "{\"success\":false,\"error\":" + escapeJson(t.getMessage()) + "}";
                sendJsonResponse(exchange, 200, err);
            }
        }
    }

    private static class TokensHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCors(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String code = extractJsonField(body, "code");

            try {
                Lexer lexer = new Lexer(code != null ? code : "");
                List<Token> tokens = lexer.tokenize();

                StringBuilder sb = new StringBuilder();
                for (Token t : tokens) {
                    sb.append(t.toString()).append("\n");
                }

                String result = "{\"success\":true,\"tokens\":" + escapeJson(sb.toString()) + "}";
                sendJsonResponse(exchange, 200, result);
            } catch (Throwable t) {
                String err = "{\"success\":false,\"error\":" + escapeJson(t.getMessage()) + "}";
                sendJsonResponse(exchange, 200, err);
            }
        }
    }

    private static void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String extractJsonField(String json, String field) {
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return null;
        int colon = json.indexOf(':', idx + key.length());
        if (colon == -1) return null;

        int startQuote = json.indexOf('"', colon + 1);
        if (startQuote == -1) return null;

        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (int i = startQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaped) {
                if (c == 'n') sb.append('\n');
                else if (c == 'r') sb.append('\r');
                else if (c == 't') sb.append('\t');
                else if (c == '\\') sb.append('\\');
                else if (c == '"') sb.append('"');
                else if (c == '/') sb.append('/');
                else sb.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                return sb.toString();
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 32 || c >= 127) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
