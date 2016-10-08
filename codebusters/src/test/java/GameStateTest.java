import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GameStateTest
{

    @Test
    public void testMoveBackFrom()
            throws Exception
    {
        Location buster = new Location(500, 500);
        Location from = new Location(1500, 1500);
        Pos pos = Calculate.moveToDistance(buster, from, from, 1000);
        System.out.println(pos);
        System.out.println(pos.distance(from));

        List<Integer> collect = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        Collections.shuffle(collect);

        System.out.println(collect);

        Quadrant quadrant = new Quadrant(16, 9);
        Pos pos1 = quadrant.toQ(new Location(15001, 8001));
        Pos pos2 = quadrant.fromQ(pos1);
    }
}