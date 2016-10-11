import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class DataPointsWithValueTest
        extends TestCase
{
    public void testFind1()
            throws Exception
    {
        List<DataPoint> dataPoints = Arrays.asList(
                new DataPoint(1, 1000, 1000),
                new DataPoint(2, 4000, 1000)
        );

        List<Enemy> enemies = Arrays.asList(
                new Enemy(1, 10, 1000, 3000).assignDataPoint(dataPoints.get(0)),
                new Enemy(2, 10, 1000, 3500).assignDataPoint(dataPoints.get(0)),

                new Enemy(3, 10, 4000, 3500).assignDataPoint(dataPoints.get(1)),
                new Enemy(4, 10, 4000, 4000).assignDataPoint(dataPoints.get(1))
        );

        Wolff wolff = new Wolff().updatePos(2000, 5000);
        Optional<DataPointsWithValue> dataPointsWithValue = new OptimalDataPointFinder().find(dataPoints, enemies, wolff);

    }

    public void testFindCornered()
            throws Exception
    {
        List<DataPoint> dataPoints = Arrays.asList(
                new DataPoint(2, 15500, 2500),
                new DataPoint(0, 15000, 8800),
                new DataPoint(1, 15500, 8500)
        );

        List<Enemy> enemies = Arrays.asList(
                new Enemy(1, 10, 11300, 4000).assignDataPoint(dataPoints.get(0)),
                new Enemy(2, 10, 11300, 5000).assignDataPoint(dataPoints.get(0)),

                new Enemy(4, 10, 11300, 7000).assignDataPoint(dataPoints.get(1)),
                new Enemy(3, 10, 11300, 7800).assignDataPoint(dataPoints.get(1)),
                new Enemy(0, 10, 11300, 8500).assignDataPoint(dataPoints.get(1))
        );

        Wolff wolff = new Wolff().updatePos(15000, 8000);
        Optional<DataPointsWithValue> dataPointsWithValue = new OptimalDataPointFinder().find(dataPoints, enemies, wolff);

    }
}