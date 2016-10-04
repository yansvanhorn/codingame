import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Marcin_Bazarnik on 2016-10-03.
 */
public class StateMachineTest {

//    @org.junit.Test
//    public void testAvgWithRounding() throws Exception {
//        StateMachine s = new StateMachine(0);
//        assertEquals(8, s.moreThanMidPoint(15, 0));
//        assertEquals(7, s.moreThanMidPoint(0, 15));
//
//
//    }
//
//    @Test
//    public void testXXX() throws Exception {
//        StateMachine s = new StateMachine(0);
//
//        assertEquals(18, s.lessThanHalfOfPrevious(11, 23));
//        assertEquals(19, s.lessThanHalfOfPrevious(12, 23));
//        assertEquals(18, s.lessThanHalfOfPrevious(13, 23));
//        assertEquals(18, s.lessThanHalfOfPrevious(14, 23));
//        assertEquals(19, s.lessThanHalfOfPrevious(15, 23));
//        assertEquals(19, s.lessThanHalfOfPrevious(16, 23));
//        assertEquals(19, s.lessThanHalfOfPrevious(17, 23));
//        assertEquals(21, s.lessThanHalfOfPrevious(18, 23));
//        assertEquals(21, s.lessThanHalfOfPrevious(19, 23));
//        assertEquals(22, s.lessThanHalfOfPrevious(20, 23));
//        assertEquals(22, s.lessThanHalfOfPrevious(21, 23));
//    }


    @Test
    public void middleTest() {
        StateMachine sm = new StateMachine(12);

        assertEquals(5,  sm.middleRounded(11, 0));
        assertEquals(6,  sm.middleRounded(0, 11));
        assertEquals(8,  sm.middleRounded(11, 6));

        assertEquals(2,  sm.middleRounded(5, 1));
    }
}