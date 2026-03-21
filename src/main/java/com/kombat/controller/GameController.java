package com.kombat.controller;

import com.kombat.controller.turn.TurnManager;
import com.kombat.core.*;
import com.kombat.strategy.parser.*;
import com.kombat.strategy.ast.Stmt;
import com.kombat.strategy.ast.expr.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


// รับคำสั่งจากนอก → สั่ง TurnManager / GameLogic
@Service
public class GameController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
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

//    // ── 2. Parse / validate strategy ─────────────────────────
//    public List<Stmt> parseStrategy(String source) {
//        List<Token> tokens = new Tokenizer(source).tokenize();
//        return new Parser(tokens).parseStrategy();
//    }
//    public boolean validateStrategy(String source) {
//        try {
//            List<Token> tokens = new Tokenizer(source).tokenize();
//            new Parser(tokens).parseStrategy();
//            return true;
//        } catch (Exception e) { return false; }
//    }



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
    public TurnResult executeTurn(String playerId) {
        logic.beginTurn(playerId);
        turnManager.applyBudget(playerId);

        Player player = logic.getPlayer(playerId);
        if (player.isAuto()) {
            autoPurchaseHex(playerId);
            autoSpawnMinion(playerId);
        }

        List<TurnManager.MinionLog> logs = turnManager.executeStrategies(playerId);
        boolean over = logic.checkEndGame();

        // ── Push real-time ──
        messagingTemplate.convertAndSend("/topic/game-state", buildPayload(over));

        if (over) {
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
            executeTurn(logic.getCurrent());
            try {
                Thread.sleep(800); // หน่วงให้ frontend ทันดู
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        // broadcast จบเกม
        messagingTemplate.convertAndSend("/topic/game-state", buildPayload(true));
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
        if (kindDefense == null || kindDefense.isEmpty()) return;
        Player player = logic.getPlayer(playerId);
        long cost = logic.getConfig().spawnCost;
        if (!player.canAfford(cost)) return;
        if (player.getSpawnsUsed() >= logic.getConfig().maxSpawns) return;

        // เลือก kind แรก (หรือจะสุ่มก็ได้)
        String kind = kindDefense.keySet().iterator().next();
        int defense = kindDefense.get(kind);
        List<Stmt> ast = kindAst.get(kind);

        // หาตำแหน่งว่างใน spawnableHexes
        for (String hex : player.getSpawnableHexes()) {
            Position pos = Position.fromString(hex);
            if (logic.getMinionAt(pos) != null) continue;
            Minion m = Minion.create(kind, logic.generateMinionId(), player, pos, logic.getConfig().initHp, defense);
            m.setStrategyAST(ast);
            if (logic.spawnMinion(playerId, m)) {
                System.out.println("🤖 " + playerId + " spawn " + kind + " ที่ (" + pos.getCol() + "," + pos.getRow() + ")");
                return;   // spawn แค่ครั้งเดียว
            }
        }
    }

    //method ที่แปลง game state ให้เป็น Map เพื่อส่งผ่าน WebSocket ครับ
    private Map<String, Object> buildPayload(boolean isOver) {
        GameState s = logic.getSnapshot();

        List<Map<String, Object>> minionList = new ArrayList<>();
        for (Minion m : s.minions.values()) {
            if (!m.isAlive()) continue;
            minionList.add(Map.of(
                    "id",       m.getId(),
                    "owner",    m.getOwner().getId(),
                    "type",     m.getKindName(),
                    "hp",       m.getHp(),
                    "defense",  m.getDefense(),
                    "row",      m.getPosition().getRow(),
                    "col",      m.getPosition().getCol(),
                    "spawnTurn", m.getSpawnTurn()
            ));
        }

        return Map.of(
                "minions",   minionList,
                "p1",        Map.of(
                        "budget",         s.p1.getBudgetFloor(),
                        "hp",             s.p1.getTotalHP(),
                        "spawnsLeft",     s.config.maxSpawns - s.p1.getSpawnsUsed(),
                        "minionCount",    s.p1.getMinionCount(),
                        "spawnableHexes", s.p1.getSpawnableHexes()
                ),
                "p2",        Map.of(
                        "budget",         s.p2.getBudgetFloor(),
                        "hp",             s.p2.getTotalHP(),
                        "spawnsLeft",     s.config.maxSpawns - s.p2.getSpawnsUsed(),
                        "minionCount",    s.p2.getMinionCount(),
                        "spawnableHexes", s.p2.getSpawnableHexes()
                ),
                "turn",      s.turn,
                "current",   s.current,
                "phase",     s.phase.name(),
                "isOver",    isOver,
                "winner",    s.winner != null ? s.winner : "",
                "endReason", s.endReason != null ? s.endReason : ""
        );
    }

    // ── Parse strategy string → AST ──────────────────────────
    public List<Stmt> parseStrategy(String source) {
        try {
            List<Token> tokens = new Tokenizer(source).tokenize();
            return new Parser(tokens).parseStrategy();
        } catch (Exception e) {
            throw new RuntimeException("Parse error: " + e.getMessage());
        }
    }

    // ── Validate strategy syntax ──────────────────────────────
    public boolean validateStrategy(String source) {
        try {
            List<Token> tokens = new Tokenizer(source).tokenize();
            new Parser(tokens).parseStrategy();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public void resetGame(GameState.Mode newMode) {
        logic.resetGame(newMode);
        this.turnManager = new TurnManager(logic);
        this.kindDefense = null;
        this.kindAst = null;
    }

}