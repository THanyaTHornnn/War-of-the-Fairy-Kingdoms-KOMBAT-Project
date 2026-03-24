package com.kombat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@CrossOrigin
public class GameRestController {

    @Autowired
    private GameWebSocketHandler wsHandler;

    @GetMapping("/api/configs")
    public ResponseEntity<?> getConfigs() {
        return ResponseEntity.ok(Map.of("minionConfigs", wsHandler.getMinionConfigs()));
    }
}