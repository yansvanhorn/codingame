import java.util.*;
import java.util.stream.IntStream;

/**
 * Send your busters out into the fog to trap ghosts and bring them home!
 */
enum State {
    IDLE(0),
    CARRIES_GHOST(1),
    STUNNED(2),
    TRAPPING(3);

    int v;

    State(int v) {
        this.v = v;
    }

    public int getV() {
        return v;
    }

    static State valueOf(int v) {
        for (State state : State.values()) {
            if (state.getV() == v) {
                return state;
            }
        }

        throw new RuntimeException("Unknown state for " + v);
    }
}

enum Action {
    IDLE,
    MOVE,
    BUST,
    RELEASE,
    STUN,
    RADAR,
    EJECT;
}

class Command {
    public static final Command release = new Command(Action.RELEASE, 1);
    public static final Command idle = new Command(Action.IDLE, 100);
    public static final Command radar = new Command(Action.RADAR, 1);

    private Action action;
    protected Pos pos;
    protected int value;
    protected int timer;

    protected Command(Action action, int timer) {
        this.action = action;
        this.timer = timer;
    }

    public int getTimer() {
        return timer;
    }

    public Command tickTimer() {
        return --timer > 0 ? this : Command.idle;
    }

    public static Command idle() {
        return idle;
    }

    public static Command release() {
        return release;
    }

    public static Command radar() {
        return radar;
    }

    public static PosCommand move(Pos pos) {
        return new PosCommand(Action.MOVE, pos, 10);
    }

    public static PosCommand eject(Pos pos) {
        return new PosCommand(Action.EJECT, pos, 1);
    }

    public static IntCommand bust(Entity entity) {
        return new IntCommand(Action.BUST, entity.getEntityId(), 1);
    }

    public static IntCommand bust(int id) {
        return new IntCommand(Action.BUST, id, 1);
    }

    public static IntCommand stun(Entity entity) {
        return new IntCommand(Action.STUN, entity.getEntityId(), 1);
    }

    public static IntCommand stun(int id) {
        return new IntCommand(Action.STUN, id, 1);
    }

    public Action getAction() {
        return action;
    }

    public boolean isAction(Action action) {
        return this.action == action;
    }

    public int getValue() {
        return value;
    }

    public boolean isValue(int value) {
        return this.value == value;
    }

    public boolean isPos(Pos somePos) {
        return this.pos.getX() == somePos.getX() && this.pos.getY() == somePos.getY();
    }

    public Pos getPos() {
        return pos;
    }

    @Override
    public String toString() {
        return action.toString();
    }
}

class IntCommand extends Command {
    public IntCommand(Action action, int value, int timer) {
        super(action, timer);
        this.value = value;
    }

    @Override
    public String toString() {
        return getAction() + " " + value;
    }
}

class PosCommand extends Command {

    protected PosCommand(Action action, Pos pos, int timer) {
        super(action, timer);
        this.pos = pos;
    }

    @Override
    public String toString() {
        return String.format("%s %d %d", getAction(), pos.getX(), pos.getY());
    }
}

class XY
        implements Pos {
    int x, y;

    static Random random = new Random(new Date().getTime());

    public XY() {
        x = random.nextInt(16000);
        y = random.nextInt(9000);
    }

    public XY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public XY(double x, double y) {
        this.x = (int) x;
        this.y = (int) y;
    }

    public XY(Pos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "pos{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

interface Pos {
    int getX();

    int getY();

    default double distance(Pos from) {
        return Calculate.distance(getX(), getY(), from.getX(), from.getY());
    }

    default int dot(Pos pos) {
        return getX() * pos.getX() + getY() * pos.getY();
    }

    default Pos subtract(Pos pos) {
        return new XY(getX() - pos.getX(), getY() - pos.getY());
    }

    default double length() {
        return Math.sqrt(getX() * getX() + getY() * getY());
    }

    default Pos multiply(double v) {
        return new XY((int) (getX() * v), (int) (getY() * v));
    }

    default Pos divide(double v) {
        return new XY((int) (getX() / v), (int) (getY() / v));
    }

    default Pos add(Pos pos) {
        return new XY(getX() + pos.getX(), getY() + pos.getY());
    }

    default Pos normalize(int length) {
        double current = this.length();
        return new XY(getX() * length / current, getY() * length / current);
    }
}

class Calculate {

    public static double distance(int x, int y, int x2, int y2) {
        return Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y));
    }

    public static double distance(Pos a, Pos b) {
        return distance(a.getX(), a.getY(), b.getX(), b.getY());
    }

    public static int findCloser(Pos who, Pos pos1, Pos pos2) {
        return (int) (distance(who, pos1) - distance(who, pos2));
    }

    public static Pos moveToDistance(Pos buster, Pos from, Pos secondaryFrom, int minDistance) {
        int x = buster.getX() - from.getX();
        int y = buster.getY() - from.getY();
        double distance = distance(buster, from);

        if (distance < 1) { // if too close from "from"
            x = (int) (buster.getX() - secondaryFrom.getX());
            y = (int) (buster.getY() - secondaryFrom.getY());

            distance = Math.sqrt(x * x + y * y);
        }

        return new XY(from.getX() + x * minDistance / distance, from.getY() + y * minDistance / distance);
    }

    public static final int BUSTER_SPEED = 800;

    public static class Interception {
        Pos velocity;
        Pos destination;
        double time;

        public Interception(Pos velocity, Pos destination, double time) {
            this.velocity = velocity;
            this.destination = destination;
            this.time = time;
        }

        public Pos getVelocity() {
            return velocity;
        }

        public Pos getDestination() {
            return destination;
        }

        public double getTime() {
            return time;
        }
    }

    public static Optional<Interception> intercept(Pos interceptor, Pos prevTarget, Pos target) {
        Pos vVelocityTarget = target.subtract(prevTarget);
        double velocityTarget = vVelocityTarget.length();

        Pos vInterceptorTarget = interceptor.subtract(target); //target.subtract(interceptor);
        double distance = distance(target, interceptor);

        if (vVelocityTarget.length() < 50) { // low speed go to target directly
            return Optional.empty();
        } else if (distance < 50) { // close ... move directly
            return Optional.empty();
        }

        double a = BUSTER_SPEED * BUSTER_SPEED - velocityTarget * velocityTarget;
        double b = 2 * vInterceptorTarget.dot(vVelocityTarget);
        double c = -distance * distance;

        Optional<QuadraticSolver.Factor> solution = QuadraticSolver.solve(a, b, c);
        if (!solution.isPresent()) { // cant intercept
            return Optional.empty();
        }

        Optional<Double> time = solution.get().getSmallestPositive();
        if (!time.isPresent()) {
            return Optional.empty();
        }

        Pos pointOfInterception = vVelocityTarget.multiply(time.get()).add(target);
        Pos interceptorVelocity = pointOfInterception.subtract(interceptor).divide(time.get());
        return Optional.of(new Interception(interceptorVelocity, interceptor.add(interceptorVelocity), time.get()));
    }
}

class QuadraticSolver {
    static class Factor {
        double v1, v2;

        public Factor(double v1, double v2) {
            this.v1 = v1;
            this.v2 = v2;
        }

        public double getV1() {
            return v1;
        }

        public double getV2() {
            return v2;
        }

        public Optional<Double> getSmallestPositive() {
            double v = Math.min(v1 < 0 ? Double.MAX_VALUE : v1, v2 < 0 ? Double.MAX_VALUE : v2);
            return v != Double.MAX_VALUE ? Optional.of(v) : Optional.empty();
        }
    }

    public static Optional<Factor> solve(double a, double b, double c) {

        double d = b * b - 4 * a * c;

        if (d < 0) {
            return Optional.empty();
        }

        double v1 = (-b - Math.sqrt(d)) / (2 * a);
        double v2 = (-b + Math.sqrt(d)) / (2 * a);

        return Optional.of(new Factor(v1, v2));
    }
}

/**
 * - If other guy is my ghost busted by me, then:
 * - call help,
 * - if no chance for help then give up?
 * <p>
 * - When returning consider help ?
 * - When returning and buster near our base (on track), use eject
 */

class Player {

    public static final int HISTORY_SIZE = 10;

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        int bustersPerPlayer = in.nextInt(); // the amount of busters you control
        int ghostCount = in.nextInt(); // the amount of ghosts on the map
        int myTeamId = in.nextInt(); // if this is 0, your base is on the top left of the map, if it is one, on the bottom right

        Base base = myTeamId == 0 ? new Base(0, 0) : new Base(16000, 9000);
        Base enemyBase = myTeamId == 0 ? new Base(16000, 9000) : new Base(0, 0);

        // game loop
        GameState gameState = new GameState(ghostCount, bustersPerPlayer);
        LinkedList<Ghost> ghosts = gameState.getGhosts();
        LinkedList<Buster> ourBusters = gameState.getOurBusters();
        LinkedList<Buster> enemyBusters = gameState.getEnemyBusters();

        //LinkedList<GameState> history = new LinkedList<GameState>();

        Roam roam = new Roam(gameState);

        while (true) {
            gameState.nextRound();

            int entities = in.nextInt(); // the number of busters and ghosts visible to you
            for (int i = 0; i < entities; i++) {
                int entityId = in.nextInt(); // buster id or ghost id
                int x = in.nextInt();
                int y = in.nextInt(); // position of this buster / ghost
                int entityType = in.nextInt(); // the team id if it is a buster, -1 if it is a ghost.
                int state = in.nextInt(); // For busters: 0=idle, 1=carrying a ghost. For ghosts: stamina
                int value = in.nextInt(); // For busters: Ghost id being carried. For ghosts: number of busters attempting to trap this ghost.

                // ghost
                if (entityType == -1) {
                    gameState.updateGhost(new Ghost(gameState.getRound(), entityId, x, y, value, state));
                } else if (entityType == myTeamId) {
                    gameState.updateOurBuster(new Buster(base, gameState.getRound(), entityId, x, y, state, value));
                } else {
                    gameState.updateEnemyBuster(new Buster(enemyBase, gameState.getRound(), entityId, x, y, state, value));
                }
            }

            gameState.eliminateMissingGhosts();

            System.err.println(gameState.toString());
            System.err.println(roam.toString());

            for (int i = 0; i < bustersPerPlayer; i++) {
                final Buster buster = gameState.getOurBusters().get(i);

                System.err.print("-----------------\n" + buster);
                roam.visit(buster);


                Optional<Buster> enemy = Optional.empty();
                Optional<Ghost> ghost = Optional.empty();

                if (buster.getState() == State.CARRIES_GHOST) {
                    // Watch out for enemy while returning... stun if can...
                    if (buster.canStun()) {
                        enemy = gameState.getEnemyBusters().stream()
                                .filter(e -> e.getState() != State.STUNNED)
                                .filter(e -> Calculate.distance(buster, e) <= 1760)
                                .filter(e -> ourBusters.stream().noneMatch(o -> o.isStunning(e))) // our is not trying to stun him already
                                .min((g1, g2) -> Calculate.findCloser(buster, g1, g2));

                        enemy.ifPresent(e -> {
                            buster.moveCloserAnd(e, base, 0, 1760, Command.stun(e));
                            buster.setInfo("Stun while carrying");
                        });
                    }

                    // if no enemy, then go home and release
                    if (!enemy.isPresent()) {
                        if (buster.canRelease()) {
                            buster.setCommand(Command.release());
                            buster.setInfo("Release");
                        } else {
                            buster.setCommand(Command.move(base));
                            buster.setInfo("Returning");
                        }
                    }
                } else {

                    // Find one carrying ghost even if possibly can't stun: TODO: consider if can catch before base ?
                    enemy = gameState.getEnemyBusters().stream()
                            .filter(e -> e.getState() == State.CARRIES_GHOST && e.getRound() == gameState.getRound())
                            .filter(e -> Calculate.distance(buster, e) < 3500)
                            .filter(e -> Calculate.distance(buster, enemyBase) <= Calculate.distance(e, enemyBase) + 2000) // we have a chance to catch him before base
                            .filter(e -> ourBusters.stream().filter(o -> o.isStunning(e) && o.getState() != State.STUNNED).findFirst().isPresent() == false) // our is not trying to stun him already
                            .min((g1, g2) -> Calculate.findCloser(buster, g1, g2));

                    enemy.ifPresent(e -> {
                        if (buster.canStun()) {
                            buster.moveCloserAnd(e, base, 0, 1760, Command.stun(e)); // try to stan if can or close distance to stun
                            buster.setInfo("Stun the carrier");
                        } else {
                            buster.moveCloser(e, base, 1000); // if cannot stun, then follow
                            buster.setInfo("No stun. Follow carrier");
                        }
                    });

                    // If not already trying to stun
                    if (buster.canStun()) {
                        if (!enemy.isPresent()) {
                            enemy = gameState.getEnemyBusters().stream()
                                    .filter(e -> e.getState() != State.STUNNED && e.getRound() == gameState.getRound())
                                    .filter(e -> Calculate.distance(buster, e) < 2500)
                                    .filter(e -> ourBusters.stream().noneMatch(o -> o.isStunning(e) && o.getState() != State.STUNNED)) // our is not trying to stun him already
                                    .min((g1, g2) -> Calculate.findCloser(buster, g1, g2));

                            enemy.ifPresent(e -> {
                                buster.moveCloserAnd(e, base, 0, 1760, Command.stun(e));
                                buster.setInfo("Stun enemy");
                            }); // try to stun or close distance to stun
                        }
                    }

                    // If no enemy and ghost present then catch ...
                    if (gameState.getGhosts().size() > ghostCount * 0.4 || gameState.getRound() > 15) {
                        if (!enemy.isPresent()) {
                            // found in range low on stamin...
                            ghost = gameState.getGhosts().stream()
//                                    .filter(g -> Calculate.distance(buster, g) < 4000)
                                    .min((g1, g2) -> (int) (g1.getStamina() * buster.distance(g1) * base.distance(g1) - g2.getStamina() * buster.distance(g2) * base.distance(g2)));

                            ghost.ifPresent(g -> {
                                buster.moveCloserAnd(g, base, 900, 1760, Command.bust(g));
                                buster.setInfo("Trap " + g.getEntityId() + "=" + g.getStamina());
                            });

                            // found lowest
//                            if (!ghost.isPresent()) {
//                                ghost = gameState.getGhosts().stream()
//                                        .min((g1, g2) -> (int)(g1.getStamina() * buster.distance(g1) * base.distance(g1) - g2.getStamina() * buster.distance(g2) * base.distance(g2)));
//
//                                ghost.ifPresent(g -> {
//                                    buster.moveCloserAnd(g, base, 900, 1760, Command.bust(g));
//                                    buster.setInfo("Trap" + g.getEntityId() + "=" + g.getStamina());
//                                });
//                            }
                        }
                    }

                    // Maybse use radar ?
                    if (buster.isIdle() || buster.hasReachedDestination()) {
                        if (gameState.getRound() > 10 && // use a bit later
                                buster.getX() > 2000 && buster.getY() < 14000 &&
                                buster.getY() > 2000 && buster.getY() < 7000 &&
                                ghosts.size() == 0 && // no ghosts found
                                ourBusters.stream().noneMatch(o -> o.isUsingRadar()) && // no radar in use
                                buster.canUseRadar()) { // can use radar
                            buster.setCommand(Command.radar());
                            buster.setInfo("Radar");
                        }
                    }

                    if (buster.isIdle() || buster.hasReachedDestination()) {
//                        buster.setCommand(Command.move(???));
                        buster.setInfo("Roam");
                        buster.setCommand(Command.move(roam.roam(buster)));
                    }
                }

//                System.err.print(buster);
                System.err.println(buster.getInfo());
                System.out.println(buster.getCommand().toString() + " " + buster.getEntityId() + "-" + buster.getInfo()); // MOVE x y | BUST id | RELEASE
            }
        }
    }
}

class Roam {
    Random random = new Random(new Date().getTime());

    GameState gameState;
    Quadrant quadrant = new Quadrant();

    int qx = quadrant.getQx();
    int qy = quadrant.getQy();

    int[][] roamVisit;

    public Roam(GameState gameState) {
        roamVisit = new int[quadrant.getQx()][quadrant.getQy()];
        this.gameState = gameState;
    }

    /**
     * If no ghosts, or round < n (20?) roamVisit the map...
     * Update with current state
     * Mark++ when visiting
     * Find closest least visited (also use ?
     * Remember who roams where ?
     */

    public Pos roam(Buster buster) {
        Pos q = quadrant.toQ(buster);
//        visitQuadrant(q);

//        Pos destination = buster.getCommand().getPos();
//        double distance = buster.distance(destination);
//        if(distance < Calculate.BUSTER_SPEED) {
//            Pos velocity = destination.subtract(buster).normalize(Calculate.BUSTER_SPEED);
//            buster.setCommand(Command.move(buster.add(velocity)));
//        }

        Pos least = findLeastVisitedQuadrantFor(q);
        visitQuadrant(least, 1);

        return quadrant.fromQ(least);
    }

    private void visitQuadrant(Pos pos, int v) {
        roamVisit[pos.getX()][pos.getY()] += v;
    }

    public void visit(Pos pos) {
        Pos q = quadrant.toQ(pos);
        double distance = quadrant.fromQ(q).distance(pos);

        if (distance < Calculate.BUSTER_SPEED / 2) {
            visitQuadrant(q, 3);
        } else {
            visitQuadrant(q, 1);
        }
    }

    private Pos findLeastVisitedQuadrantFor(Pos q) {
        return findLeastVisitedQuadrantFor(q, 1, Integer.MAX_VALUE, q);
    }

    private Pos findLeastVisitedQuadrantFor(final Pos q, final int range, int least, Pos leastPos) {
        int xmin = q.getX() - range;
        int xmax = q.getX() + range;
        int ymin = q.getY() - range;
        int ymax = q.getY() + range;

        int[] xvalues = shuffle(IntStream.range(Math.max(0, xmin), Math.min(qx - 1, xmax) + 1).toArray());
        int[] yvalues = shuffle(IntStream.range(Math.max(0, ymin), Math.min(qy - 1, ymax) + 1).toArray());

        for (int y : yvalues) {
            for (int x : xvalues) {
                if (y == ymin || y == ymax || x == xmin || x == xmax) {
                    if (least > roamVisit[x][y]) {
                        least = roamVisit[x][y];
                        leastPos = new XY(x, y);
                    }
                }
            }
        }

        if (range == 5) {
            return leastPos;
        }

        return findLeastVisitedQuadrantFor(q, range + 1, least, leastPos);
    }

    public int[] shuffle(int[] array) {
        int length = array.length;
        for (int i = 0; i < array.length; i++) {
            int r = random.nextInt(length);
            int v = array[i];
            array[i] = array[r];
            array[r] = v;
        }
        return array;
    }

    public int range(int v, int min, int max) {
        return v < min ? min : (v > max ? max : v);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (int y = 0; y < qy; y++) {
            for (int x = 0; x < qx; x++) {
                out.append(String.format(" %3d", roamVisit[x][y]));
            }
            out.append("\n");
        }

        return out.toString();
    }
}

class Quadrant {
//    final int borderx = 1500;
//    final int bordery = 1500;

    final int qx, qy;
    final int ox; // = 1500;
    final int oy; // = 1500;
    final int w = 16000; // - borderx * 2;
    final int h = 9000; // - bordery * 2;

    public Quadrant() {
        this(7, 4);
    }

    public Quadrant(int qx, int qy) {
        this.qx = qx;
        this.qy = qy;
        this.ox = w / qx / 2;
        this.oy = h / qy / 2;
    }

    public int getQx() {
        return qx;
    }

    public int getQy() {
        return qy;
    }

    public Pos toQ(Pos pos) {
        int x = Math.min(pos.getX() * qx / w, qx - 1);
        int y = Math.min(pos.getY() * qy / h, qy - 1);
        return new XY(x, y);
    }

    public Pos fromQ(Pos pos) {
        int x = (pos.getX() * w / qx) + ox;
        int y = (pos.getY() * h / qy) + oy;
        return new XY(x, y);
    }
}

class GameState {
    private int round = 0;
    private int ghostCount = 0;
    private int busterCount = 0;

    private LinkedList<Ghost> ghosts = new LinkedList<>();
    private LinkedList<Buster> ourBusters = new LinkedList<>();
    private LinkedList<Buster> enemyBusters = new LinkedList<>();

    public GameState(int ghostCount, int busterCount) {
        this.ghostCount = ghostCount;
        this.busterCount = busterCount;
    }

    public LinkedList<Ghost> getGhosts() {
        return ghosts;
    }

    public LinkedList<Buster> getOurBusters() {
        return ourBusters;
    }

    public LinkedList<Buster> getEnemyBusters() {
        return enemyBusters;
    }

    public Buster updateOurBuster(Buster updatedBuster) {
        return updateBuster(ourBusters, updatedBuster);
    }

    public Buster updateEnemyBuster(Buster updatedBuster) {
        return updateBuster(enemyBusters, updatedBuster);
    }

    private Buster updateBuster(LinkedList<Buster> busters, Buster updatedBuster) {
        Optional<Buster> maybeBuster = busters.stream()
                .filter(b -> b.getEntityId() == updatedBuster.getEntityId())
                .findFirst();

        Buster buster = maybeBuster.isPresent()
                        ? maybeBuster.get().update(updatedBuster)
                        : updatedBuster;

        if (!maybeBuster.isPresent()) {
            busters.add(buster);
        }

        if (buster.isCarryingGhost()) {
            int ghostId = buster.getGhostId();
            System.err.println("Ghost " + ghostId + " caught. Removing!");
            ghosts.removeIf(ghost -> ghost.getEntityId() == ghostId);
        }

        return buster;
    }

    public void updateGhost(Ghost updatedGhost) {
        ghosts.remove(updatedGhost); // Entity equals based on EntityId
        ghosts.add(updatedGhost);
    }

    public int getGhostCount() {
        return ghostCount;
    }

    public int getBusterCount() {
        return busterCount;
    }

    @Override
    public String toString() {
        return "#### GameState: [" + round + "] (ghosts=" + ghosts.size() + " / " + ghostCount + ") ####\n" +
                "ghosts=" + ghosts +
                ", ourBusters=" + ourBusters +
                ", enemyBusters=" + enemyBusters +
                '}';
    }

    public void nextRound() {
//        ghosts.clear();
        enemyBusters.clear();

        round++;
    }

    public int getRound() {
        return round;
    }

    public void eliminateMissingGhosts() {
        ghosts.removeIf(ghost -> {
            boolean shouldRemove = ghost.getRound() < round && ourBusters.stream().anyMatch(buster -> buster.distance(ghost) < 2200);
            if (shouldRemove) {
                System.err.println("Ghost not there! Removing " + ghost.getEntityId());
            }
            return shouldRemove;
        });
    }
}

class Base
        implements Pos {
    private int x, y;

    public Base(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Base{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

abstract class Entity
        implements Pos {

    protected int entityId;
    protected int x;
    protected int y;
    protected int round;

    Entity(int round, int entityId, int x, int y) {
        this.entityId = entityId;
        this.y = y;
        this.x = x;
        this.round = round;
    }

    int getEntityId() {
        return entityId;
    }

    public int getRound() {
        return round;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entity entity = (Entity) o;

        return entityId == entity.entityId;

    }

    @Override
    public int hashCode() {
        return entityId;
    }
}

class Buster
        extends Entity {
    private State state;
    private int ghostId = -1;

    private int stunCounter = 0;
    boolean radarAvailable = true;

    private String info;
    private Command command = Command.idle;
    private Base base;

    boolean caughtGhost = false;
    boolean lostGhost = false;

    Deque<Buster> history = new ArrayDeque<>(Player.HISTORY_SIZE);

    public Buster(Base base, int round, int entityId, int x, int y, int state, int ghostId) {
        super(round, entityId, x, y);

        this.base = base;
        this.state = State.valueOf(state);
        this.ghostId = ghostId;
    }

    public Buster update(Buster updatedBuster) {
        if (this.entityId != updatedBuster.getEntityId()) {
            throw new RuntimeException("Wrong updatedBuster for assignment");
        }

        history.addFirst(this);
        if (history.size() > Player.HISTORY_SIZE) {
            history.pollLast();
        }

        this.x = updatedBuster.getX();
        this.y = updatedBuster.getY();
        this.stunCounter = Math.max(0, updatedBuster.getStunCounter() - 1);
        this.state = updatedBuster.getState();

        this.caughtGhost = this.ghostId < 0 && updatedBuster.getGhostId() > -1;
        this.lostGhost = this.ghostId > -1 && updatedBuster.getGhostId() < 0;
        this.ghostId = updatedBuster.getGhostId();

        this.command = this.getCommand().tickTimer();

        return this;
    }

    public int getStunCounter() {
        return stunCounter;
    }

    public void moveCloserAnd(Pos enemy, Pos secondary, int minDistance, int maxDistance, Command command) {
        int currentDistance = (int) Calculate.distance(enemy, this);

        if (currentDistance < minDistance || currentDistance > maxDistance) {
            this.command = Command.move(Calculate.moveToDistance(this, enemy, secondary, minDistance));
        } else {
            this.command = command;
        }
    }

    public void moveCloser(Pos enemy, Pos secondary, int minDistance) {
        this.command = command.move(Calculate.moveToDistance(this, enemy, secondary, minDistance));
    }

    public boolean hasReachedDestination() {
        return command.isAction(Action.MOVE) && Calculate.distance(this, command.getPos()) < 5;
    }

    public int getEntityId() {
        return entityId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Command getCommand() {
        return command;
    }

    public State getState() {
        return state;
    }

    public int getGhostId() {
        return ghostId;
    }

    public Deque<Buster> getHistory() {
        return history;
    }

    @Override
    public String toString() {
        return "Buster: [" + round + "] (" + entityId + ") {" +
                "x=" + x +
                ", y=" + y +
                ", state=" + state +
                ", stun=" + stunCounter +
                ", radar=" + radarAvailable +
                ", ghostId=" + ghostId +
                ", ghostCaught=" + caughtGhost +
                ", ghostLost=" + lostGhost +
                ", " + command +
                ", info='" + info +
                "}\n";
    }

    public String outputAction() {
        return command.toString() + " " + info;
    }

    // ------------- IS ---------------------
    public boolean isIdle() {
        return command.getAction() == Action.IDLE;
    }

    public boolean isMoving() {
        return command.isAction(Action.MOVE) && !hasReachedDestination();
    }

    public boolean isBusting() {
        return command.isAction(Action.BUST);
    }

    // ------------- CAN ---------------------
    public boolean canRelease() {
        return Calculate.distance(base, this) < 1600;
    }

    public boolean canStun() {
        return stunCounter == 0 && !command.isAction(Action.STUN);
    }

    public boolean canUseRadar() {
        return radarAvailable;
    }

//    public boolean isStunning() {
//        return command.isAction(Action.STUN);
//    }

    public boolean isStunning(Buster enemy) {
        return command.isAction(Action.STUN) && command.isValue(enemy.getEntityId());
    }

    public boolean isUsingRadar() {
        return command.isAction(Action.RADAR);
    }

    public boolean isCarryingGhost() {
        return State.CARRIES_GHOST == state;
    }

    public void setInfo(String s) {
        this.info = s;
    }

    public String getInfo() {
        return info;
    }

    public void setCommand(Command command) {
        this.command = command;
    }
}

class Ghost
        extends Entity {
    private int bustersTrapping;
    private int stamina;

    public Ghost(int round, int entityId, int x, int y, int bustersTrapping, int stamina) {
        super(round, entityId, x, y);
        this.bustersTrapping = bustersTrapping;
        this.stamina = stamina;
    }

    public int getEntityId() {
        return entityId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getBustersTrapping() {
        return bustersTrapping;
    }

    public int getStamina() {
        return stamina;
    }

    @Override
    public String toString() {
        return "Ghost: [" + round + "] (" + entityId + ") {" +
                ", x=" + x +
                ", y=" + y +
                ", busters=" + bustersTrapping +
                ", stamina=" + stamina +
                "}\n";
    }


}

