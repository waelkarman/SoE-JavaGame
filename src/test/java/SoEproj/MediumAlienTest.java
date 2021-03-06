package SoEproj;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;


public class MediumAlienTest implements CommonValues{

    private MediumAlien alien;


    @Before
    public void initAlien() {
        alien = new MediumAlien(B_WIDTH, 150);
    }
    

    @Test
    public void testCoordinates() {
        assertEquals(alien.getX(), B_WIDTH);
        assertEquals(alien.getY(), 150);
    }


    @Test
    public void testDefaultValue() {
        assertEquals(alien.getLife(), 2);
        assertEquals(alien.getPoints(), 75);
        assertNotNull(alien.getMissiles());
        assertNotNull(alien.getSPACE());
        assertNotNull(alien.getShape());
        assertTrue(alien.isVisible());
        assertFalse(alien.isDying());
    }


    @Test
    public void testMove() {
        alien.move();
        assertEquals(alien.getX(), B_WIDTH-2);
        assertEquals(alien.getY(), 150);
    }


    @Test(timeout = 5000)
    public void testIsDying() {
        while(alien.getX() + alien.getWidth() >= 0)
            alien.move();

        assertTrue(alien.isDying());
    }


    @Test
    public void testFire() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        synchronized(alien.getMissiles()){
            assertTrue(alien.getMissiles().size() > 0);
        }
    }

}
