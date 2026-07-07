package env;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.grid.Location;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import model.*;

/**
 * Jason Environment class representing the bridge between the BDI agents
 * and the physical model {@link KitchenModel}. Handles action mapping, permissions,
 * and perception delivery.
 */
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

    private Map<String, Integer> agentIds = new ConcurrentHashMap<>();
    private AtomicInteger nextAgId = new AtomicInteger(0);


    @Override
    public void init(String[] args) {
        logger.info("Kitchen Environment initialized. Setting up workstations...");

        this.model = new KitchenModelImpl();

        agentIds.clear();
        nextAgId.set(0);
    }

    @Override
    public Collection<Literal> getPercepts(String agName) {
        List<Literal> percepts = new ArrayList<>();

        percepts.add(kso);
        percepts.add(wsGrill);
        percepts.add(wsOven);
        percepts.add(wsPrep);

        Integer agId = agentIds.get(agName);
        if (agId != null) {
            Location loc = model.getAgPos(agId);
            if (loc != null) {
                percepts.add(Literal.parseLiteral("at(" + agName + ", " + loc.x + ", " + loc.y + ")"));
            }
        }

        return percepts;
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

        // Action: register
        if (functor.equals("register")) {
            getOrAllocateAgentId(agName);
            return true;
        }

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
        return agentIds.computeIfAbsent(agName, k -> nextAgId.getAndIncrement());
    }

    private boolean checkPermission(String agName, Role requiredRole, String actionDesc) {
        Role agRole = Role.fromAgentName(agName);
        return agRole == requiredRole;
    }

}