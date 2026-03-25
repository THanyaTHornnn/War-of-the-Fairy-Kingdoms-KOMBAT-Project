package com.kombat.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StrategyController {

    // ใช้ GameController instance ชั่วคราว เพื่อ validate เท่านั้น
    // ไม่ต้อง createGame ก่อน เพราะ validateStrategy ใช้แค่ Parser
    private final GameController validator = new GameController();

    @PostMapping("/validate-strategy")
    public Map<String, Object> validate(@RequestBody Map<String, String> body) {
        String strategy = body.getOrDefault("strategy", "");
        try {
            boolean valid = validator.validateStrategy(strategy);
            return Map.of("valid", valid);
        } catch (Exception e) {
            return Map.of("valid", false, "message", e.getMessage());
        }
    }
}