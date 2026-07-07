package env;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import model.KitchenModel;
import model.Role;

public class KitchenEnv extends Environment {

    // action literals
    public static final Literal rb = Literal.parseLiteral("ring_bell");

    public static final String ACT_LOCK = "lock";
    public static final String ACT_UNLOCK = "unlock";
    public static final String ACT_MOVE = "move_towards";

    // belief literals
    public static final Literal kso = Literal.parseLiteral("kitchen_status(open)");
    public static final Literal wsGrill = Literal.parseLiteral("workstation(grill, 1, 1)");
    public static final Literal wsOven = Literal.parseLiteral("workstation(oven, 3, 3)");
    public static final Literal wsPrep = Literal.parseLiteral("workstation(prep_counter, 4, 2)");

    
    private Logger logger = Logger.getLogger("chefsync.mas2j." + KitchenEnv.class.getName());

    private KitchenModel model;

    private Map<String, Integer> agentIds = new HashMap<>();
    private int nextAgId = 0;


    @Override
    public void init(String[] args) {
        logger.info("Kitchen Environment initialized. Setting up workstations...");
        this.clearPercepts();

        this.model = new KitchenModel();

        agentIds.clear();
        nextAgId = 0;

        this.addPercept(KitchenEnv.kso);

        this.addPercept(KitchenEnv.wsGrill);
        this.addPercept(KitchenEnv.wsOven);
        this.addPercept(KitchenEnv.wsPrep);
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        logger.info("[" + agName + "] doing: " + action);

        if (action.equals(KitchenEnv.rb)) {
            if (!checkPermission(agName, Role.HEAD_CHEF, KitchenEnv.rb.toString()))
                return false;
            logger.info("DING! Pass service completed.");
            return true;
        }
        String functor = action.getFunctor();

        // Action: lock(Workstation)
        if (functor.equals(ACT_LOCK)) {
            if (!checkPermission(agName, Role.STATION_CHEF, functor))
                return false;
            String station = action.getTerm(0).toString();

            if (model.lock(station, agName)) {
                logger.info("[" + agName + "] physically locked workstation: " + station);
                return true;
            } else {
                logger.warning("[" + agName + "] failed to lock " + station + " (already in use by "
                        + model.getLockOwner(station) + ")");
                return false;
            }
        }

        // Action: unlock(Workstation)
        if (functor.equals(ACT_UNLOCK)) {
            if (!checkPermission(agName, Role.STATION_CHEF, functor))
                return false;
            String station = action.getTerm(0).toString();

            if (model.unlock(station, agName)) {
                logger.info("[" + agName + "] released workstation: " + station);
                return true;
            }
            logger.warning("[" + agName + "] attempted to unlock " + station + " without permission.");
            return false;
        }

        // Action: move_towards(X, Y)
        if (functor.equals(ACT_MOVE)) {
            if (!checkPermission(agName, Role.STATION_CHEF, functor))
                return false;
            try {
                int x = Integer.parseInt(action.getTerm(0).toString());
                int y = Integer.parseInt(action.getTerm(1).toString());

                int agId = getOrAllocateAgentId(agName);

                model.moveTowards(agId, x, y);
                logger.info("[" + agName + "] moving towards coordinates (" + x + ", " + y + ")");
                return true;
            } catch (Exception e) {
                logger.severe("Invalid coordinates format for move_towards");
                return false;
            }
        }

        logger.info("[" + agName + "] attempted unknown action: " + action);
        return false;
    }


    private int getOrAllocateAgentId(String agName) {
        return agentIds.computeIfAbsent(agName, k -> nextAgId++);
    }

    private boolean checkPermission(String agName, Role requiredRole, String actionDesc) {
        Role agRole = Role.fromAgentName(agName);
        return agRole == requiredRole;
    }

}