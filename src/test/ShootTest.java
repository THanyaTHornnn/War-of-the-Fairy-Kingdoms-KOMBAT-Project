package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import core.*;

public class ShootTest {

    @Test
    void testShootDamagesOpponent() {
        // 1. สร้างเกมจำลอง (ใช้ config default หรือกำหนดเอง)
        Config config = Config.defaultConfig();
        GameLogic logic = new GameLogic(config, GameState.Mode.DUEL); // โหมดไม่สำคัญ

        // 2. สร้างผู้เล่น
        Player p1 = logic.getPlayer("p1");
        Player p2 = logic.getPlayer("p2");

        // 3. วาง minion ให้อยู่ติดกัน โดยไม่ต้องผ่าน spawn zone
        //    (ใช้ method ภายในหรือเพิ่ม method ช่วยใน GameLogic)
        Position pos1 = new Position(1, 1);
        Position pos2 = new Position(2, 1); // อยู่ติดกันในทิศ DOWN

        Minion attacker = Minion.create("A", "m1", p1, pos1, 100, 5);
        Minion target = Minion.create("B", "m2", p2, pos2, 100, 5);

        // ต้องเพิ่ม minion เข้าไปใน GameLogic โดยตรง (ถ้าไม่มี public method ให้ใช้ reflection หรือเพิ่ม setter ชั่วคราว)
        // สมมติว่ามี method `addMinionForTest` หรือใช้ reflection
        logic.getMinions().put(attacker.getId(), attacker);
        logic.getMinions().put(target.getId(), target);
        p1.addMinion(attacker);
        p2.addMinion(target);

        // 4. กำหนด budget ให้พอ
        p1.setBudget(1000);

        // 5. ทดสอบยิง
        boolean result = logic.shoot(attacker, Position.DOWN, 50);

        // 6. ตรวจสอบผล
        assertTrue(result);                     // ยิงสำเร็จ
        assertEquals(1000 - (50 + 1), p1.getBudgetFloor()); // หักเงินถูกต้อง (expenditure+1)
        assertTrue(target.getHp() < 100);        // HP ลดลง
        // คำนวณดาเมจ: max(1, 50 - 5) = 45, ดังนั้น HP เหลือ 100-45 = 55
        assertEquals(55, target.getHp());
    }

    @Test
    void testShootFailsWhenNoBudget() {
        Config config = Config.defaultConfig();
        GameLogic logic = new GameLogic(config, GameState.Mode.DUEL);

        Player p1 = logic.getPlayer("p1");
        Player p2 = logic.getPlayer("p2");

        Minion attacker = Minion.create("A", "m1", p1, new Position(1,1), 100, 0);
        Minion target   = Minion.create("A", "m2", p2, new Position(2,1), 100, 0);

        logic.getMinions().put(attacker.getId(), attacker);
        logic.getMinions().put(target.getId(), target);
        p1.addMinion(attacker);
        p2.addMinion(target);

        p1.setBudget(0); // ไม่มีเงิน

        boolean result = logic.shoot(attacker, Position.DOWN, 10);

        assertFalse(result);
        assertEquals(100, target.getHp()); // ไม่โดนดาเมจ
    }

    @Test
    void testShootNoTarget() {
        Config config = Config.defaultConfig();
        GameLogic logic = new GameLogic(config, GameState.Mode.DUEL);

        Player p1 = logic.getPlayer("p1");
        Minion attacker = Minion.create("A", "m1", p1, new Position(4,4), 100, 0);
        logic.getMinions().put(attacker.getId(), attacker);
        p1.addMinion(attacker);
        p1.setBudget(1000);

        boolean result = logic.shoot(attacker, Position.DOWN, 10);

        assertFalse(result); // ยิงช่องว่าง
    }

    @Test
    void testShootAlly() {
        Config config = Config.defaultConfig();
        GameLogic logic = new GameLogic(config, GameState.Mode.DUEL);

        Player p1 = logic.getPlayer("p1");
        Minion attacker = Minion.create("A", "m1", p1, new Position(1,1), 100, 0);
        Minion ally     = Minion.create("A", "m2", p1, new Position(2,1), 100, 0);
        logic.getMinions().put(attacker.getId(), attacker);
        logic.getMinions().put(ally.getId(), ally);
        p1.addMinion(attacker);
        p1.addMinion(ally);
        p1.setBudget(1000);

        boolean result = logic.shoot(attacker, Position.DOWN, 10);

        assertFalse(result);
        assertEquals(100, ally.getHp());
    }
}