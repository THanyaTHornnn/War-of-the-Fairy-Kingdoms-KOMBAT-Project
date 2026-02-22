package controller;//package controller;
//
//import core.*;
//import dto.*;
//import parser.Node;
//import org.springframework.web.bind.annotation.*;
//import java.io.IOException;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/game")
//@CrossOrigin(origins = "*")
//public class GameRestController {
//
//    private final GameController gameController = new GameController();
//
//    // ── 1. Create game ────────────────────────────────────────
//    @PostMapping("/create")
//    public ApiResponse createGame(@RequestBody Map<String, String> req) {
//        try {
//            String configPath = req.get("config");
//            String modeStr    = req.getOrDefault("mode", "DUEL");
//            GameState.Mode mode = GameState.Mode.valueOf(modeStr.toUpperCase());
//            gameController.createGame(configPath, mode);
//            return ApiResponse.success("Game created", null);
//        } catch (IOException e) {
//            return ApiResponse.error("Failed to create game: " + e.getMessage());
//        }
//    }
//

//    // ── 2. Validate strategy ──────────────────────────────────
//    @PostMapping("/strategy/validate")
//    public ApiResponse validateStrategy(@RequestBody Map<String, String> req) {
//        boolean valid = gameController.validateStrategy(req.get("strategy"));
//        return ApiResponse.success(valid ? "Valid" : "Invalid", Map.of("valid", valid));
//    }
//
//    // ── 3. Setup spawn ────────────────────────────────────────
//    @PostMapping("/setup-spawn")
//    public ApiResponse setupSpawn(@RequestBody Map<String, Object> req) {
//        try {
//            String playerId  = (String) req.get("playerId");
//            String kindName  = (String) req.get("kindName");
//            int row          = (int) req.get("row");
//            int col          = (int) req.get("col");
//            String strategy  = (String) req.get("strategy");
//
//            Node.Strategy ast = gameController.parseStrategy(strategy);
//            Minion minion     = gameController.createMinion(kindName, playerId, row, col);
//            boolean success   = gameController.setupSpawn(playerId, minion, ast);
//
//            if (success) {
//                GameStateDto dto = GameStateMapper.toDto(gameController.getGameState());
//                return ApiResponse.success("Spawned", dto);
//            }
//            return ApiResponse.error("Cannot spawn here");
//        } catch (Exception e) {
//            return ApiResponse.error(e.getMessage());
//        }
//    }
//
//    // ── 4. Start game ─────────────────────────────────────────
//    @PostMapping("/start")
//    public ApiResponse startGame() {
//        try {
//            gameController.startGame();
//            GameStateDto dto = GameStateMapper.toDto(gameController.getGameState());
//            return ApiResponse.success("Game started", dto);
//        } catch (Exception e) {
//            return ApiResponse.error(e.getMessage());
//        }
//    }
//
//    // ── 5. Purchase hex ───────────────────────────────────────
//    @PostMapping("/purchase-hex")
//    public ApiResponse purchaseHex(@RequestBody Map<String, Object> req) {
//        try {
//            String playerId = (String) req.get("playerId");
//            int row         = (int) req.get("row");
//            int col         = (int) req.get("col");
//            boolean success = gameController.purchaseHex(playerId, row, col);
//            GameStateDto dto = GameStateMapper.toDto(gameController.getGameState());
//            return ApiResponse.success(success ? "Purchased" : "Cannot purchase", dto);
//        } catch (Exception e) {
//            return ApiResponse.error(e.getMessage());
//        }
//    }
//
//    // ── 6. Spawn minion ───────────────────────────────────────
//    @PostMapping("/spawn")
//    public ApiResponse spawnMinion(@RequestBody Map<String, Object> req) {
//        try {
//            String playerId = (String) req.get("playerId");
//            String kindName = (String) req.get("kindName");
//            int row         = (int) req.get("row");
//            int col         = (int) req.get("col");
//            String strategy = (String) req.get("strategy");
//
//            Node.Strategy ast = gameController.parseStrategy(strategy);
//            Minion minion     = gameController.createMinion(kindName, playerId, row, col);
//            boolean success   = gameController.spawnMinion(playerId, minion, ast);
//            GameStateDto dto  = GameStateMapper.toDto(gameController.getGameState());
//            return ApiResponse.success(success ? "Spawned" : "Cannot spawn", dto);
//        } catch (Exception e) {
//            return ApiResponse.error(e.getMessage());
//        }
//    }
//
//    // ── 7. Execute turn ───────────────────────────────────────
//    @PostMapping("/execute-turn")
//    public ApiResponse executeTurn(@RequestBody Map<String, String> req) {
//        try {
//            String playerId = req.get("playerId");
//            GameController.TurnResult result = gameController.executeTurn(playerId);
//            GameStateDto dto = GameStateMapper.toDto(gameController.getGameState());
//            return ApiResponse.success(
//                    result.isOver ? "Game ended" : "Turn executed",
//                    Map.of("state", dto, "isOver", result.isOver,
//                            "winner", result.winner != null ? result.winner : "")
//            );
//        } catch (Exception e) {
//            return ApiResponse.error(e.getMessage());
//        }
//    }
//
//    // ── 8. Get state ──────────────────────────────────────────
//    @GetMapping("/state")
//    public ApiResponse getState() {
//        try {
//            GameStateDto dto = GameStateMapper.toDto(gameController.getGameState());
//            return ApiResponse.success("OK", dto);
//        } catch (Exception e) {
//            return ApiResponse.error(e.getMessage());
//        }
//    }
//
//    // ── API Response wrapper ──────────────────────────────────
//    public static class ApiResponse {
//        public boolean ok;
//        public String message;
//        public Object data;
//
//        public ApiResponse(boolean ok, String message, Object data) {
//            this.ok      = ok;
//            this.message = message;
//            this.data    = data;
//        }
//        public static ApiResponse success(String msg, Object data) {
//            return new ApiResponse(true, msg, data);
//        }
//        public static ApiResponse error(String msg) {
//            return new ApiResponse(false, msg, null);
//        }
//    }
//}