package model;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.HashMap;
import java.util.Map;

/**
 * The spatial model of the kitchen grid environment.
 * Manages workstations, positional tracking, and lock mutual exclusions.
 */
public class KitchenModel extends GridWorldModel {

    public static final int GSize = 7;
    
    private Map<String, String> workstationLocks = new HashMap<>();


    /**
     * Initializes a {@value #GSize}x{@value #GSize} grid world with two starting mobile agent positions
     * and sets up the default workstation layout.
     */
    public KitchenModel() {
        super(GSize, GSize, 2);
        
        this.setAgPos(0, 0, 0);
        this.setAgPos(1, 0, 1);
        
        workstationLocks.put("grill", null);
        workstationLocks.put("oven", null);
        workstationLocks.put("prep_counter", null);
    }

    /**
     * Attempts to acquire an exclusive lock on a workstation.
     *
     * @param station the name of the target workstation
     * @param agName  the name of the agent requesting the lock
     * @return true if the lock was successfully acquired, false otherwise
     */
    public boolean lock(String station, String agName) {
        if (workstationLocks.containsKey(station) && workstationLocks.get(station) == null) {
            workstationLocks.put(station, agName);
            return true;
        }
        return false;
    }

    /**
     * Attempts to release an exclusive lock on a workstation.
     *
     * @param station the name of the target workstation
     * @param agName  the name of the agent releasing the lock
     * @return true if the lock was successfully released, false otherwise
     */
    public boolean unlock(String station, String agName) {
        if (agName.equals(workstationLocks.get(station))) {
            workstationLocks.put(station, null);
            return true;
        }
        return false;
    }

    /**
     * Moves an agent one step towards a target destination.
     *
     * @param agId  the internal model ID of the agent
     * @param destX the target X coordinate
     * @param destY the target Y coordinate
     * @return true when movement completes
     */
    public boolean moveTowards(int agId, int destX, int destY) {
        Location r1 = this.getAgPos(agId);
        if (r1.x < destX) r1.x++;
        else if (r1.x > destX) r1.x--;

        if (r1.y < destY) r1.y++;
        else if (r1.y > destY) r1.y--;

        this.setAgPos(agId, r1);
        
        if (this.view != null) {
            this.view.update(r1.x, r1.y);
        }
        
        return true;
    }
    
    /**
     * Retrieves the name of the agent currently holding a workstation's lock.
     *
     * @param station the name of the target workstation
     * @return the name of the agent holding the lock, or null if unlocked
     */
    public String getLockOwner(String station) {
        return workstationLocks.get(station);
    }
}
