package com.kombat.kombat;

import core.*;
import java.util.*;
import controller.GameController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins ="http://localhost:3000")
public class GameRestController {

    private GameController gameController = new GameController();
    private boolean gameReady = false;
    private boolean p1SetupSpawned = false;
    private boolean p2SetupSpawned = false;

    // ── auto-init ถ้า frontend ไม่ได้เรียก /create ก่อน ──────
    private boolean ensureReady() {
        if (!gameReady) {
            try {
                gameController = new GameController();
                gameController.createGame(null, GameState.Mode.DUEL);
                p1SetupSpawned = false;
                p2SetupSpawned = false;
                gameReady = true;
                System.out.println("⚠️ Auto-initialized game (frontend did not call /create)");
            } catch (Exception e) {
                System.out.println("❌ Auto-init failed: " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    // สร้างเกม
    @PostMapping("/create")
    public Map<String, Object> createGame(@RequestBody Map<String, Object> req) {
        try {
            String modeStr = (String) req.getOrDefault("mode", "DUEL");
            GameState.Mode mode = GameState.Mode.valueOf(modeStr.toUpperCase());
            gameController = new GameController();
            gameController.createGame(null, mode);
            p1SetupSpawned = false;
            p2SetupSpawned = false;
            gameReady = true;
            return ok("Game created", null);
        } catch (Exception e) {
            return err(e.getMessage());
        }
    }

    // ดู state
    @GetMapping("/state")
    public Map<String, Object> getState() {
        try {
            if (!ensureReady()) return err("Game not initialized");
            return ok("OK", stateToMap(gameController.getGameState()));
        } catch (Exception e) {
            return err(e.getMessage());
        }
    }

    // ซื้อ hex
    @PostMapping("/purchase-hex")
    public Map<String, Object> purchaseHex(@RequestBody Map<String, Object> req) {
        try {
            if (!ensureReady()) return err("Game not initialized");
            String playerId = (String) req.get("playerId");
            int row = (int) req.get("row");
            int col = (int) req.get("col");
            boolean success = gameController.purchaseHex(playerId, row, col);
            return ok(success ? "Purchased" : "Cannot purchase",
                    stateToMap(gameController.getGameState()));
        } catch (Exception e) {
            return err(e.getMessage());
        }
    }

    // spawn minion
    @PostMapping("/spawn")
    public Map<String, Object> spawnMinion(@RequestBody Map<String, Object> req) {
        try {
            if (!ensureReady()) return err("Game not initialized");

            String playerId = (String) req.get("playerId");
            String kindName = (String) req.get("kindName");
            int row         = (int) req.get("row");
            int col         = (int) req.get("col");
            int defense     = req.containsKey("defense") ? (int) req.get("defense") : 10;
            String strategy = (String) req.getOrDefault("strategy", "done");

            var ast  = gameController.parseStrategy(strategy);
            Minion m = gameController.createMinion(kindName, playerId, row, col, defense);

            GameState state = gameController.getGameState();
            boolean spawnOk;

            if (state.phase == GameState.Phase.SETUP) {
                // setup phase: ฟรี
                spawnOk = gameController.setupSpawn(playerId, m, ast);
                if (spawnOk) {
                    if ("p1".equals(playerId)) p1SetupSpawned = true;
                    else p2SetupSpawned = true;

                    // ทั้งสองฝ่าย spawn แล้ว → startGame
                    if (p1SetupSpawned && p2SetupSpawned) {
                        gameController.startGame();
                        System.out.println("✅ Both players spawned → Game STARTED!");
                    }
                }
            } else {
                // playing phase: หักเงินปกติ
                spawnOk = gameController.spawnMinion(playerId, m, ast);
            }

            return ok(spawnOk ? "Spawned" : "Cannot spawn",
                    stateToMap(gameController.getGameState()));
        } catch (Exception e) {
            return err(e.getMessage());
        }
    }

    // execute turn
    @PostMapping("/execute-turn")
    public Map<String, Object> executeTurn(@RequestBody Map<String, Object> req) {
        try {
            if (!ensureReady()) return err("Game not initialized");

            GameState state = gameController.getGameState();
            if (state.phase == GameState.Phase.SETUP) {
                return err("Setup ยังไม่เสร็จ — ทั้งสองฝ่ายต้อง spawn ก่อน");
            }

            String playerId = (String) req.get("playerId");
            var result = gameController.executeTurn(playerId);

            Map<String, Object> data = new HashMap<>();
            data.put("state",  stateToMap(gameController.getGameState()));
            data.put("isOver", result.isOver);
            data.put("winner", result.winner != null ? result.winner : "");
            return ok(result.isOver ? "Game ended" : "Turn executed", data);
        } catch (Exception e) {
            return err(e.getMessage());
        }
    }

    // validate strategy
    @PostMapping("/strategy/validate")
    public Map<String, Object> validateStrategy(@RequestBody Map<String, String> req) {
        try {
            boolean valid = gameController.validateStrategy(req.get("strategy"));
            return ok(valid ? "Valid" : "Invalid", Map.of("valid", valid));
        } catch (Exception e) {
            return ok("Invalid", Map.of("valid", false));
        }
    }

    // ── helpers ───────────────────────────────────────────────
    private Map<String, Object> stateToMap(GameState s) {
        Map<String, Object> map = new HashMap<>();
        map.put("turn",      s.turn);
        map.put("phase",     s.phase.name());
        map.put("current",   s.current);
        map.put("winner",    s.winner);
        map.put("endReason", s.endReason);
        map.put("p1", playerToMap(s.p1));
        map.put("p2", playerToMap(s.p2));

        List<Map<String,Object>> minionList = new ArrayList<>();
        for (Minion m : s.minions.values()) {
            Map<String,Object> mm = new HashMap<>();
            mm.put("id",    m.getId());
            mm.put("owner", m.getOwner().getId());
            mm.put("type",  m.getKindName());
            mm.put("hp",    m.getHp());
            mm.put("row",   m.getPosition().getRow());
            mm.put("col",   m.getPosition().getCol());
            minionList.add(mm);
        }
        map.put("minions", minionList);
        return map;
    }

    private Map<String, Object> playerToMap(Player p) {
        return Map.of(
                "id",     p.getId(),
                "budget", p.getBudget(),
                "spawns", p.getSpawnsUsed(),
                "hp",     p.getTotalHP()
        );
    }

    private Map<String, Object> ok(String msg, Object data) {
        Map<String, Object> r = new HashMap<>();
        r.put("ok", true); r.put("message", msg); r.put("data", data);
        return r;
    }

    private Map<String, Object> err(String msg) {
        Map<String, Object> r = new HashMap<>();
        r.put("ok", false); r.put("message", msg); r.put("data", null);
        return r;
    }

}