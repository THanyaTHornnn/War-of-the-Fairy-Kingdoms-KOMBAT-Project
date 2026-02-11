package controller;

import gameState.GameState;
import gameState.player.Player;


public class EndgameChecker  {

    private GameState gameState;

    public EndgameChecker(GameState gameState) {
        this.gameState = gameState;
    }

    public boolean checkEndgame() {
        Player p1 = gameState.getPlayer1();
        Player p2 = gameState.getPlayer2();

        // กรณีมีผู้เล่นคนใดคนหนึ่งไม่มี minion เหลือ
        if (p1.getMinions().isEmpty()) {
            gameState.setWinner(p2);
            return true;
        }

        if (p2.getMinions().isEmpty()) {
            gameState.setWinner(p1);
            return true;
        }

        // กรณีเทิร์นครบตามที่กำหนด
        if (gameState.getTurnCount() >= gameState.getMaxTurns()) {
            Player winner = compareWinner(p1, p2);
            gameState.setWinner(winner);
            return true;
        }

        // เกมยังไม่จบ
        return false;
    }

    //เปรียบเทียบนะจ้ะะ อย่างแรก size เช็คจำนวนมินเนี่ยน ถ้าเท่ากันไปต่อ เช็ค hp รวม ต่อไปเช็คเงิน //เรียกจากplayer class Player
    private Player compareWinner(Player p1, Player p2) {

        // 1. เทียบจำนวน minion //เรียกจากplayer medthod getMinions
        if (p1.getMinions().size() > p2.getMinions().size()) {
            return p1;
        } else if (p1.getMinions().size() < p2.getMinions().size()) {
            return p2;
        }

        // 2. เทียบผลรวม HP //เรียกจากplayer medthod totalHP
        if (p1.totalHP() > p2.totalHP()) {
            return p1;
        } else if (p1.totalHP() < p2.totalHP()) {
            return p2;
        }

        // 3. เทียบ budget //เรียกจากplayer medthod getBudget
        if (p1.getBudget() > p2.getBudget()) {
            return p1;
        } else if (p1.getBudget() < p2.getBudget()) {
            return p2;
        }

        // เสมอทุกอย่าง
        return null;
    }
}


