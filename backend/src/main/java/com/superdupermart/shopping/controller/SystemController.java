package com.superdupermart.shopping.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.superdupermart.shopping.service.ProductService;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

@RestController
@RequestMapping("/api/system")
@PreAuthorize("hasRole('ADMIN')")
public class SystemController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private ProductService productService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> getHealth() {
        Map<String, String> status = new HashMap<>();

        // Database Check
        try (Connection conn = dataSource.getConnection()) {
            status.put("database", conn.isValid(1) ? "UP" : "DOWN");
        } catch (SQLException e) {
            status.put("database", "DOWN: " + e.getMessage());
        }

        // Redis Check
        try {
            String ping = redisConnectionFactory.getConnection().ping();
            status.put("redis", "PONG".equals(ping) ? "UP" : "DOWN (" + ping + ")");
        } catch (Exception e) {
            status.put("redis", "DOWN: " + e.getMessage());
        }

        // Elasticsearch Check (via ProductService or Repository)
        // Simple check: Sync a dummy product or count
        try {
            // We can check if search returns without error
            productService.searchProducts("", null, null, 0, 1);
            status.put("elasticsearch", "UP");
        } catch (Exception e) {
            status.put("elasticsearch", "DOWN: " + e.getMessage());
        }

        return ResponseEntity.ok(status);
    }

    @GetMapping("/logs")
    public ResponseEntity<List<String>> getLogs(@RequestParam(defaultValue = "100") int lines) {
        // Read from default log file path. Adjust if logback config differs.
        // Assuming /app/logs/application.log or stdout capture.
        // For Docker, we might need to read a specific file if mounted,
        // or just return a static message if logs are only on stdout.
        // LET'S ASSUME a standard log file exists for now, or just return mock if file
        // not found.

        File logFile = new File("logs/application.log"); // Default Spring Boot log location if configured?
        // Actually, Spring Boot default doesn't write to file unless logging.file.name
        // is set.
        // We might need to configure logging to file in application.properties first!

        if (!logFile.exists()) {
            // Fallback: try /tmp/spring.log
            logFile = new File("/tmp/spring.log");
        }

        if (!logFile.exists()) {
            return ResponseEntity.ok(Collections.singletonList("Log file not found. Ensure logging.file.name is set."));
        }

        List<String> tail = tailFile(logFile, lines);
        return ResponseEntity.ok(tail);
    }

    private List<String> tailFile(File file, int lines) {
        List<String> result = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long length = raf.length();
            long pos = length - 1;
            int count = 0;
            StringBuilder sb = new StringBuilder();

            while (pos >= 0 && count < lines) {
                raf.seek(pos);
                char c = (char) raf.readByte();

                if (c == '\n') {
                    if (sb.length() > 0) {
                        result.add(0, sb.reverse().toString());
                        sb.setLength(0);
                        count++;
                    }
                } else {
                    sb.append(c);
                }
                pos--;
            }
            if (sb.length() > 0) {
                result.add(0, sb.reverse().toString());
            }
        } catch (IOException e) {
            result.add("Error reading log file: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/sync-products")
    public ResponseEntity<String> syncProducts() {
        try {
            productService.syncAllProducts();
            return ResponseEntity.ok("Products synced successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Sync failed: " + e.getMessage());
        }
    }

    @PostMapping("/flush-cache")
    @SuppressWarnings("deprecation")
    public ResponseEntity<String> flushCache() {
        try {
            redisConnectionFactory.getConnection().flushAll();
            return ResponseEntity.ok("Cache flushed successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Flush failed: " + e.getMessage());
        }
    }
}
