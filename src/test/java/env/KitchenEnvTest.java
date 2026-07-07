package env;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class KitchenEnvTest {

    private KitchenEnv env;

    @Before
    public void setUp() {
        env = new KitchenEnv();
        env.init(new String[]{});
    }

    @Test
    public void testInitAddsKitchenStatusPercept() {
        boolean hasPercept = env.containsPercept(KitchenEnv.kso);
        assertTrue("Environment should contain kitchen_status(open) percept after initialization", hasPercept);
        
        boolean hasGrill = env.containsPercept(KitchenEnv.wsGrill);
        assertTrue("Environment should contain grill workstation", hasGrill);
    }

    @Test
    public void testExecuteActionRingBell() {
        Structure action = (Structure) KitchenEnv.rb;
        
        assertFalse("Action 'ring_bell' should fail for station_chef", env.executeAction("station_chef1", action));
        assertTrue("Action 'ring_bell' should return true for head_chef", env.executeAction("head_chef", action));
    }

    @Test
    public void testExecuteActionLockAndUnlock() {
        Structure lockAction = (Structure) Literal.parseLiteral(KitchenEnv.ACT_LOCK + "(grill)");
        Structure unlockAction = (Structure) Literal.parseLiteral(KitchenEnv.ACT_UNLOCK + "(grill)");
        
        assertFalse("Head chef should not be able to lock", env.executeAction("head_chef", lockAction));
        assertTrue(env.executeAction("station_chef1", lockAction));
        assertFalse(env.executeAction("station_chef2", lockAction));
        assertFalse(env.executeAction("station_chef2", unlockAction));
        assertTrue(env.executeAction("station_chef1", unlockAction));
        assertTrue(env.executeAction("station_chef2", lockAction));
    }

    @Test
    public void testExecuteActionMoveTowards() {
        Structure moveAction = (Structure) Literal.parseLiteral(KitchenEnv.ACT_MOVE + "(1, 1)");
        
        assertFalse("Waiter should not be able to move towards", env.executeAction("waiter", moveAction));
        assertTrue("Action 'move_towards' should return true", env.executeAction("station_chef1", moveAction));
        
        Structure invalidMove = (Structure) Literal.parseLiteral(KitchenEnv.ACT_MOVE + "(invalid, 1)");
        assertFalse("Action 'move_towards' with invalid coords should return false", env.executeAction("station_chef1", invalidMove));
    }

    @Test
    public void testExecuteActionUnknown() {
        Structure action = (Structure) Literal.parseLiteral("unknown_action");
        boolean result = env.executeAction("station_chef1", action);
        assertFalse("Unknown action should return false (failure)", result);
    }
}
