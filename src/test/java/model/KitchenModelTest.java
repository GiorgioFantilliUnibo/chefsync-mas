package model;

import jason.environment.grid.Location;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

public class KitchenModelTest {
    
    private KitchenModel model;
    
    @Before
    public void setUp() {
        model = new KitchenModelImpl();
    }
    
    @Test
    public void testAddWorkstation() {
        Workstation newWs = new WorkstationImpl("test_station", 10, 10);
        model.addWorkstation(newWs);
        
        assertNotNull("Workstation should be retrievable by coordinates", model.getWorkstationAt(10, 10));
        assertEquals("test_station", model.getWorkstationAt(10, 10).getName());
        
        boolean found = model.getWorkstations().stream().anyMatch(ws -> ws.getName().equals("test_station"));
        assertTrue("Workstation should be present in the collection", found);
    }
    
    @Test
    public void testLockUnlock() {
        int ag1 = 1;
        int ag2 = 2;
        model.addAgent(ag1, "chef1", 2, 2);
        model.addAgent(ag2, "chef2", 3, 3);

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
        model.addAgent(agId, "station_chef1", 0, 0);
        Location initialPos = model.getAgPos(agId);
        
        Location dest = new Location(initialPos.x + 2, initialPos.y + 1);
        
        model.moveTowards(agId, dest.x, dest.y);
        Location newPos = model.getAgPos(agId);
        assertEquals(initialPos.x + 1, newPos.x);
        assertEquals(initialPos.y + 1, newPos.y);
    }
    
    @Test
    public void testLockDistanceConstraint() {
        int agFar = 3;
        model.addAgent(agFar, "chefFar", 0, 0);
        assertFalse("Agent should not be able to lock if distance is > 1", model.lock("grill", "chefFar"));
        
        int agClose = 4;
        model.addAgent(agClose, "chefClose", 2, 2);
        assertTrue("Agent should lock if distance is <= 1", model.lock("grill", "chefClose"));
    }
    
    @Test
    public void testMoveTowardsAvoidsAgents() {
        int ag1 = 5;
        int ag2 = 6;
        model.addAgent(ag1, "chefA", 5, 5);
        model.addAgent(ag2, "chefB", 6, 6);
        
        model.moveTowards(ag1, 7, 7);
        Location newPos = model.getAgPos(ag1);
        
        assertFalse("Agent should not step on another agent", newPos.x == 6 && newPos.y == 6);
        assertTrue("Agent should move to a free adjacent cell", 
            (newPos.x == 5 && newPos.y == 6) || (newPos.x == 6 && newPos.y == 5));
    }
    
    @Test
    public void testStepOff() {
        int agId = 7;
        model.addAgent(agId, "chefStepOff", 2, 3);
        
        model.addAgent(8, "blocker", 2, 2);
        
        assertTrue("Agent should successfully step off the workstation", model.stepOff(agId));
        Location newPos = model.getAgPos(agId);
        
        assertFalse("Agent should no longer be on the grill", newPos.x == 2 && newPos.y == 3);
        assertFalse("Agent should not step on another agent", newPos.x == 2 && newPos.y == 2);
        
        assertNotNull(model.getWorkstationAt(2, 3));
    }

    @Test
    public void testStartCookingAndUnlock() throws InterruptedException {
        int agId = 9;
        model.addAgent(agId, "chefCook", 2, 3);
        model.lock("grill", "chefCook");
        
        assertTrue("Agent should be able to start cooking", model.startCooking(agId, "patty", 50));
        
        Workstation ws = model.getWorkstationAt(2, 3);
        assertNull(ws.getCompletedTask());
        
        Thread.sleep(100);
        assertEquals("patty", ws.getCompletedTask());
        
        model.unlock("grill", "chefCook");
        assertNull("Task should be cleared after unlock", ws.getCompletedTask());
    }

    @Test
    public void testOrderTracking() {
        assertEquals(0, model.getOrders().size());
        
        model.addOrder(1, "pizza", List.of("make_dough", "add_topping", "bake"));
        model.addOrder(2, "pasta", List.of("boil", "sauce"));
        
        assertEquals(2, model.getOrders().size());
        
        OrderRecord order1 = model.getOrders().stream().filter(o -> o.id() == 1).findFirst().orElse(null);
        assertNotNull(order1);
        assertEquals("pizza", order1.dish());
        assertEquals("PENDING", order1.status());
        assertEquals(3, order1.tasks().size());
        assertNull(order1.tasks().get(0).assignedTo());
        assertFalse(order1.tasks().get(0).completed());
        
        model.assignTask(1, "make_dough", "station_chef1");
        order1 = model.getOrders().stream().filter(o -> o.id() == 1).findFirst().orElse(null);
        assertEquals("station_chef1", order1.tasks().get(0).assignedTo());
        assertFalse(order1.tasks().get(0).completed());
        assertNull(order1.tasks().get(1).assignedTo());
        
        model.completeTask(1, "make_dough");
        order1 = model.getOrders().stream().filter(o -> o.id() == 1).findFirst().orElse(null);
        assertTrue(order1.tasks().get(0).completed());
        
        model.updateOrderStatus(1, "COMPLETED");
        order1 = model.getOrders().stream().filter(o -> o.id() == 1).findFirst().orElse(null);
        assertEquals("COMPLETED", order1.status());
        
        OrderRecord order2 = model.getOrders().stream().filter(o -> o.id() == 2).findFirst().orElse(null);
        assertEquals("PENDING", order2.status());
        assertEquals(2, order2.tasks().size());
    }
}
