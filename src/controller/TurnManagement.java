package controller;

import gameState.GameState;
import gameState.minnion.Minion;
import gameState.player.Player;
import strategy.evaluator.StrategyEvaluator;
import strategy.evaluator.StrategyEvaluatorImpl;


public class TurnManagement {

    private GameState gameState;
    private EndgameChecker endgameChecker;
    private StrategyEvaluator evaluator;

    public TurnManagement(GameState gameState) {
        this.gameState = gameState;
        this.endgameChecker = new EndgameChecker(gameState);
        this.evaluator = new StrategyEvaluatorImpl(); //  ติดปัญหาของวุ้นเส้นค่อยแก้
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
        currentPlayer.applyInterest(gameState.getConfig().interestPct);


        // ให้ minion ของผู้เล่นนี้ทำงานตาม strategy
        for (Minion minion : currentPlayer.getMinions()) {
            if (minion.isAlive()) {
                evaluator.evaluate(minion, gameState);// แดงอยู่ไอ่วุ้นเช็คหน่อยนะะ
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