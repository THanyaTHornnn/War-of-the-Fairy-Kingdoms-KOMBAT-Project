//import controller.GameController;
//import core.GameState;
//import core.Minion;
//import core.Position;
//import strategy.ast.Stmt;
//import strategy.parser.Parser;
//import strategy.parser.Tokenizer;
//
//import java.util.*;
//
//public class Rungame {
//    static Scanner sc = new Scanner(System.in);
//    static GameController gc = new GameController();
//    static Map<String, Integer> kindDefense = new LinkedHashMap<>();
//    static Map<String, List<Stmt>> kindAst  = new LinkedHashMap<>();
//    static final int MAX_TURNS = 69;
//
//    public static void main(String[] args) throws Exception {
//        printBanner();
//        System.out.println("กด Enter เพื่อเริ่ม...");
//        sc.nextLine();
//
//        // ── เลือก Mode ────────────────────────────────────────
//        System.out.println("\n╔══ เลือก Mode ══╗");
//        System.out.println("║ 1. DUEL        ║  (2 ผู้เล่น)");
//        System.out.println("║ 2. SOLITAIRE   ║  (1 ผู้เล่น vs BOT)");
//        System.out.println("║ 3. AUTO        ║  (BOT vs BOT)");
//        System.out.println("╚════════════════╝");
//        System.out.print("เลือก (1-3): ");
//        int modeChoice = readInt();
//        GameState.Mode mode = switch (modeChoice) {
//            case 2 -> GameState.Mode.SOLITAIRE;
//            case 3 -> GameState.Mode.AUTO;
//            default -> GameState.Mode.DUEL;
//        };
//        gc.createGame(null, mode);
//        System.out.println("✓ Mode: " + mode + "\n");
//
//        // ── ตั้งค่า Kind ──────────────────────────────────────
//        System.out.print("จำนวน kind ตัวละคร (1-5): ");
//        int numKinds = Math.max(1, Math.min(5, readInt()));
//
//        for (int i = 1; i <= numKinds; i++) {
//            System.out.println("\n── ตั้งค่า Kind ที่ " + i + " ──");
//            System.out.print("ชื่อ kind: ");
//            String name = sc.nextLine().trim();
//            System.out.print("defense factor (1-10): ");
//            int defense = readInt();
//
//            List<Stmt> ast = null;
//            while (ast == null) {
//                System.out.println("ใส่ strategy (พิมพ์ 'end' เมื่อเสร็จ):");
//                String stratStr = readMultiLineStrategy();
//                try {
//                    ast = new Parser(new Tokenizer(stratStr).tokenize()).parseStrategy();
//                    System.out.println("✓ Strategy ถูกต้อง!");
//                } catch (Exception e) {
//                    System.out.println("✗ Strategy ผิด syntax: " + e.getMessage());
//                    System.out.println("กรุณาลองใหม่...");
//                }
//            }
//            kindDefense.put(name, defense);
//            kindAst.put(name, ast);
//        }
//
//        System.out.println("\n╔══ Kind ที่เลือก ══════╗");
//        kindDefense.forEach((k, d) ->
//                System.out.printf("║ %-10s defense=%d ║%n", k, d));
//        System.out.println("╚═══════════════════════╝");
//
//        // ── Setup Spawn ฟรี ───────────────────────────────────
//        System.out.println("\n── Setup Spawn ฟรี ──");
//        System.out.println("P1 spawn zone: (col,row) (1,1)(2,1)(3,1)(1,2)(2,2)");
//        System.out.println("P2 spawn zone: (col,row) (8,8)(7,8)(6,8)(8,7)(7,7)");
//
//        GameState snap = gc.getGameState();
//        if (snap.p1.isAuto()) autoSpawnSetup("p1");
//        else spawnFree("p1");
//
//        if (snap.p2.isAuto()) autoSpawnSetup("p2");
//        else spawnFree("p2");
//
//        gc.setKinds(kindDefense, kindAst);
//        // ── เริ่มเกม ──────────────────────────────────────────
//        gc.startGame();
//        System.out.println("\n╔══════════════════╗");
//        System.out.println("║   เกมเริ่มแล้ว!   ║");
//        System.out.println("╚══════════════════╝\n");
//
//        gameLoop();
//    }
//
//    // ── Read multi-line strategy ──────────────────────────────
//    static String readMultiLineStrategy() {
//        StringBuilder sb = new StringBuilder();
//        while (true) {
//            String line = sc.nextLine();
//            if (line.trim().equalsIgnoreCase("end")) break;
//            int commentIdx = line.indexOf('#');
//            if (commentIdx != -1) line = line.substring(0, commentIdx);
//            sb.append(line).append(" ");
//        }
//        return sb.toString();
//    }
//
//    // ── Spawn ฟรี (human) ─────────────────────────────────────
//    static void spawnFree(String playerId) {
//        System.out.println("\n── " + playerId.toUpperCase() + " เลือก spawn ฟรี ──");
//        boolean spawned = false;
//        while (!spawned) {
//            System.out.print("เลือก kind " + kindDefense.keySet() + ": ");
//            String kind = sc.nextLine().trim();
//            if (!kindDefense.containsKey(kind)) { System.out.println("✗ ไม่มี kind นี้"); continue; }
//            System.out.print("col: "); int col = readInt();
//            System.out.print("row: "); int row = readInt();
//            try {
//                int defense = kindDefense.get(kind);
//                Minion m = gc.createMinion(kind, playerId, row, col, defense);
//                spawned = gc.setupSpawn(playerId, m, kindAst.get(kind));
//                System.out.println(spawned ? "✓ Spawn ฟรีสำเร็จ!" : "✗ ตำแหน่งไม่ถูกต้อง ลองใหม่");
//            } catch (Exception e) {
//                System.out.println("✗ Error: " + e.getMessage());
//            }
//        }
//    }
//
//    // ── Auto Spawn ตอน setup (ฟรี) ───────────────────────────
//    static void autoSpawnSetup(String playerId) {
//        String kind = kindDefense.keySet().iterator().next();
//        int defense = kindDefense.get(kind);
//        int[][] zones = playerId.equals("p1")
//                ? new int[][]{{1,1},{1,2},{2,1},{2,2},{1,3}}
//                : new int[][]{{8,8},{8,7},{7,8},{7,7},{8,6}};
//        for (int[] pos : zones) {
//            try {
//                Minion m = gc.createMinion(kind, playerId, pos[0], pos[1], defense);
//                if (gc.setupSpawn(playerId, m, kindAst.get(kind))) {
//                    System.out.println("🤖 " + playerId + " setup spawn " + kind +
//                            " ที่ (" + pos[1] + "," + pos[0] + ")");
//                    return;
//                }
//            } catch (Exception ignored) {}
//        }
//    }
//
//    // ── Auto Spawn ระหว่างเกม (หัก budget) ───────────────────
//    static void autoSpawnGame(String playerId) {
//        GameState snap = gc.getGameState();
//        long budget = playerId.equals("p1") ? snap.p1.getBudgetFloor() : snap.p2.getBudgetFloor();
//        if (budget < snap.config.spawnCost) return;
//
//        String kind = kindDefense.keySet().iterator().next();
//        int defense = kindDefense.get(kind);
//        core.Player player = playerId.equals("p1") ? snap.p1 : snap.p2;
//
//        for (String hex : player.getSpawnableHexes()) {
//            Position pos = Position.fromString(hex);
//            try {
//                Minion m = gc.createMinion(kind, playerId, pos.getRow(), pos.getCol(), defense);
//                if (gc.spawnMinion(playerId, m, kindAst.get(kind))) {
//                    System.out.println("🤖 " + playerId + " spawn " + kind +
//                            " ที่ (" + pos.getCol() + "," + pos.getRow() + ")");
//                    return;
//                }
//            } catch (Exception ignored) {}
//        }
//    }
//
//    // ── Auto Buy Hex ──────────────────────────────────────────
//    // ซื้อ hex เฉพาะเมื่อ budget เหลือพอทั้งซื้อ hex และ spawn ด้วย
//    static void autoBuyHex(String playerId) {
//        GameState snap = gc.getGameState();
//        long budget = playerId.equals("p1") ? snap.p1.getBudgetFloor() : snap.p2.getBudgetFloor();
//        long totalNeeded = snap.config.hexPurchaseCost + snap.config.spawnCost;
//        if (budget < totalNeeded) return; // ต้องมีพอทั้งคู่
//
//        core.Player player = playerId.equals("p1") ? snap.p1 : snap.p2;
//        for (String hex : new ArrayList<>(player.getSpawnableHexes())) {
//            Position owned = Position.fromString(hex);
//            for (int dir = Position.UP; dir <= Position.UPLEFT; dir++) {
//                Position candidate = owned.move(dir);
//                if (!candidate.isValid()) continue;
//                if (player.isSpawnable(candidate)) continue;
//                boolean ok = gc.purchaseHex(playerId, candidate.getRow(), candidate.getCol());
//                if (ok) {
//                    System.out.println("🤖 " + playerId + " ซื้อ hex (" +
//                            candidate.getCol() + "," + candidate.getRow() + ")");
//                    return;
//                }
//            }
//        }
//    }
//
//    // ── Game Loop ─────────────────────────────────────────────
//    static void gameLoop() {
//        while (true) {
//            GameState snap = gc.getGameState();
//            printBoard(snap);
//
//            String current = snap.current;
//            long budget = current.equals("p1") ? snap.p1.getBudgetFloor() : snap.p2.getBudgetFloor();
//            boolean auto = current.equals("p1") ? snap.p1.isAuto() : snap.p2.isAuto();
//
//           System.out.println("\n╔══ ตา " + snap.turn + "/" + MAX_TURNS +
//                     " | " + current.toUpperCase() +
//                    " | budget=" + budget + " ══╗");
//
//            if (!auto) {
//                // ── HUMAN TURN ────────────────────────────────
//                System.out.print("ซื้อ hex ไหม? (y/n): ");
//                if (sc.nextLine().trim().equalsIgnoreCase("y")) {
//                    System.out.print("col: "); int col = readInt();
//                    System.out.print("row: "); int row = readInt();
//                    System.out.println(gc.purchaseHex(current, row, col) ? "✓ ซื้อสำเร็จ!" : "✗ ซื้อไม่ได้");
//                }
//
//                System.out.print("Spawn minion ไหม? (y/n): ");
//                if (sc.nextLine().trim().equalsIgnoreCase("y")) {
//                    System.out.print("เลือก kind " + kindDefense.keySet() + ": ");
//                    String kind = sc.nextLine().trim();
//                    if (!kindDefense.containsKey(kind)) {
//                        System.out.println("✗ ไม่มี kind นี้");
//                    } else {
//                        System.out.print("col: "); int col = readInt();
//                        System.out.print("row: "); int row = readInt();
//                        try {
//                            int defense = kindDefense.get(kind);
//                            Minion m = gc.createMinion(kind, current, row, col, defense);
//                            System.out.println(gc.spawnMinion(current, m, kindAst.get(kind))
//                                    ? "✓ Spawn สำเร็จ!" : "✗ Spawn ไม่ได้");
//                        } catch (Exception e) {
//                            System.out.println("✗ Error: " + e.getMessage());
//                        }
//                    }
//                }
//            } else {
//                // ── BOT TURN ──────────────────────────────────
//                System.out.println("🤖 BOT กำลังเล่น...");
//            }
//
//
//            // ── Execute turn ──────────────────────────────────
//            GameController.TurnResult result = gc.executeTurn(current);
//
//            // DEBUG: แสดง strategy log
//            for (var log : result.log) {
//                if (!log.success)
//                    System.out.println("  ⚠️ minion " + log.minionId + ": " + log.error);
//            }
//
//            if (result.isOver) {
//                printBoard(gc.getGameState());
//                System.out.println("\n╔══════════════════╗");
//                System.out.println("║    จบเกม!         ║");
//                System.out.println("╠══════════════════╣");
//                System.out.println("║ ผู้ชนะ: " + (result.winner != null ? result.winner : "TIE"));
//                System.out.println("║ เหตุผล: " + result.reason);
//                System.out.println("╚══════════════════╝");
//                break;
//            }
//        }
//    }
//
//    // ── Print Board ───────────────────────────────────────────
//    static void printBoard(GameState snap) {
//        System.out.println("\n   1 2 3 4 5 6 7 8");
//        System.out.println("  ─────────────────");
//        char[][] board = new char[9][9];
//        for (int r = 1; r <= 8; r++)
//            for (int c = 1; c <= 8; c++)
//                board[r][c] = '.';
//
//        for (Minion m : snap.minions.values()) {
//            if (m.isAlive()) {
//                int r = m.getPosition().getRow();
//                int c = m.getPosition().getCol();
//                board[r][c] = m.getOwner().getId().equals("p1") ? '1' : '2';
//            }
//        }
//        for (int r = 1; r <= 8; r++) {
//            System.out.print(r + " | ");
//            for (int c = 1; c <= 8; c++) System.out.print(board[r][c] + " ");
//            System.out.println();
//        }
//        System.out.println("  ─────────────────");
//        System.out.printf("P1: budget=%-8d minions=%d hp=%d%n",
//                snap.p1.getBudgetFloor(), snap.p1.getMinionCount(), snap.p1.getTotalHP());
//        System.out.printf("P2: budget=%-8d minions=%d hp=%d%n",
//                snap.p2.getBudgetFloor(), snap.p2.getMinionCount(), snap.p2.getTotalHP());
//        System.out.println("Owned Hex:");
//        printOwnedHex(snap.p1);
//        printOwnedHex(snap.p2);
//    }
//
//    static void printOwnedHex(core.Player p) {
//        System.out.print(p.getId().toUpperCase() + ": ");
//        if (p.getSpawnableHexes().isEmpty()) { System.out.println("(none)"); return; }
//        for (String s : p.getSpawnableHexes()) {
//            String[] rc = s.split(",");
//            System.out.print("(" + rc[1] + "," + rc[0] + ") ");
//        }
//        System.out.println();
//    }
//
//    static void printBanner() {
//        System.out.println("╔══════════════════════════════════╗");
//        System.out.println("║   K O M B A T                    ║");
//        System.out.println("║   Kickstart Offense with         ║");
//        System.out.println("║   Minion's Best in an            ║");
//        System.out.println("║   Amicable Territory             ║");
//        System.out.println("╚══════════════════════════════════╝");
//    }
//
//    static int readInt() {
//        try { return Integer.parseInt(sc.nextLine().trim()); }
//        catch (Exception e) { return 0; }
//    }
//}