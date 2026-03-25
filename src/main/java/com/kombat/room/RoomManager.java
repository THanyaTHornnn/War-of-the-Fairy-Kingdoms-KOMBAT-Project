package com.kombat.room;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * จัดการห้องทั้งหมดในระบบ
 * Singleton (Spring @Component) — มีตัวเดียวทั้งแอป
 */
@Component
public class RoomManager {

    // key = roomCode (4 หลัก) → GameRoom
    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private final Random random = new Random();

    // ── สร้างห้องใหม่ ─────────────────────────────────────────
    public GameRoom createRoom() {
        String code;
        // วนจนได้ code ที่ไม่ซ้ำ
        do {
            code = String.format("%04d", random.nextInt(10000));
        } while (rooms.containsKey(code));

        GameRoom room = new GameRoom(code);
        rooms.put(code, room);
        System.out.println("🏠 Room created: " + code);
        return room;
    }

    // ── ค้นหาห้องจากรหัส ─────────────────────────────────────
    public GameRoom getRoom(String roomCode) {
        return rooms.get(roomCode);
    }

    // ── หาห้องจาก sessionId ──────────────────────────────────
    public GameRoom getRoomBySessionId(String sessionId) {
        for (GameRoom room : rooms.values()) {
            if (room.sessionToPlayer.containsKey(sessionId)) {
                return room;
            }
        }
        return null;
    }

    // ── ลบห้องถ้าว่างเปล่า ───────────────────────────────────
    public void cleanupIfEmpty(String roomCode) {
        GameRoom room = rooms.get(roomCode);
        if (room != null && room.isEmpty()) {
            rooms.remove(roomCode);
            System.out.println("🗑️ Room removed: " + roomCode);
        }
    }

    // ── ตรวจว่าห้องมีอยู่ไหม ─────────────────────────────────
    public boolean roomExists(String roomCode) {
        return rooms.containsKey(roomCode);
    }

    // ── จำนวนห้องทั้งหมด (debug) ─────────────────────────────
    public int roomCount() {
        return rooms.size();
    }
}