import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Marcin_Bazarnik on 2016-10-11.
 */
public class EnemyTest {

    @Test
    public void testTurnsTo_Killed() throws Exception {
        DataPoint dataPoint = new DataPoint(1, 500, 500);
        Enemy enemy = new Enemy(2, 10, 3500, 500);
        enemy.assignDataPoint(dataPoint);

        int turnsToDp = enemy.turnsFromDataPoint();
        int turnsToKill = enemy.turnsToKillBy(new Wolff().updatePos(500, 6000));

        assertEquals(6, turnsToDp);
        assertEquals(4, turnsToKill);
    }

    @Test
    public void testTurnsTo_ReachDP() throws Exception {
        DataPoint dataPoint = new DataPoint(1, 500, 500);
        Enemy enemy = new Enemy(2, 30, 3500, 500);
        enemy.assignDataPoint(dataPoint);

        int turnsToDp = enemy.turnsFromDataPoint();
        int turnsToKill = enemy.turnsToKillBy(new Wolff().updatePos(500, 6000));

        assertEquals(6, turnsToDp);
        assertEquals(6, turnsToKill);
    }

    @Test
    public void testTurnsTo_Killed_Battle() throws Exception {
        DataPoint dataPoint = new DataPoint(1, 1099, 300);
        Enemy enemy = new Enemy(2, 50, 7200, 0);
        enemy.assignDataPoint(dataPoint);

        int turnsToDp = enemy.turnsFromDataPoint();
        int turnsToKill = enemy.turnsToKillBy(new Wolff().updatePos(8000, 8999));

        assertEquals(12, turnsToDp);
        assertEquals(43, turnsToKill);
    }
}