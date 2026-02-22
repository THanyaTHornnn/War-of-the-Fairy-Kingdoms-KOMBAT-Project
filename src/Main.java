import controller.GameController;
import core.GameState;
import core.Minion;
import strategy.ast.Stmt;
import strategy.parser.Parser;
import strategy.parser.Tokenizer;

import java.util.*;

public class Main {
    static Scanner sc = new Scanner(System.in);
    static GameController gc = new GameController();
    static Map<String, Integer> kindDefense   = new LinkedHashMap<>();
    static Map<String, List<Stmt>> kindAst    = new LinkedHashMap<>();
    static final int MAX_TURNS = 69;

    public static void main(String[] args) throws Exception {
        // ── 1. Banner + Start ─────────────────────────────────
        printBanner();
        System.out.println("กด Enter เพื่อเริ่ม...");
        sc.nextLine();

        // ── 2. เลือก Mode ────────────────────────────────────
        System.out.println("\n╔══ เลือก Mode ══╗");
        System.out.println("║ 1. DUEL        ║  (2 ผู้เล่น)");
        System.out.println("║ 2. SOLITAIRE   ║  (1 ผู้เล่น vs BOT)");
        System.out.println("║ 3. AUTO        ║  (BOT vs BOT)");
        System.out.println("╚════════════════╝");
        System.out.print("เลือก (1-3): ");
        int modeChoice = readInt();
        GameState.Mode mode = switch (modeChoice) {
            case 2 -> GameState.Mode.SOLITAIRE;
            case 3 -> GameState.Mode.AUTO;
            default -> GameState.Mode.DUEL;
        };
        gc.createGame(null, mode);
        System.out.println("✓ Mode: " + mode + "\n");

        // ── 3. เลือกจำนวน Kind ───────────────────────────────
        System.out.print("จำนวน kind ตัวละคร (1-5): ");
        int numKinds = Math.max(1, Math.min(5, readInt()));

        for (int i = 1; i <= numKinds; i++) {
            System.out.println("\n── ตั้งค่า Kind ที่ " + i + " ──");
            System.out.print("ชื่อ kind: ");
            String name = sc.nextLine().trim();

            System.out.print("defense factor (1-10): ");
            int defense = readInt();

            // เช็ค strategy จนกว่าจะถูก
            List<Stmt> ast = null;
            while (ast == null) {
                System.out.println("ใส่ strategy (เช่น: move down):");
                String stratStr = sc.nextLine().trim();
                try {
                    ast = new Parser(new Tokenizer(stratStr).tokenize()).parseStrategy();
                    System.out.println("✓ Strategy ถูกต้อง!");
                } catch (Exception e) {
                    System.out.println("✗ Strategy ผิด syntax: " + e.getMessage());
                    System.out.println("กรุณาลองใหม่...");
                }
            }
            kindDefense.put(name, defense);
            kindAst.put(name, ast);
        }

        // แสดง kind ทั้งหมด
        System.out.println("\n╔══ Kind ที่เลือก ══════╗");
        kindDefense.forEach((k, d) ->
                System.out.printf("║ %-10s defense=%d ║%n", k, d));
        System.out.println("╚═══════════════════════╝");

        // ── 4. Setup Spawn ฟรี ────────────────────────────────
        System.out.println("\n── Setup Spawn ฟรี ──");
        System.out.println("P1 spawn zone: (1,1)(1,2)(1,3)(2,1)(2,2)");
        System.out.println("P2 spawn zone: (8,8)(8,7)(8,6)(7,8)(7,7)");

        spawnFree("p1");
        spawnFree("p2");

        // ── 5. เริ่มเกม ───────────────────────────────────────
        gc.startGame();
        System.out.println("\n╔══════════════════╗");
        System.out.println("║   เกมเริ่มแล้ว!   ║");
        System.out.println("╚══════════════════╝\n");

        // ── 6. Game Loop ──────────────────────────────────────
        gameLoop();
    }

    // ── Spawn ฟรี 1 ตัวก่อนเริ่ม ─────────────────────────────
    static void spawnFree(String playerId) {
        System.out.println("\n── " + playerId.toUpperCase() + " เลือก spawn ฟรี ──");
        boolean spawned = false;
        while (!spawned) {
            System.out.print("เลือก kind " + kindDefense.keySet() + ": ");
            String kind = sc.nextLine().trim();
            if (!kindDefense.containsKey(kind)) {
                System.out.println("✗ ไม่มี kind นี้");
                continue;
            }
            System.out.print("row: "); int row = readInt();
            System.out.print("col: "); int col = readInt();
            try {
                Minion m = gc.createMinion("Minion" + kind, playerId, row, col);
                spawned = gc.setupSpawn(playerId, m, kindAst.get(kind));
                if (spawned) System.out.println("✓ Spawn ฟรีสำเร็จ!");
                else System.out.println("✗ ตำแหน่งไม่ถูกต้อง ลองใหม่");
            } catch (Exception e) {
                System.out.println("✗ Error: " + e.getMessage());
            }
        }
    }

    // ── Game Loop ─────────────────────────────────────────────
    static void gameLoop() {
        while (true) {
            GameState snap = gc.getGameState();
            printBoard(snap);

            String current = snap.current;
            int turnsLeft  = MAX_TURNS - snap.turn + 1;
            long budget    = current.equals("p1") ?
                    snap.p1.getBudgetFloor() : snap.p2.getBudgetFloor();

            System.out.println("\n╔══ ตา " + snap.turn + "/" + MAX_TURNS +
                    " | " + current.toUpperCase() +
                    " | budget=" + budget +
                    " | เหลือ " + turnsLeft + " ตา ══╗");

            // ── ซื้อ hex ──────────────────────────────────────
            System.out.print("ซื้อ hex ไหม? (y/n): ");
            if (sc.nextLine().trim().equalsIgnoreCase("y")) {
                System.out.print("row: "); int row = readInt();
                System.out.print("col: "); int col = readInt();
                boolean ok = gc.purchaseHex(current, row, col);
                System.out.println(ok ? "✓ ซื้อสำเร็จ!" : "✗ ซื้อไม่ได้");
            }

            // ── Spawn minion ──────────────────────────────────
            System.out.print("Spawn minion ไหม? (y/n): ");
            if (sc.nextLine().trim().equalsIgnoreCase("y")) {
                System.out.print("เลือก kind " + kindDefense.keySet() + ": ");
                String kind = sc.nextLine().trim();
                System.out.print("row: "); int row = readInt();
                System.out.print("col: "); int col = readInt();
                try {
                    Minion m = gc.createMinion("Minion" + kind, current, row, col);
                    boolean ok = gc.spawnMinion(current, m, kindAst.get(kind));
                    System.out.println(ok ? "✓ Spawn สำเร็จ!" : "✗ Spawn ไม่ได้");
                } catch (Exception e) {
                    System.out.println("✗ Error: " + e.getMessage());
                }
            }

            // ── Execute turn ──────────────────────────────────
            System.out.println("── Minions execute strategies... ──");
            GameController.TurnResult result = gc.executeTurn(current);

            // ── จบเกม ─────────────────────────────────────────
            if (result.isOver) {
                printBoard(gc.getGameState());
                System.out.println("\n╔══════════════════╗");
                System.out.println("║    จบเกม!         ║");
                System.out.println("╠══════════════════╣");
                System.out.println("║ ผู้ชนะ: " + (result.winner != null ? result.winner : "TIE"));
                System.out.println("║ เหตุผล: " + result.reason);
                System.out.println("╚══════════════════╝");
                break;
            }
        }
    }

    // ── Print Board ───────────────────────────────────────────
    static void printBoard(GameState snap) {
        System.out.println("\n   1 2 3 4 5 6 7 8");
        System.out.println("  ─────────────────");
        char[][] board = new char[9][9];
        for (int r = 1; r <= 8; r++)
            for (int c = 1; c <= 8; c++)
                board[r][c] = '.';

        for (Minion m : snap.minions.values()) {
            if (m.isAlive()) {
                int r = m.getPosition().getRow();
                int c = m.getPosition().getCol();
                board[r][c] = m.getOwner().getId().equals("p1") ? '1' : '2';
            }
        }

        for (int r = 1; r <= 8; r++) {
            System.out.print(r + " | ");
            for (int c = 1; c <= 8; c++)
                System.out.print(board[r][c] + " ");
            System.out.println();
        }

        System.out.println("  ─────────────────");
        System.out.printf("P1: budget=%-8d minions=%d hp=%d%n",
                snap.p1.getBudgetFloor(),
                snap.p1.getMinionCount(),
                snap.p1.getTotalHP());
        System.out.printf("P2: budget=%-8d minions=%d hp=%d%n",
                snap.p2.getBudgetFloor(),
                snap.p2.getMinionCount(),
                snap.p2.getTotalHP());
    }

    // ── Banner ────────────────────────────────────────────────
    static void printBanner() {
        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║                                  ║");
        System.out.println("║   K O M B A T                    ║");
        System.out.println("║   Kickstart Offense with         ║");
        System.out.println("║   Minion's Best in an            ║");
        System.out.println("║   Amicable Territory             ║");
        System.out.println("║                                  ║");
        System.out.println("╚══════════════════════════════════╝");
    }

    // ── Read int helper ───────────────────────────────────────
    static int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (Exception e) {
            return 0;
        }
    }
}