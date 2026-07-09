package env;

import jason.infra.local.RunLocalMAS;
import org.junit.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertTrue;

public class ChefsyncEndToEndTest {

    @Test
    public void testMASExecutionSmokeTest() throws InterruptedException {
        KitchenEnv.testLatch = new CountDownLatch(1);

        Thread masThread = new Thread(() -> {
            try {
                RunLocalMAS.main(new String[]{"chefsync.mas2j"});
            } catch (Exception e) {
                System.err.println("Fatal error: " + e.getMessage());
            }
        });
        
        masThread.setDaemon(true);
        masThread.start();

        boolean completed = KitchenEnv.testLatch.await(45, TimeUnit.SECONDS);

        assertTrue("MAS failed to complete order within 45 seconds timeout!", completed);
    }
}
