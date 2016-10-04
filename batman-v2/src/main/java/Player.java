import java.util.*;

import static java.lang.Math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 */
enum State
{
    MOVE_ORIGIN,
    FINDY,
    FINDX;
}

class Player
{

    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);
        int W = in.nextInt(); // width of the building.
        int H = in.nextInt(); // height of the building.
        int N = in.nextInt(); // maximum number of turns before game over.
        int X0 = in.nextInt();
        int Y0 = in.nextInt(); // top left is zero, zero

        System.err.println("W=" + W + ", H=" + H);

        StateMachine sm = new StateMachine(W, H);

        // game loop
        while (true) {
            Heat heat = Heat.valueOf(in.next()); // Current distance to the bomb compared to previous distance (COLDER, WARMER, SAME or UNKNOWN)

            sm.addPosWithHeat(X0, Y0, heat);
            sm.findMove();

            Pos move = sm.getMove();
            X0 = move.getX();
            Y0 = move.getY();

            System.out.println(X0 + " " + Y0);
        }
    }
}

enum Heat
{
    COLDER,
    WARMER,
    SAME,
    UNKNOWN;
}

class Pos
{
    int x, y;

    public Pos(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }
}

class StateMachine
{
    LinkedList<Pos> moves = new LinkedList();

    private final int w, h;
    private int minx, maxx, miny, maxy;

    private int x1, x2, x3;
    private int y1, y2, y3;

    private Heat heat1 = Heat.UNKNOWN, heat2 = Heat.UNKNOWN;

    boolean foundx = false, foundy = false;
    boolean ignoredMove = false;

    public StateMachine(int w, int h)
    {
        this.w = w;
        this.h = h;
        this.maxx = w - 1;
        this.maxy = h - 1;

        if (maxx - minx == 0) {
            foundx = true;
        }
        if (maxy - miny == 0) {
            foundy = true;
        }
    }

    public void addPosWithHeat(int x, int y, Heat heat)
    {

        x3 = x2;
        x2 = x1;
        x1 = x;

        y3 = y2;
        y2 = y1;
        y1 = y;

        heat2 = heat1;
        heat1 = heat;
    }

    public Pos getMove()
    {
        return moves.getLast();
    }

    public boolean findMove()
    {
        System.err.println("foundx="+foundx+", foundy="+foundy + ",ignored="+ignoredMove);
        System.err.printf("[1] x=%-4d, y=%-4d, heat1=%s\n", x1, y1, heat1);
        System.err.printf("[2] x=%-4d, y=%-4d, heat1=%s\n", x2, y2, heat2);
        System.err.printf("[3] x=%-4d, y=%-4d\n", x3, y3);

        System.err.printf("minx=%d, maxx=%d\n", minx, maxx);
        System.err.printf("miny=%d, maxy=%d\n", miny, maxy);

        switch (heat1) {
            case UNKNOWN: {

                int nx = (maxx + minx) - x1;
                int ny = (maxy + miny) - y1;

                if (abs(nx - x1) > abs(ny - y1) && nx != x1) {
                    return addMove(nx, y1);
                }
                else {
                    return addMove(x1, ny);
                }
            }

            case WARMER: {
                if (!ignoredMove) {
                    if (x2 != x1) { // x-move
                        System.err.println("WARMER: Moved X");

                        int midx = middleRounded(x2, x1);

                        if (x1 > x2) {
                            minx = Math.max(minx, midx);
                        }
                        else if (x1 < x2) {
                            maxx = Math.min(maxx, midx);
                        }
                    }
                    else if (y2 != y1) { // y-move
                        System.err.println("WARMER: Moved Y");

                        int midy = middleRounded(y2, y1);

                        if (y1 > y2) {
                            miny = Math.max(miny, midy);
                        }
                        else if (y1 < y2) {
                            maxy = Math.min(maxy, midy);
                        }
                    }
                }
                ignoredMove = false;

                // what next move ?
                int nx = (maxx + minx) - x1;
                int ny = (maxy + miny) - y1;

                System.err.printf("minx=%d, maxx=%d\n", minx, maxx);
                System.err.printf("miny=%d, maxy=%d\n", miny, maxy);
                System.err.printf("nx=%d, ny=%d\n", nx, ny);

                if (abs(nx - x1) >= abs(ny - y1) && abs(nx - x1) > 0 && nx >= 0 && nx < w) { // x wider
                    return addMove(nx, y1);

                }
                if (abs(nx - x1) <= abs(ny - y1) && abs(ny - y1) > 0 && ny >= 0 && ny < h) { // y wider
                    return addMove(x1, ny);

                }
                else { // safe move
                    ignoredMove = true;
                    return addMove(minx, miny);
                }
            }

            case COLDER: {
                if (!ignoredMove) {
                    if (x2 != x1) { // x-move
                        System.err.println("COLDER: Moved X");
                        int midx = middleRounded(x1, x2);

                        if (x1 > x2) {
                            maxx = Math.min(maxx, midx);
                        }
                        else if (x1 < x2) {
                            minx = Math.max(minx, midx);
                        }
                    }
                    else if (y2 != y1) { // y-move
                        System.err.println("COLDER: Moved Y");
                        int midy = middleRounded(y1, y2);

                        if (y1 > y2) {
                            maxy = Math.min(maxy, midy);
                        }
                        else if (y1 < y2) {
                            miny = Math.max(miny, midy);
                        }
                    }
                }
                ignoredMove = false;
                System.err.printf("minx=%d, maxx=%d\n", minx, maxx);
                System.err.printf("miny=%d, maxy=%d\n", miny, maxy);

//                switch (heat2) {
//                    case COLDER: { // move to corner
//                        System.err.println("COLDER, COLDER -> minx, miny");
//
//                        ignoredMove = true;
//                        return addMove(minx, miny);
//                    }
//
//                    default: {
                        if (x2 != x1 || foundx == true) { // last move was x -> move y
                            System.err.println("COLDER, ???: Moved X -> Moving Y");
                            int ny = (maxy + miny) - y1;

                            System.err.printf("ny=%d\n", ny);

                            if (abs(ny - y1) > 0 && ny >= 0 && ny < h) {
                                return addMove(x1, ny);
                            }
                            else { // safe move
                                ignoredMove = true;
                                return addMove(minx, miny);
                            }
                        }
                        else if (y2 != y1 || foundy == true) { // last move was y -> move x
                            System.err.println("COLDER, ???: Moved Y -> Moving X");
                            int nx = (maxx + minx) - x1;

                            System.err.printf("nx=%d\n", nx);

                            if (abs(nx - x1) > 0 && nx >= 0 && nx < w) {
                                return addMove(nx, y1);
                            }
                            else { // safe move
                                ignoredMove = true;
                                return addMove(minx, miny);
                            }
                        }
//                    }
//                }
            }

            case SAME: {
                if (x2 != x1) { // x-move
                    int nx = (x2 + x1) / 2;
                    System.err.println("Found X=" + nx);

                    foundx = true;
                    minx = nx;
                    maxx = nx;
                    ignoredMove = true;

                    return addMove(nx, y1);
                }
                else if (y2 != y1) { // y-move
                    int ny = (x2 + x1) / 2;
                    System.err.println("Found Y=" + ny);

                    foundy = true;
                    miny = ny;
                    maxy = ny;
                    ignoredMove = true;

                    return addMove(x1, ny);
                }
            }
        }

        throw new IllegalStateException("No logic applied ?!");
    }

    public int middleRounded(int v2, int v1)
    {

        return v1 > v2
               ? (int) Math.ceil((double) (v1 + v2) / 2)
               : (int) Math.floor((double) (v1 + v2) / 2);
    }

    public boolean addMove(int x, int y)
    {
        if (x < 0 || x >= w) {
            throw new IllegalStateException("X out of range?");
        }
        if (y < 0 || y >= h) {
            throw new IllegalStateException("Y out of range?");
        }

        moves.addLast(new Pos(x, y));
        return false;
    }
}

