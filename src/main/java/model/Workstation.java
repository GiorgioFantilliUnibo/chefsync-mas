package model;

import jason.environment.grid.Location;

/**
 * Domain interface for a Workstation in the kitchen.
 */
public interface Workstation {
    
    /**
     * @return the name of the workstation.
     */
    String getName();
    
    /**
     * @return the grid location of the workstation.
     */
    Location getLocation();
    
    /**
     * @return the X coordinate.
     */
    int getX();
    
    /**
     * @return the Y coordinate.
     */
    int getY();
    
    /**
     * @return the name of the agent currently holding the lock, or null if free.
     */
    String getLockOwner();
    
    /**
     * @return true if the workstation is currently locked by an agent.
     */
    boolean isLocked();
    
    /**
     * Attempts to lock the workstation for a specific agent.
     * @param agName the agent requesting the lock.
     * @return true if successful, false if already locked.
     */
    boolean lock(String agName);
    
    /**
     * Unlocks the workstation if the requesting agent is the current owner.
     * @param agName the agent requesting the unlock.
     * @return true if successful, false if not the owner.
     */
    boolean unlock(String agName);
    
    /**
     * @return true if the workstation is operational, false if broken.
     */
    boolean isOperational();
    
    /**
     * Sets the operational status of the workstation.
     * @param operational true for operational, false for broken.
     */
    void setOperational(boolean operational);
}
