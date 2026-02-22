package controller;

import controller.turn.TurnManager;
import core.*;
import strategy.parser.*;
import strategy.ast.Stmt;
import strategy.ast.expr.*;
import java.io.IOException;
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
        // Step 1: budget
        turnManager.applyBudget(playerId);
        // Step 4: run strategies
        List<TurnManager.MinionLog> logs = turnManager.executeStrategies(playerId);
        // Check end
        if (logic.checkEndGame()) {
            GameState snap = logic.getSnapshot();
            return new TurnResult(true, snap.winner, snap.endReason, logs);
        }
        logic.switchPlayer();
        return new TurnResult(false, null, null, logs);
    }

    // ── 8. Create minion helper ───────────────────────────────
    public Minion createMinion(String kindName, String playerId, int row, int col) {
        Player player = logic.getPlayer(playerId);
        Position pos  = new Position(row, col);
        String id     = logic.generateMinionId();
        long hp       = logic.getConfig().initHp;
        String kind   = kindName.replace("Minion", ""); // "MinionA" → "A"
        return Minion.create(kind, id, player, pos, hp);
    }


    // ── 9. Get state (snapshot) ───────────────────────────────
    public GameState getGameState() { return logic.getSnapshot(); }
    public boolean isGameOver()     { return logic.isGameOver(); }

    // ── TurnResult ────────────────────────────────────────────
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
}