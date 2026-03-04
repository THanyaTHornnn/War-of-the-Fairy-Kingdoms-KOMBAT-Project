package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import core.Position;
public class PositionTest {
    @Test
    void testMoveUp() {
        Position p = new Position(4,4);
        Position moved = p.move(Position.UP);

        assertEquals(3, moved.getRow());
        assertEquals(4, moved.getCol());
    }

    @Test
    void testMoveDownRightOddRow() {
        Position p = new Position(3,4); // odd row
        Position moved = p.move(Position.DOWNRIGHT);

        assertEquals(4, moved.getRow());
        assertEquals(5, moved.getCol());
    }

    @Test
    void testMoveDownRightEvenRow() {
        Position p = new Position(4,4); // even row
        Position moved = p.move(Position.DOWNRIGHT);

        assertEquals(5, moved.getRow());
        assertEquals(4, moved.getCol());
    }
}
