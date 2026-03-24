package com.kombat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kombat.controller.GameController;
import com.kombat.strategy.ast.Stmt;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.kombat.core.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToPlayer = new ConcurrentHashMap<>();

    private GameController gameController = new GameController();
    private boolean gameReady = false;
    private boolean p1SetupSpawned = false;
    private boolean p2SetupSpawned = false;
    private boolean p1Joined = false;
    private boolean p2Joined = false;
    private String gameMode = "PVP";

    private List<Map<String, Object>> minionConfigs = new ArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("✅ WebSocket connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> req = mapper.readValue(message.getPayload(), Map.class);
        String action = (String) req.get("action");
        System.out.println("📨 action=" + action);
        try {
            switch (action) {
                case "join"         -> handleJoin(session, req);
                case "create"       -> handleCreate(session, req);
                case "spawn"        -> handleSpawn(session, req);
                case "purchase-hex" -> handlePurchaseHex(session, req);
                case "execute-turn" -> handleExecuteTurn(session, req);
                case "validate"     -> handleValidate(session, req);
                case "get-configs"  -> handleGetConfigs(session);
                case "confirm-game" -> handleConfirmGame(session);
                case "cancel-game"  -> handleCancelGame(session);
                case "state"        -> sendToSession(session, ok("state", stateToMap(gameController.getGameState())));
                default             -> sendToSession(session, err("Unknown action: " + action));
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            sendToSession(session, err(e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String playerId = sessionToPlayer.remove(session.getId());
        if (playerId != null) sessions.remove(playerId);
        System.out.println("❌ Disconnected: " + session.getId() + " was " + playerId);
    }

    private void handleJoin(WebSocketSession session, Map<String, Object> req) throws Exception {
        Object pidObj = req.get("playerId");
        String playerId = (pidObj != null) ? pidObj.toString() : null;

        if (playerId != null && !playerId.isEmpty()) {
            sessions.put(playerId, session);
            sessionToPlayer.put(session.getId(), playerId);
            sendToSession(session, ok("joined", Map.of("playerId", playerId)));
            System.out.println("👤 " + playerId + " rejoined");
            return;
        }

        if (!p1Joined) { playerId = "p1"; p1Joined = true; }
        else if (!p2Joined) { playerId = "p2"; p2Joined = true; }
        else { sendToSession(session, err("Game is full")); return; }

        sessions.put(playerId, session);
        sessionToPlayer.put(session.getId(), playerId);
        sendToSession(session, ok("joined", Map.of("playerId", playerId)));
        System.out.println("👤 " + playerId + " joined");
    }

    private void handleCreate(WebSocketSession session, Map<String, Object> req) throws Exception {
        String modeStr = (String) req.getOrDefault("mode", "PVP");
        gameMode = modeStr.toUpperCase();

        GameState.Mode logicMode = switch (gameMode) {
            case "BVB" -> GameState.Mode.AUTO;
            case "PVB" -> GameState.Mode.SOLITAIRE;
            default    -> GameState.Mode.DUEL;
        };

        gameController = new GameController();
        gameController.createGame(null, logicMode);
        p1SetupSpawned = false;
        p2SetupSpawned = false;
        gameReady = true;

        Object configs = req.get("minionConfigs");
        if (configs instanceof List) {
            minionConfigs = (List<Map<String, Object>>) configs;
            System.out.println("📦 minionConfigs saved: " + minionConfigs.size());

            // ตั้ง kindDefense/kindAst ให้ bot
            Map<String, Integer> kindDefense = new HashMap<>();
            Map<String, List<Stmt>> kindAst = new HashMap<>();
            for (Map<String, Object> cfg : minionConfigs) {
                String minionId = (String) cfg.get("minionId");
                int defense = cfg.containsKey("defense") ? ((Number) cfg.get("defense")).intValue() : 10;
                String strategy = (String) cfg.getOrDefault("strategy", "done");
                kindDefense.put(minionId, defense);
                kindAst.put(minionId, gameController.parseStrategy(strategy));
            }
            gameController.setKinds(kindDefense, kindAst);
        }

        System.out.println("🎮 Game created mode=" + gameMode);

        // PVP: broadcast created ให้ P2 เห็น config เพื่อ confirm
        // PVB/BVB: broadcast created แล้วเข้าเกมเลย
        broadcast(ok("created", stateToMap(gameController.getGameState())));

        // PVB/BVB: auto setup spawn bot ทันที
        if ("PVB".equals(gameMode) || "BVB".equals(gameMode)) {
            autoSetupAll();
        }
    }

    // P2 confirm config → เริ่มเกม
    private void handleConfirmGame(WebSocketSession session) throws Exception {
        broadcast(ok("p2_confirmed", Map.of()));
    }

    // P2 ปฏิเสธ → แจ้ง P1
    private void handleCancelGame(WebSocketSession session) throws Exception {
        broadcast(ok("game_cancelled", Map.of()));
    }

    private void handleGetConfigs(WebSocketSession session) throws Exception {
        sendToSession(session, ok("configs", Map.of("minionConfigs", minionConfigs)));
    }

    // Auto setup spawn ทั้งคู่ (PVB: P2 bot, BVB: ทั้งคู่)
    private void autoSetupAll() throws Exception {
        if ("BVB".equals(gameMode)) {
            autoSetupSpawn("p1");
            autoSetupSpawn("p2");
            gameController.startGame();
            p1SetupSpawned = true;
            p2SetupSpawned = true;
            System.out.println("🤖 BVB: both auto spawned → PLAYING");
            broadcast(ok("spawned", stateToMap(gameController.getGameState())));
            // รัน BVB อัตโนมัติ
            runBvbGame();
        } else if ("PVB".equals(gameMode)) {
            // PVB: auto spawn P2 bot เท่านั้น P1 spawn เอง
            autoSetupSpawn("p2");
            p2SetupSpawned = true;
            System.out.println("🤖 PVB: P2 bot auto spawned");
            broadcast(ok("bot_spawned", stateToMap(gameController.getGameState())));
        }
    }

    private void autoSetupSpawn(String playerId) {
        if (minionConfigs.isEmpty()) return;
        GameState state = gameController.getGameState();
        Player player = gameController.getGameState().getPlayer(playerId);

        for (Map<String, Object> cfg : minionConfigs) {
            String minionId = (String) cfg.get("minionId");
            int defense = cfg.containsKey("defense") ? ((Number) cfg.get("defense")).intValue() : 10;
            String strategy = (String) cfg.getOrDefault("strategy", "done");

            for (String hexKey : player.getSpawnableHexes()) {
                Position pos = Position.fromString(hexKey);
                boolean occupied = state.minions.values().stream().anyMatch(m -> m.getPosition().equals(pos));
                if (occupied) continue;
                try {
                    var ast = gameController.parseStrategy(strategy);
                    Minion m = gameController.createMinion(minionId, playerId, pos.getRow(), pos.getCol(), defense);
                    if (gameController.setupSpawn(playerId, m, ast)) {
                        System.out.println("🤖 Auto spawn " + playerId + ": " + minionId + " at " + pos);
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("❌ autoSetupSpawn error: " + e.getMessage());
                }
                break;
            }
        }
    }

    private void handleSpawn(WebSocketSession session, Map<String, Object> req) throws Exception {
        ensureReady();
        Object pidObj = req.get("playerId");
        String playerId = pidObj != null ? pidObj.toString() : "p1";
        String kindName = (String) req.get("kindName");
        int row         = ((Number) req.get("row")).intValue();
        int col         = ((Number) req.get("col")).intValue();
        int defense     = req.containsKey("defense") ? ((Number) req.get("defense")).intValue() : 10;
        String strategy = (String) req.getOrDefault("strategy", "done");

        System.out.println("🎯 spawn: playerId=" + playerId + " kind=" + kindName);

        var ast  = gameController.parseStrategy(strategy);
        Minion m = gameController.createMinion(kindName, playerId, row, col, defense);

        GameState state = gameController.getGameState();
        boolean spawnOk;

        if (state.phase == GameState.Phase.SETUP) {
            spawnOk = gameController.setupSpawn(playerId, m, ast);
            if (spawnOk) {
                if ("p1".equals(playerId)) p1SetupSpawned = true;
                else p2SetupSpawned = true;

                if (p1SetupSpawned && p2SetupSpawned) {
                    gameController.startGame();
                    System.out.println("✅ Both spawned → PLAYING");

                    if ("BVB".equals(gameMode)) {
                        sendOrBroadcast(session, ok("spawned", stateToMap(gameController.getGameState())));
                        runBvbGame();
                        return;
                    }
                }
            }
        } else {
            spawnOk = gameController.spawnMinion(playerId, m, ast);
        }

        sendOrBroadcast(session, ok(spawnOk ? "spawned" : "spawn_failed", stateToMap(gameController.getGameState())));
    }

    private void runBvbGame() {
        new Thread(() -> {
            try {
                while (!gameController.isGameOver()) {
                    Thread.sleep(1500);
                    String current = gameController.getGameState().current;
                    gameController.executeTurn(current);
                    GameState snap = gameController.getGameState();
                    Map<String, Object> data = new HashMap<>();
                    data.put("state", stateToMap(snap));
                    data.put("isOver", snap.phase == GameState.Phase.ENDED);
                    data.put("winner", snap.winner != null ? snap.winner : "");
                    String event = snap.phase == GameState.Phase.ENDED ? "game_over" : "turn_executed";
                    broadcast(ok(event, data));
                }
            } catch (Exception e) {
                System.out.println("❌ BVB error: " + e.getMessage());
            }
        }).start();
    }

    private void handlePurchaseHex(WebSocketSession session, Map<String, Object> req) throws Exception {
        ensureReady();
        Object pidObj = req.get("playerId");
        String playerId = pidObj != null ? pidObj.toString() : "p1";
        int row = ((Number) req.get("row")).intValue();
        int col = ((Number) req.get("col")).intValue();
        boolean success = gameController.purchaseHex(playerId, row, col);
        sendOrBroadcast(session, ok(success ? "hex_purchased" : "hex_failed", stateToMap(gameController.getGameState())));
    }

    private void handleExecuteTurn(WebSocketSession session, Map<String, Object> req) throws Exception {
        ensureReady();
        GameState state = gameController.getGameState();
        if (state.phase == GameState.Phase.SETUP) {
            sendToSession(session, err("Setup ยังไม่เสร็จ"));
            return;
        }

        Object pidObj = req.get("playerId");
        String playerId = pidObj != null ? pidObj.toString() : "p1";
        var result = gameController.executeTurn(playerId);

        Map<String, Object> data = new HashMap<>();
        data.put("state",  stateToMap(gameController.getGameState()));
        data.put("isOver", result.isOver);
        data.put("winner", result.winner != null ? result.winner : "");
        sendOrBroadcast(session, ok(result.isOver ? "game_over" : "turn_executed", data));

        // PVB: หลัง P1 end turn → bot P2 รันอัตโนมัติ
        if (!result.isOver && "PVB".equals(gameMode) && "p1".equals(playerId)) {
            Thread.sleep(800);
            var botResult = gameController.executeTurn("p2");
            Map<String, Object> botData = new HashMap<>();
            botData.put("state",  stateToMap(gameController.getGameState()));
            botData.put("isOver", botResult.isOver);
            botData.put("winner", botResult.winner != null ? botResult.winner : "");
            sendOrBroadcast(session, ok(botResult.isOver ? "game_over" : "turn_executed", botData));
        }
    }

    private void handleValidate(WebSocketSession session, Map<String, Object> req) throws Exception {
        String strategy = (String) req.get("strategy");
        try {
            boolean valid = gameController.validateStrategy(strategy);
            sendToSession(session, ok("validated", Map.of("valid", valid)));
        } catch (Exception e) {
            sendToSession(session, ok("validated", Map.of("valid", false)));
        }
    }

    private void sendOrBroadcast(WebSocketSession sender, Map<String, Object> msg) throws Exception {
        String json = mapper.writeValueAsString(msg);
        if (sender.isOpen()) sender.sendMessage(new TextMessage(json));
        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen() && !s.getId().equals(sender.getId())) s.sendMessage(new TextMessage(json));
        }
    }

    private void broadcast(Map<String, Object> msg) throws Exception {
        String json = mapper.writeValueAsString(msg);
        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen()) s.sendMessage(new TextMessage(json));
        }
    }

    private void sendToSession(WebSocketSession session, Map<String, Object> msg) throws Exception {
        session.sendMessage(new TextMessage(mapper.writeValueAsString(msg)));
    }

    private void ensureReady() throws Exception {
        if (!gameReady) {
            gameController = new GameController();
            gameController.createGame(null, GameState.Mode.DUEL);
            p1SetupSpawned = false;
            p2SetupSpawned = false;
            gameReady = true;
        }
    }

    private Map<String, Object> stateToMap(GameState s) {
        Map<String, Object> map = new HashMap<>();
        map.put("turn",      s.turn);
        map.put("phase",     s.phase.name());
        map.put("current",   s.current);
        map.put("winner",    s.winner);
        map.put("endReason", s.endReason);
        map.put("p1", Map.of("id", s.p1.getId(), "budget", s.p1.getBudget(),
                "spawns", s.p1.getSpawnsUsed(), "hp", s.p1.getTotalHP()));
        map.put("p2", Map.of("id", s.p2.getId(), "budget", s.p2.getBudget(),
                "spawns", s.p2.getSpawnsUsed(), "hp", s.p2.getTotalHP()));

        // spawnable hexes → หน้าบ้านอัปเดต zone สี
        List<String> p1Hexes = new ArrayList<>(s.p1.getSpawnableHexes());
        List<String> p2Hexes = new ArrayList<>(s.p2.getSpawnableHexes());
        map.put("p1SpawnableHexes", p1Hexes);
        map.put("p2SpawnableHexes", p2Hexes);

        // valid purchase hexes → หน้าบ้านไฮไลท์ hex ที่ซื้อได้ถูกต้อง
        map.put("p1ValidPurchaseHexes", getValidPurchaseHexes(s, "p1"));
        map.put("p2ValidPurchaseHexes", getValidPurchaseHexes(s, "p2"));

        List<Map<String, Object>> minionList = new ArrayList<>();
        for (Minion m : s.minions.values()) {
            Map<String, Object> mm = new HashMap<>();
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

    private List<String> getValidPurchaseHexes(GameState s, String playerId) {
        Player player = "p1".equals(playerId) ? s.p1 : s.p2;
        List<String> valid = new ArrayList<>();
        Set<String> spawnable = player.getSpawnableHexes();

        System.out.println("🔍 spawnable hexes: " + spawnable);

        for (String hexKey : spawnable) {
            Position owned = Position.fromString(hexKey);
            for (int dir = Position.UP; dir <= Position.UPLEFT; dir++) {
                Position next = owned.move(dir);
                if (!next.isValid()) continue;
                String nextKey = next.getRow() + "-" + next.getCol();
                if (spawnable.contains(nextKey)) continue;
                if (s.minions.values().stream().anyMatch(m -> m.getPosition().equals(next))) continue;
                valid.add(next.getRow() + "-" + next.getCol());
            }
        }
        return valid;
    }

    private Map<String, Object> ok(String event, Object data) {
        Map<String, Object> r = new HashMap<>();
        r.put("ok", true); r.put("event", event); r.put("data", data);
        return r;
    }

    private Map<String, Object> err(String msg) {
        Map<String, Object> r = new HashMap<>();
        r.put("ok", false); r.put("event", "error"); r.put("message", msg);
        return r;
    }
}