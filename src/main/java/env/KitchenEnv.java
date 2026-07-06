package env;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import java.util.logging.Logger;

public class KitchenEnv extends Environment {

    // action literals
    public static final Literal rb = Literal.parseLiteral("ring_bell");

    // belief literals
    public static final Literal kso = Literal.parseLiteral("kitchen_status(open)");

    private Logger logger = Logger.getLogger("chefsync.mas2j." + KitchenEnv.class.getName());

    @Override
    public void init(String[] args) {
        logger.info("Kitchen Environment initialized. Setting up workstations...");
        this.clearPercepts();
        this.addPercept(KitchenEnv.kso);
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        logger.info("[" + agName + "] doing: " + action);

        if (action.equals(KitchenEnv.rb)) {
            logger.info("DING! Pass service completed.");
            return true;
        }

        return false;
    }
}