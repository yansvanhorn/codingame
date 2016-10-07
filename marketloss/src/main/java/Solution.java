import java.util.*;
import java.io.*;
import java.math.*;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Solution {

    public static void main(String args[]) {

        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        int loss = StockLoss(() -> in.nextInt(), n);

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        System.out.println(loss);
    }

    public static int StockLoss(IntSupplier nextValue, int n) {
        int hi = 0, lo = 0;
        int thi = Integer.MIN_VALUE, tlo = Integer.MAX_VALUE;

        for (int i = 0; i < n; i++) {
            int v = nextValue.getAsInt();

            if (v > thi) {
                thi = v;
                tlo = v;
            }
            if (v < tlo) {
                tlo = v;
            }
            if (thi - tlo > hi - lo) {
                hi = thi;
                lo = tlo;
            }
        }

        return hi > lo ? lo - hi : 0;
    }
}