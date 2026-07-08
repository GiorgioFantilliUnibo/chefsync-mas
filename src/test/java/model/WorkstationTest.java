package model;

import jason.environment.grid.Location;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class WorkstationTest {

    private Workstation workstation;

    @Before
    public void setUp() {
        workstation = new WorkstationImpl("prep_counter", 5, 9);
    }

    @Test
    public void testInitialization() {
        assertEquals("prep_counter", workstation.getName());
        assertEquals(5, workstation.getX());
        assertEquals(9, workstation.getY());
        
        Location loc = workstation.getLocation();
        assertNotNull(loc);
        assertEquals(5, loc.x);
        assertEquals(9, loc.y);
        
        assertFalse("Workstation should be unlocked initially", workstation.isLocked());
        assertNull("Lock owner should be null initially", workstation.getLockOwner());
        
        assertTrue("Workstation should be operational initially", workstation.isOperational());
    }

    @Test
    public void testLocking() {
        String agent1 = "station_chef1";
        String agent2 = "station_chef2";

        assertTrue("First agent should acquire the lock", workstation.lock(agent1));
        assertTrue("Workstation should be marked as locked", workstation.isLocked());
        assertEquals("Lock owner should be agent1", agent1, workstation.getLockOwner());

        assertFalse("Second agent should not acquire the lock", workstation.lock(agent2));
        assertEquals("Lock owner should still be agent1", agent1, workstation.getLockOwner());
    }

    @Test
    public void testUnlocking() {
        String agent1 = "station_chef1";
        String agent2 = "station_chef2";

        workstation.lock(agent1);

        assertFalse("Second agent should not be able to unlock agent1's lock", workstation.unlock(agent2));
        assertTrue("Workstation should still be locked", workstation.isLocked());

        assertTrue("First agent should be able to unlock", workstation.unlock(agent1));
        assertFalse("Workstation should be unlocked", workstation.isLocked());
        assertNull("Lock owner should be null", workstation.getLockOwner());
    }
    
    @Test
    public void testOperationalStatus() {
        assertTrue(workstation.isOperational());
        
        workstation.setOperational(false);
        assertFalse("Workstation should not be operational", workstation.isOperational());
        
        workstation.setOperational(true);
        assertTrue("Workstation should be operational again", workstation.isOperational());
    }
}
