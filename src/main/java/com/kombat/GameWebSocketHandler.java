
package com.kombat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kombat.controller.GameController;
import com.kombat.core.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

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

    // เก็บ minion configs ของ P1 ไว้ให้ P2 ใช้
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
                case "join"           -> handleJoin(session, req);
                case "create"         -> handleCreate(session, req);
                case "spawn"          -> handleSpawn(session, req);
                case "purchase-hex"   -> handlePurchaseHex(session, req);
                case "execute-turn"   -> handleExecuteTurn(session, req);
                case "validate"       -> handleValidate(session, req);
                case "get-configs"    -> handleGetConfigs(session);
                case "confirm"        -> handleConfirm(session, req);  // เพิ่ม
                case "cancel"         -> handleCancel(session, req);   // เพิ่ม
                case "state"          -> sendToSession(session, ok("state", stateToMap(gameController.getGameState())));
                default               -> sendToSession(session, err("Unknown action: " + action));
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            sendToSession(session, err(e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    // GameWebSocketHandler.java
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String playerId = sessionToPlayer.remove(session.getId());
        if (playerId == null) return;

        sessions.remove(playerId);

        if ("p1".equals(playerId)) {
            p1Joined = false;
            p1SetupSpawned = false;

            // ถ้า P2 ยังอยู่ → เลื่อน P2 ขึ้นเป็น P1
            WebSocketSession p2Session = sessions.get("p2");
            if (p2Session != null && p2Session.isOpen()) {
                // ย้าย P2 → P1 ใน map
                sessions.remove("p2");
                sessions.put("p1", p2Session);

                // อัปเดต sessionToPlayer
                sessionToPlayer.put(p2Session.getId(), "p1");

                p2Joined = false;
                p2SetupSpawned = false;

                // แจ้ง P2 ว่าตอนนี้เป็น P1 แล้ว
                try {
                    sendToSession(p2Session, Map.of(
                            "event", "joined",
                            "data", Map.of("playerId", "p1")
                    ));
                } catch (Exception e) {}
            }

            // แจ้งทุกคนว่า P1 ออก
            try {
                broadcast(Map.of("event", "player_left", "data", Map.of("playerId", "p1")));
            } catch (Exception e) {}

        } else if ("p2".equals(playerId)) {
            p2Joined = false;
            p2SetupSpawned = false;

            try {
                broadcast(Map.of("event", "player_left", "data", Map.of("playerId", "p2")));
            } catch (Exception e) {}

        }
        // spectator ออกไม่ต้อง broadcast
    }

    // GameWebSocketHandler.java - handleJoin
    private void handleJoin(WebSocketSession session, Map<String, Object> req) throws Exception {
        String requestedId = (String) req.get("requestedId");
        String finalId = null;

        boolean p1Empty = !sessions.containsKey("p1") || !sessions.get("p1").isOpen();
        boolean p2Empty = !sessions.containsKey("p2") || !sessions.get("p2").isOpen();

        // ✅ ถ้าขอ ID เดิมและ slot นั้นว่างอยู่ → คืนให้
        if ("p1".equals(requestedId) && p1Empty) {
            finalId = "p1";
        } else if ("p2".equals(requestedId) && p2Empty) {
            finalId = "p2";
        } else if (p1Empty) {
            finalId = "p1";
        } else if (p2Empty) {
            finalId = "p2";
        } else {
            finalId = "spectator-" + session.getId().substring(0, 6);
        }

        sessions.put(finalId, session);
        sessionToPlayer.put(session.getId(), finalId);

        sendToSession(session, Map.of(
                "event", "joined",
                "data", Map.of("playerId", finalId)
        ));
    }

    private void handleCreate(WebSocketSession session, Map<String, Object> req) throws Exception {
        String modeStr = (String) req.getOrDefault("mode", "DUEL");
        GameState.Mode mode = GameState.Mode.valueOf(modeStr.toUpperCase());
        gameController = new GameController();
        gameController.createGame(null, mode);
        p1SetupSpawned = false;
        p2SetupSpawned = false;
        gameReady = true;

        // เก็บ minionConfigs จาก P1
        Object configs = req.get("minionConfigs");
        if (configs instanceof List) {
            minionConfigs = (List<Map<String, Object>>) configs;
            System.out.println("📦 minionConfigs saved: " + minionConfigs.size() + " configs");
        }

        System.out.println("🎮 Game created mode=" + mode);
        broadcast(ok("created", stateToMap(gameController.getGameState())));
    }

    // P2 ขอ configs ของ P1
    private void handleGetConfigs(WebSocketSession session) throws Exception {
        sendToSession(session, ok("configs", Map.of("minionConfigs", minionConfigs)));
    }

    // เพิ่ม Helper นี้ใน GameWebSocketHandler.java
    private int forceInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).intValue();
        if (obj instanceof String) {
            try {
                return (int) Double.parseDouble((String) obj);
            } catch (Exception e) { return 0; }
        }
        return 0;
    }

    private void handleSpawn(WebSocketSession session, Map<String, Object> req) throws Exception {
        ensureReady();
        Object pidObj = req.get("playerId");
        String playerId = pidObj != null ? pidObj.toString() : "p1";
        String kindName = (String) req.get("kindName");
        // ใน handleSpawn ให้เรียกใช้แบบนี้:
        int row = forceInt(req.get("row"));
        int col = forceInt(req.get("col"));
        int defense = req.containsKey("defense") ? forceInt(req.get("defense")) : 10;
        String strategy = (String) req.getOrDefault("strategy", "done");

        System.out.println("🎯 spawn: playerId=" + playerId + " kind=" + kindName
                + " p1Spawned=" + p1SetupSpawned + " p2Spawned=" + p2SetupSpawned);

        var ast  = gameController.parseStrategy(strategy);
        Minion m = gameController.createMinion(kindName, playerId, row, col, defense);

        GameState state = gameController.getGameState();
        boolean spawnOk;

        if (state.phase == GameState.Phase.SETUP) {
            spawnOk = gameController.setupSpawn(playerId, m, ast);
            System.out.println("🎯 setupSpawn result=" + spawnOk);
            if (spawnOk) {
                if ("p1".equals(playerId)) p1SetupSpawned = true;
                else p2SetupSpawned = true;
                System.out.println("🎯 p1Spawned=" + p1SetupSpawned + " p2Spawned=" + p2SetupSpawned);
                // เพิ่ม
                if (p1SetupSpawned && !p2SetupSpawned) {
                    // P1 spawn เสร็จ รอ P2 ยืนยัน
                    broadcast(ok("waiting_for_p2", stateToMap(gameController.getGameState())));
                }
                if (p1SetupSpawned && p2SetupSpawned) {
                    gameController.startGame();
                    System.out.println("✅ Both spawned → PLAYING");
                }
            }
        } else {
            spawnOk = gameController.spawnMinion(playerId, m, ast);
        }

        sendOrBroadcast(session, ok(spawnOk ? "spawned" : "spawn_failed",
                stateToMap(gameController.getGameState())));
    }

    private void handleConfirm(WebSocketSession session, Map<String, Object> req) throws Exception {
        String playerId = sessionToPlayer.get(session.getId());
        System.out.println("✅ " + playerId + " confirmed");
        broadcast(ok("p2_confirmed", Map.of(
                "message", "P2 พร้อมแล้ว",
                "minionConfigs", minionConfigs
        )));
    }

    private void handleCancel(WebSocketSession session, Map<String, Object> req) throws Exception {
        String playerId = sessionToPlayer.get(session.getId());
        System.out.println("❌ " + playerId + " cancelled");
        gameReady = false;
        p1SetupSpawned = false;
        p2SetupSpawned = false;
        gameController = new GameController();
        broadcast(ok("game_cancelled", Map.of("message", "ยกเลิกการเข้าร่วมเกม")));
    }

    private void handlePurchaseHex(WebSocketSession session, Map<String, Object> req) throws Exception {
        ensureReady();
        Object pidObj = req.get("playerId");
        String playerId = pidObj != null ? pidObj.toString() : "p1";
        int row = ((Number) req.get("row")).intValue();
        int col = ((Number) req.get("col")).intValue();
        boolean success = gameController.purchaseHex(playerId, row, col);
        sendOrBroadcast(session, ok(success ? "hex_purchased" : "hex_failed",
                stateToMap(gameController.getGameState())));
    }

    private void handleExecuteTurn(WebSocketSession session, Map<String, Object> req) throws Exception {
        ensureReady();
        GameState state = gameController.getGameState();
        System.out.println("⚔️ executeTurn: phase=" + state.phase);

        if (state.phase == GameState.Phase.SETUP) {
            sendToSession(session, err("Setup ยังไม่เสร็จ — ทั้งสองฝ่ายต้อง spawn ก่อน"));
            return;
        }

        Object pidObj = req.get("playerId");
        String playerId = pidObj != null ? pidObj.toString() : "p1";
        var result = gameController.endTurn(playerId);

        Map<String, Object> data = new HashMap<>();
        data.put("state",  stateToMap(gameController.getGameState()));
        data.put("isOver", result.isOver);
        data.put("winner", result.winner != null ? result.winner : "");

        sendOrBroadcast(session, ok(result.isOver ? "game_over" : "turn_executed", data));
    }

    private void handleValidate(WebSocketSession session, Map<String, Object> req) throws Exception {
        String strategy = (String) req.get("strategy");
        System.out.println("📋 validate strategy: [" + strategy + "]");
        try {
            boolean valid = gameController.validateStrategy(strategy);
            sendToSession(session, ok("validated", Map.of("valid", valid)));
        } catch (Exception e) {
            System.out.println("❌ validate error: " + e.getMessage());
            sendToSession(session, ok("validated", Map.of("valid", false, "error", e.getMessage())));
        }
    }

    private void sendOrBroadcast(WebSocketSession sender, Map<String, Object> msg) throws Exception {
        String json = mapper.writeValueAsString(msg);
        if (sender.isOpen()) sender.sendMessage(new TextMessage(json));
        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen() && !s.getId().equals(sender.getId())) {
                s.sendMessage(new TextMessage(json));
            }
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