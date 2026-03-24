package com.kombat.controller;

import com.kombat.controller.turn.TurnManager;
import com.kombat.core.*;
import com.kombat.strategy.ast.Stmt;
import com.kombat.strategy.parser.Parser;
import com.kombat.strategy.parser.Token;
import com.kombat.strategy.parser.Tokenizer;


import java.io.IOException;
import java.util.List;
import java.util.Map;

// รับคำสั่งจากนอก → สั่ง TurnManager / GameLogic
public class GameController {
    private GameLogic logic;
    private TurnManager turnManager;
    private Map<String, Integer> kindDefense;   // เพิ่ม
    private Map<String, List<Stmt>> kindAst;    // เพิ่ม

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

//    // ── 7. Execute turn ───────────────────────────────────────
//    public TurnResult executeTurn(String playerId) {
//
//        // Step 0: increment turnCount ก่อน applyBudget
//        logic.beginTurn(playerId);
//
//        // Step 1: budget
//        turnManager.applyBudget(playerId);
//
//        // Step 2: auto purchase hex (bot only)
//        Player player = logic.getPlayer(playerId);
//        if (player.isAuto()) {
//            autoPurchaseHex(playerId); // ซื้อ hex
//            autoSpawnMinion(playerId); // สั่ง spawn minion (ถ้าอยากให้ bot สั่ง spawn ด้วย)
//        }
//
//        // Step 3: execute strategies ของ player นี้เท่านั้น
//        List<TurnManager.MinionLog> logs = turnManager.executeStrategies(playerId);
//
//        // check end
//        if (logic.checkEndGame()) {
//            GameState snap = logic.getSnapshot();
//            return new TurnResult(true, snap.winner, snap.endReason, logs);
//        }
//
//        logic.switchPlayer();
//        return new TurnResult(false, null, null, logs);
//    }
// ── 7a. Begin turn (เรียกตอนเริ่ม turn ของผู้เล่น) ──────────
public void beginTurn(String playerId) {
    logic.beginTurn(playerId);
    turnManager.applyBudget(playerId);

    // bot จัดการอัตโนมัติ
    Player player = logic.getPlayer(playerId);
    if (player.isAuto()) {
        autoPurchaseHex(playerId);
        autoSpawnMinion(playerId);
        endTurn(playerId); // bot จบ turn เลย
    }
}

    // ── 7b. End turn (ผู้เล่นกดปุ่ม End Turn) ───────────────────
    public TurnResult endTurn(String playerId) {
        // execute strategies ของ player นี้
        List<TurnManager.MinionLog> logs = turnManager.executeStrategies(playerId);

        // check end game
        if (logic.checkEndGame()) {
            GameState snap = logic.getSnapshot();
            return new TurnResult(true, snap.winner, snap.endReason, logs);
        }

        logic.switchPlayer();

        // เริ่ม turn ของผู้เล่นถัดไปทันที
        beginTurn(logic.getCurrent());

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
            endTurn(id);
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

    private void autoSpawnMinion(String playerId) {
        System.out.println("[autoSpawn] kindDefense = " + kindDefense);

        if (kindDefense == null || kindDefense.isEmpty()) {
            System.out.println("[autoSpawn] No kindDefense! Cannot spawn");
            return;
        }

        Player player = logic.getPlayer(playerId);
        System.out.println("[autoSpawn] Player " + playerId + " spawnableHexes: " + player.getSpawnableHexes());

        long cost = logic.getConfig().spawnCost;
        if (!player.canAfford(cost)) {
            System.out.println("[autoSpawn] Cannot afford, budget=" + player.getBudget());
            return;
        }
        if (player.getSpawnsUsed() >= logic.getConfig().maxSpawns) {
            System.out.println("[autoSpawn] Max spawns reached");
            return;
        }

        String kind = kindDefense.keySet().iterator().next();
        int defense = kindDefense.get(kind);
        List<Stmt> ast = kindAst.get(kind);

        System.out.println("[autoSpawn] Trying kind=" + kind + " defense=" + defense);

        for (String hex : player.getSpawnableHexes()) {
            Position pos = Position.fromString(hex);
            System.out.println("[autoSpawn] Checking hex: " + pos);
            if (logic.getMinionAt(pos) != null) {
                System.out.println("[autoSpawn] Occupied");
                continue;
            }

            Minion m = Minion.create(kind, logic.generateMinionId(), player, pos, logic.getConfig().initHp, defense);
            m.setStrategyAST(ast);
            if (logic.spawnMinion(playerId, m)) {
                System.out.println("🤖 " + playerId + " spawn " + kind + " ที่ (" + pos.getCol() + "," + pos.getRow() + ")");
                return;
            } else {
                System.out.println("[autoSpawn] Spawn failed at " + pos);
            }
        }
        System.out.println("[autoSpawn] No valid spawn position found!");
    }
    public void resetGame(GameState.Mode newMode) {
        logic.resetGame(newMode);
        this.turnManager = new TurnManager(logic);
        beginTurn("p1");
    }

}