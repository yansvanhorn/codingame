import java.util.*;

import static java.lang.Math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 */
enum State {
    MOVE_ORIGIN,
    FINDY,
    FINDX;
}

class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int W = in.nextInt(); // width of the building.
        int H = in.nextInt(); // height of the building.
        int N = in.nextInt(); // maximum number of turns before game over.
        int X0 = in.nextInt();
        int Y0 = in.nextInt(); // top left is zero, zero

        System.err.println("W=" + W + ", H=" + H);

        BiSection bs = new BiSection(W, H).withStart(X0, Y0);

        // game loop
        while (true) {
            Heat heat = Heat.valueOf(in.next()); // Current distance to the bomb compared to previous distance (COLDER, WARMER, SAME or UNKNOWN)


            bs.addHeat(heat, Axis.XY);

//            Pos move = bs.findMove();
//            X0 = move.getX();
//            Y0 = move.getY();

            System.out.println(X0 + " " + Y0);
        }
    }
}

enum Heat {
    COLDER,
    WARMER,
    SAME,
    UNKNOWN;
}

class Pos {
    int x, y;

    public Pos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

class StateMachine {
    LinkedList<Pos> moves = new LinkedList();

    private final int w, h;
    private int minx, maxx, miny, maxy;

    private int x1, x2, x3;
    private int y1, y2, y3;

    private Heat heat1 = Heat.UNKNOWN, heat2 = Heat.UNKNOWN;

    boolean foundx = false, foundy = false;
    boolean ignoredMove = false;

    public StateMachine(int w, int h) {
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

    public void addPosWithHeat(int x, int y, Heat heat) {

        x3 = x2;
        x2 = x1;
        x1 = x;

        y3 = y2;
        y2 = y1;
        y1 = y;

        heat2 = heat1;
        heat1 = heat;
    }

    public Pos getMove() {
        return moves.getLast();
    }

    public boolean findMove() {
        System.err.println("foundx=" + foundx + ", foundy=" + foundy + ",ignored=" + ignoredMove);
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
                } else {
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
                        } else if (x1 < x2) {
                            maxx = Math.min(maxx, midx);
                        }
                    } else if (y2 != y1) { // y-move
                        System.err.println("WARMER: Moved Y");

                        int midy = middleRounded(y2, y1);

                        if (y1 > y2) {
                            miny = Math.max(miny, midy);
                        } else if (y1 < y2) {
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

                } else { // safe move
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
                        } else if (x1 < x2) {
                            minx = Math.max(minx, midx);
                        }
                    } else if (y2 != y1) { // y-move
                        System.err.println("COLDER: Moved Y");
                        int midy = middleRounded(y1, y2);

                        if (y1 > y2) {
                            maxy = Math.min(maxy, midy);
                        } else if (y1 < y2) {
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
                    } else { // safe move
                        ignoredMove = true;
                        return addMove(minx, miny);
                    }
                } else if (y2 != y1 || foundy == true) { // last move was y -> move x
                    System.err.println("COLDER, ???: Moved Y -> Moving X");
                    int nx = (maxx + minx) - x1;

                    System.err.printf("nx=%d\n", nx);

                    if (abs(nx - x1) > 0 && nx >= 0 && nx < w) {
                        return addMove(nx, y1);
                    } else { // safe move
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
                } else if (y2 != y1) { // y-move
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

    public int middleRounded(int v2, int v1) {

        return v1 > v2
                ? (int) Math.ceil((double) (v1 + v2) / 2)
                : (int) Math.floor((double) (v1 + v2) / 2);
    }

    public boolean addMove(int x, int y) {
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
//
//enum Axis {
//    X, Y;
//}

//class MoveXY
//{
//    final int x, y;
////    Axis axis;
//    Heat heat;
//
//    public MoveXY(int x, int y) //, Axis axis)
//    {
//        this.x = x;
//        this.y = y;
//    }
//
//    public void setHeat(Heat heat) {
//        this.heat = heat;
//    }
//
//    // No getters
//}

class Move {
    final int v;
    Heat heat;
    boolean found = false;
    boolean safeMove = false;

    public Move(int v) {
        this.v = v;
    }

    public Move(int v, boolean found) {
        this.v = v;
        this.found = found;
    }

    public Move withSafeMove() {
        this.safeMove = true;
        return this;
    };

    public void setSafeMove(boolean safeMove) {
        this.safeMove = safeMove;
    }

    public void setHeat(Heat heat) {
        this.heat = heat;
    }

    public boolean isSafeMove() {
        return safeMove;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move move = (Move) o;

        if (v != move.v) return false;
        if (found != move.found) return false;
        return safeMove == move.safeMove;

    }

    @Override
    public int hashCode() {
        int result = v;
        result = 31 * result + (found ? 1 : 0);
        result = 31 * result + (safeMove ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Move{" +
                "v=" + v +
                ", heat=" + heat +
                ", found=" + found +
                ", safeMove=" + safeMove +
                '}';
    }

    public Move withHeat(Heat heat) {
        this.heat = heat;
        return this;
    }
}

class Bounds {
    int lo, hi;
    int max;

    public Bounds(int max) {
        this.max = max;
        this.lo = 0;
        this.hi = max - 1;
    }

    public Bounds(int max, int lo, int hi) {
        this.max = max;
        this.lo = lo;
        this.hi = hi;
    }

    public int getWindow() {
        return hi - lo + 1;
    }

    public double getMiddle() {
        return ((double) (hi + lo)) / 2;
    }

    boolean isInMax(int v) {
        return v >= 0 && v <= max;
    }

    public int updateLo(int mid) {
        lo = min(max(lo, mid), hi);
        return lo;
    }

    public int updateHi(int mid) {
        hi = max(min(hi, mid), lo);
        return hi;
    }

    @Override
    public String toString() {
        return "Bounds{" +
                "lo=" + lo +
                ", hi=" + hi +
                ", max=" + max +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bounds bounds = (Bounds) o;

        if (lo != bounds.lo) return false;
        if (hi != bounds.hi) return false;
        return max == bounds.max;

    }

    @Override
    public int hashCode() {
        int result = lo;
        result = 31 * result + hi;
        result = 31 * result + max;
        return result;
    }
}

enum Axis {
    X,
    Y, XY;
}

class MovePair {
    Move x;
    Move y;
    Axis axis;

    public MovePair(Axis axis, Move x, Move y) {
        this.x = x;
        this.axis = axis;
        this.y = y;
    }
}

class BiSection {

    Move x[] = new Move[4];
    Move y[] = new Move[4];

    Bounds xb, yb;

    Axis last = Axis.XY;

    public BiSection(int w, int h) {
        xb = new Bounds(w);
        yb = new Bounds(h);
    }

    public BiSection withStart(int x, int y) {
        this.x[1] = new Move(x);
        this.y[1] = new Move(y);
        return this;
    }

    public BiSection addHeat(Heat heat, Axis axis) {
        if(axis == Axis.X || axis == Axis.XY) {
            x[1].setHeat(heat);
        }
        if(axis == Axis.Y || axis == Axis.XY) {
            y[1].setHeat(heat);
        }
        return this;
    }

    public <T> T[] shift(T[] values) {
        for (int i = values.length - 2; i >= 0; i--) {
            values[i + 1] = values[i];
        }
        return values;
    }

    public MovePair findMove() {

        // use result
        if(last == Axis.X || last == Axis.XY) {
            updateBounds(x, xb);
        }
        if(last == Axis.Y || last == Axis.XY) {
            updateBounds(y, yb);
        }

        return null;
    }

    public Bounds updateBounds(Move[] values, Bounds bounds) {

        switch (values[1].heat) {
            case COLDER: {

                int mid = middleRounded(values[1].v, values[2].v);
                if (values[1].v > values[2].v) {
                    bounds.updateHi(mid);
                } else if (values[1].v < values[2].v) {
                    bounds.updateLo(mid);
                }

                break;
            }

            case WARMER: {

                int mid = middleRounded(values[2].v, values[1].v);
                if (values[1].v > values[2].v) {
                    bounds.updateLo(mid);

                } else if (values[1].v < values[2].v) {
                    bounds.updateHi(mid);
                }

                break;
            }

            case SAME: {
                int mid = (values[1].v + values[2].v)/2;

                bounds.updateHi(mid);
                bounds.updateLo(mid);
            }
        }

        return bounds;
    }

    public int middleRounded(int v2, int v1) {

        if(v1 > v2) {
            if(Math.abs(v1 - v2) % 2 == 0) {
                return (v1 + v2) / 2 + (int)signum(v1 - v2);
            } else {
                return (int) Math.ceil((double) (v1 + v2) / 2);
            }
        } else if(v1 < v2){
            if(Math.abs(v1 - v2) % 2 == 0) {
                return (v1 + v2) / 2 + (int)signum(v1 - v2);
            } else {
                return (int) Math.floor((double) (v1 + v2) / 2);
            }
        } else {
            return v1;
        }
    }

    public Move nextMove(Move[] values, Bounds bound) {

        int window = bound.getWindow();
        double boundMiddle = bound.getMiddle();
        int boundMiddleInt;

//        NextMoveState state = USE_NV;
        Move value = values[1];
        int v = value.v;
        int nv = -1;

        if (window == 1) {
            return new Move((int) boundMiddle, true);
        }

        if (window % 2 == 0) {
            if (ceil(boundMiddle) < v) {
                boundMiddleInt = (int) ceil(boundMiddle);
            } else if (floor(boundMiddle) < v) {
                boundMiddleInt = (int) floor(boundMiddle);
            } else if (floor(boundMiddle) > v) {
                boundMiddleInt = (int) floor(boundMiddle);
            } else if (ceil(boundMiddle) > v) {
                boundMiddleInt = (int) ceil(boundMiddle);
            } else {
                throw new IllegalStateException("Cannot calculate boundMiddleInt!");
            }

            nv = 2 * boundMiddleInt - v;
        } else if (window % 2 != 0) {
            boundMiddleInt = (int) boundMiddle;
            nv = 2 * boundMiddleInt - v;
         } else {
            throw new IllegalStateException("Unknown window");
        }

        System.out.println("nv = " + nv);

        if(nv < 0) {
            int newnw = 2 * (boundMiddleInt + 1) - v;
            if(bound.hi - boundMiddleInt >= 0.4 * (double)window) {
                nv = newnw;
            }
        } else if(nv > bound.max) {
            int newnw = 2 * (boundMiddleInt - 1) - v;
            if(boundMiddleInt - bound.lo >= 0.4 * (double)window) {
                nv = newnw;
            }
        }

        System.out.println("final nv = " + nv);

        if(v == nv) {
            return new Move(bound.lo, false);
        }

        if (bound.isInMax(nv)) {
            return new Move(nv, false);
        }

        if(v > boundMiddleInt) {
            return new Move(bound.hi).withSafeMove();
        } else {
            return new Move(bound.lo).withSafeMove();
        }


//        if (nv < 0) {
//            int mid = (0 + v) / 2;
//            if(mid <= bound.hi) {
//                return new Move(0, false);
//            } else {
//                return new Move(bound.hi, false).withSafeMove();
//            }
//        } else { // if (v < boundMiddleInt) {
//            int mid = (bound.max + v) / 2;
//            if(mid >= bound.lo) {
//                return new Move(bound.max, false);
//            } else {
//                return new Move(bound.lo, false).withSafeMove();
//            }
//        }
   }
}