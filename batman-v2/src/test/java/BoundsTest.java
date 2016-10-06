import org.junit.Test;

import static java.lang.Math.floor;
import static org.junit.Assert.*;

/**
 * Created by Marcin_Bazarnik on 2016-10-06.
 */
public class BoundsTest {

    @Test
    public void testGetMiddle() throws Exception {

        Bounds bounds = new Bounds(64, 0, 64);

        assertEquals(16, bounds.getMiddle(1, 3), 0.001);
        assertEquals(20, bounds.getMiddle(5, 11), 0.001);
        assertEquals(19, (int)floor(bounds.getMiddle(5, 11)) - 1);

        bounds = new Bounds(100, 0, 100);
        assertEquals(30, (int)floor(bounds.getMiddle(5, 11)) - 1);
    }
}