import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Solution {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int N = in.nextInt();
        int price = in.nextInt();

        int[] budget = new int[N];

        int sum = 0;
        for (int i = 0; i < N; i++) {
            budget[i] = in.nextInt();
            sum += budget[i];
        }

        if(sum < price) {
            System.out.println("IMPOSSIBLE");

        } else {
            Arrays.sort(budget);
            for (int i = 0, contribution; i < budget.length && sum > 0; i++) {
                price -= contribution =
                        Math.min(
                                Math.min(price / (N - i), budget[i]),
                                price
                        );

                System.out.println(contribution);
            }
        }

    }
}