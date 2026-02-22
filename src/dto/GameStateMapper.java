package dto;

import core.Config;
import core.GameState;
import core.Minion;
import core.Player;

import java.util.List;
import java.util.stream.Collectors;

// แปลง GameState snapshot → DTO สำหรับส่ง REST API
public class GameStateMapper {

    public static GameStateDto toDto(GameState gs) {
        GameStateDto dto = new GameStateDto();
        dto.turn      = gs.turn;
        dto.phase     = gs.phase.name();
        dto.current   = gs.current;
        dto.winner    = gs.winner;
        dto.endReason = gs.endReason;
        dto.p1        = toPlayerDto(gs.p1, gs);
        dto.p2        = toPlayerDto(gs.p2, gs);
        dto.minions   = toMinionDtos(gs);
        dto.config    = toConfigDto(gs.config);
        return dto;
    }

    private static GameStateDto.PlayerDto toPlayerDto(Player p, GameState gs) {
        GameStateDto.PlayerDto dto = new GameStateDto.PlayerDto();
        dto.budget          = p.getBudgetFloor();
        dto.turnCount       = p.getTurnCount();
        dto.spawnsUsed      = p.getSpawnsUsed();
        dto.spawnsLeft      = (int)(gs.config.maxSpawns - p.getSpawnsUsed());
        dto.spawnableHexes  = p.getSpawnableHexes();
        dto.interestRate    = p.interestRate(gs.config.interestPct);
        dto.minionCount     = p.getMinionCount();
        dto.totalHP         = p.getTotalHP();
        return dto;
    }

    private static List<GameStateDto.MinionDto> toMinionDtos(GameState gs) {
        return gs.minions.values().stream()
                .filter(Minion::isAlive)
                .map(m -> {
                    GameStateDto.MinionDto dto = new GameStateDto.MinionDto();
                    dto.id       = m.getId();
                    dto.owner    = m.getOwner().getId();
                    dto.row      = m.getPosition().getRow();
                    dto.col      = m.getPosition().getCol();
                    dto.hp       = m.getHp();
                    dto.defense  = m.getDefense();
                    dto.kind     = m.getKindName();
                    dto.spawnTurn = m.getSpawnTurn();
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private static GameStateDto.ConfigDto toConfigDto(Config c) {
        GameStateDto.ConfigDto dto = new GameStateDto.ConfigDto();
        dto.spawnCost       = c.spawnCost;
        dto.hexPurchaseCost = c.hexPurchaseCost;
        dto.initBudget      = c.initBudget;
        dto.initHp          = c.initHp;
        dto.turnBudget      = c.turnBudget;
        dto.maxBudget       = c.maxBudget;
        dto.interestPct     = c.interestPct;
        dto.maxTurns        = c.maxTurns;
        dto.maxSpawns       = c.maxSpawns;
        return dto;
    }
}