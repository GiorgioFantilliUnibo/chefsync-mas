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
import view.*;

/**
 * Jason Environment class representing the bridge between the BDI agents
 * and the physical model {@link KitchenModel}. Handles action mapping, permissions,
 * and perception delivery.
 */
public class KitchenEnv extends Environment {

    // action literals
    public static final String ACT_LOCK = "lock";
    public static final String ACT_UNLOCK = "unlock";
    public static final String ACT_STEP = "step_towards";
    public static final String ACT_STEP_OFF = "step_off";
    public static final String ACT_REGISTER = "register";
    public static final String ACT_START_COOKING = "start_cooking";
    public static final String ACT_REGISTER_ORDER = "register_order";
    public static final String ACT_RING_BELL = "ring_bell";

    // belief literals
    public static final Literal kso = Literal.parseLiteral("kitchen_status(open)");
    public static final String BEL_WORKSTATION = "workstation";
    
    
    private Logger logger = Logger.getLogger("chefsync.mas2j." + KitchenEnv.class.getName());

    private KitchenModel model;

    private Map<String, Integer> agentIds = new ConcurrentHashMap<>();
    private AtomicInteger nextAgId = new AtomicInteger(0);


    @Override
    public void init(String[] args) {
        logger.info("Kitchen Environment initialized. Setting up workstations...");

        KitchenModelImpl impl = new KitchenModelImpl();
        this.model = impl;
        KitchenView view = new KitchenViewImpl(impl);

        this.model.setView(view);
        this.model.setEnvironmentListener(() -> informAgsEnvironmentChanged());

        agentIds.clear();
        nextAgId.set(0);
    }

    @Override
    public Collection<Literal> getPercepts(String agName) {
        List<Literal> percepts = new ArrayList<>();

        percepts.add(kso);
        
        for (Workstation ws : model.getWorkstations()) {
            percepts.add(Literal.parseLiteral(BEL_WORKSTATION + "(" + ws.getName() + ", " + ws.getX() + ", " + ws.getY() + ")"));
        }

        for (Map.Entry<String, Integer> entry : agentIds.entrySet()) {
            Location loc = model.getAgPos(entry.getValue());
            if (loc != null) {
                percepts.add(Literal.parseLiteral("at(" + entry.getKey() + ", " + loc.x + ", " + loc.y + ")"));
                
                if (entry.getKey().equals(agName)) {
                    Workstation ws = model.getWorkstationAt(loc.x, loc.y);
                    if (ws != null && ws.getCompletedTask() != null) {
                        percepts.add(Literal.parseLiteral("cooked(" + ws.getCompletedTask() + ")"));
                    }
                }
            }
        }

        return percepts;
    }

    @Override
    public boolean executeAction(String agName, Structure action) {
        logger.info("[" + agName + "] doing: " + action);
        
        String functor = action.getFunctor();
        
        if (functor.equals(ACT_RING_BELL)) {
            if (!checkPermission(agName, Role.HEAD_CHEF, ACT_RING_BELL)) return false;
            int orderId = -1;
            try {
                if (action.getArity() > 0) {
                    orderId = (int) ((jason.asSyntax.NumberTerm) action.getTerm(0)).solve();
                }
            } catch (Exception e) {}
            
            if (orderId > 0 && model instanceof KitchenModelImpl) {
                ((KitchenModelImpl) model).updateOrderStatus(orderId, "COMPLETED");
            }
            logger.info("DING! Pass service completed for Order " + orderId);
            return true;
        }

        if (functor.equals(ACT_REGISTER_ORDER)) {
            if (!checkPermission(agName, Role.HEAD_CHEF, ACT_REGISTER_ORDER)) return false;
            try {
                int orderId = (int) ((jason.asSyntax.NumberTerm) action.getTerm(0)).solve();
                String dish = action.getTerm(1).toString();
                if (model instanceof KitchenModelImpl) {
                    ((KitchenModelImpl) model).addOrder(orderId, dish);
                }
                logger.info("Order " + orderId + " (" + dish + ") injected into the dashboard.");
                return true;
            } catch (Exception e) {
                logger.warning("Failed to register order: " + e.getMessage());
                return false;
            }
        }

        // Action: register
        if (functor.equals(ACT_REGISTER)) {
            int agId = getOrAllocateAgentId(agName);
            if (agName.startsWith("station_chef")) {
                model.addAgent(agId, agName, 0, agId);
            }
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

        // Action: step_towards(X, Y)
        if (functor.equals(ACT_STEP)) {
            if (!checkPermission(agName, Role.STATION_CHEF, functor)) return false;
            try {
                int x = Integer.parseInt(action.getTerm(0).toString());
                int y = Integer.parseInt(action.getTerm(1).toString());

                int agId = getOrAllocateAgentId(agName);

                boolean moved = model.moveTowards(agId, x, y);
                if (moved) {
                    logger.info("[" + agName + "] stepping towards coordinates (" + x + ", " + y + ")");
                }
                return moved;
            } catch (Exception e) {
                logger.severe("Invalid coordinates format for step_towards");
                return false;
            }
        }
        
        // Action: step_off
        if (functor.equals(ACT_STEP_OFF)) {
            if (!checkPermission(agName, Role.STATION_CHEF, functor)) return false;
            int agId = getOrAllocateAgentId(agName);
            boolean steppedOff = model.stepOff(agId);
            if (steppedOff) {
                logger.info("[" + agName + "] stepped off the workstation.");
            } else {
                logger.warning("[" + agName + "] could not step off the workstation (blocked).");
            }
            return steppedOff;
        }

        // Action: start_cooking
        if (functor.equals(ACT_START_COOKING)) {
            if (!checkPermission(agName, Role.STATION_CHEF, functor)) return false;
            String task = action.getTerm(0).toString();
            int time = Integer.parseInt(action.getTerm(1).toString());
            int agId = getOrAllocateAgentId(agName);
            
            boolean started = model.startCooking(agId, task, time);
            if (started) {
                logger.info("[" + agName + "] started cooking " + task + " for " + time + "ms.");
            } else {
                logger.warning("[" + agName + "] could not start cooking " + task);
            }
            return started;
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