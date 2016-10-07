import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
public class Player {

}

class Solution {

    public static void main(String args[]) {
        int hi = Integer.MIN_VALUE, lo = Integer.MAX_VALUE;
        int thi = Integer.MIN_VALUE, tlo = Integer.MAX_VALUE;

        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        for (int i = 0; i < n; i++) {
            int v = in.nextInt();

            if(v > thi) {
                thi = v;
                tlo = v;
            }
            if(v <= tlo) {
                tlo = v;
            }
            if(v > tlo) {
                if(thi - tlo > hi - lo) {
                    hi = thi;
                    lo = tlo;
                }
            }
        }

        if(thi - tlo > hi - lo) {
            hi = thi;
            lo = tlo;
        }

        System.out.println(hi > lo ? hi - lo : 0);

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");



        System.out.println("answer");
    }
}