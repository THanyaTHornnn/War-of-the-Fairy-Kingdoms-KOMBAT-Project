package controller;

import controller.turn.TurnManager;
import core.*;
import strategy.parser.*;
import strategy.ast.Stmt;
import strategy.ast.expr.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

// รับคำสั่งจากนอก → สั่ง TurnManager / GameLogic
public class GameController {
    private GameLogic logic;
    private TurnManager turnManager;
    private Map<String, Integer> kindDefense;   // เพิ่ม
    private Map<String, List<Stmt>> kindAst;    // เพิ่ม


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
        logic.initBudgets();
        logic.startGame();
        beginTurn("p1");

    }

    // ── 5. Purchase hex ───────────────────────────────────────
    public boolean purchaseHex(String playerId, int row, int col) {
        return turnManager.purchaseHex(playerId, row, col);
    }

    // ── 6. Spawn minion ───────────────────────────────────────
    public boolean spawnMinion(String playerId, Minion minion, List<Stmt> ast) {
        minion.setStrategyAST(ast);
        return turnManager.spawnMinion(playerId, minion);
    }

    // ── 7. Execute turn ───────────────────────────────────────
    public TurnResult executeTurn(String playerId) {

        // Step 0: increment turnCount ก่อน applyBudget
        logic.beginTurn(playerId);

        // Step 1: budget
        turnManager.applyBudget(playerId);

        // Step 2: auto purchase hex (bot only)
        Player player = logic.getPlayer(playerId);
        if (player.isAuto()) {
            autoPurchaseHex(playerId); // ซื้อ hex
            autoSpawnMinion(playerId); // สั่ง spawn minion (ถ้าอยากให้ bot สั่ง spawn ด้วย)
        }

        // Step 3: execute strategies ของ player นี้เท่านั้น
        List<TurnManager.MinionLog> logs = turnManager.executeStrategies(playerId);

        // check end
        if (logic.checkEndGame()) {
            GameState snap = logic.getSnapshot();
            return new TurnResult(true, snap.winner, snap.endReason, logs);
        }

        logic.switchPlayer();
        return new TurnResult(false, null, null, logs);
    }
    public void beginTurn(String playerId) {
        logic.beginTurn(playerId);
        logic.resetSpawnedThisTurn(playerId);
        logic.resetPurchasedThisTurn(playerId);
        turnManager.applyBudget(playerId);

        // bot จัดการอัตโนมัติ
        Player player = logic.getPlayer(playerId);
        if (player.isAuto()) {
            autoPurchaseHex(playerId);
            autoSpawnMinion(playerId);
            executeTurn(playerId); // bot จบ turn เลย
        }
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
        long cost = logic.getConfig().hexPurchaseCost;

        // 40% โอกาสที่จะซื้อ (สุ่ม)
        if (random.nextInt(100) > 20) {
            return;
        }

        if (!p.canAfford(cost)) {
            return;
        }

        // หา hex ที่สามารถซื้อได้ทั้งหมด
        List<Position> validHexes = new ArrayList<>();

        for (String hex : p.getSpawnableHexes()) {
            Position owned = Position.fromString(hex);

            for (int dir = Position.UP; dir <= Position.UPLEFT; dir++) {
                Position next = owned.move(dir);

                if (!next.isValid()) continue;
                if (logic.getMinionAt(next) != null) continue;
                if (p.isSpawnable(next)) continue;

                // ตรวจสอบ adjacency
                boolean isAdjacent = false;
                for (String spawnHex : p.getSpawnableHexes()) {
                    Position spawnPos = Position.fromString(spawnHex);
                    if (Board.isAdjacent(spawnPos, next)) {
                        isAdjacent = true;
                        break;
                    }
                }

                if (isAdjacent) {
                    validHexes.add(next);
                }
            }
        }

        if (validHexes.isEmpty()) {
            return;
        }

        // สุ่มเลือก hex ที่จะซื้อ
        Position chosen = validHexes.get(random.nextInt(validHexes.size()));
        boolean success = turnManager.purchaseHex(playerId, chosen.getRow(), chosen.getCol());

        if (success) {
            System.out.println("🤖 " + playerId + " bought hex at (" + chosen.getCol() + "," + chosen.getRow() + ")");
        }
    }

    private void autoSpawnMinion(String playerId) {
        if (kindDefense == null || kindDefense.isEmpty()) return;

        // สุ่ม 50% โอกาส spawn
        if (Math.random() > 0.5) return;

        Player player = logic.getPlayer(playerId);
        long cost = logic.getConfig().spawnCost;

        if (!player.canAfford(cost)) return;
        if (player.getSpawnsUsed() >= logic.getConfig().maxSpawns) return;

        // หา hex ว่างใน spawn zone
        List<Position> availableHexes = new ArrayList<>();
        for (String hex : player.getSpawnableHexes()) {
            Position pos = Position.fromString(hex);
            if (logic.getMinionAt(pos) == null) {
                availableHexes.add(pos);
            }
        }

        if (availableHexes.isEmpty()) return;

        // สุ่มเลือกชนิด minion
        List<String> kinds = new ArrayList<>(kindDefense.keySet());
        String kind = kinds.get((int)(Math.random() * kinds.size()));
        int defense = kindDefense.get(kind);
        List<Stmt> ast = kindAst.get(kind);

        // สุ่มเลือกตำแหน่ง
        Position pos = availableHexes.get((int)(Math.random() * availableHexes.size()));

        Minion m = Minion.create(kind, logic.generateMinionId(), player, pos,
                logic.getConfig().initHp, defense);
        m.setStrategyAST(ast);

        if (logic.spawnMinion(playerId, m)) {
            System.out.println("🤖 " + playerId + " spawned " + kind + " at (" + pos.getCol() + "," + pos.getRow() + ")");
        }
    }

    public void resetGame(GameState.Mode newMode) {
        logic.resetGame(newMode);
        this.turnManager = new TurnManager(logic);
    }

}