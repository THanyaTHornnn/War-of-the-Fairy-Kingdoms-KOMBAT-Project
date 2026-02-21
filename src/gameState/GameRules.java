package gameState;
import gameState.*;
import gameState.minnion.Minion;
import gameState.player.Player;

public class GameRules {

    private GameRules() {}

    public static int findOpponent(Minion me, Board board) {
        Position start = me.getPosition();

        for (int dist = 1; dist < Board.SIZE; dist++) {
            int bestDir = Integer.MAX_VALUE;

            for (Direction d : Direction.values()) {
                Position p = start.move(d, dist);
                if (!board.isInBoard(p)) continue;

                Hex h = board.getHex(p);
                if (h.isOccupied() &&
                        h.getOccupant().getOwner() != me.getOwner()) {

                    bestDir = Math.min(bestDir, d.code);
                }
            }
            if (bestDir != Integer.MAX_VALUE)
                return dist * 10 + bestDir;
        }
        return 0;
    }

    public static int findAlly(Minion me, Board board) {
        int best = Integer.MAX_VALUE;
        Position start = me.getPosition();

        for (Direction d : Direction.values()) {
            for (int dist = 1;; dist++) {
                Position p = start.move(d, dist);
                if (!board.isInBoard(p)) break;

                Hex h = board.getHex(p);
                if (h.isOccupied() &&
                        h.getOccupant().getOwner() == me.getOwner()) {

                    int val = dist * 10 + d.code;
                    best = Math.min(best, val);
                    break;
                }
            }
        }
        return best == Integer.MAX_VALUE ? 0 : best;
    }

    public static int nearby(Minion me, Direction dir, Board board) {
        Position start = me.getPosition();

        for (int dist = 1;; dist++) {
            Position p = start.move(dir, dist);
            if (!board.isInBoard(p)) break;

            Hex h = board.getHex(p);
            if (h.isOccupied()) {
                Minion m = h.getOccupant();

                int hpDigits = String.valueOf(m.getHp()).length();
                int defDigits = String.valueOf(m.getDefense()).length();

                int value = 100 * hpDigits + 10 * defDigits + dist;

                return m.getOwner() == me.getOwner() ? -value : value;
            }
        }
        return 0;
    }

    public static boolean hasNearbyOpponent(Minion me, Board board) {
        Position start = me.getPosition();

        for (Direction d : Direction.values()) {
            Position p = start.move(d, 1);
            if (!board.isInBoard(p)) continue;

            Hex h = board.getHex(p);
            if (h.isOccupied() &&
                    h.getOccupant().getOwner() != me.getOwner())
                return true;
        }
        return false;
    }


    public static boolean canMove(Minion m, Direction d, Board board) {
        Position target = m.getPosition().move(d, 1);
        if (!board.isInBoard(target)) return false;
        return !board.getHex(target).isOccupied();
    }

    public static boolean move(Minion m, Direction d, Board board) {
        Player player = m.getOwner();
        Position current = m.getPosition();
        Position target = current.move(d, 1);

        if (!player.canAfford(1)) return false;

        player.useBudget(1);
        //

        if (!board.isInBoard(target)) return false; // เดินชนขอบ (เสียเงินฟรี)
        if (board.getHex(target).isOccupied()) return false; // ช่องไม่ว่าง (เสียเงินฟรี)

        // ย้ายตำแหน่ง
        board.removeMinion(current);
        m.setPosition(target);
        board.placeMinion(m);

        return true;
    }
    public static boolean shoot(Minion shooter, Direction dir, int expenditure, Board board) {
        Player player = shooter.getOwner();
        int totalCost = expenditure + 1; // ค่าธรรมเนียม 1 + พลังโจมตี x

        if (!player.canAfford(totalCost))
            return false;
        player.useBudget(totalCost);

        Position targetPos = shooter.getPosition().move(dir, 1);
        if (!board.isInBoard(targetPos)) return true;

        Hex hex = board.getHex(targetPos);
        if (!hex.isOccupied()) return true;

        Minion target = hex.getOccupant();

        int damage = Math.max(1, expenditure - target.getDefense());
        target.takeDamage(damage);

        if (!target.isAlive()) {
            board.removeMinion(target.getPosition());
            target.getOwner().removeMinion(target);
        }

        return true;
    }
    public static boolean buyMinion(Player player, Minion m, Hex hex, int cost) {

        if (!player.canAfford(cost)) return false;
        if (hex.isOccupied()) return false;
        if (hex.getOwner() != player) return false;
        if (!hex.isSpawnable()) return false;

        player.useBudget(cost);
        player.addMinion(m);
        hex.placeMinion(m);

        return true;
    }

    public static boolean buyHex(Player player, Hex hex, Board board, int cost) {

        if (!player.canAfford(cost)) return false;
        if (hex.getOwner() != null) return false;
        if (!hex.isAdjacentToOwned(player, board)) return false;

        player.useBudget(cost);
        hex.setOwnerHex(player);
        hex.setSpawnable(true);

        return true;
    }
}

