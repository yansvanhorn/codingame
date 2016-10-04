import java.util.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
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

        boolean startedFromOrigin = X0 == 0 && Y0 == 0;

        State state = X0 == 0 && Y0 == 0 ? State.FINDY : State.MOVE_ORIGIN;

        StateMachine ymachine = new StateMachine(H);
        StateMachine xmachine = new StateMachine(W);

        System.err.println("W=" + W + ", H=" + H);

        // game loop
        while (true) {
            Heat heat = Heat.valueOf(in.next()); // Current distance to the bomb compared to previous distance (COLDER, WARMER, SAME or UNKNOWN)

            switch (state) {
                case MOVE_ORIGIN:
                    System.err.println("Go to origin");
                    X0 = 0;
                    Y0 = 0;
                    state = State.FINDY;
                    break;

                case FINDY: {
                    ymachine.addPos(Y0, heat);
                    if (ymachine.findMove()) {
                        System.err.println("Found Y!");
                        state = State.FINDX;
                    }

                    int move = ymachine.getMove();
                    if (move != Y0) {
                        Y0 = move;
                    }
                    if (state == State.FINDY) {
                        break;
                    }
                }

                case FINDX: {
                    xmachine.addPos(X0, heat);
                    if (xmachine.findMove()) {
                        System.err.println("Found X!");
                    }

                    int move = xmachine.getMove();
                    if (move != X0) {
                        X0 = move;
                    }
                }
            }
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

class StateMachine {
//    LinkedList<Integer> poss = new LinkedList<Integer>();
//    LinkedList<Heat> heats = new LinkedList<Heat>();

    LinkedList<Integer> moves = new LinkedList();

    private int positionCount = 0;

    private final int max;
    private int hmin = 0;
    private int hmax = 0;

    private int last1offset = 0;
    private int last2offset = 0;
    private int last3offset = 0;
    private int last1pos = -1;
    private int last2pos = -1;
    private int last3pos = -1;
    private Heat last1heat = Heat.UNKNOWN, last2heat = Heat.UNKNOWN;

    public StateMachine(int max) {
        this.max = max;
        this.hmax = max - 1;
    }

    public void addPos(int pos, Heat heat) {
        positionCount++;

        last3pos = last2pos;
        last2pos = last1pos;
        last1pos = pos;

        last2heat = last1heat;
        last1heat = heat;
    }

    public int getMove() {
        return moves.getLast();
    }

    public boolean findMove() {
        System.err.printf("last1pos=%d, last1offset=%d, last1heat=%s\n", last1pos, last1offset, last1heat);
        System.err.printf("last2pos=%d, last2offset=%d, last2heat=%s\n", last2pos, last2offset, last2heat);
        System.err.printf("last3pos=%d, last3offset=%d\n", last3pos, last3offset);
        System.err.printf("hmax=%d, hmin=%d, hmax-hmin+1=%d\n", hmax, hmin, hmax - hmin + 1);

        // First move (second position)
        if (positionCount == 1) {
            System.err.println("First move");
            if (last1pos != 0) {
                throw new IllegalArgumentException("Wrong start position");
            }
            if (max == 1) {
                System.err.println("To narrow, no need to find!");
                return addMove(last1pos, 0, true);
            }

            last1heat = Heat.UNKNOWN; // reset heat
            return addMove(hmax, 0, false); // need offset when %2
        }

        int last1direction = (int) Math.signum(last1pos - last2pos);
        int last2direction = (int) Math.signum(last2pos - last3pos);

        switch (last1heat) {
            case COLDER: {
                if (last2heat == Heat.COLDER) {
                    throw new IllegalStateException("Colder, Colder :-/");
                }

                int middle = middleRounded(last1pos - last1offset, last2pos - last2offset);

                if (last1pos > last2pos) {
                    hmax = Math.min(hmax, middle);
                } else if (last1pos < last2pos) {
                    hmin = Math.max(hmin, middle);
                }

                int last1elements = hmax - hmin + 1;
                System.err.printf("COLD: middle=%d, hmin=%d, hmax=%d, last1elements=%d\n", middle, hmin, hmax, last1elements);

                if (last1elements <= 2) { // <- check this
                    return addMove(last2pos, 0, true); // previous position is actually solution
                } else {
                    return addMove(middle, 0, false);
                }
            }
            case WARMER: {

                switch (last2heat) {
                    case COLDER: {
                        int last1elements = hmax - hmin + 1;
                        System.err.printf("WARM <- COLD: hmin=%d, hmax=%d, last1elements=%d\n", hmin, hmax, last1elements);

                        if (last1elements <= 3) {
                            return addMove(last3pos, 0, false);
                        } else if (last1elements % 2 == 0) {
                            return addMove(last3pos, last2direction, false);
                        } else {
                            return addMove(last3pos, 0, false);
                        }
                    }
                    case UNKNOWN:
                    case WARMER: {
                        int middle = middleRounded(last2pos - last2offset, last1pos - last1offset);

                        if (last1pos > last2pos) {
                            hmin = Math.max(hmin, middle);
                        } else if (last1pos < last2pos) {
                            hmax = Math.min(hmax, middle);
                        }

                        int last1elements = hmax - hmin + 1;
                        System.err.printf("WARM <- WARM: middle=%d, hmin=%d, hmax=%d, last1elements=%d\n", middle, hmin, hmax, last1elements);

                        // check distance, if three we should alreay know the solution ?
                        if (last1elements <= 2) {
                            return addMove(last1pos, 0, true); // check this
                        } else if (last1elements % 2 == 0) {
                            return addMove(middle, last1direction, false);
                        } else {
                            return addMove(middle, 0, false);
                        }
                    }
                }
            }

            case SAME:
                return addMove((last1pos + last2pos) / 2, 0, true); // final solution
        }

        throw new IllegalStateException("No logic applied ?!");
    }

    public int middleRounded(int last2pos, int last1pos) {

        return last1pos > last2pos
                ? (int) Math.ceil((double) (last1pos + last2pos) / 2)
                : (int) Math.floor((double) (last1pos + last2pos) / 2);
    }

    private boolean addMove(int pos, int offset, boolean found) {
        int posWithOffset = Math.min(Math.max(0, pos + offset), max - 1);

        moves.addLast(posWithOffset);

        last3offset = last2offset;
        last2offset = last1offset;
        last1offset = offset;

        System.err.println("Added move: " + posWithOffset + " offset=" + offset + " found=" + found);

        return found;
    }
}

