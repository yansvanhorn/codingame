import org.junit.Test;

import static org.junit.Assert.*;

public class BatmanTest {

    public int testFindBomb(int W, int H, Point start, Point bomb) {
        Player player = new Player(W, H, start.getX(), start.getY());
        int noOfPlays = 1;
        Point previous = start;
        Point current = player.play("UNKNOWN");
        noOfPlays++;

        while (true) {
            String direction = getDirection(bomb, previous, current);
            Point play = player.play(direction);
            assertFalse(current.equals(play));
            noOfPlays++;

            if (play.equals(bomb) || noOfPlays == 1000)
                break;

            previous = current;
            current = play;
        }
        return noOfPlays;
    }

    @Test
    public void testFromMarcin() {
        Point start = new Point(2320, 3800);
//        Point bomb = new Point(30, 30); 48
//        Point bomb = new Point(0, 0); // 60
//        Point bomb = new Point(3, 3); // 24, 60
//        Point bomb = new Point(5, 5); 57
        Point bomb = new Point(7954, 2359); // 25, ?
        int steps = testFindBomb(8000, 8000, start, bomb);
        System.out.println("found in steps: " + steps);
    }


    @Test
    public void testTowerIn12Steps() {
        Point start = new Point(0, 1);
        Point bomb = new Point(0, 4);
        int steps = testFindBomb(1, 100, start, bomb);
        System.out.println("found in steps: " + steps);
    }

    @Test
    public void testFindBomb02() {
        Point start = new Point(17, 31);
        Point bomb = new Point(2, 1);
        int steps = testFindBomb(18, 32, start, bomb);
        System.out.println("found in steps: " + steps);
    }

    @Test
    public void testExactNb() {
        Point start = new Point(22, 13);
        Point bomb = new Point(23, 22);
        int steps = testFindBomb(24, 24, start, bomb);
        System.out.println("found in steps: " + steps);
    }

    @Test
    public void testFindBomb1000() {
        Point start = new Point(501, 501);
        Point bomb = new Point(502, 506);
        int steps = testFindBomb(1000, 1000, start, bomb);
        System.out.println("found in steps: " + steps);
    }

    @Test
    public void testFindBomb8000() {
        Point start = new Point(3200, 2100);
        Point bomb = new Point(0, 1);
        int steps = testFindBomb(8000, 8000, start, bomb);
        System.out.println("found in steps: " + steps);
    }

    private String getDirection(Point bomb, Point prev, Point current) {
        int d1 = direction2(bomb, prev);
        int d2 = direction2(bomb, current);

        if (d2 < d1)
            return "WARMER";
        else if (d2 > d1)
            return "COLDER";
        else
            return "SAME";
    }

    private int direction2(Point d1, Point d2) {
        return (d1.getX() - d2.getX()) * (d1.getX() - d2.getX()) + (d1.getY() - d2.getY()) * (d1.getY() - d2.getY());
    }
}
