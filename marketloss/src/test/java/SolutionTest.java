import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;

import static org.junit.Assert.*;

/**
 * Created by Marcin_Bazarnik on 2016-10-07.
 */
public class SolutionTest {

    @Test
    public void testStockLoss() throws Exception {

        internalTestLosses(-4, new int[] { 4, 3, 5, 2, 1, 6 });
        internalTestLosses(-10, new int[] { 0, 1, 5, 10, 9, 8, 7, 8, 5, 2, 0, 1, 5, 10, 20, 18, 17, 15 });
    }

    void internalTestLosses(int expected, int[] prices) {
        AtomicInteger v = new AtomicInteger(-1);

        assertEquals(expected, Solution.StockLoss(() -> prices[v.incrementAndGet()], prices.length));
    }
}