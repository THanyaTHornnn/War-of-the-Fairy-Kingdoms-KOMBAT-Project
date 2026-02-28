package controller;

import controller.turn.TurnManager;
import core.*;
import strategy.parser.*;
import strategy.ast.Stmt;

import java.io.IOException;
import java.util.*;

// รับคำสั่งจากนอก → สั่ง TurnManager / GameLogic
public class GameController {

    private GameLogic logic;
    private TurnManager turnManager;
    private Random rand = new Random();
    private List<String> kinds;
    private Map<String,List<Stmt>> kindAst;

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
        } catch (Exception e) {
            return false;
        }
    }

    // ── 3. Setup spawn (before startGame) ────────────────────
    public boolean setupSpawn(String playerId, Minion minion, List<Stmt> ast) {
        minion.setStrategyAST(cloneAST(ast));
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
        minion.setStrategyAST(cloneAST(ast));
        return logic.spawnMinion(playerId, minion);
    }

    // ── 7. Execute turn ───────────────────────────────────────
    public TurnResult executeTurn(String playerId) {

        logic.beginTurn(playerId);

        Player player = logic.getPlayer(playerId);

        turnManager.applyBudget(playerId);
        turnManager.applyInterest(playerId);

        if (player.isAuto()) {

            if (Math.random() < 0.5)
                autoPurchaseHex(playerId);

            if (Math.random() < 0.5)
                autoSpawnMinion(playerId);
        }

        List<TurnManager.MinionLog> logs =
                turnManager.executeStrategies(playerId);

        GameState snap = logic.getSnapshot();

        if (logic.checkEndGame()) {
            return new TurnResult(true, snap.winner, snap.endReason, logs, snap);
        }

        logic.switchPlayer();

        return new TurnResult(false, null, null, logs, snap);
    }
    // ── 8. Create minion helper ───────────────────────────────
    public Minion createMinion(String kindName, String playerId, int row, int col) {

        Player player = logic.getPlayer(playerId);
        Position pos  = new Position(row, col);

        String id     = logic.generateMinionId();
        long hp       = logic.getConfig().initHp;
        String kind   = kindName.replace("Minion", "");

        return Minion.create(kind, id, player, pos, hp);
    }

    // ── 9. Get state (snapshot) ───────────────────────────────
    public GameState getGameState() { return logic.getSnapshot(); }
    public boolean isGameOver()     { return logic.isGameOver(); }

    // ── TurnResult ───────────────────────────────────────────
    public static class TurnResult {

        public final boolean isOver;
        public final String winner;
        public final String reason;
        public final List<TurnManager.MinionLog> log;
        public final GameState snapshot;

        public TurnResult(boolean isOver,
                          String winner,
                          String reason,
                          List<TurnManager.MinionLog> log,
                          GameState snapshot) {

            this.isOver   = isOver;
            this.winner   = winner;
            this.reason   = reason;
            this.log      = log;
            this.snapshot = snapshot;
        }
    }

    // ── Auto loop ───────────────────────────────────────────
    public void runAutoGame() {
        while (!logic.isGameOver()) {
            executeTurn(logic.getCurrent());
        }
    }

    // ── Auto hex purchase ───────────────────────────────────
    private void autoPurchaseHex(String playerId) {

        Player p = logic.getPlayer(playerId);

        long cost = logic.getConfig().hexPurchaseCost;
        int bought = 0;
        int maxBuy = 1; // ⭐ ปรับจำนวนที่อยากให้ซื้อสูงสุดต่อเทิร์น

        for (String hex : new ArrayList<>(p.getSpawnableHexes())) {

            Position owned = Position.fromString(hex);

            for (int dir = Position.UP; dir <= Position.UPLEFT; dir++) {

                if (!p.canAfford(cost)) return;
                if (bought >= maxBuy) return;

                Position next = owned.move(dir);

                if (!next.isValid()) continue;
                if (logic.getMinionAt(next) != null) continue;
                if (p.isSpawnable(next)) continue;

                if (turnManager.purchaseHex(playerId, next.getRow(), next.getCol())) {
                    bought++;
                }
            }
        }
    }

    // ── Strategy kinds setup ────────────────────────────────
    public void setKinds(Map<String,List<Stmt>> map){

        if (map == null || map.isEmpty())
            throw new IllegalArgumentException("Kinds map cannot be empty");

        this.kindAst = map;
        this.kinds   = new ArrayList<>(map.keySet());
    }

    private String randomKind(){

        if (kinds == null || kinds.isEmpty())
            throw new IllegalStateException("Kinds not initialized");

        return kinds.get((int)(Math.random() * kinds.size()));
    }

    // ── Auto spawn ──────────────────────────────────────────
    private void autoSpawnMinion(String playerId) {

        Player p = logic.getPlayer(playerId);
        if (!p.isAuto()) return;

        long cost = logic.getConfig().spawnCost;
        if (!p.canAfford(cost)) return;

        List<String> hexes = new ArrayList<>(p.getSpawnableHexes());
        if (hexes.isEmpty()) return;

        String pick = hexes.get((int)(Math.random() * hexes.size()));
        Position pos = Position.fromString(pick);

        String kind = randomKind();
        Minion m = createMinion(kind, playerId, pos.getRow(), pos.getCol());

        List<Stmt> ast = kindAst.get(kind);

        if (ast == null)
            throw new IllegalStateException("No strategy for kind " + kind);

        m.setStrategyAST(cloneAST(ast));

        turnManager.spawnMinion(playerId, m);
    }

    // ── AST clone helper ────────────────────────────────────
    private List<Stmt> cloneAST(List<Stmt> original){
        return new ArrayList<>(original);
    }

    public void registerKind(String kind, String source){

        if(!validateStrategy(source))
            throw new IllegalArgumentException("Invalid strategy");

        List<Stmt> ast = parseStrategy(source);

        kindAst.put(kind, ast);
        kinds = new ArrayList<>(kindAst.keySet());
    }

}