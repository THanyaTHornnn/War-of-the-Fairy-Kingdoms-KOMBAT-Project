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
        logic.startGame();
        logic.beginTurn("p1");

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
    public void beginTurn(String playerId) {
        logic.beginTurn(playerId); // แค่ increment turnCount
        logic.resetSpawnedThisTurn(playerId);
        logic.resetPurchasedThisTurn(playerId);

        Player player = logic.getPlayer(playerId);
        if (player.isAuto()) {
            autoPurchaseHex(playerId);
            autoSpawnMinion(playerId);
            executeTurn(playerId);
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
        logic.beginTurn(next);
        logic.resetSpawnedThisTurn(next);
        logic.resetPurchasedThisTurn(next);

        // ถ้า next เป็น bot ให้ทำ action แล้วหยุด รอ frontend สั่งอีกรอบ
        if (logic.getPlayer(next).isAuto()) {
            autoPurchaseHex(next);
            autoSpawnMinion(next);
            // ไม่เรียก executeTurn ต่อ → หยุดรอ frontend
        }

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

        if (random.nextInt(100) > 20) return;
        if (!p.canAfford(cost)) return;

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

        boolean success = turnManager.purchaseHex(
                playerId, chosen.getRow(), chosen.getCol());

        if (success) {
            System.out.println("🤖 " + playerId +
                    " bought hex at (" + chosen.getCol() + "," + chosen.getRow() + ")");
        }
    }
    private void autoSpawnMinion(String playerId) {
        if (kindDefense == null || kindDefense.isEmpty()) return;
        if (Math.random() > 0.5) return;

        Player player = logic.getPlayer(playerId);
        long cost = logic.getConfig().spawnCost;

        if (!player.canAfford(cost)) return;
        if (player.getSpawnsUsed() >= logic.getConfig().maxSpawns) return;
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
            player.setSpawnedThisTurn(player.getTurnCount()); // ← เพิ่มตรงนี้
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