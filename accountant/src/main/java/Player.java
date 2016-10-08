import java.util.*;

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
                        .ifPresent(dp -> enemy.setDataPoint(dp));
            }

            // calculate value
            datas.forEach(dp -> datas.forEach(dp2 -> dp.addValue(dp2)));

            System.err.println(wolff);
            System.err.println(datas);
            System.err.println(enemies);

            enemies.forEach(e -> System.err.printf("[%d] damage/Life=%d/%d\n", e.getId(), e.willDamageBy(wolff), e.getLife()));

            if (wolff.neetToRun(enemies)) {
                System.err.println("Need to run");
                Optional<Pos> evade = wolff.evade(enemies);

                if (evade.isPresent()) {
                    command = Command.move(evade.get());
                }
                else {
                    System.err.println("Nowhere to run ?");
                }
            }

            if (command == null) {
                Optional<Pos> move = wolff.allowedMoves().stream().
                        max(comparingInt(p -> moveValue(enemies, p)));

                if (move.isPresent()) {
                    int moveValueCurrent = moveValue(enemies, wolff);
                    int moveValue = moveValue(enemies, move.get());

                    System.err.printf("Current/New value= %d / %d\n", moveValueCurrent, moveValue);

                    if (moveValue > moveValueCurrent) {
                        System.err.println("Moving to " + move.get());
                        command = Command.move(move.get());
                    }
                }
            }

            if (command == null) {
                Optional<Enemy> enemy = wolff.canKillBeforeDataPoint(enemies);
                if (enemy.isPresent()) {
                    command = Command.shoot(enemy.get().getId());
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

    private static int moveValue(LinkedList<Enemy> enemies, Pos p)
    {
        return enemies.stream()
                .mapToInt(e ->
                                  ((e.turnsToKill(p) < e.turnsFromDataPoint() - 1) ? 100000 : 0) +
                                  e.willDamageBy(p) +
                                  ((e.getNexPos().distance(p) <= Wolff.SAFE_DISTANCE) ? -1000000 : 0) +
                                  ((e.distance(p) <= Wolff.SAFE_DISTANCE) ? -1000000 : 0)
                )
                .sum();
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
                .filter(e -> e.turnsToKill(this) < e.turnsFromDataPoint())
                .min(comparingInt(e -> e.turnsToKill(this)));
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
        int range = SAFE_DISTANCE;

        int lox = Math.max(0, this.x - range);
        int hix = Math.min(16000 - 1, this.x + range);
        int loy = Math.max(0, this.y - range);
        int hiy = Math.min(9000 - 1, this.y + range);

        for (int y = loy; y < hiy; y += 250) {
            for (int x = lox; x < hix; x += 250) {
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

    int value = 0;

    public DataPoint(int id, int x, int y)
    {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public void addValue(DataPoint dp)
    {
        if (dp != this) {
            value += DATAPOINT_MAX_VALUE / this.distance(dp);
        }
    }

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

    public int getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return "DataPoint{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                ", enemies=" + enemies.size() +
                ", value=" + value +
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

    public Enemy(int id, int life, int x, int y)
    {
        this.id = id;
        this.life = life;
        this.y = y;
        this.x = x;
    }

    public int willDamageBy(Pos player)
    {
        double distance = this.distance(player);
        return (int) (MAX_DAMAGE / Math.pow(distance, 1.2));
    }

    public int turnsToKill(Pos player)
    {
        int damageBy = willDamageBy(player);
        return (int) Math.ceil(life / damageBy);
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

    public void setDataPoint(DataPoint dataPoint)
    {
        this.dataPoint = dataPoint;
        dataPoint.addEnemy(this);
        turnsFromDataPoint = (int) Math.ceil((this.distance(dataPoint) - 499) / ENEMY_SPEED);
        nextPos = getNexPos(1);
    }

    public Pos getNexPos()
    {
        return nextPos;
    }

    public Pos getNexPos(int round)
    {
        return this.add(dataPoint.subtract(this).normalize(ENEMY_SPEED).multiply(round));
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
            double v = Math.min(v1 < 0 ? Double.MAX_VALUE : v1, v2 < 0 ? Double.MAX_VALUE : v2);
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


