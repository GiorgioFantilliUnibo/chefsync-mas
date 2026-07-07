package model;

import jason.environment.grid.Location;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class KitchenModelTest {
    
    private KitchenModel model;
    
    @Before
    public void setUp() {
        model = new KitchenModel();
    }
    
    @Test
    public void testLockUnlock() {
        assertTrue("Agent should be able to lock a free station", model.lock("grill", "chef1"));
        assertFalse("Agent should not be able to lock an occupied station", model.lock("grill", "chef2"));
        assertEquals("chef1", model.getLockOwner("grill"));
        
        assertFalse("Agent should not be able to unlock someone else's station", model.unlock("grill", "chef2"));
        assertTrue("Agent should be able to unlock its own station", model.unlock("grill", "chef1"));
        assertNull("Station should be free after unlock", model.getLockOwner("grill"));
    }
    
    @Test
    public void testMoveTowards() {
        int agId = 0;
        Location initialPos = model.getAgPos(agId);
        
        Location dest = new Location(initialPos.x + 2, initialPos.y + 1);
        
        model.moveTowards(agId, dest.x, dest.y);
        Location newPos = model.getAgPos(agId);
        assertEquals(initialPos.x + 1, newPos.x);
        assertEquals(initialPos.y + 1, newPos.y);
        
        model.moveTowards(agId, dest.x, dest.y);
        newPos = model.getAgPos(agId);
        assertEquals(initialPos.x + 2, newPos.x);
        assertEquals(initialPos.y + 1, newPos.y);
    }
}
