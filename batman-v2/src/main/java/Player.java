import java.util.*;

import static java.lang.Math.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 */
class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int W = in.nextInt(); // width of the building.
        int H = in.nextInt(); // height of the building.
        int N = in.nextInt(); // maximum number of turns before game over.
        int X0 = in.nextInt();
        int Y0 = in.nextInt(); // top left is zero, zero

        System.err.println("W=" + W + ", H=" + H);

        BiSection bs = new BiSection(W - 1, H - 1).withStart(X0, Y0);

        // game loop
        while (true) {
            Heat heat = Heat.valueOf(in.next()); // Current distance to the bomb compared to previous distance (COLDER, WARMER, SAME or UNKNOWN)

            bs.addHeat(heat);
            MovePair move = bs.findMove();
//            System.err.println(move);
            System.out.println(move.x.v + " " + move.y.v);
        }
    }
}

enum Heat {
    COLDER,
    WARMER,
    SAME,
    UNKNOWN;
}

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
    }

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
        this.hi = max;
    }

    public Bounds(int max, int lo, int hi) {
        this.max = max;
        this.lo = lo;
        this.hi = hi;
    }

    public int size() {
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

    @Override
    public String toString() {
        return "MovePair{" +
                "\nx=" + x +
                ", \ny=" + y +
                ", \naxis=" + axis +
                '}';
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

    public BiSection addHeat(Heat heat) {
        if (last == Axis.X || last == Axis.XY) {
            x[1].setHeat(heat);
        }
        if (last == Axis.Y || last == Axis.XY) {
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
//        outputState();

        MovePair movePair = internalFindMove();
        last = movePair.axis;

        if (last == Axis.X || last == Axis.XY) {
            shift(x);
            x[1] = movePair.x;
        }
        if (last == Axis.Y || last == Axis.XY) {
            shift(y);
            y[1] = movePair.y;
        }

//        outputState();
        return movePair;
    }

    public MovePair internalFindMove() {

        // use result
        if (last == Axis.X || last == Axis.XY) {
            updateBounds(x, xb);
        }
        if (last == Axis.Y || last == Axis.XY) {
            updateBounds(y, yb);
        }

        Move nx = nextMove(x, xb);
        Move ny = nextMove(y, yb);

        System.err.println("NextMove X: " + nx);
        System.err.println("NextMove Y: " + ny);

        if(nx.found && ny.found) {
            return new MovePair(Axis.XY, nx, ny);
        }
        else if (!nx.isSafeMove() && !ny.isSafeMove()) {
            int xsize = xb.size();
            int ysize = yb.size();

            if (xsize > ysize) {
                return new MovePair(Axis.X, nx, y[1]);
            } else {
                return new MovePair(Axis.Y, x[1], ny);
            }
        } else if (nx.isSafeMove() && !ny.isSafeMove() && !ny.found) {
            return new MovePair(Axis.Y, x[1], ny);

        } else if (!nx.isSafeMove() && ny.isSafeMove() && !nx.found) {
            return new MovePair(Axis.X, nx, y[1]);

        } else {
            return new MovePair(Axis.XY, nx, ny);
        }
    }

    public Bounds updateBounds(Move[] values, Bounds bounds) {

        switch (values[1].heat) {
            case COLDER: {

                int mid = middleOptimalRounded(values[1].v, values[2].v);
                if (values[1].v > values[2].v) {
                    bounds.updateHi(mid);
                } else if (values[1].v < values[2].v) {
                    bounds.updateLo(mid);
                }

                break;
            }

            case WARMER: {

                int mid = middleOptimalRounded(values[2].v, values[1].v);
                if (values[1].v > values[2].v) {
                    bounds.updateLo(mid);

                } else if (values[1].v < values[2].v) {
                    bounds.updateHi(mid);
                }

                break;
            }

            case SAME: {
                int mid = (values[1].v + values[2].v) / 2;

                bounds.updateHi(mid);
                bounds.updateLo(mid);
            }
        }

        return bounds;
    }

    public int middleOptimalRounded(int v2, int v1) {

        if (v1 > v2) {
            if (Math.abs(v1 - v2) % 2 == 0) {
                return (v1 + v2) / 2 + (int) signum(v1 - v2);
            } else {
                return (int) Math.ceil((double) (v1 + v2) / 2);
            }
        } else if (v1 < v2) {
            if (Math.abs(v1 - v2) % 2 == 0) {
                return (v1 + v2) / 2 + (int) signum(v1 - v2);
            } else {
                return (int) Math.floor((double) (v1 + v2) / 2);
            }
        } else {
            return v1;
        }
    }

    public Move nextMove(Move[] values, Bounds bound) {

        int window = bound.size();
        double boundMiddle = bound.getMiddle();
        int boundMiddleInt;

        Move value = values[1];
        int v = value.v;
        int nv = -1;

        if (window == 1) {
            return new Move((int) boundMiddle, true);
        }

        if (window % 2 == 0) {
//            boundMiddleInt = (int)ceil(boundMiddle) != v ? (int)ceil(boundMiddle) : (int)floor(boundMiddle);

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

        // If optimal not found, find second optimal
        if (nv < 0) {
            int newnw = 2 * (boundMiddleInt + 1) - v;
            if (bound.hi - boundMiddleInt >= 0.4 * (double) window) {
                nv = newnw;
            }
        } else if (nv > bound.max) {
            int newnw = 2 * (boundMiddleInt - 1) - v;
            if (boundMiddleInt - bound.lo >= 0.4 * (double) window) {
                nv = newnw;
            }
        }

        if (v == nv) {
            return new Move(v == bound.lo ? bound.hi : bound.lo);
        }

        if (bound.isInMax(nv)) {
            return new Move(nv, false);
        }

        return new Move(v > boundMiddleInt ? bound.hi : bound.lo).withSafeMove();
    }

    private void outputState() {
        for (int i = 1; i < 3; i++) {
            System.err.printf("[X%d]: %s\n", i, x[i]);
        }
        for (int i = 1; i < 3; i++) {
            System.err.printf("[Y%d]: %s\n", i, y[i]);
        }
        System.err.println("xb=" + xb);
        System.err.println("yb=" + yb);
        System.err.println("lastAxis=" + last);
    }
}