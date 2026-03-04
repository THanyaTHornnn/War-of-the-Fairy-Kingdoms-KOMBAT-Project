package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import core.Position;
import core.Board;
public class BoardAdjacencyTest {
    @Test
    void testAdjacentUsingMove() {
        Position a = new Position(4,4);

        for (int dir = Position.UP; dir <= Position.UPLEFT; dir++) {
            Position neighbor = a.move(dir);

            assertTrue(
                    Board.isAdjacent(a, neighbor),
                    "Should be adjacent but is not for direction: " + dir
            );
        }
    }

    @Test
    void testNotAdjacent() {
        Position a = new Position(4,4);
        Position far = new Position(6,6);

        assertFalse(Board.isAdjacent(a, far));
    }
}
