package com.kombat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kombat.controller.GameController;
import com.kombat.room.*;
import com.kombat.strategy.ast.Stmt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.kombat.core.*;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private RoomManager roomManager;

    // ─────────────────────────────────────────────────────────
    // Connection lifecycle
    // ─────────────────────────────────────────────────────────

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("✅ WebSocket connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> req = mapper.readValue(message.getPayload(), Map.class);
        String action = (String) req.get("action");
        System.out.println("📨 action=" + action + " session=" + session.getId());

        try {
            // action ที่ไม่ต้องการห้องก่อน
            switch (action) {
                case "create-room" -> handleCreateRoom(session, req);
                case "join-room"   -> handleJoinRoom(session, req);
                default -> {
                    // action อื่น ๆ ต้องอยู่ในห้องก่อน
                    GameRoom room = roomManager.getRoomBySessionId(session.getId());
                    if (room == null) {
                        sendToSession(session, err("ยังไม่ได้เข้าห้อง กรุณาส่ง join-room ก่อน"));
                        return;
                    }
                    handleRoomAction(session, room, action, req);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            sendToSession(session, err(e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        GameRoom room = roomManager.getRoomBySessionId(session.getId());
        if (room == null) return;

        String playerId = room.removeSession(session);
        if (playerId == null) return;

        System.out.println("❌ Disconnected: " + session.getId() + " was " + playerId + " in room " + room.roomCode);

        if ("p1".equals(playerId)) {
            room.p1Joined = false;
            room.p1SetupSpawned = false;

            // เลื่อน P2 → P1 ถ้ามี
            WebSocketSession promoted = room.promoteP2ToP1();
            if (promoted != null) {
                try {
                    sendToSession(promoted, ok("joined", Map.of(
                            "playerId", "p1",
                            "roomCode", room.roomCode
                    )));
                    System.out.println("⬆️ P2 promoted to P1 in room " + room.roomCode);
                } catch (Exception e) {
                    System.out.println("⚠️ Failed to notify promoted P1: " + e.getMessage());
                }
            }

            // แจ้งทุกคนว่า P1 ออก
            broadcastRoom(room, ok("player_left", Map.of("playerId", "p1")));

        } else if ("p2".equals(playerId)) {
            room.p2Joined = false;
            room.p2SetupSpawned = false;
            broadcastRoom(room, ok("player_left", Map.of("playerId", "p2")));

        }
        // spectator ออก → ไม่ broadcast

        // ลบห้องถ้าว่าง
        roomManager.cleanupIfEmpty(room.roomCode);
    }

    // ─────────────────────────────────────────────────────────
    // action: create-room (P1 สร้างห้อง)
    // ─────────────────────────────────────────────────────────
    private void handleCreateRoom(WebSocketSession session, Map<String, Object> req) throws Exception {
        // สร้างห้องใหม่
        GameRoom room = roomManager.createRoom();

        // assign P1
        String requestedId = (String) req.get("requestedId");
        String playerId = room.assignPlayer(session, requestedId);

        // ตอบกลับ: roomCode + playerId
        sendToSession(session, ok("room_created", Map.of(
                "roomCode", room.roomCode,
                "playerId", playerId
        )));
        System.out.println("🏠 Room " + room.roomCode + " created by " + playerId);
    }

    // ─────────────────────────────────────────────────────────
    // action: join-room (P2 / SPECTATOR เข้าห้อง)
    // ─────────────────────────────────────────────────────────
    private void handleJoinRoom(WebSocketSession session, Map<String, Object> req) throws Exception {
        String roomCode = (String) req.get("roomCode");
        if (roomCode == null || roomCode.isBlank()) {
            sendToSession(session, err("กรุณาระบุรหัสห้อง"));
            return;
        }

        GameRoom room = roomManager.getRoom(roomCode.trim());
        if (room == null) {
            sendToSession(session, err("ไม่พบห้อง: " + roomCode));
            return;
        }

        String requestedId = (String) req.get("requestedId");
        String playerId = room.assignPlayer(session, requestedId);
        String displayId = playerId.startsWith("spectator") ? "spectator" : playerId;

        // แจ้ง session ที่เพิ่งเข้า
        sendToSession(session, ok("room_joined", Map.of(
                "playerId", displayId,
                "roomCode", room.roomCode
        )));
        System.out.println("👤 " + displayId + " joined room " + room.roomCode);
        if (room.gameReady) {
            sendToSession(session, ok("state", stateToMap(room.gameController.getGameState())));
        }


        // ✅ broadcast ให้ทุกคนในห้องรู้ว่ามีคนเข้ามา (P1 จะได้รู้ว่า P2 มาแล้ว)
        // ส่งเฉพาะ session อื่น (ไม่ส่งกลับตัวเอง)
        for (Map.Entry<String, WebSocketSession> entry : room.sessions.entrySet()) {
            WebSocketSession other = entry.getValue();
            if (other.isOpen() && !other.getId().equals(session.getId())) {
                Map<String, Object> notifyData = new HashMap<>();
                notifyData.put("playerId", displayId);
                notifyData.put("roomCode", room.roomCode);
                // ✅ เพิ่ม state ให้ P1 ด้วย
                if (room.gameReady) {
                    notifyData.put("state", stateToMap(room.gameController.getGameState()));
                }
                sendToSession(other, ok("player_joined", notifyData));
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // action ภายในห้อง
    // ─────────────────────────────────────────────────────────
    private void handleRoomAction(WebSocketSession session, GameRoom room,
                                  String action, Map<String, Object> req) throws Exception {
        switch (action) {
            case "create"       -> handleCreate(session, room, req);
            case "spawn"        -> handleSpawn(session, room, req);
            case "reset"        -> handleReset(session);
            case "purchase-hex" -> handlePurchaseHex(session, room, req);
            case "execute-turn" -> handleExecuteTurn(session, room, req);
            case "validate"     -> handleValidate(session, room, req);
            case "get-configs"  -> handleGetConfigs(session, room);
            case "confirm-game" -> handleConfirmGame(session, room);
            case "cancel-game"  -> handleCancelGame(session, room);
            case "state"        -> sendToSession(session,
                    ok("state", stateToMap(room.gameController.getGameState())));
            default -> sendToSession(session, err("Unknown action: " + action));
        }
    }
    private void handleReset(WebSocketSession session) throws Exception {
        GameRoom room = roomManager.getRoomBySessionId(session.getId());
        if (room == null) return;

        room.gameController.resetGame(GameState.Mode.DUEL);
        room.p1SetupSpawned = false;
        room.p2SetupSpawned = false;
        room.p1Joined = false;
        room.p2Joined = false;
        room.gameReady = false;
        room.gameMode = "PVP";
        room.minionConfigs = new ArrayList<>();
        System.out.println("🔄 Game reset room=" + room.roomCode);
        broadcastRoom(room, ok("reset", Map.of()));
    }

    // ─────────────────────────────────────────────────────────
    // action: create (P1 สร้างเกม + ส่ง minionConfigs)
    // ─────────────────────────────────────────────────────────
    private void handleCreate(WebSocketSession session, GameRoom room,
                              Map<String, Object> req) throws Exception {
        String modeStr = (String) req.getOrDefault("mode", "PVP");
        room.gameMode = modeStr.toUpperCase();

        GameState.Mode logicMode = switch (room.gameMode) {
            case "BVB" -> GameState.Mode.AUTO;
            case "PVB" -> GameState.Mode.SOLITAIRE;
            default    -> GameState.Mode.DUEL;
        };

        room.gameController = new GameController();
        room.gameController.createGame(null, logicMode);
        room.p1SetupSpawned = false;
        room.p2SetupSpawned = false;
        room.gameReady = true;

        Object configs = req.get("minionConfigs");
        if (configs instanceof List) {
            room.minionConfigs = (List<Map<String, Object>>) configs;
            System.out.println("📦 room=" + room.roomCode + " minionConfigs=" + room.minionConfigs.size());

            Map<String, Integer> kindDefense = new HashMap<>();
            Map<String, List<Stmt>> kindAst = new HashMap<>();
            for (Map<String, Object> cfg : room.minionConfigs) {
                String minionId = (String) cfg.get("minionId");
                int defense = cfg.containsKey("defense") ? ((Number) cfg.get("defense")).intValue() : 10;
                String strategy = (String) cfg.getOrDefault("strategy", "done");
                kindDefense.put(minionId, defense);
                kindAst.put(minionId, room.gameController.parseStrategy(strategy));
            }
            room.gameController.setKinds(kindDefense, kindAst);
        }

        System.out.println("🎮 Room " + room.roomCode + " game created mode=" + room.gameMode);
        broadcastRoom(room, ok("created", stateToMap(room.gameController.getGameState())));

        // ✅ broadcast configs แยกต่างหาก เพื่อให้ P2 รับไปแสดง confirm screen
        if (!room.minionConfigs.isEmpty()) {
            broadcastRoom(room, ok("configs", Map.of("minionConfigs", room.minionConfigs)));
            System.out.println("📤 configs broadcast to room " + room.roomCode);
        }

        // PVB/BVB: auto setup bot ทันที ไม่ต้องรอ P2
        if ("PVB".equals(room.gameMode) || "BVB".equals(room.gameMode)) {
            autoSetupAll(room);
        }
    }

    // ─────────────────────────────────────────────────────────
    // action: confirm-game (P2 ยืนยัน config → เริ่มเกม)
    // ─────────────────────────────────────────────────────────
    private void handleConfirmGame(WebSocketSession session, GameRoom room) throws Exception {
        broadcastRoom(room, ok("p2_confirmed", Map.of()));
    }

    // ─────────────────────────────────────────────────────────
    // action: cancel-game (P2 ปฏิเสธ → P1 กลับไป setup)
    // ─────────────────────────────────────────────────────────
    private void handleCancelGame(WebSocketSession session, GameRoom room) throws Exception {
        broadcastRoom(room, ok("game_cancelled", Map.of()));
    }

    // ─────────────────────────────────────────────────────────
    // action: get-configs
    // ─────────────────────────────────────────────────────────
    private void handleGetConfigs(WebSocketSession session, GameRoom room) throws Exception {
        sendToSession(session, ok("configs", Map.of("minionConfigs", room.minionConfigs)));
    }

    // ─────────────────────────────────────────────────────────
    // action: spawn
    // ─────────────────────────────────────────────────────────
//    private void handleSpawn(WebSocketSession session, GameRoom room,
//                             Map<String, Object> req) throws Exception {
//        ensureReady(room);
//        Object pidObj = req.get("playerId");
//        String playerId = pidObj != null ? pidObj.toString() : "p1";
//
//        String minionKind = (String) req.getOrDefault("kindName",
//                req.getOrDefault("minionKind", "verdant")).toString();
//        int row     = ((Number) req.get("row")).intValue();
//        int col     = ((Number) req.get("col")).intValue();
//        int defense = req.containsKey("defense") ? ((Number) req.get("defense")).intValue() : 10;
//        String strategySource = (String) req.getOrDefault("strategy", "done");
//
//        List<Stmt> ast = room.gameController.parseStrategy(strategySource);
//        Minion m = room.gameController.createMinion(minionKind, playerId, row, col, defense);
//
//        boolean spawnOk;
//        boolean isSetupPhase = room.gameController.getGameState().phase == GameState.Phase.SETUP;
//
//        if (isSetupPhase) {
//            // BVB → ไม่รับ manual spawn
//            if ("BVB".equals(room.gameMode)) {
//                sendToSession(session, err("BVB mode: spawn is handled automatically"));
//                return;
//            }
//
//            // ✅ PVP / PVB → spawn ปกติ
//            spawnOk = room.gameController.setupSpawn(playerId, m, ast);
//
//            if (spawnOk) {
//                if ("p1".equals(playerId)) room.p1SetupSpawned = true;
//                if ("p2".equals(playerId)) room.p2SetupSpawned = true;
//            }
//
//            // PVP → startGame เมื่อทั้งคู่ spawn เองเสร็จ
//            if ("PVP".equals(room.gameMode) && room.p1SetupSpawned && room.p2SetupSpawned) {
//                room.gameController.startGame();
//                System.out.println("🚀 PVP Room " + room.roomCode + ": PLAYING");
//            }
//
//        } else {
//            spawnOk = room.gameController.spawnMinion(playerId, m, ast);
//        }
//
//        sendOrBroadcastRoom(session, room,
//                ok(spawnOk ? "spawned" : "spawn_failed",
//                        stateToMap(room.gameController.getGameState())));
//    }
    private void handleSpawn(WebSocketSession session, GameRoom room,
                             Map<String, Object> req) throws Exception {
        ensureReady(room);
        Object pidObj = req.get("playerId");
        String playerId = pidObj != null ? pidObj.toString() : "p1";

        String minionKind = (String) req.getOrDefault("kindName",
                req.getOrDefault("minionKind", "verdant")).toString();
        int row     = ((Number) req.get("row")).intValue();
        int col     = ((Number) req.get("col")).intValue();
        int defense = req.containsKey("defense") ? ((Number) req.get("defense")).intValue() : 10;
        String strategySource = (String) req.getOrDefault("strategy", "done");

        List<Stmt> ast = room.gameController.parseStrategy(strategySource);
        Minion m = room.gameController.createMinion(minionKind, playerId, row, col, defense);

        boolean spawnOk;
        boolean isSetupPhase = room.gameController.getGameState().phase == GameState.Phase.SETUP;

        if (isSetupPhase) {
            // ========== BVB: ไม่รับ manual spawn ==========
            if ("BVB".equals(room.gameMode)) {
                sendToSession(session, err("BVB mode: spawn is handled automatically"));
                return;
            }

            // ========== PVB: P1 spawn ได้, P2 spawn อัตโนมัติ ==========
            if ("PVB".equals(room.gameMode) && "p2".equals(playerId)) {
                sendToSession(session, err("PVB mode: P2 is bot, spawn automatically"));
                return;
            }

            // PVP และ PVB (P1) spawn ปกติ
            spawnOk = room.gameController.setupSpawn(playerId, m, ast);

            if (spawnOk) {
                if ("p1".equals(playerId)) room.p1SetupSpawned = true;
                if ("p2".equals(playerId)) room.p2SetupSpawned = true;
            }

            // PVP: เริ่มเกมเมื่อทั้งคู่ spawn เสร็จ
            if ("PVP".equals(room.gameMode) && room.p1SetupSpawned && room.p2SetupSpawned) {
                room.gameController.startGame();
                System.out.println("🚀 PVP Room " + room.roomCode + ": PLAYING");
                broadcastRoom(room, ok("game_started", stateToMap(room.gameController.getGameState())));
            }

            // PVB: เริ่มเกมเมื่อ P1 spawn เสร็จ (P2 spawn อัตโนมัติแล้ว)
            if ("PVB".equals(room.gameMode) && room.p1SetupSpawned && room.p2SetupSpawned) {
                room.gameController.startGame();
                System.out.println("🚀 PVB Room " + room.roomCode + ": PLAYING");
                broadcastRoom(room, ok("game_started", stateToMap(room.gameController.getGameState())));
            }

        } else {
            // PLAYING phase: spawn ปกติ
            spawnOk = room.gameController.spawnMinion(playerId, m, ast);
        }

        sendOrBroadcastRoom(session, room,
                ok(spawnOk ? "spawned" : "spawn_failed",
                        stateToMap(room.gameController.getGameState())));
    }
    // ─────────────────────────────────────────────────────────
    // action: purchase-hex
    // ─────────────────────────────────────────────────────────
    private void handlePurchaseHex(WebSocketSession session, GameRoom room,
                                   Map<String, Object> req) throws Exception {
        ensureReady(room);
        Object pidObj = req.get("playerId");
        String playerId = pidObj != null ? pidObj.toString() : "p1";
        int row = ((Number) req.get("row")).intValue();
        int col = ((Number) req.get("col")).intValue();
        boolean success = room.gameController.purchaseHex(playerId, row, col);
        sendOrBroadcastRoom(session, room,
                ok(success ? "hex_purchased" : "hex_failed",
                        stateToMap(room.gameController.getGameState())));
    }

    // ─────────────────────────────────────────────────────────
    // action: execute-turn
    // ─────────────────────────────────────────────────────────
    private void handleExecuteTurn(WebSocketSession session, GameRoom room,
                                   Map<String, Object> req) throws Exception {
        ensureReady(room);
        GameState state = room.gameController.getGameState();
        if (state.phase == GameState.Phase.SETUP) {
            sendToSession(session, err("Setup ยังไม่เสร็จ"));
            return;
        }

        Object pidObj = req.get("playerId");
        String playerId = pidObj != null ? pidObj.toString() : "p1";
        var result = room.gameController.executeTurn(playerId);

        Map<String, Object> data = new HashMap<>();
        data.put("state",  stateToMap(room.gameController.getGameState()));
        data.put("isOver", result.isOver);
        data.put("winner", result.winner != null ? result.winner : "");
        sendOrBroadcastRoom(session, room,
                ok(result.isOver ? "game_over" : "turn_executed", data));

        // PVB: หลัง P1 end turn → bot P2 รันอัตโนมัติ
        if (!result.isOver && "PVB".equals(room.gameMode) && "p1".equals(playerId)) {
            Thread.sleep(800);
            var botResult = room.gameController.executeTurn("p2");
            Map<String, Object> botData = new HashMap<>();
            botData.put("state",  stateToMap(room.gameController.getGameState()));
            botData.put("isOver", botResult.isOver);
            botData.put("winner", botResult.winner != null ? botResult.winner : "");
            sendOrBroadcastRoom(session, room,
                    ok(botResult.isOver ? "game_over" : "turn_executed", botData));
        }
    }

    // ─────────────────────────────────────────────────────────
    // action: validate
    // ─────────────────────────────────────────────────────────
    private void handleValidate(WebSocketSession session, GameRoom room,
                                Map<String, Object> req) throws Exception {
        String strategy = (String) req.get("strategy");
        try {
            boolean valid = room.gameController.validateStrategy(strategy);
            sendToSession(session, ok("validated", Map.of("valid", valid)));
        } catch (Exception e) {
            sendToSession(session, ok("validated", Map.of("valid", false)));
        }
    }

    // ─────────────────────────────────────────────────────────
    // Auto setup (PVB / BVB)
    // ─────────────────────────────────────────────────────────
    private void autoSetupAll(GameRoom room) throws Exception {
        if ("BVB".equals(room.gameMode)) {
            // BVB → spawn โดย startGame() เท่านั้น
            autoSetupSpawn(room,"p1");
            autoSetupSpawn(room,"p2");
            room.gameController.startGame();
            room.p1SetupSpawned = true;
            room.p2SetupSpawned = true;
            System.out.println("🤖 BVB room=" + room.roomCode + ": both spawned → PLAYING");
            broadcastRoom(room, ok("spawned", stateToMap(room.gameController.getGameState())));
            runBvbGame(room);
        } else if ("PVB".equals(room.gameMode)) {
            // PVB → spawn bot แค่ P2
            autoSetupSpawn(room, "p2");
            room.p2SetupSpawned = true;
            System.out.println("🤖 PVB room=" + room.roomCode + ": P2 bot spawned");
            broadcastRoom(room, ok("bot_spawned", stateToMap(room.gameController.getGameState())));
        }
        // ✅ PVP ไม่ต้อง auto spawn เลย
    }
//    private void autoSetupSpawn(GameRoom room, String playerId) {
//        if (room.minionConfigs.isEmpty()) return;
//        GameState state = room.gameController.getGameState();
//        Player player = "p1".equals(playerId) ? state.p1 : state.p2;
//        if (player == null) return;
//
//        List<String> spawnHexes = new ArrayList<>(player.getSpawnableHexes());
//        int idx = 0;
//        for (Map<String, Object> cfg : room.minionConfigs) {
//            if (idx >= spawnHexes.size()) break;
//
//            String minionId = (String) cfg.get("minionId");
//            int defense = cfg.containsKey("defense")
//                    ? ((Number) cfg.get("defense")).intValue() : 10;
//            String strategy = (String) cfg.getOrDefault("strategy", "done");
//
//            // ✅ รองรับทั้ง "row,col" และ "row-col"
//            String hexKey = spawnHexes.get(idx);
//            String[] parts = hexKey.contains(",")
//                    ? hexKey.split(",")
//                    : hexKey.split("-");
//
//            try {
//                int row = Integer.parseInt(parts[0].trim());
//                int col = Integer.parseInt(parts[1].trim());
//
//                Minion m = room.gameController.createMinion(minionId, playerId, row, col, defense);
//                List<Stmt> ast = room.gameController.parseStrategy(strategy);
//                boolean ok = room.gameController.setupSpawn(playerId, m, ast);
//                System.out.println("🤖 autoSpawn " + playerId + " " + minionId
//                        + " at (" + row + "," + col + ") → " + ok);
//            } catch (Exception e) {
//                System.out.println("⚠️ autoSetupSpawn parse error: hexKey='"
//                        + hexKey + "' → " + e.getMessage());
//            }
//
//            idx++;
//        }
//    }
private void autoSetupSpawn(GameRoom room, String playerId) {
    if (room.minionConfigs.isEmpty()) return;
    GameState state = room.gameController.getGameState();
    Player player = "p1".equals(playerId) ? state.p1 : state.p2;
    if (player == null) return;

    List<String> spawnHexes = new ArrayList<>(player.getSpawnableHexes());
    if (spawnHexes.isEmpty()) return;

    // ✅ เอา for loop ออก → spawn แค่ตัวแรกตัวเดียว
    Map<String, Object> cfg = room.minionConfigs.get(0);
    String minionId = (String) cfg.get("minionId");
    int defense = cfg.containsKey("defense")
            ? ((Number) cfg.get("defense")).intValue() : 10;
    String strategy = (String) cfg.getOrDefault("strategy", "done");

    String hexKey = spawnHexes.get(0);
    String[] parts = hexKey.contains(",")
            ? hexKey.split(",")
            : hexKey.split("-");

    try {
        int row = Integer.parseInt(parts[0].trim());
        int col = Integer.parseInt(parts[1].trim());

        Minion m = room.gameController.createMinion(minionId, playerId, row, col, defense);
        List<Stmt> ast = room.gameController.parseStrategy(strategy);
        boolean ok = room.gameController.setupSpawn(playerId, m, ast);
        System.out.println("🤖 autoSpawn " + playerId + " " + minionId
                + " at (" + row + "," + col + ") → " + ok);
    } catch (Exception e) {
        System.out.println("⚠️ autoSetupSpawn parse error: hexKey='"
                + hexKey + "' → " + e.getMessage());
    }
}

    private void runBvbGame(GameRoom room) {
        new Thread(() -> {
            try {
                while (!room.gameController.isGameOver()) {
                    Thread.sleep(1500);
                    String current = room.gameController.getGameState().current;
                    room.gameController.executeTurn(current);
                    GameState snap = room.gameController.getGameState();
                    Map<String, Object> data = new HashMap<>();
                    data.put("state", stateToMap(snap));
                    data.put("isOver", snap.phase == GameState.Phase.ENDED);
                    data.put("winner", snap.winner != null ? snap.winner : "");
                    String event = snap.phase == GameState.Phase.ENDED ? "game_over" : "turn_executed";
                    broadcastRoom(room, ok(event, data));
                }
            } catch (Exception e) {
                System.out.println("❌ BVB error room=" + room.roomCode + ": " + e.getMessage());
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────
    // Utility helpers
    // ─────────────────────────────────────────────────────────
    private void ensureReady(GameRoom room) throws Exception {
        if (!room.gameReady) {
            room.gameController = new GameController();
            room.gameController.createGame(null, GameState.Mode.DUEL);
            room.p1SetupSpawned = false;
            room.p2SetupSpawned = false;
            room.gameReady = true;
        }
    }

    private void sendOrBroadcastRoom(WebSocketSession sender, GameRoom room,
                                     Map<String, Object> msg) throws Exception {
        String json = mapper.writeValueAsString(msg);
        if (sender.isOpen()) sender.sendMessage(new TextMessage(json));
        for (WebSocketSession s : room.sessions.values()) {
            if (s.isOpen() && !s.getId().equals(sender.getId())) {
                s.sendMessage(new TextMessage(json));
            }
        }
    }

    private void broadcastRoom(GameRoom room, Map<String, Object> msg) {
        try {
            room.broadcast(mapper.writeValueAsString(msg));
        } catch (Exception e) {
            System.out.println("⚠️ broadcastRoom error: " + e.getMessage());
        }
    }

    private void sendToSession(WebSocketSession session, Map<String, Object> msg) throws Exception {
        session.sendMessage(new TextMessage(mapper.writeValueAsString(msg)));
    }

    // ─────────────────────────────────────────────────────────
    // State serialization
    // ─────────────────────────────────────────────────────────
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

        List<String> p1Hexes = new ArrayList<>(s.p1.getSpawnableHexes());
        List<String> p2Hexes = new ArrayList<>(s.p2.getSpawnableHexes());
        map.put("p1SpawnableHexes", p1Hexes);
        map.put("p2SpawnableHexes", p2Hexes);
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