package controller;

import controller.turn.TurnManager;
import core.*;
import strategy.parser.*;
import strategy.ast.Stmt;
import strategy.ast.expr.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// รับคำสั่งจากนอก → สั่ง TurnManager / GameLogic
public class GameController {
    private GameLogic logic;
    private TurnManager turnManager;

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
            autoPurchaseHex(playerId);
            // NOTE: autoSpawn ถูกจัดการโดย Rungame ก่อนเรียก executeTurn
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
        if (!p.canAfford(cost)) return;

        // ลองหาตำแหน่งติด hex เดิม
        for (String hex : p.getSpawnableHexes()) {

            Position owned = Position.fromString(hex);

            for (int dir = Position.UP; dir <= Position.UPLEFT; dir++) {

                Position next = owned.move(dir);

                if (!next.isValid()) continue;
                if (logic.getMinionAt(next) != null) continue;
                if (p.isSpawnable(next)) continue;

                // ซื้อเลย
                turnManager.purchaseHex(playerId, next.getRow(), next.getCol());
                return;
            }
        }
    }
    // kindStrategies: ต้องส่งมาจากภายนอก (Rungame) เพราะ GameController ไม่รู้จัก kind
    // แก้: ย้าย auto spawn logic ไปอยู่ที่ Rungame แทน ไม่ควรอยู่ใน GameController
    // method นี้เก็บไว้เฉยๆ แต่ไม่เรียกแล้ว
    private void autoSpawnMinion(String playerId) {
        // ย้ายไป Rungame.autoSpawnGame() แล้ว
    }



}