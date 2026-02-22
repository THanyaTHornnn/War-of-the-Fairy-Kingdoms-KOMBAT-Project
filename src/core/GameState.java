package core;

import java.util.Map;

// แค่ snapshot ข้อมูล ณ เวลานั้น ส่งออกให้ DTO / REST API
// ไม่มี logic ใดๆ ทั้งสิ้น
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


    public enum Phase { SETUP, PLAYING, ENDED }
    public enum Mode  { DUEL, SOLITAIRE, AUTO }
}