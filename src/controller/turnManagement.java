package controller;

import gameState.GameState;
import gameState.Player;
import gameState.Minion;


public class turnManagement  {

        private GameState gameState;
        private EndgameChecker endgameChecker;

        public turnManagement(GameState gameState) {
            this.gameState = gameState;
            this.endgameChecker = new EndgameChecker(gameState);
        }

        /**
         * เรียกทุกครั้งเมื่อผู้เล่นกด End Turn
         */
        public void endTurn() {
            if (gameState.isGameOver()) {
                return;
            }

            Player currentPlayer = gameState.getCurrentPlayer();

            // เพิ่มงบประมาณประจำเทิร์น
            currentPlayer.addTurnBudget(); // เรียกใช้addTurnBudget();

            //  คิดดอกเบี้ย
            currentPlayer.applyInterest(); // เรียกใช้ applyInterest();

            // ให้ minion ของผู้เล่นนี้ทำงานตาม strategy
            for (Minion minion : currentPlayer.getMinions()) {
                if (minion.isAlive()) {
                    minion.executeStrategy(gameState);
                }
            }

            // เช็คเกมจบหรือยัง
            if (endgameChecker.checkEndgame()) {
                gameState.setGameOver(true);
                return;
            }

            // เปลี่ยนเทิร์นไปอีกฝ่าย
            gameState.switchTurn();
        }

}
