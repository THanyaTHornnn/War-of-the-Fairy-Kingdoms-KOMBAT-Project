package gameState;
import gameState.minnion.Minion;

public class GameRules {

    private GameRules() {}

    public static int findOpponent(Minion me, Board board) {

        int best = Integer.MAX_VALUE;
        Position start = me.getPosition();

        for (Direction d : Direction.values()) {
            for (int dist = 1; ; dist++) {

                Position p = start.move(d, dist);
                if (!board.isInBoard(p)) break;

                Hex h = board.getHex(p);
                if (h.isOccupied() &&
                        h.getOccupant().getOwner() != me.getOwner()) {

                    int value = dist * 10 + d.code;
                    best = Math.min(best, value);
                    break; // ใกล้สุดของทิศนี้แล้ว
                }
            }
        }

        return best == Integer.MAX_VALUE ? 0 : best;
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

}
