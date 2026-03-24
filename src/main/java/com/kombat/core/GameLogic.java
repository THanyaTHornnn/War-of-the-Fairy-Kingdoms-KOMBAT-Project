package com.kombat.core;

import java.util.*;

// ศูนย์กลาง: ถือ state ทั้งหมด + logic ทั้งหมดอยู่ที่นี่
public class GameLogic {

    private final Config config;
    private final GameState.Mode mode;
    private GameState.Phase phase;
    private int turn;
    private String current;
    private String winner;
    private String endReason;
    private final Player p1;
    private final Player p2;
    private final Map<String, Minion> minions = new HashMap<>();
    private int nextMinionId = 1;


    public GameLogic(Config config, GameState.Mode mode) {
        this.config  = config;
        this.mode    = mode;
        this.phase   = GameState.Phase.SETUP;
        this.turn    = 1;
        this.current = "p1";
        this.p1 = new Player("p1", mode == GameState.Mode.AUTO);
        this.p2 = new Player("p2", mode == GameState.Mode.AUTO
                || mode == GameState.Mode.SOLITAIRE);
        initSpawnZones();
        initBudgets();
    }
    private void initSpawnZones() {
        p1.addSpawnableHex(new Position(1, 1));
        p1.addSpawnableHex(new Position(1, 2));
        p1.addSpawnableHex(new Position(2, 1));
        p1.addSpawnableHex(new Position(2, 2));
        p1.addSpawnableHex(new Position(1, 3));

        p2.addSpawnableHex(new Position(8, 8));
        p2.addSpawnableHex(new Position(8, 7));
        p2.addSpawnableHex(new Position(7, 8));
        p2.addSpawnableHex(new Position(7, 7));
        p2.addSpawnableHex(new Position(8, 6));
    }

    public void initBudgets() {
        p1.setBudget(config.initBudget);
        p2.setBudget(config.initBudget);
    }

    public void startGame() {
        assertPhase(GameState.Phase.SETUP);
        this.phase = GameState.Phase.PLAYING;
    }

    public void endGame(String winner, String reason) {
        this.phase     = GameState.Phase.ENDED;
        this.winner    = winner;
        this.endReason = reason;
    }

    public void switchPlayer() {
        if ("p1".equals(current)) {
            current = "p2";
        } else {
            current = "p1";
            turn++; // ครบรอบ = p1 และ p2 เล่นแล้ว
            if (turn > config.maxTurns) {
                endGame(determineWinner(), "Max turns reached");
            }
        }
    }

    // เรียกตอนเริ่ม turn ของ player นั้น (ก่อน applyTurnBudget)
    public void beginTurn(String playerId) {
        getPlayer(playerId).incrementTurnCount();

    }

    // ── Budget ────────────────────────────────────────────────
    public void applyTurnBudget(String playerId) {
        Player player = getPlayer(playerId);
        System.out.println("[Budget] " + playerId + " before: " + player.getBudget());
        player.addBudget(config.turnBudget);
        System.out.println("[Budget] after turn_budget: " + player.getBudget());

        if (player.getBudget() >= 1 && player.getTurnCount() > 0) {
            double rate = player.interestRate(config.interestPct);
            double interest = player.getBudget() * rate / 100.0;
            player.addBudget(interest);
            System.out.println("[Budget] after interest: " + player.getBudget());
        }

        if (player.getBudget() > config.maxBudget) {
            player.setBudget(config.maxBudget);
            System.out.println("[Budget] capped to max: " + player.getBudget());
        }
    }

    // ── Hex purchase ──────────────────────────────────────────
    public boolean purchaseHex(String playerId, int row, int col) {
        Player player = getPlayer(playerId);

        int currentTurn = this.turn;
        // ตรวจสอบว่าซื้อไปแล้วในเทิร์นนี้หรือยัง
        if (player.hasPurchasedThisTurn(currentTurn)) return false;
        Position pos = new Position(row, col);

        if (player.isSpawnable(pos)) return false;
        if (getMinionAt(pos) != null) return false;
        if (!player.canAfford(config.hexPurchaseCost)) return false;

        // ต้องติดกับ hex ที่มีอยู่แล้ว
        boolean adjacent = false;
        for (String hex : player.getSpawnableHexes()) {

            Position owned = Position.fromString(hex);

            if (Board.isAdjacent(owned, pos)) {
                adjacent = true;
                break;
            }
        }
        if (!adjacent) return false;
        player.deductBudget(config.hexPurchaseCost);
        player.addSpawnableHex(pos);
        player.setLastPurchasedHex(pos);
        player.setPurchasedThisTurn(currentTurn);   // บันทึกว่าซื้อแล้ว
        return true;
    }

    // ── Spawn ───────────────────────────────────────────────
    public boolean spawnMinion(String playerId, Minion minion) {
        Player player = getPlayer(playerId);
        Position pos  = minion.getPosition();

        if (player.getSpawnsUsed() >= config.maxSpawns) return false;
        if (!player.isSpawnable(pos))                   return false;
        if (getMinionAt(pos) != null)                   return false;

        if (phase == GameState.Phase.PLAYING) {
            if (!player.canAfford(config.spawnCost)) return false;
            player.deductBudget(config.spawnCost);
        }

        player.incrementSpawnsUsed();
        minion.setSpawnTurn(turn);
        minions.put(minion.getId(), minion);
        player.addMinion(minion);
        return true;
    }

    // เพิ่มใน GameLogic.java
    public boolean move(Minion minion, int dir) {

        Player player = minion.getOwner();

        if (!player.canAfford(1))
            return false;

        // จ่ายก่อนเสมอ
        player.deductBudget(1);

        Position newPos = minion.getPosition().move(dir);

        if (!newPos.isValid()) {
            System.out.println(minion.getId() + " move ล้มเหลว: นอกขอบ");
            return false;
        }
        if (getMinionAt(newPos) != null) {
            System.out.println(minion.getId() + " move ล้มเหลว: มี minion ขวาง");
            return false;
        }
        Position old = minion.getPosition();
        minion.setPosition(newPos);

        System.out.println(minion.getId()+" ("+player.getId()+") "
                +"เดินจาก "+old+" → "+newPos);

        return true;
    }

    public boolean shoot(Minion attacker, int dir, long expenditure) {
        long cost = expenditure + 1;
        Player player = attacker.getOwner();

        if (!player.canAfford(cost))
            return false;

        player.deductBudget(cost);

        Position targetPos = attacker.getPosition().move(dir);

        if (!targetPos.isValid())
            return true;

        Minion target = getMinionAt(targetPos);

        if (target == null)
            return true;


        long actual = target.takeDamage(expenditure);
        System.out.println(attacker.getId() + " ยิง " + target.getId() + " dmg=" + actual);

        if (target.isDead())
            removeMinion(target.getId());

        return true;
    }

    public long nearby(Minion minion, int dir) {
        Position check = minion.getPosition();
        for (int dist = 1; dist <= 8; dist++) {
            check = check.move(dir);
            if (!check.isValid()) break;
            Minion m = getMinionAt(check);
            if (m != null) {
                int hpDigits  = String.valueOf(m.getHp()).length();
                int defDigits = String.valueOf(m.getDefense()).length();
                long val = 100L * hpDigits + 10L * defDigits + dist;
                return m.getOwner().getId().equals(minion.getOwner().getId()) ? -val : val;
            }
        }
        return 0;
    }

    public int findAlly(Minion minion) {
        return findClosest(minion, true);
    }

    public int findOpponent(Minion minion) {
        return findClosest(minion, false);
    }

    private int findClosest(Minion minion, boolean ally) {
        String ownerId = minion.getOwner().getId();
        int minDist = Integer.MAX_VALUE;
        int resultDir = 0;

        for (int dir = Position.UP; dir <= Position.UPLEFT; dir++) {
            Position check = minion.getPosition().move(dir);
            int dist = 1;
            while (check.isValid()) {
                Minion m = getMinionAt(check);
                if (m != null) {
                    boolean isAlly = m.getOwner().getId().equals(ownerId);
                    if (isAlly == ally) {
                        if (dist < minDist ||
                                (dist == minDist && dir < resultDir)) {
                            minDist = dist;
                            resultDir = dir;
                        }
                    }
                    break; // หยุดทิศนี้เมื่อเจอ minion แรก
                }
                check = check.move(dir);
                dist++;
            }
        }

        return minDist == Integer.MAX_VALUE ? 0 : minDist * 10 + resultDir;
    }
    // ── Apply Action (จาก Evaluator) ─────────────────────────
//    public void applyAction(Action action, Minion minion) {
//        switch (action.type) {
//            case MOVE  -> applyMove(action, minion);
//            case SHOOT -> applyShoot(action, minion);
//            case DONE  -> {}
//        }
//    }
//
//    private void applyMove(Action action, Minion minion) {
//        Player player = minion.getOwner();
//        if (!player.canAfford(1)) return;
//
//        Position newPos = minion.getPosition().move(action.direction);
//        if (!newPos.isValid())          return;
//        if (getMinionAt(newPos) != null) return;
//
//        player.deductBudget(1);
//        minion.setPosition(newPos);
//    }
//
//    private void applyShoot(Action action, Minion minion) {
//        long cost     = action.expenditure + 1;
//        Player player = minion.getOwner();
//        if (!player.canAfford(cost)) return;
//
//        player.deductBudget(cost);
//
//        Position targetPos = minion.getPosition().move(action.direction);
//        if (!targetPos.isValid()) return;
//
//        Minion target = getMinionAt(targetPos);
//        if (target == null) return;
//
//        target.takeDamage(action.expenditure);
//        if (target.isDead()) removeMinion(target.getId());
//    }


    public boolean checkEndGame() {
        int p1m = p1.getMinionCount();
        int p2m = p2.getMinionCount();

        if (p1m == 0 && p2m == 0) { endGame("tie", "All minions eliminated"); return true; }
        if (p1m == 0)              { endGame("p2",  "P1 has no minions left"); return true; }
        if (p2m == 0)              { endGame("p1",  "P2 has no minions left"); return true; }

        boolean boardFull = true;
        for (Position p : Board.allPositions()) {
            if (getMinionAt(p) == null) {
                boardFull = false;
                break;
            }
        }
        if (boardFull) {
            endGame(determineWinner(), "Board full");
            return true;
        }

        if (turn >= config.maxTurns) {
            endGame(determineWinner(), "Max turns reached");
            return true;
        }

        return false;
    }

    private String determineWinner() {
        if (p1.getMinionCount() != p2.getMinionCount())
            return p1.getMinionCount() > p2.getMinionCount() ? "p1" : "p2";
        if (p1.getTotalHP() != p2.getTotalHP())
            return p1.getTotalHP() > p2.getTotalHP() ? "p1" : "p2";
        if (p1.getBudgetFloor() != p2.getBudgetFloor())
            return p1.getBudgetFloor() > p2.getBudgetFloor() ? "p1" : "p2";
        return "tie";
    }

    // ── Minion utils ──────────────────────────────────────────
    public Minion getMinionAt(Position pos) {
        for (Minion m : minions.values())
            if (m.isAlive() && m.getPosition().equals(pos)) return m;
        return null;
    }

    public List<Minion> getMinionsByOwner(String playerId) {
        List<Minion> list = new ArrayList<>();
        for (Minion m : minions.values())
            if (m.getOwner().getId().equals(playerId) && m.isAlive()) list.add(m);
        list.sort(Comparator.comparingInt(Minion::getSpawnTurn));
        return list;
    }

    public void removeMinion(String minionId) {
        Minion m = minions.get(minionId);
        if (m != null) {
            System.out.println(" Minion " + m.getId() +
                    " (" + m.getKindName() + ") ของ " +
                    m.getOwner().getId() +
                    " ตายที่ (" +
                    m.getPosition().getCol() + "," +
                    m.getPosition().getRow() + ")");

            m.getOwner().removeMinion(minionId);
            minions.remove(minionId);
        }
    }

    public String generateMinionId() { return "m" + (nextMinionId++); }

    public Player getPlayer(String id)  { return "p1".equals(id) ? p1 : p2; }
    public Player getP1()               { return p1; }
    public Player getP2()               { return p2; }
    public Map<String, Minion> getMinions() { return minions; }
    public Config getConfig()           { return config; }
    public GameState.Phase getPhase()   { return phase; }
    public int getTurn()                { return turn; }
    public String getCurrent()          { return current; }
    public boolean isGameOver()         { return phase == GameState.Phase.ENDED; }


    public GameState getSnapshot() {
        return new GameState(turn, phase, current, winner, endReason,
                p1, p2, Collections.unmodifiableMap(new HashMap<>(minions)), config);
    }
    private void assertPhase(GameState.Phase expected) {
        if (phase != expected)
            throw new IllegalStateException("Expected " + expected + " got " + phase);
    }
    public void resetGame(GameState.Mode newMode) {
        this.phase = GameState.Phase.SETUP;
        this.turn = 1;
        this.current = "p1";
        this.winner = null;
        this.endReason = null;

        this.minions.clear();
        this.nextMinionId = 1;

        p1.reset();
        p2.reset();

        initSpawnZones();
        initBudgets();
    }
    public void resetSpawnedThisTurn(String playerId) {
        getPlayer(playerId).resetSpawnedThisTurn();
    }

    public void resetPurchasedThisTurn(String playerId) {
        getPlayer(playerId).resetPurchasedThisTurn();
    }
}