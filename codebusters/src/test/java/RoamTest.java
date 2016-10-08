import org.junit.Test;

import java.util.Arrays;

public class RoamTest
{
    @Test
    public void testRoam()
            throws Exception
    {
        Roam roam = new Roam(new GameState(0, 0));

        int[] shuffle = roam.shuffle(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        System.out.println(Arrays.toString(shuffle));

        Pos busterA = new Location(0, 0);
        Pos busterB = new Location(0, 0);
        Pos busterC = new Location(0, 0);
        Pos busterD = new Location(0, 0);
        Pos roamA = busterA, roamB = busterB, roamC = busterC, roamD = busterD;

        Quadrant quadrant = new Quadrant();
        for(int i = 0; i < 200; i++) {
            roam.visit(busterA);
            roam.visit(busterB);
            roam.visit(busterC);
            roam.visit(busterD);

            roamA = busterA.distance(roamA) < 400 ? roam.roam(new Buster(null, 0, 0, busterA.getX(), busterA.getY(), 0, 0)) : roamA;
            roamB = busterB.distance(roamB) < 400 ? roam.roam(new Buster(null, 0, 0, busterB.getX(), busterB.getY(), 0, 0)) : roamB;
//            roamC = busterC.distance(roamC) < 400 ? roam.roam(new Buster(null, 0, 0, busterC.getX(), busterC.getY(), 0, 0)) : roamC;
//            roamD = busterD.distance(roamD) < 400 ? roam.roam(new Buster(null, 0, 0, busterD.getX(), busterD.getY(), 0, 0)) : roamD;

            System.out.println("roamA " + quadrant.toQ(roamA) + " / " + roamA);
            System.out.println("roamB " + quadrant.toQ(roamB) + " / " + roamB);
            System.out.println("roamC " + quadrant.toQ(roamC) + " / " + roamC);
            System.out.println("roamD " + quadrant.toQ(roamD) + " / " + roamD);

            busterA = busterA.add(roamA.subtract(busterA).normalize(800));
            busterB = busterB.add(roamB.subtract(busterB).normalize(800));
            busterC = busterC.add(roamC.subtract(busterC).normalize(800));
            busterD = busterD.add(roamD.subtract(busterD).normalize(800));

            System.out.println("busterA " + quadrant.toQ(busterA) + " / " + busterA);
            System.out.println("busterB " + quadrant.toQ(busterB) + " / " + busterB);
            System.out.println("busterC " + quadrant.toQ(busterC) + " / " + busterC);
            System.out.println("busterD " + quadrant.toQ(busterD) + " / " + busterD);
            System.out.println(roam.toString());
        }

    }
}