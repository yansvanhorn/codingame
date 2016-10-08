import junit.framework.TestCase;
import org.junit.Test;

public class WolffTest
        extends TestCase
{

    @Test
    public void testAllowedMoves()
            throws Exception
    {
        Wolff wolff = new Wolff().updatePos(1000, 1500);

        System.out.println(wolff.allowedMoves());
    }
}