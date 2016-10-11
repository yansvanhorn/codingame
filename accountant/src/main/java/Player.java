import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

/**
 * Shoot enemies before they collect all the incriminating data!
 * The closer you are to an enemy, the more damage you do but don't get too close or you'll get killed.
 */
class Player
{

    public static void main(String args[])
    {
        Scanner in = new Scanner(System.in);

        Wolff wolff = new Wolff();
        OptimalDataPointFinder finder = new OptimalDataPointFinder();
        DataPointsWithValue currentDPV = new DataPointsWithValue();

        // game loop
        while (true) {
            int x = in.nextInt();
            int y = in.nextInt();

            wolff.updatePos(x, y);

            LinkedList<DataPoint> datas = new LinkedList<>();
            LinkedList<Enemy> enemies = new LinkedList<>();
            Command command = null;

            int dataCount = in.nextInt();
            for (int i = 0; i < dataCount; i++) {
                int dataId = in.nextInt();
                int dataX = in.nextInt();
                int dataY = in.nextInt();
                datas.add(new DataPoint(dataId, dataX, dataY));
            }
            int enemyCount = in.nextInt();
            for (int i = 0; i < enemyCount; i++) {
                int enemyId = in.nextInt();
                int enemyX = in.nextInt();
                int enemyY = in.nextInt();
                int enemyLife = in.nextInt();
                Enemy enemy = new Enemy(enemyId, enemyLife, enemyX, enemyY);
                enemies.add(enemy);

                datas.stream()
                        .min(comparingInt(dp -> (int) enemy.distance(dp)))
                        .ifPresent(dp -> enemy.assignDataPoint(dp));
            }

            // sort enemies
//            datas.forEach(dp -> dp.getEnemies().sort(comparingInt(e -> (int) e.distance(dp))));

            // calculate value
//            datas.forEach(dataPoint -> datas.forEach(dp2 -> dataPoint.addValue(dp2)));

            System.err.println(wolff);
            System.err.println(datas);
            System.err.println(enemies);

//            enemies.forEach(e -> System.err.printf("[%d] damage/Life=%d/%d\n", e.getId(), e.willDamageBy(wolff), e.getLife()));

            // Need to run ?
            if (wolff.neetToRun(enemies)) {
                System.err.println("Need to run");
//                Optional<DataPointsWithValue> evade = finder.find(datas, enemies, wolff);
                Optional<Pos> evade = wolff.evade(enemies);
                if (evade.isPresent()) {
                    command = Command.move(evade.get());
//                    command = Command.move(evade.get().getWolff());
                }
                else {
                    System.err.println("Nowhere to run ?");
                }
            }

            // Should we select new and move ?
            if (command == null) {
                Optional<DataPointsWithValue> maybeDPV = finder.find(datas, enemies, wolff);
                currentDPV = finder.dataPointsValue(datas, wolff, false);
                System.err.println("maybeDPV=" + maybeDPV);

                if (maybeDPV.isPresent()) {
                    DataPointsWithValue latestDPV = maybeDPV.get();

                    System.err.printf("Current/New value = %d / %d\n",
                                      currentDPV == null ? 0 : currentDPV.getSum(),
                                      latestDPV.getSum());

                    if (latestDPV.getSum() < currentDPV.getSum()) {
                        System.err.printf("New datapoint to save! [%d] (wolf=%s)",
                                          latestDPV.getDataPointWithValues().get(0).getDataPoint().getId(),
                                          latestDPV.getWolff());

                        currentDPV = latestDPV.sort();

                        System.err.println("Moving to " + currentDPV.getWolff());
                        command = Command.move(currentDPV.getWolff());
                    }
                }
            }

            // Shoot closest enemy from current dp
            if (command == null) {
                Optional<DataPointWithValue> firstDP = currentDPV.getDataPointWithValues().stream()
                        .filter(dp -> dp.getDataPoint().getEnemies().peek() != null)
                        .findFirst();

                if (firstDP.isPresent()) {
                    Optional<Enemy> maybeEnemy = firstDP.get().getDataPoint().getEnemies().stream()
                            .filter(e -> e.turnsFromDataPoint() >= e.turnsToKillBy(wolff))
                            .min(comparingInt(Enemy::turnsFromDataPoint));

                    if (maybeEnemy.isPresent()) {
                        System.err.printf("Shoot closest to dp id=%d, turns=%d\n", maybeEnemy.get().getId(), maybeEnemy.get().turnsToKillBy(wolff));
                        command = Command.shoot(maybeEnemy.get().getId());
                    }

                    maybeEnemy = firstDP.get().getDataPoint().getEnemies().stream()
                            .min(comparingInt(e -> e.turnsToKillBy(wolff)));

                    if(maybeEnemy.isPresent() && command == null) {
                        System.err.println("Shoot easiest to kill id=" + maybeEnemy.get().getId());
                        command = Command.shoot(maybeEnemy.get().getId());
                    }
                }
                else {
                    System.err.println("No one to shoot?");
                }
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");

            final Command c = command;
            enemies.forEach(e -> System.err.printf("[%d] distance = %d\n", e.getId(), (int) e.distance(c)));

            System.out.println(command.toString()); // MOVE x y or SHOOT id
        }
    }
}

class OptimalDataPointFinder
{
    public Optional<DataPointsWithValue> find(List<DataPoint> datas, List<Enemy> enemies, Wolff wolff)
    {
        List<DataPointsWithValue> sorted = wolff.allowedMoves().stream()
                .filter(m -> !enemies.stream() // filter unallowed moves
                        .filter(e -> min(m.distance(e.getNexPos()), e.distance(m)) <= Wolff.SAFE_DISTANCE)
                        .findFirst().isPresent())
                .map(m -> dataPointsValue(datas, m, true))
//                .peek(dpv -> System.err.printf("%s value=%d\n", dpv.getWolff(), dpv.getSum()))
                .sorted(comparingInt(DataPointsWithValue::getSum))
                .collect(toList());

        int startValue = sorted.get(0).getSum();
        int tolerance = (int)(startValue * 0.1);

//        System.err.println("StartValue=" + startValue);
//        System.err.println("Sorted:\n" + sorted);
        Optional<DataPointsWithValue> found = sorted.stream()
                .filter(dpv -> abs(dpv.getSum() - startValue) < tolerance)
                .max(comparingInt(dpv -> dpv.getTotalDistance()));

        return found;
    }

    public DataPointsWithValue dataPointsValue(List<DataPoint> datas, Pos wolff, boolean needToMove)
    {
        List<DataPointWithValue> dataPointsWithValue = datas.stream()
                .map(dp -> new DataPointWithValue(dp, dataPointValue(dp, wolff, true)))
                .collect(toList());

        return new DataPointsWithValue(wolff, dataPointsWithValue).sort();
    }

    private int dataPointValue(DataPoint dp, Pos wolff, boolean needToMove)
    {
        int killTurns = 0;
        int distanceSum = 0;
        boolean canBeSaved = true;

        int size = dp.getEnemies().size();
        if (size == 0) {
            return 10000;
        }

        for (Enemy e : dp.getEnemies()) {
            int turnsFromDataPoint = e.turnsFromDataPoint() - (needToMove ? -1 : 0);
            int turnsToKill = e.turnsToKillBy(wolff);

            if (turnsToKill + killTurns > turnsFromDataPoint) {
                canBeSaved = false;
            }
            distanceSum += turnsFromDataPoint;
            killTurns += turnsToKill;
        }

        int value = killTurns - distanceSum + (canBeSaved ? 0 : 5000);

        return value;
    }
}

class DataPointsWithValue
{
    private Pos wolff;
    private List<DataPointWithValue> dataPointWithValues;
    private int sum;
    private int totalDistance = -1;

    public DataPointsWithValue()
    {
        sum = 20000;
    }

    public DataPointsWithValue(Pos wolff, List<DataPointWithValue> dataPointWithValues)
    {
        this.wolff = wolff;

        this.dataPointWithValues = dataPointWithValues.stream()
                .filter(dpv -> dpv.getValue() < 10000)
                .sorted(comparingInt(DataPointWithValue::getValue))
                .limit(2)
                .collect(toList()); // find two dp's of interest

        this.sum = this.dataPointWithValues.stream().mapToInt(DataPointWithValue::getValue).sum();
    }

    public DataPointsWithValue sort()
    {
        dataPointWithValues.sort(comparingInt(DataPointWithValue::getValue));
        return this;
    }

    public List<DataPointWithValue> getDataPointWithValues()
    {
        return dataPointWithValues;
    }

    public int getSum()
    {
        return sum;
    }

    public int getTotalDistance()
    {
        if (totalDistance < 0) {
            totalDistance = (int) this.dataPointWithValues
                    .stream()
                    .flatMap(dpv -> dpv.getDataPoint().getEnemies().stream())
                    .mapToInt(e -> (int) e.distance(wolff))
                    .average().orElseGet(() -> 0);
        }

        return totalDistance;
    }

    public Pos getWolff()
    {
        return wolff;
    }

    @Override
    public String toString()
    {
        return "DataPointsWithValue{" +
                "totalDistance=" + totalDistance +
                ", sum=" + sum +
                ", wolff=" + wolff +
                "}\n";
    }
}

class DataPointWithValue
{
    private DataPoint dataPoint;
    private int value;

    public DataPointWithValue(DataPoint dataPoint, int value)
    {
        this.value = value;
        this.dataPoint = dataPoint;
    }

    public DataPoint getDataPoint()
    {
        return dataPoint;
    }

    public int getValue()
    {
        return value;
    }
}

class Wolff
        implements Pos
{
    public static final int SAFE_DISTANCE = 2000;
    public static final int WOLFF_MOVE = 2000;
    int x, y;

    public Wolff updatePos(int x, int y)
    {
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public int getX()
    {
        return x;
    }

    @Override
    public int getY()
    {
        return y;
    }

    @Override
    public String toString()
    {
        return "Wolff{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    public Optional<Enemy> canKillBeforeDataPoint(LinkedList<Enemy> enemies)
    {
        return enemies.stream()
                .filter(e -> e.turnsToKillBy(this) < e.turnsFromDataPoint())
                .min(comparingInt(e -> e.turnsToKillBy(this)));
    }

    public boolean neetToRun(LinkedList<Enemy> enemies)
    {
        return enemies.stream()
                .map(e -> e.getNexPos())
                .filter(e -> e.distance(this) < SAFE_DISTANCE).findAny().isPresent();
    }

    public Optional<Pos> evade(LinkedList<Enemy> enemies)
    {
        List<Pos> enemyPoss = enemies.stream()
                .flatMap(e -> Arrays.asList(e.getNexPos(), e.getNexPos(2)).stream())
                .filter(e -> e.distance(this) < SAFE_DISTANCE * 1.5).collect(toList());

        Optional<Pos> min = allowedMoves().stream()
                .max(comparingInt(p -> enemyPoss.stream()
                             .mapToInt(e -> (int) e.distance(p))
                             .min()
                             .getAsInt())
                );

        return min;
    }

    public List<Pos> allowedMoves()
    {
        LinkedList<Pos> moves = new LinkedList<>();
        int range = (int) (SAFE_DISTANCE * 1.25);

        int lox = Math.max(0, this.x - range);
        int hix = min(16000 - 1, this.x + range);
        int loy = Math.max(0, this.y - range);
        int hiy = min(9000 - 1, this.y + range);

        for (int y = loy; y < hiy; y += 400) {
            for (int x = lox; x < hix; x += 400) {
                if (Calculate.distance(x, y, this.x, this.y) <= WOLFF_MOVE) {
                    moves.add(new XY(x, y));
                }
            }
        }

        return moves;
    }
}

class DataPoint
        implements Pos
{
    public static final int DATAPOINT_MAX_VALUE = 100000;
    int id;
    int x, y;

    LinkedList<Enemy> enemies = new LinkedList<Enemy>();

//    int value = 0;

    public DataPoint(int id, int x, int y)
    {
        this.id = id;
        this.x = x;
        this.y = y;
    }

//    public void addValue(DataPoint dataPoint)
//    {
//        if (dataPoint != this) {
//            value += DATAPOINT_MAX_VALUE / this.distance(dataPoint);
//        }
//    }

    public void addEnemy(Enemy enemy)
    {
        enemies.add(enemy);
    }

    @Override
    public int getX()
    {
        return x;
    }

    @Override
    public int getY()
    {
        return y;
    }

    public int getId()
    {
        return id;
    }

    public LinkedList<Enemy> getEnemies()
    {
        return enemies;
    }

//    public int getValue()
//    {
//        return value;
//    }

    @Override
    public String toString()
    {
        return "DataPoint{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                ", enemies=" + enemies.size() +
//                ", value=" + value +
                "}\n";
    }
}

class Enemy
        implements Pos
{
    public static final double MAX_DAMAGE = 125000;
    public static final int ENEMY_SPEED = 500;

    private int id;
    private int x, y;
    private int life;
    private DataPoint dataPoint;
    private int turnsFromDataPoint = Integer.MAX_VALUE;
    private Pos nextPos;
    private Pos enemySpeedVector;

    public Enemy(int id, int life, int x, int y)
    {
        this.id = id;
        this.life = life;
        this.y = y;
        this.x = x;
    }

//    public int willDamageBy(Pos player)
//    {
//        return willDamageBy(this, player);
//    }

    public int willDamageBy(Pos enemy, Pos player)
    {
        double distance = enemy.distance(player);
        return Math.max(1, (int) (MAX_DAMAGE / Math.pow(distance, 1.2)));

    }

    public int turnsToKillBy(Pos player)
    {
        int lifeLeft = life, rounds = 0;
//        Pos enemyPos = this;

        while (lifeLeft > 0) {
            Pos enemyPos = getNexPos(++rounds);
            lifeLeft -= willDamageBy(enemyPos, player);
        }

        return rounds;
    }

    public int getId()
    {
        return id;
    }

    @Override
    public int getX()
    {
        return x;
    }

    @Override
    public int getY()
    {
        return y;
    }

    public int getLife()
    {
        return life;
    }

    public DataPoint getDataPoint()
    {
        return dataPoint;
    }

    public Enemy assignDataPoint(DataPoint dataPoint)
    {
        this.dataPoint = dataPoint;
        dataPoint.addEnemy(this);
        turnsFromDataPoint = (int) Math.ceil((this.distance(dataPoint) - 499) / ENEMY_SPEED);

        enemySpeedVector = dataPoint.subtract(this).normalize(ENEMY_SPEED);
        nextPos = getNexPos(1);

        return this;
    }

    public Pos getNexPos()
    {
        return nextPos;
    }

    public Pos getNexPos(int round)
    {
        return this.add(enemySpeedVector.multiply(round));
    }

    public int turnsFromDataPoint()
    {
        return turnsFromDataPoint;
    }

    @Override
    public String toString()
    {
        return "Enemy{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                ", life=" + life +
                ", dataPoint=" + dataPoint +
                ", turnsFromDP=" + turnsFromDataPoint +
                ", nextPos=" + nextPos +
                "}\n";
    }
}

class XY
        implements Pos
{
    int x, y;

    static Random random = new Random(new Date().getTime());

    public XY()
    {
        x = random.nextInt(16000);
        y = random.nextInt(9000);
    }

    public XY(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public XY(double x, double y)
    {
        this.x = (int) x;
        this.y = (int) y;
    }

    public XY(Pos pos)
    {
        this.x = pos.getX();
        this.y = pos.getY();
    }

    @Override
    public int getX()
    {
        return x;
    }

    @Override
    public int getY()
    {
        return y;
    }

    @Override
    public String toString()
    {
        return "pos{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

interface Pos
{
    int getX();

    int getY();

    default double distance(Pos from)
    {
        return Calculate.distance(getX(), getY(), from.getX(), from.getY());
    }

    default int dot(Pos pos)
    {
        return getX() * pos.getX() + getY() * pos.getY();
    }

    default Pos subtract(Pos pos)
    {
        return new XY(getX() - pos.getX(), getY() - pos.getY());
    }

    default double length()
    {
        return Math.sqrt(getX() * getX() + getY() * getY());
    }

    default Pos multiply(double v)
    {
        return new XY((int) (getX() * v), (int) (getY() * v));
    }

    default Pos divide(double v)
    {
        return new XY((int) (getX() / v), (int) (getY() / v));
    }

    default Pos add(Pos pos)
    {
        return new XY(getX() + pos.getX(), getY() + pos.getY());
    }

    default Pos normalize(int length)
    {
        double current = this.length();
        return new XY(getX() * length / current, getY() * length / current);
    }
}

class Calculate
{

    public static double distance(int x, int y, int x2, int y2)
    {
        return Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y));
    }

    public static double distance(Pos a, Pos b)
    {
        return distance(a.getX(), a.getY(), b.getX(), b.getY());
    }

    public static int findCloser(Pos who, Pos pos1, Pos pos2)
    {
        return (int) (distance(who, pos1) - distance(who, pos2));
    }

    public static Pos moveToDistance(Pos buster, Pos from, Pos secondaryFrom, int minDistance)
    {
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

    public static class Interception
    {
        Pos velocity;
        Pos destination;
        double time;

        public Interception(Pos velocity, Pos destination, double time)
        {
            this.velocity = velocity;
            this.destination = destination;
            this.time = time;
        }

        public Pos getVelocity()
        {
            return velocity;
        }

        public Pos getDestination()
        {
            return destination;
        }

        public double getTime()
        {
            return time;
        }
    }

    public static Optional<Interception> intercept(Pos interceptor, Pos prevTarget, Pos target)
    {
        Pos vVelocityTarget = target.subtract(prevTarget);
        double velocityTarget = vVelocityTarget.length();

        Pos vInterceptorTarget = interceptor.subtract(target); //target.subtract(interceptor);
        double distance = distance(target, interceptor);

        if (vVelocityTarget.length() < 50) { // low speed go to target directly
            return Optional.empty();
        }
        else if (distance < 50) { // close ... move directly
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

class QuadraticSolver
{
    static class Factor
    {
        double v1, v2;

        public Factor(double v1, double v2)
        {
            this.v1 = v1;
            this.v2 = v2;
        }

        public double getV1()
        {
            return v1;
        }

        public double getV2()
        {
            return v2;
        }

        public Optional<Double> getSmallestPositive()
        {
            double v = min(v1 < 0 ? Double.MAX_VALUE : v1, v2 < 0 ? Double.MAX_VALUE : v2);
            return v != Double.MAX_VALUE ? Optional.of(v) : Optional.empty();
        }
    }

    public static Optional<Factor> solve(double a, double b, double c)
    {
        double d = b * b - 4 * a * c;

        if (d < 0) {
            return Optional.empty();
        }

        double v1 = (-b - Math.sqrt(d)) / (2 * a);
        double v2 = (-b + Math.sqrt(d)) / (2 * a);

        return Optional.of(new Factor(v1, v2));
    }
}

enum Action
{
    MOVE,
    SHOOT;
}

class Command
        implements Pos
{
    private final Action action;
    private int id;
    private int x, y;

    protected Command(Action action, int id)
    {
        this.action = action;
        this.id = id;
    }

    public Command(Action action, Pos pos)
    {
        this.action = action;
        this.x = pos.getX();
        this.y = pos.getY();
    }

    public static Command move(Pos pos)
    {
        return new MoveCommand(pos);
    }

    public static Command shoot(int id)
    {
        return new Command(Action.SHOOT, id);
    }

    public int getId()
    {
        return id;
    }

    @Override
    public int getY()
    {
        return y;
    }

    @Override
    public int getX()
    {
        return x;
    }

    public Action getAction()
    {
        return action;
    }

    @Override
    public String toString()
    {
        return action + " " + id;
    }
}

class MoveCommand
        extends Command
{
    public MoveCommand(Pos pos)
    {
        super(Action.MOVE, pos);
    }

    @Override
    public String toString()
    {
        return getAction() + " " + getX() + " " + getY();
    }
}


