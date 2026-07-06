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
    }

    @Test
    public void testExecuteActionRingBell() {
        Structure action = (Structure) KitchenEnv.rb;
        boolean result = env.executeAction("station_chef", action);
        assertTrue("Action 'ring_bell' should return true (success)", result);
    }

    @Test
    public void testExecuteActionUnknown() {
        Structure action = (Structure) Literal.parseLiteral("unknown_action");
        boolean result = env.executeAction("station_chef", action);
        assertFalse("Unknown action should return false (failure)", result);
    }
}
