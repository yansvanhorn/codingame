import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Marcin_Bazarnik on 2016-10-05.
 */
public class BiSectionTest {

    @Test
    public void testNextMoveOddOutOfRangeMoveFromHigh() throws Exception {
        BiSection bs = new BiSection(0, 0);
        Bounds bound = new Bounds(11, 0, 4);

        assertEquals(new Move(4, false).withSafeMove(), bs.nextMove(toMoves(11), bound));
        assertEquals(new Move(4, false).withSafeMove(), bs.nextMove(toMoves(10), bound));
        assertEquals(new Move(4, false).withSafeMove(), bs.nextMove(toMoves(9), bound));
        assertEquals(new Move(4, false).withSafeMove(), bs.nextMove(toMoves(8), bound));
        assertEquals(new Move(4, false).withSafeMove(), bs.nextMove(toMoves(7), bound));
        assertEquals(new Move(0, false), bs.nextMove(toMoves(6), bound));
        assertEquals(new Move(1, false), bs.nextMove(toMoves(5), bound));
    }

    @Test
    public void testNextMoveEvenOutOfRangeMoveFromHigh() throws Exception {
        BiSection bs = new BiSection(0, 0);
        Bounds bound = new Bounds(11, 0, 5);

        assertEquals(new Move(5, false).withSafeMove(), bs.nextMove(toMoves(11), bound));
        assertEquals(new Move(5, false).withSafeMove(), bs.nextMove(toMoves(10), bound));
        assertEquals(new Move(5, false).withSafeMove(), bs.nextMove(toMoves(9), bound));
        assertEquals(new Move(5, false).withSafeMove(), bs.nextMove(toMoves(8), bound));
        assertEquals(new Move(5, false).withSafeMove(), bs.nextMove(toMoves(7), bound));
        assertEquals(new Move(0, false), bs.nextMove(toMoves(6), bound));
        assertEquals(new Move(1, false), bs.nextMove(toMoves(5), bound));
    }

    @Test
    public void testNextMoveOddOutOfRangeMoveFromLow() throws Exception {
        BiSection bs = new BiSection(0, 0);
        Bounds bound = new Bounds(11, 7, 11);

        assertEquals(new Move(7, false).withSafeMove(), bs.nextMove(toMoves(3), bound));
        assertEquals(new Move(7, false).withSafeMove(), bs.nextMove(toMoves(4), bound));
        assertEquals(new Move(11, false), bs.nextMove(toMoves(5), bound));
        assertEquals(new Move(10, false), bs.nextMove(toMoves(6), bound));
        assertEquals(new Move(11, false), bs.nextMove(toMoves(7), bound));
        assertEquals(new Move(10, false), bs.nextMove(toMoves(8), bound));
//        assertEquals(new Move(1, false), bs.nextMove(toMoves(9), bound)); // <-- what to do ?
    }

    @Test
    public void testNextMoveOddOutOfRangeMove() throws Exception {
        BiSection bs = new BiSection(0, 0);
        Bounds bound = new Bounds(11, 0, 4);

        assertEquals(new Move(4, false).withSafeMove(), bs.nextMove(toMoves(11), bound));
        assertEquals(new Move(4, false).withSafeMove(), bs.nextMove(toMoves(10), bound));
        assertEquals(new Move(4, false).withSafeMove(), bs.nextMove(toMoves(9), bound));
        assertEquals(new Move(4, false).withSafeMove(), bs.nextMove(toMoves(8), bound));
        assertEquals(new Move(4, false).withSafeMove(), bs.nextMove(toMoves(7), bound));
        assertEquals(new Move(0, false), bs.nextMove(toMoves(6), bound));
        assertEquals(new Move(1, false), bs.nextMove(toMoves(5), bound));
    }

    @Test
    public void testNextMoveEvenBound() throws Exception {
        BiSection bs = new BiSection(0, 0);
        Bounds bound = new Bounds(11, 2, 5);

//        assertEquals(0, bs.nextMove(toMoves(11), new Bounds(11, 2, 5)).v); // <- what to do now ?

        assertEquals(new Move(0), bs.nextMove(toMoves(8), bound));
        assertEquals(new Move(1), bs.nextMove(toMoves(7), bound));
        assertEquals(new Move(2), bs.nextMove(toMoves(6), bound));
        assertEquals(new Move(3), bs.nextMove(toMoves(5), bound));
        assertEquals(new Move(2), bs.nextMove(toMoves(4), bound));
        assertEquals(new Move(5), bs.nextMove(toMoves(3), bound));
        assertEquals(new Move(4), bs.nextMove(toMoves(2), bound));
        assertEquals(new Move(5), bs.nextMove(toMoves(1), bound));
        assertEquals(new Move(6), bs.nextMove(toMoves(0), bound));
    }

    @Test
    public void testNextMoveOddBound() throws Exception {
        BiSection bs = new BiSection(0, 0);
        Bounds bounds = new Bounds(11, 3, 5);

        assertEquals(new Move(5).withSafeMove(), bs.nextMove(toMoves(11), new Bounds(11, 3, 5))); // <- what to do now ?
        assertEquals(new Move(5).withSafeMove(), bs.nextMove(toMoves(10), new Bounds(11, 3, 5))); // <- what to do now ?

        assertEquals(new Move(0), bs.nextMove(toMoves(8), bounds));
        assertEquals(new Move(1), bs.nextMove(toMoves(7), bounds));
        assertEquals(new Move(2), bs.nextMove(toMoves(6), bounds));
        assertEquals(new Move(3), bs.nextMove(toMoves(5), bounds));
        assertEquals(new Move(3), bs.nextMove(toMoves(4), bounds)); // <- what to do now ?
        assertEquals(new Move(5), bs.nextMove(toMoves(3), bounds));
        assertEquals(new Move(6), bs.nextMove(toMoves(2), bounds));
        assertEquals(new Move(7), bs.nextMove(toMoves(1), bounds));
        assertEquals(new Move(8), bs.nextMove(toMoves(0), bounds));
    }

    public Move[] toMoves(int... values) {
        Move moves[] = new Move[values.length + 1];

        for(int i = 0; i < values.length; i++) {
            moves[i + 1] = new Move(values[i]);
        }
        return moves;
    }

    public Move[] toMoves(int[] values, Heat[] heats) {
        Move moves[] = new Move[values.length + 1];

        for(int i = 0; i < values.length; i++) {
            moves[i + 1] = new Move(values[i]).withHeat(heats[i]);
        }
        return moves;
    }

    @Test
    public void testShift() throws Exception {
        BiSection bs = new BiSection(0, 0);
        assertArrayEquals(new Integer[] {0, 0, 1, 2 }, bs.shift(new Integer[] { 0, 1, 2, 3}));

    }

    @Test
    public void testUpdateBounds() throws Exception {
        BiSection bs = new BiSection(0, 0);

        // warmer
        assertEquals(new Bounds(11, 5, 5), bs.updateBounds(toMoves(new int[] {6, 2, -1}, new Heat[] {Heat.WARMER, Heat.UNKNOWN, Heat.UNKNOWN}), new Bounds(11, 3, 5)));
        assertEquals(new Bounds(11, 3, 3), bs.updateBounds(toMoves(new int[] {2, 6, -1}, new Heat[] {Heat.WARMER, Heat.UNKNOWN, Heat.UNKNOWN}), new Bounds(11, 3, 5)));

        assertEquals(new Bounds(11, 5, 5), bs.updateBounds(toMoves(new int[] {5, 3, -1}, new Heat[] {Heat.WARMER, Heat.UNKNOWN, Heat.UNKNOWN}), new Bounds(11, 3, 5)));
        assertEquals(new Bounds(11, 3, 3), bs.updateBounds(toMoves(new int[] {3, 5, -1}, new Heat[] {Heat.WARMER, Heat.UNKNOWN, Heat.UNKNOWN}), new Bounds(11, 3, 5)));

        assertEquals(new Bounds(11, 4, 5), bs.updateBounds(toMoves(new int[] {4, 2, -1}, new Heat[] {Heat.WARMER, Heat.UNKNOWN, Heat.UNKNOWN}), new Bounds(11, 3, 5)));
        assertEquals(new Bounds(11, 3, 3), bs.updateBounds(toMoves(new int[] {2, 4, -1}, new Heat[] {Heat.WARMER, Heat.UNKNOWN, Heat.UNKNOWN}), new Bounds(11, 3, 5)));

        assertEquals(new Bounds(11, 5, 5), bs.updateBounds(toMoves(new int[] {6, 4, -1}, new Heat[] {Heat.WARMER, Heat.UNKNOWN, Heat.UNKNOWN}), new Bounds(11, 3, 5)));
        assertEquals(new Bounds(11, 3, 4), bs.updateBounds(toMoves(new int[] {4, 6, -1}, new Heat[] {Heat.WARMER, Heat.UNKNOWN, Heat.UNKNOWN}), new Bounds(11, 3, 5)));

        // colder
        assertEquals(new Bounds(11, 3, 3), bs.updateBounds(toMoves(new int[] {6, 2, -1}, new Heat[] {Heat.COLDER, Heat.UNKNOWN, Heat.UNKNOWN}), new Bounds(11, 3, 5)));
        assertEquals(new Bounds(11, 5, 5), bs.updateBounds(toMoves(new int[] {2, 6, -1}, new Heat[] {Heat.COLDER, Heat.UNKNOWN, Heat.UNKNOWN}), new Bounds(11, 3, 5)));

        assertEquals(new Bounds(11, 3, 3), bs.updateBounds(toMoves(new int[] {5, 3, -1}, new Heat[] {Heat.COLDER, Heat.UNKNOWN, Heat.UNKNOWN}), new Bounds(11, 3, 5)));
        assertEquals(new Bounds(11, 5, 5), bs.updateBounds(toMoves(new int[] {3, 5, -1}, new Heat[] {Heat.COLDER, Heat.UNKNOWN, Heat.UNKNOWN}), new Bounds(11, 3, 5)));

        assertEquals(new Bounds(11, 3, 3), bs.updateBounds(toMoves(new int[] {4, 2, -1}, new Heat[] {Heat.COLDER, Heat.UNKNOWN, Heat.UNKNOWN}), new Bounds(11, 3, 5)));
        assertEquals(new Bounds(11, 4, 5), bs.updateBounds(toMoves(new int[] {2, 4, -1}, new Heat[] {Heat.COLDER, Heat.UNKNOWN, Heat.UNKNOWN}), new Bounds(11, 3, 5)));

        assertEquals(new Bounds(11, 3, 4), bs.updateBounds(toMoves(new int[] {6, 4, -1}, new Heat[] {Heat.COLDER, Heat.UNKNOWN, Heat.UNKNOWN}), new Bounds(11, 3, 5)));
        assertEquals(new Bounds(11, 5, 5), bs.updateBounds(toMoves(new int[] {4, 6, -1}, new Heat[] {Heat.COLDER, Heat.UNKNOWN, Heat.UNKNOWN}), new Bounds(11, 3, 5)));


    }
}