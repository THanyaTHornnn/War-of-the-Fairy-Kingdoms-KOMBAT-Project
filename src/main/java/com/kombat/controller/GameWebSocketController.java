package com.kombat.controller;

import com.kombat.core.*;
import com.kombat.dto.*;
import com.kombat.strategy.ast.Stmt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class GameWebSocketController {

    @Autowired private GameController gameController;
    @Autowired private SimpMessagingTemplate messaging;

    // ── Broadcast helper ──────────────────────────────────────
    private void ok(Object data) {
        messaging.convertAndSend("/topic/game-state",
                Map.of("ok", true, "data", data));
    }

    private void err(String msg) {
        messaging.convertAndSend("/topic/game-error",
                Map.of("ok", false, "message", msg));
    }

    // ── 1. Create game ────────────────────────────────────────
    @MessageMapping("/game/create")
    public void createGame(Map<String, String> req) {
        try {
            String configPath = req.get("config");
            String modeStr    = req.getOrDefault("mode", "DUEL");
            GameState.Mode mode = GameState.Mode.valueOf(modeStr.toUpperCase());
            gameController.createGame(configPath, mode);
            ok(GameStateMapper.toDto(gameController.getGameState()));
        } catch (Exception e) { err(e.getMessage()); }
    }

    // ── 2. Validate strategy ──────────────────────────────────
    @MessageMapping("/game/strategy/validate")
    public void validateStrategy(Map<String, String> req) {
        boolean valid = gameController.validateStrategy(req.get("strategy"));
        messaging.convertAndSend("/topic/game-state",
                Map.of("ok", true, "valid", valid));
    }

    // ── 3. Setup spawn ────────────────────────────────────────
    @MessageMapping("/game/setup-spawn")
    public void setupSpawn(Map<String, Object> req) {
        try {
            String playerId = (String) req.get("playerId");
            String kindName = (String) req.get("kindName");
            int row         = ((Number) req.get("row")).intValue();
            int col         = ((Number) req.get("col")).intValue();
            int defense     = req.get("defense") != null
                    ? ((Number) req.get("defense")).intValue() : 0;
            String strategy = (String) req.get("strategy");

            List<Stmt> ast = gameController.parseStrategy(strategy);
            Minion minion  = gameController.createMinion(kindName, playerId, row, col, defense);
            boolean success = gameController.setupSpawn(playerId, minion, ast);

            if (success) ok(GameStateMapper.toDto(gameController.getGameState()));
            else         err("Cannot spawn here");
        } catch (Exception e) { err(e.getMessage()); }
    }

    // ── 4. Start game ─────────────────────────────────────────
    @MessageMapping("/game/start")
    public void startGame() {
        try {
            gameController.startGame();
            ok(GameStateMapper.toDto(gameController.getGameState()));
        } catch (Exception e) { err(e.getMessage()); }
    }

    // ── 5. Purchase hex ───────────────────────────────────────
    @MessageMapping("/game/purchase-hex")
    public void purchaseHex(Map<String, Object> req) {
        try {
            String playerId = (String) req.get("playerId");
            int row = ((Number) req.get("row")).intValue();
            int col = ((Number) req.get("col")).intValue();
            gameController.purchaseHex(playerId, row, col);
            ok(GameStateMapper.toDto(gameController.getGameState()));
        } catch (Exception e) { err(e.getMessage()); }
    }

    // ── 6. Spawn minion ───────────────────────────────────────
    @MessageMapping("/game/spawn")
    public void spawnMinion(Map<String, Object> req) {
        try {
            String playerId = (String) req.get("playerId");
            String kindName = (String) req.get("kindName");
            int row         = ((Number) req.get("row")).intValue();
            int col         = ((Number) req.get("col")).intValue();
            int defense     = req.get("defense") != null
                    ? ((Number) req.get("defense")).intValue() : 0;
            String strategy = (String) req.get("strategy");

            List<Stmt> ast  = gameController.parseStrategy(strategy);
            Minion minion   = gameController.createMinion(kindName, playerId, row, col, defense);
            gameController.spawnMinion(playerId, minion, ast);
            ok(GameStateMapper.toDto(gameController.getGameState()));
        } catch (Exception e) { err(e.getMessage()); }
    }

    // ── 7. Execute turn ───────────────────────────────────────
    @MessageMapping("/game/execute-turn")
    public void executeTurn(Map<String, String> req) {
        try {
            String playerId = req.get("playerId");
            GameController.TurnResult result = gameController.executeTurn(playerId);
            // executeTurn broadcast อัตโนมัติอยู่แล้วใน GameController
            // ส่ง isOver/winner เพิ่มเติม
            messaging.convertAndSend("/topic/game-state", Map.of(
                    "ok",       true,
                    "data",     GameStateMapper.toDto(gameController.getGameState()),
                    "isOver",   result.isOver,
                    "winner",   result.winner != null ? result.winner : ""
            ));
        } catch (Exception e) { err(e.getMessage()); }
    }

    // ── 8. Get state ──────────────────────────────────────────
    @MessageMapping("/game/state")
    public void getState() {
        try {
            ok(GameStateMapper.toDto(gameController.getGameState()));
        } catch (Exception e) { err(e.getMessage()); }
    }

    // ── 9. Run auto game ──────────────────────────────────────
    @MessageMapping("/game/run-auto")
    public void runAuto(Map<String, Object> req) {
        try {
            // รับ kinds จาก frontend
            // req = { "kinds": [ { "name": "verdant", "defense": 10, "strategy": "move down" } ] }
            List<Map<String, Object>> kinds = (List<Map<String, Object>>) req.get("kinds");

            Map<String, Integer> defenseMap = new HashMap<>();
            Map<String, List<Stmt>> astMap  = new HashMap<>();

            for (Map<String, Object> k : kinds) {
                String name     = (String) k.get("name");
                int defense     = ((Number) k.get("defense")).intValue();
                String strategy = (String) k.get("strategy");
                defenseMap.put(name, defense);
                astMap.put(name, gameController.parseStrategy(strategy));
            }

            gameController.setKinds(defenseMap, astMap);

            // สร้างเกม AUTO แล้ว setup spawn ฟรีให้ bot ทั้งสองฝั่ง
            gameController.createGame(null, GameState.Mode.AUTO);

            // setup spawn P1
            String firstName = defenseMap.keySet().iterator().next();
            Minion m1 = gameController.createMinion(firstName, "p1", 1, 1, defenseMap.get(firstName));
            gameController.setupSpawn("p1", m1, astMap.get(firstName));

            // setup spawn P2
            Minion m2 = gameController.createMinion(firstName, "p2", 8, 8, defenseMap.get(firstName));
            gameController.setupSpawn("p2", m2, astMap.get(firstName));

            gameController.startGame();

            // broadcast state เริ่มต้น
            messaging.convertAndSend("/topic/game-state",
                    Map.of("ok", true, "message", "Auto game started",
                            "data", GameStateMapper.toDto(gameController.getGameState())));

            // รัน auto ใน thread แยก
            new Thread(() -> gameController.runAutoGame()).start();

        } catch (Exception e) { err(e.getMessage()); }
    }

    //-- 10. Reset game ──────────────────────────────────────
    @MessageMapping("/game/reset")
    public void resetGame(Map<String, String> req) {
        try {
            String modeStr = req.getOrDefault("mode", "DUEL");
            GameState.Mode mode = GameState.Mode.valueOf(modeStr.toUpperCase());
            gameController.resetGame(mode);
            ok(GameStateMapper.toDto(gameController.getGameState()));
        } catch (Exception e) { err(e.getMessage()); }
    }


}