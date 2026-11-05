package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Value("${app.allowed-root:./allowed}")
    private String allowedRoot;

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody Map<String, List<String>> body) {
        List<String> paths = body.getOrDefault("paths", List.of());
        Path root = Paths.get(allowedRoot).toAbsolutePath().normalize();
        List<String> deleted = new ArrayList<>();
        List<Map<String, String>> errors = new ArrayList<>();

        for (String p : paths) {
            try {
                Path target = root.resolve(p).normalize();
                if (!target.startsWith(root)) {
                    errors.add(Map.of("path", p, "error", "outside allowed root"));
                    continue;
                }
                if (Files.isSymbolicLink(target)) {
                    errors.add(Map.of("path", p, "error", "symlink refused"));
                    continue;
                }
                if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                    errors.add(Map.of("path", p, "error", "not found"));
                    continue;
                }
                if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) {
                    errors.add(Map.of("path", p, "error", "not a regular file"));
                    continue;
                }
                Files.delete(target);
                deleted.add(p);
            } catch (IOException e) {
                errors.add(Map.of("path", p, "error", e.getMessage()));
            }
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("deleted", deleted);
        resp.put("errors", errors);
        return ResponseEntity.ok(resp);
    }
}
