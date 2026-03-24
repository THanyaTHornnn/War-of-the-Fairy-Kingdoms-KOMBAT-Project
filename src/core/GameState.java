package core;

import java.util.Map;

public class GameState {
    public final int turn;
    public final Phase phase;
    public final String current;
    public final String winner;
    public final String endReason;
    public final Player p1;
    public final Player p2;
    public final Map<String, Minion> minions;
    public final Config config;

    public GameState(int turn, Phase phase, String current,
                     String winner, String endReason,
                     Player p1, Player p2,
                     Map<String, Minion> minions, Config config) {
        this.turn      = turn;
        this.phase     = phase;
        this.current   = current;
        this.winner    = winner;
        this.endReason = endReason;
        this.p1        = p1;
        this.p2        = p2;
        this.minions   = minions;
        this.config    = config;
    }

    // เพิ่ม helper method
    public Player getPlayer(String playerId) {
        return "p1".equals(playerId) ? p1 : p2;
    }

    public enum Phase { SETUP, PLAYING, ENDED }
    public enum Mode  { DUEL, SOLITAIRE, AUTO }
}