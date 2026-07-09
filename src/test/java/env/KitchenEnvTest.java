package env;

import java.util.Collection;
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
        Collection<Literal> percepts = env.getPercepts("head_chef");
        assertNotNull("getPercepts should not return null", percepts);
        assertTrue("Should contain kitchen_status(open)",
                percepts.stream().anyMatch(p -> p.equals(KitchenEnv.kso)));
        
        Literal expectedGrill = Literal.parseLiteral(KitchenEnv.BEL_WORKSTATION + "(grill, 2, 3)");
        assertTrue("Should contain dynamic grill workstation percept",
                percepts.stream().anyMatch(p -> p.equals(expectedGrill)));
    }

    @Test
    public void testUpdatePerceptsAddsAgentLocation() {
        Structure registerAction = (Structure) Literal.parseLiteral(KitchenEnv.ACT_REGISTER);
        env.executeAction("station_chef1", registerAction);
        
        Collection<Literal> percepts = env.getPercepts("station_chef1");
        Literal expected = Literal.parseLiteral("at(station_chef1, 0, 0)");
        assertTrue("getPercepts should include 'at' percept after registration",
                percepts != null && percepts.stream().anyMatch(p -> p.equals(expected)));
    }

    @Test
    public void testExecuteActionRingBell() {
        Structure action = (Structure) Literal.parseLiteral(KitchenEnv.ACT_RING_BELL + "(1)");
        
        assertFalse("Action 'ring_bell' should fail for station_chef", env.executeAction("station_chef1", action));
        assertTrue("Action 'ring_bell' should return true for head_chef", env.executeAction("head_chef", action));
    }

    @Test
    public void testExecuteActionRegisterOrder() {
        Structure action = (Structure) Literal.parseLiteral(
            KitchenEnv.ACT_REGISTER_ORDER + "(1, smash_burger, [grill_patty, toast_bun])");
        
        assertFalse("register_order should fail for station_chef",
            env.executeAction("station_chef1", action));
        assertTrue("register_order should succeed for head_chef",
            env.executeAction("head_chef", action));
    }

    @Test
    public void testExecuteActionAssignTask() {
        env.executeAction("head_chef", (Structure) Literal.parseLiteral(
            KitchenEnv.ACT_REGISTER_ORDER + "(1, burger, [grill_patty])"));
        
        Structure action = (Structure) Literal.parseLiteral(
            KitchenEnv.ACT_ASSIGN_TASK + "(1, grill_patty, station_chef1)");
        
        assertFalse("assign_task should fail for station_chef",
            env.executeAction("station_chef1", action));
        assertTrue("assign_task should succeed for head_chef",
            env.executeAction("head_chef", action));
    }

    @Test
    public void testExecuteActionCompleteTask() {
        env.executeAction("head_chef", (Structure) Literal.parseLiteral(
            KitchenEnv.ACT_REGISTER_ORDER + "(1, burger, [grill_patty])"));
        env.executeAction("head_chef", (Structure) Literal.parseLiteral(
            KitchenEnv.ACT_ASSIGN_TASK + "(1, grill_patty, station_chef1)"));
        
        Structure action = (Structure) Literal.parseLiteral(
            KitchenEnv.ACT_COMPLETE_TASK + "(1, grill_patty)");
        
        assertFalse("complete_task should fail for station_chef",
            env.executeAction("station_chef1", action));
        assertTrue("complete_task should succeed for head_chef",
            env.executeAction("head_chef", action));
    }

    @Test
    public void testExecuteActionLockAndUnlock() {
        Structure registerAction1 = (Structure) Literal.parseLiteral(KitchenEnv.ACT_REGISTER);
        env.executeAction("station_chef1", registerAction1);
        env.executeAction("station_chef1", (Structure) Literal.parseLiteral(KitchenEnv.ACT_STEP + "(2, 2)"));
        env.executeAction("station_chef1", (Structure) Literal.parseLiteral(KitchenEnv.ACT_STEP + "(2, 2)"));

        Structure registerAction2 = (Structure) Literal.parseLiteral(KitchenEnv.ACT_REGISTER);
        env.executeAction("station_chef2", registerAction2);
        env.executeAction("station_chef2", (Structure) Literal.parseLiteral(KitchenEnv.ACT_STEP + "(3, 3)"));
        env.executeAction("station_chef2", (Structure) Literal.parseLiteral(KitchenEnv.ACT_STEP + "(3, 3)"));
        env.executeAction("station_chef2", (Structure) Literal.parseLiteral(KitchenEnv.ACT_STEP + "(3, 3)"));

        Structure lockAction = (Structure) Literal.parseLiteral(KitchenEnv.ACT_LOCK + "(grill)");
        Structure unlockAction = (Structure) Literal.parseLiteral(KitchenEnv.ACT_UNLOCK + "(grill)");
        
        assertFalse("Head chef should not be able to lock", env.executeAction("head_chef", lockAction));
        assertTrue("station_chef1 should lock", env.executeAction("station_chef1", lockAction));
        assertFalse("station_chef2 should fail to lock occupied", env.executeAction("station_chef2", lockAction));
        assertFalse("station_chef2 should fail to unlock others", env.executeAction("station_chef2", unlockAction));
        assertTrue(env.executeAction("station_chef1", unlockAction));
        assertTrue(env.executeAction("station_chef2", lockAction));
    }

    @Test
    public void testExecuteActionMoveTowards() {
        Structure registerAction = (Structure) Literal.parseLiteral(KitchenEnv.ACT_REGISTER);
        env.executeAction("station_chef1", registerAction);
        
        Structure moveAction = (Structure) Literal.parseLiteral(KitchenEnv.ACT_STEP + "(1, 1)");
        
        assertFalse("Waiter should not be able to move towards", env.executeAction("waiter", moveAction));
        assertTrue("Action 'step_towards' should return true", env.executeAction("station_chef1", moveAction));
        
        Structure invalidMove = (Structure) Literal.parseLiteral(KitchenEnv.ACT_STEP + "(invalid, 1)");
        assertFalse("Action 'step_towards' with invalid coords should return false", env.executeAction("station_chef1", invalidMove));
    }

    @Test
    public void testExecuteActionUnknown() {
        Structure action = (Structure) Literal.parseLiteral("unknown_action");
        boolean result = env.executeAction("station_chef1", action);
        assertFalse("Unknown action should return false (failure)", result);
    }
    
    @Test
    public void testExecuteActionStepOff() {
        Structure registerAction = (Structure) Literal.parseLiteral(KitchenEnv.ACT_REGISTER);
        env.executeAction("station_chef1", registerAction);
        
        Structure stepOffAction = (Structure) Literal.parseLiteral(KitchenEnv.ACT_STEP_OFF);
        
        assertFalse("Waiter should not be able to step off", env.executeAction("waiter", stepOffAction));
        assertTrue("Station chef should be able to step off", env.executeAction("station_chef1", stepOffAction));
    }

    @Test
    public void testExecuteActionCooking() {
        Structure registerAction = (Structure) Literal.parseLiteral(KitchenEnv.ACT_REGISTER);
        env.executeAction("station_chef1", registerAction);
        
        env.executeAction("station_chef1", (Structure) Literal.parseLiteral(KitchenEnv.ACT_STEP + "(1, 1)"));
        env.executeAction("station_chef1", (Structure) Literal.parseLiteral(KitchenEnv.ACT_STEP + "(2, 2)"));
        env.executeAction("station_chef1", (Structure) Literal.parseLiteral(KitchenEnv.ACT_LOCK + "(grill)"));
        env.executeAction("station_chef1", (Structure) Literal.parseLiteral(KitchenEnv.ACT_STEP + "(2, 3)"));
        
        Structure startCooking = (Structure) Literal.parseLiteral(KitchenEnv.ACT_START_COOKING + "(grill_patty, 10)");
        
        assertFalse("Waiter cannot cook", env.executeAction("waiter", startCooking));
        assertTrue("Station chef can start cooking when at workstation", env.executeAction("station_chef1", startCooking));
    }
}
