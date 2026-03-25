package com.kombat.room;

import com.kombat.controller.GameController;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * เก็บ state ของห้องเกม 1 ห้อง
 * แต่ละห้องมี gameController, sessions, และ flags ของตัวเอง
 */
public class GameRoom {

    // ── ข้อมูลห้อง ──────────────────────────────────────────
    public final String roomCode;           // รหัส 4 หลัก เช่น "1234"
    public String gameMode = "PVP";         // PVP / PVB / BVB

    // ── Sessions ─────────────────────────────────────────────
    // key = playerId ("p1", "p2", "spectator-xxxxxx")
    public final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    // key = session.getId() → playerId
    public final Map<String, String> sessionToPlayer = new ConcurrentHashMap<>();

    // ── Game state flags ─────────────────────────────────────
    public GameController gameController = new GameController();
    public boolean gameReady       = false;
    public boolean p1SetupSpawned  = false;
    public boolean p2SetupSpawned  = false;
    public boolean p1Joined        = false;
    public boolean p2Joined        = false;

    // config ที่ P1 ตั้งไว้ (ส่งให้ P2 ยืนยัน)
    public List<Map<String, Object>> minionConfigs = new ArrayList<>();

    public GameRoom(String roomCode) {
        this.roomCode = roomCode;
    }

    // ── Helper: หา session จาก playerId ──────────────────────
    public WebSocketSession getSession(String playerId) {
        return sessions.get(playerId);
    }

    // ── Helper: ตรวจว่าห้องมีคนอยู่ไหม ──────────────────────
    public boolean isEmpty() {
        return sessions.isEmpty();
    }

    // ── Helper: นับจำนวน player จริง (ไม่นับ spectator) ─────
    public int playerCount() {
        int count = 0;
        if (p1Joined && sessions.containsKey("p1") && sessions.get("p1").isOpen()) count++;
        if (p2Joined && sessions.containsKey("p2") && sessions.get("p2").isOpen()) count++;
        return count;
    }

    // ── Helper: broadcast ไปทุก session ในห้องนี้ ────────────
    public void broadcast(String json) {
        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(new org.springframework.web.socket.TextMessage(json));
                } catch (Exception e) {
                    System.out.println("⚠️ broadcast error: " + e.getMessage());
                }
            }
        }
    }

    // ── Helper: กำหนด playerId ให้ session ใหม่ ──────────────
    // คืนค่า playerId ที่ได้รับ ("p1", "p2", หรือ "spectator-xxxxxx")
    public String assignPlayer(WebSocketSession session, String requestedId) {
        // ถ้าขอ ID เดิม และ slot นั้นว่าง → คืนให้เลย (กรณี refresh)
        if ("p1".equals(requestedId)) {
            boolean p1Empty = !sessions.containsKey("p1") || !sessions.get("p1").isOpen();
            if (p1Empty) {
                p1Joined = true;
                register(session, "p1");
                return "p1";
            }
        }
        if ("p2".equals(requestedId)) {
            boolean p2Empty = !sessions.containsKey("p2") || !sessions.get("p2").isOpen();
            if (p2Empty) {
                p2Joined = true;
                register(session, "p2");
                return "p2";
            }
        }

        // assign ตาม slot ว่าง
        if (!p1Joined || !sessions.containsKey("p1") || !sessions.get("p1").isOpen()) {
            p1Joined = true;
            register(session, "p1");
            return "p1";
        }
        if (!p2Joined || !sessions.containsKey("p2") || !sessions.get("p2").isOpen()) {
            p2Joined = true;
            register(session, "p2");
            return "p2";
        }

        // เต็มแล้ว → spectator (unique key เพื่อให้หลายคนดูพร้อมกันได้)
        String spectatorId = "spectator-" + session.getId().substring(0, 6);
        register(session, spectatorId);
        return spectatorId;
    }

    // ── Helper: ลบ session ออกจากห้อง ────────────────────────
    // คืนค่า playerId ที่ถูกลบ (null ถ้าไม่เจอ)
    public String removeSession(WebSocketSession session) {
        String playerId = sessionToPlayer.remove(session.getId());
        if (playerId == null) return null;
        sessions.remove(playerId);
        return playerId;
    }

    // ── Helper: เลื่อน P2 → P1 เมื่อ P1 ออก ─────────────────
    // คืน session ของ P2 เดิม (ที่กลายเป็น P1) หรือ null ถ้าไม่มี P2
    public WebSocketSession promoteP2ToP1() {
        WebSocketSession p2Session = sessions.get("p2");
        if (p2Session == null || !p2Session.isOpen()) return null;

        sessions.remove("p2");
        sessions.put("p1", p2Session);
        sessionToPlayer.put(p2Session.getId(), "p1");

        p1Joined = true;
        p2Joined = false;
        p2SetupSpawned = false;

        return p2Session;
    }

    // ── Private ───────────────────────────────────────────────
    private void register(WebSocketSession session, String playerId) {
        sessions.put(playerId, session);
        sessionToPlayer.put(session.getId(), playerId);
    }
}