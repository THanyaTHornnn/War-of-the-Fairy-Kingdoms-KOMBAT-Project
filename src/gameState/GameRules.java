package gameState;
import gameState.minnion.Minion;
import gameState.player.Player;

public class GameRules {

    private GameRules() {}

    public static int findOpponent(Minion me, Board board) {
        Position start = me.getPosition();

        for (int dist = 1; dist < Board.SIZE; dist++) {
            int bestCodeThisDist = Integer.MAX_VALUE;

            for (Direction d : Direction.values()) {
                Position p = start.move(d, dist);
                if (!board.isInBoard(p)) continue;

                Hex h = board.getHex(p);
                if (h.isOccupied() && h.getOccupant().getOwner() != me.getOwner()) {
                    bestCodeThisDist = Math.min(bestCodeThisDist, d.code);
                }
            }
            if (bestCodeThisDist != Integer.MAX_VALUE) {
                return dist * 10 + bestCodeThisDist;
            }
        }
        return 0;
    }

    public static int findAlly(Minion me, Board board) {

        int best = Integer.MAX_VALUE;
        Position start = me.getPosition();

        for (Direction d : Direction.values()) {
            for (int dist = 1; ; dist++) {

                Position p = start.move(d, dist);
                if (!board.isInBoard(p)) break;

                Hex h = board.getHex(p);
                if (h.isOccupied() &&
                        h.getOccupant().getOwner() == me.getOwner()) {

                    int value = dist * 10 + d.code;
                    best = Math.min(best, value);
                    break;
                }
            }
        }

        return best == Integer.MAX_VALUE ? 0 : best;
    }
    public static boolean hasNearbyOpponent(Minion me, Board board) {

        Position start = me.getPosition();

        for (Direction d : Direction.values()) {
            Position p = start.move(d, 1);
            if (!board.isInBoard(p)) continue;

            Hex h = board.getHex(p);
            if (h.isOccupied() &&
                    h.getOccupant().getOwner() != me.getOwner()) {
                return true;
            }
        }
        return false;
    }
    public static boolean canMove(Minion m, Direction d, Board board) {
        Position current = m.getPosition();
        Position target = current.move(d, 1);

        if (!board.isInBoard(target)) return false;

        Hex hex = board.getHex(target);
        return !hex.isOccupied();
    }
    public static boolean buyMinion(Player player, Minion m, Hex hex, int cost) {

        if(!player.canAfford(cost)) return false;
        if(hex.isOccupied()) return false;
        if(hex.getOwner()!=player) return false;

        player.useBudget(cost);
        player.addMinion(m);
        hex.placeMinion(m);

        return true;
    }
    public static boolean buyHex(Player player, Hex hex, int cost){

        if(!player.canAfford(cost)) return false;
        if(!hex.isSpawnable()) return false;
        if(hex.getOwner()!=null) return false;

        player.useBudget(cost);
        hex.setOwner(player);

        return true;
    }
    public static boolean move(Minion minion, Direction dir, Board board) {

        Position current = minion.getPosition();
        Position targetPos = current.move(dir, 1);

        if (!board.isInBoard(targetPos)) return false;

        Hex target = board.getHex(targetPos);
        if (target.isOccupied()) return false;

        board.removeMinion(current);
        minion.setPosition(targetPos);
        board.placeMinion(minion);

        return true;
    }
    public static boolean shoot(Minion shooter, Direction dir, long dmg, Board board) {

        Position current = shooter.getPosition();
        Position targetPos = current.move(dir, 1);

        if (!board.isInBoard(targetPos)) return false;

        Hex hex = board.getHex(targetPos);
        if (!hex.isOccupied()) return false;

        Minion target = hex.getOccupant();
        target.takeDamage((int) dmg);

        if (!target.isAlive()) {
            board.removeMinion(target.getPosition());
            target.getOwner().removeMinion(target);
        }

        return true;
    }

    //




}
