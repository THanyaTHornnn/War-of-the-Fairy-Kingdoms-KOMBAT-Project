package com.kombat.controller;

import com.kombat.controller.turn.TurnManager;
import com.kombat.core.Config;
import com.kombat.core.GameLogic;
import com.kombat.core.GameState;
import com.kombat.core.Minion;
import com.kombat.strategy.ast.Stmt;
import com.kombat.strategy.parser.Parser;
import com.kombat.strategy.parser.Token;
import com.kombat.strategy.parser.Tokenizer;
import com.kombat.core.Player;
import com.kombat.core.Position;

import java.io.IOException;
import java.util.*;

// รับคำสั่งจากนอก → สั่ง TurnManager / GameLogic
public class GameController {
    private GameLogic logic;
    private TurnManager turnManager;
    private Map<String, Integer> kindDefense;   // เพิ่ม
    private Map<String, List<Stmt>> kindAst;
    private boolean processingP1Turn = false;
    private boolean processingP2Turn = false;

    private final Random random = new Random();
    // เพิ่ม method สำหรับรับข้อมูล kinds จาก Rungame
    public void setKinds(Map<String, Integer> defense, Map<String, List<Stmt>> ast) {
        this.kindDefense = defense;
        this.kindAst = ast;
    }
    // ── 1. Create game ────────────────────────────────────────
    public void createGame(String configPath, GameState.Mode mode) throws IOException {
        Config config = (configPath != null && !configPath.isEmpty())
                ? Config.parse(configPath)
                : Config.defaultConfig();
        this.logic       = new GameLogic(config, mode);
        this.turnManager = new TurnManager(logic);
    }

    // ── 2. Parse / validate strategy ─────────────────────────
    public List<Stmt> parseStrategy(String source) {
        List<Token> tokens = new Tokenizer(source).tokenize();
        return new Parser(tokens).parseStrategy();
    }
    public boolean validateStrategy(String source) {
        try {
            List<Token> tokens = new Tokenizer(source).tokenize();
            new Parser(tokens).parseStrategy();
            return true;
        } catch (Exception e) { return false; }
    }



    // ── 3. Setup spawn (before startGame) ────────────────────
    public boolean setupSpawn(String playerId, Minion minion, List<Stmt> ast) {
        minion.setStrategyAST(ast);
        return logic.spawnMinion(playerId, minion);
    }

    // ── 4. Start game ─────────────────────────────────────────
    public void startGame() {
        logic.startGame();

        // ✅ mark ทั้งคู่ก่อน เพื่อให้ autoSpawnMinion ไม่ spawn ซ้ำในเทิร์นแรก
        logic.getPlayer("p1").setSpawnedThisTurn(logic.getPlayer("p1").getTurnCount());
        logic.getPlayer("p2").setSpawnedThisTurn(logic.getPlayer("p2").getTurnCount());

        beginTurn("p1");
//        if (kindDefense != null && !kindDefense.isEmpty()) {
//            String kind = kindDefense.keySet().iterator().next();
//            int defense = kindDefense.get(kind);
//            List<Stmt> ast = kindAst.get(kind);
//
//            Minion m1 = Minion.create(kind, logic.generateMinionId(),
//                    logic.getPlayer("p1"), new Position(1, 1),
//                    logic.getConfig().initHp, defense);
//            m1.setStrategyAST(ast);
//            logic.spawnMinion("p1", m1);
//
//            Minion m2 = Minion.create(kind, logic.generateMinionId(),
//                    logic.getPlayer("p2"), new Position(8, 8),
//                    logic.getConfig().initHp, defense);
//            m2.setStrategyAST(ast);
//            logic.spawnMinion("p2", m2);
//        }
//
//        logic.startGame(); // ← startGame อาจ reset turnCount
//
//        // ✅ mark หลัง startGame เพื่อให้ turnCount ถูกต้อง
//        logic.getPlayer("p1").setSpawnedThisTurn(logic.getPlayer("p1").getTurnCount());
//        logic.getPlayer("p2").setSpawnedThisTurn(logic.getPlayer("p2").getTurnCount());
//
//        System.out.println("=== BEFORE beginTurn p1 ===");
//        System.out.println("p2 minions: " + logic.getPlayer("p2").getMinionCount());
//        System.out.println("p2 turnCount: " + logic.getPlayer("p2").getTurnCount());
//        System.out.println("p2 lastSpawnTurn: " + logic.getPlayer("p2").hasSpawnedThisTurn(logic.getPlayer("p2").getTurnCount()));
//
//        beginTurn("p1");
//
//        System.out.println("=== AFTER beginTurn p1 ===");
//        System.out.println("p2 minions: " + logic.getPlayer("p2").getMinionCount());
    }
    // ── 5. Purchase hex ───────────────────────────────────────
    public boolean purchaseHex(String playerId, int row, int col) {
        Player player = logic.getPlayer(playerId);

        if (logic.getSnapshot().phase == GameState.Phase.SETUP) {
            System.out.println("ไม่สามารถซื้อ hex ในช่วง SETUP");
            return false;
        }

        if (player.hasSkippedHexThisTurn()) {
            System.out.println("รอบนี้ไม่สามารถซื้อ Hex เพราะ spawn Minion ก่อน");
            return false;
        }

        boolean success = turnManager.purchaseHex(playerId, row, col);
        if (success) {
            player.setPurchasedThisTurn(player.getTurnCount());
        }
        return success;
    }

    // ── 6. Spawn minion ───────────────────────────────────────
    public boolean spawnMinion(String playerId, Minion minion, List<Stmt> ast) {
        Player player = logic.getPlayer(playerId);

        // ถ้า spawn Minion ก่อนซื้อ Hex รอบนี้ → mark ว่า skip hex
        boolean canBuyHex = !player.getSpawnableHexes().isEmpty() &&
                player.canAfford(logic.getConfig().hexPurchaseCost);

        if (canBuyHex && !player.hasPurchasedThisTurn(player.getTurnCount())) {
            player.setSkippedHexThisTurn(true);
        }

        // log ข้อความถ้า spawn แล้วจะ block Hex
        if (player.hasSkippedHexThisTurn()) {
            System.out.println("❌ รอบนี้ไม่สามารถซื้อ Hex ได้เพราะ spawn Minion ไปแล้ว");
        }

        minion.setStrategyAST(ast);
        boolean success = turnManager.spawnMinion(playerId, minion);

        if (success) {
            player.setSpawnedThisTurn(player.getTurnCount());
        }

        return success;
    }
    // ── 7. Execute turn ───────────────────────────────────────
//    public void beginTurn(String playerId) {
//        Player player = logic.getPlayer(playerId);
//        logic.beginTurn(playerId);
//        player.resetTurnFlags();
//
//        if (player.isAuto()) {
//            autoPurchaseHex(playerId);
//            autoSpawnMinion(playerId);
//            // ✅ เรียก executeTurn แต่ไม่เรียก beginTurn ต่อเอง
//            executeTurn(playerId);
//        }
//    }
    public void beginTurn(String playerId) {
        Player player = logic.getPlayer(playerId);

        boolean isFirstTurn = (player.getTurnCount() == 0);

        logic.beginTurn(playerId);
        player.resetTurnFlags();

        System.out.println("🔄 beginTurn: " + playerId + " isAuto=" + player.isAuto() + " isFirstTurn=" + isFirstTurn);

        boolean alreadyProcessing = playerId.equals("p1") ? processingP1Turn : processingP2Turn;

        if (player.isAuto() && !alreadyProcessing) {
            if (playerId.equals("p1")) processingP1Turn = true;
            else processingP2Turn = true;

            try {
//                autoPurchaseHex(playerId);
                autoSpawnMinion(playerId); // ← ให้ autoSpawnMinion จัดการเอง
                executeTurn(playerId);
            } finally {
                if (playerId.equals("p1")) processingP1Turn = false;
                else processingP2Turn = false;
            }
        }
    }
    public TurnResult executeTurn(String playerId) {
        turnManager.applyBudget(playerId);
        List<TurnManager.MinionLog> logs = turnManager.executeStrategies(playerId);

        if (logic.checkEndGame()) {
            GameState snap = logic.getSnapshot();
            return new TurnResult(true, snap.winner, snap.endReason, logs);
        }

        logic.switchPlayer();
        String next = logic.getCurrent();

        beginTurn(next);

        return new TurnResult(false, null, null, logs);
    }
    // ── 8. Create minion helper ───────────────────────────────
//    public Minion createMinion(String kindName, String playerId, int row, int col) {
//        Player player = logic.getPlayer(playerId);
//        Position pos  = new Position(row, col);
//        String id     = logic.generateMinionId();
//        long hp       = logic.getConfig().initHp;
//        String kind   = kindName.replace("Minion", ""); // "MinionA" → "A"
//        return Minion.create(kind, id, player, pos, hp);
//    }
    public Minion createMinion(String kindName, String playerId, int row, int col, int defense) {
        Player player = logic.getPlayer(playerId);
        Position pos  = new Position(row, col);
        String id     = logic.generateMinionId();
        long hp       = logic.getConfig().initHp;
        return Minion.create(kindName, id, player, pos, hp, defense);
    }

    public Minion createMinion(String kindName, String playerId, int row, int col) {
        return createMinion(kindName, playerId, row, col, 0);
    }


    // ── 9. Get state (snapshot) ───────────────────────────────
    public GameState getGameState() { return logic.getSnapshot(); }
    public boolean isGameOver()     { return logic.isGameOver(); }

    // ── TurnResult ───────────────────────────────────────────-
    public static class TurnResult {
        public final boolean isOver;
        public final String winner;
        public final String reason;
        public final List<TurnManager.MinionLog> log;

        public TurnResult(boolean isOver, String winner, String reason,
                          List<TurnManager.MinionLog> log) {
            this.isOver = isOver;
            this.winner = winner;
            this.reason = reason;
            this.log    = log;
        }
    }
    public void runAutoGame() {
        while (!logic.isGameOver()) {
            String id = logic.getCurrent();
            executeTurn(id);
        }
    }

    private void autoPurchaseHex(String playerId) {
        Player p = logic.getPlayer(playerId);
        Player opponent = logic.getPlayer(playerId.equals("p1") ? "p2" : "p1");
        long cost = logic.getConfig().hexPurchaseCost;

        if (random.nextInt(100) > 30) return;
        if (!p.canAfford(cost)) return;

        // ✅ ใช้ player.getTurnCount()
        if (p.hasPurchasedThisTurn(p.getTurnCount())) return;

        List<Position> validHexes = new ArrayList<>();
        for (String hex : p.getSpawnableHexes()) {
            Position owned = Position.fromString(hex);
            for (int dir = Position.UP; dir <= Position.UPLEFT; dir++) {
                Position next = owned.move(dir);
                if (!next.isValid()) continue;
                if (logic.getMinionAt(next) != null) continue;
                if (p.isSpawnable(next)) continue;
                if (opponent.isSpawnable(next)) continue;
                validHexes.add(next);
            }
        }

        if (validHexes.isEmpty()) return;

        validHexes = new ArrayList<>(new HashSet<>(validHexes));
        Position chosen = validHexes.get(random.nextInt(validHexes.size()));

        boolean success = logic.purchaseHex(playerId, chosen.getRow(), chosen.getCol());

        if (success) {
            // ✅ set ด้วย player.getTurnCount()
            p.setPurchasedThisTurn(p.getTurnCount());
            System.out.println("🤖 " + playerId +
                    " bought hex at (" + chosen.getCol() + "," + chosen.getRow() + ")");
        }
    }
    private void autoSpawnMinion(String playerId) {
        if (kindDefense == null || kindDefense.isEmpty()) return;
        if (Math.random() > 0.2) return;

        Player player = logic.getPlayer(playerId);
        long cost = logic.getConfig().spawnCost;

        if (!player.canAfford(cost)) return;
        if (player.getSpawnsUsed() >= logic.getConfig().maxSpawns) return;

        // ✅ ใช้ player.getTurnCount()
        if (player.hasSpawnedThisTurn(player.getTurnCount())) return;

        List<Position> availableHexes = new ArrayList<>();
        for (String hex : player.getSpawnableHexes()) {
            Position pos = Position.fromString(hex);
            if (logic.getMinionAt(pos) == null) {
                availableHexes.add(pos);
            }
        }

        if (availableHexes.isEmpty()) return;

        List<String> kinds = new ArrayList<>(kindDefense.keySet());
        String kind = kinds.get((int)(Math.random() * kinds.size()));
        int defense = kindDefense.get(kind);
        List<Stmt> ast = kindAst.get(kind);

        Position pos = availableHexes.get((int)(Math.random() * availableHexes.size()));

        Minion m = Minion.create(kind, logic.generateMinionId(), player, pos,
                logic.getConfig().initHp, defense);
        m.setStrategyAST(ast);

        if (logic.spawnMinion(playerId, m)) {
            // ✅ set ด้วย player.getTurnCount()
            player.setSpawnedThisTurn(player.getTurnCount());
            System.out.println("🤖 " + playerId + " spawned " + kind +
                    " at (" + pos.getCol() + "," + pos.getRow() + ")");
        }
    }
    public void resetGame(GameState.Mode newMode) {
        logic.resetGame(newMode);
        this.turnManager = new TurnManager(logic);
    }
    private int distanceToClosestEnemy(Position pos, String playerId) {
        int min = Integer.MAX_VALUE;

        for (Minion m : logic.getMinions().values()) {
            if (!m.getOwner().getId().equals(playerId)) {
                int d = pos.distanceTo(m.getPosition());
                if (d < min) min = d;
            }
        }

        // ถ้าไม่เจอศัตรูเลย
        return min == Integer.MAX_VALUE ? 999 : min;
    }

}