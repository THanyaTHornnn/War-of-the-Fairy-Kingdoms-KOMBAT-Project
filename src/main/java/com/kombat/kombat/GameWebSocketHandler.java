package com.kombat.kombat;

import com.fasterxml.jackson.databind.ObjectMapper;
import controller.GameController;
import core.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    // เก็บ session ของทั้งสองผู้เล่น
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    // เก็บ playerId ของแต่ละ session
    private final Map<String, String> sessionToPlayer = new ConcurrentHashMap<>();

    private GameController gameController = new GameController();
    private boolean gameReady = false;
    private boolean p1SetupSpawned = false;
    private boolean p2SetupSpawned = false;

    // ── เมื่อ client เชื่อมต่อ ─────────────────────────────────
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("✅ WebSocket connected: " + session.getId());
    }

    // ── เมื่อ client ส่ง message ──────────────────────────────
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> req = mapper.readValue(message.getPayload(), Map.class);
        String action = (String) req.get("action");

        try {
            switch (action) {
                case "join"           -> handleJoin(session, req);
                case "create"         -> handleCreate(session, req);
                case "spawn"          -> handleSpawn(session, req);
                case "purchase-hex"   -> handlePurchaseHex(session, req);
                case "execute-turn"   -> handleExecuteTurn(session, req);
                case "validate"       -> handleValidate(session, req);
                case "state"          -> sendToSession(session, ok("state", stateToMap(gameController.getGameState())));
                default               -> sendToSession(session, err("Unknown action: " + action));
            }
        } catch (Exception e) {
            sendToSession(session, err(e.getMessage()));
        }
    }

    // ── เมื่อ client ตัดการเชื่อมต่อ ──────────────────────────
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String playerId = sessionToPlayer.remove(session.getId());
        if (playerId != null) sessions.remove(playerId);
        System.out.println("❌ WebSocket disconnected: " + session.getId());
    }

    // ── Handlers ──────────────────────────────────────────────

    // join: ผูก session กับ playerId
    private void handleJoin(WebSocketSession session, Map<String, Object> req) throws Exception {
        String playerId = (String) req.get("playerId"); // "p1" หรือ "p2"
        sessions.put(playerId, session);
        sessionToPlayer.put(session.getId(), playerId);
        sendToSession(session, ok("joined", Map.of("playerId", playerId)));
        System.out.println("👤 " + playerId + " joined");
    }

    // create: สร้างเกมใหม่
    private void handleCreate(WebSocketSession session, Map<String, Object> req) throws Exception {
        String modeStr = (String) req.getOrDefault("mode", "DUEL");
        GameState.Mode mode = GameState.Mode.valueOf(modeStr.toUpperCase());
        gameController = new GameController();
        gameController.createGame(null, mode);
        p1SetupSpawned = false;
        p2SetupSpawned = false;
        gameReady = true;
        // broadcast ให้ทั้งสองหน้าจอ
        broadcast(ok("created", stateToMap(gameController.getGameState())));
    }

    // spawn: spawn minion (SETUP หรือ PLAYING)
    private void handleSpawn(WebSocketSession session, Map<String, Object> req) throws Exception {
        ensureReady();
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
            spawnOk = gameController.setupSpawn(playerId, m, ast);
            if (spawnOk) {
                if ("p1".equals(playerId)) p1SetupSpawned = true;
                else p2SetupSpawned = true;
                if (p1SetupSpawned && p2SetupSpawned) {
                    gameController.startGame();
                    System.out.println("✅ Both spawned → PLAYING");
                }
            }
        } else {
            spawnOk = gameController.spawnMinion(playerId, m, ast);
        }

        // broadcast state ใหม่ให้ทั้งสองหน้าจอ
        broadcast(ok(spawnOk ? "spawned" : "spawn_failed",
                stateToMap(gameController.getGameState())));
    }

    // purchase-hex
    private void handlePurchaseHex(WebSocketSession session, Map<String, Object> req) throws Exception {
        ensureReady();
        String playerId = (String) req.get("playerId");
        int row = (int) req.get("row");
        int col = (int) req.get("col");
        boolean success = gameController.purchaseHex(playerId, row, col);
        broadcast(ok(success ? "hex_purchased" : "hex_failed",
                stateToMap(gameController.getGameState())));
    }

    // execute-turn
    private void handleExecuteTurn(WebSocketSession session, Map<String, Object> req) throws Exception {
        ensureReady();
        GameState state = gameController.getGameState();
        if (state.phase == GameState.Phase.SETUP) {
            sendToSession(session, err("Setup ยังไม่เสร็จ"));
            return;
        }
        String playerId = (String) req.get("playerId");
        var result = gameController.executeTurn(playerId);

        Map<String, Object> data = new HashMap<>();
        data.put("state",  stateToMap(gameController.getGameState()));
        data.put("isOver", result.isOver);
        data.put("winner", result.winner != null ? result.winner : "");

        // broadcast ให้ทั้งสองหน้าจอเห็นผลพร้อมกัน
        broadcast(ok(result.isOver ? "game_over" : "turn_executed", data));
    }

    // validate strategy
    private void handleValidate(WebSocketSession session, Map<String, Object> req) throws Exception {
        String strategy = (String) req.get("strategy");
        boolean valid = gameController.validateStrategy(strategy);
        sendToSession(session, ok("validated", Map.of("valid", valid)));
    }

    // ── Broadcast / Send ──────────────────────────────────────

    // ส่งให้ทุก session ที่เชื่อมต่ออยู่
    private void broadcast(Map<String, Object> msg) throws Exception {
        String json = mapper.writeValueAsString(msg);
        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen()) s.sendMessage(new TextMessage(json));
        }
    }

    // ส่งให้ session เดียว
    private void sendToSession(WebSocketSession session, Map<String, Object> msg) throws Exception {
        session.sendMessage(new TextMessage(mapper.writeValueAsString(msg)));
    }

    // ── Helpers ───────────────────────────────────────────────
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