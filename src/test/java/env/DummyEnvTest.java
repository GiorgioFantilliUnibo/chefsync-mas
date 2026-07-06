package env;

import org.junit.Test;
import static org.junit.Assert.*;

public class DummyEnvTest {

    @Test
    public void testEnvironmentInitialization() {
        DummyEnv env = new DummyEnv();
        assertNotNull("Environment should not be null", env);
        assertTrue("JUnit is configured correctly", true);
    }
}