package model;

import jason.environment.grid.Location;
import java.util.Collection;
import view.KitchenView;
import java.util.List;

/**
 * The spatial model interface of the kitchen grid environment.
 * Exposes methods for workstations, positional tracking, lock mutual exclusions, and view notification.
 */
public interface KitchenModel {

    /**
     * Registers a new workstation in the kitchen model and the underlying grid.
     * @param ws the workstation to add.
     */
    void addWorkstation(Workstation ws);

    /**
     * Dynamically registers an agent into the physical grid model.
     * @param agId the internal model ID of the agent
     * @param name the agent's name
     * @param x the starting X coordinate
     * @param y the starting Y coordinate
     */
    void addAgent(int agId, String name, int x, int y);

    /**
     * Gets the name of the agent by its ID.
     */
    String getAgentName(int agId);

    /**
     * @return all workstations present in the kitchen.
     */
    Collection<Workstation> getWorkstations();

    /**
     * Retrieves the workstation at a specific grid location.
     * @param x the X coordinate.
     * @param y the Y coordinate.
     * @return the Workstation, or null if none is present at the location.
     */
    Workstation getWorkstationAt(int x, int y);

    /**
     * Attempts to acquire an exclusive lock on a workstation.
     *
     * @param station the name of the target workstation
     * @param agName  the name of the agent requesting the lock
     * @return true if the lock was successfully acquired, false otherwise
     */
    boolean lock(String station, String agName);

    /**
     * Attempts to release an exclusive lock on a workstation.
     *
     * @param station the name of the target workstation
     * @param agName  the name of the agent releasing the lock
     * @return true if the lock was successfully released, false otherwise
     */
    boolean unlock(String station, String agName);

    /**
     * Moves an agent one step towards a target destination.
     *
     * @param agId  the internal model ID of the agent
     * @param destX the target X coordinate
     * @param destY the target Y coordinate
     * @return true when movement completes
     */
    boolean moveTowards(int agId, int destX, int destY);

    /**
     * Moves an agent to a free adjacent cell, stepping off a workstation.
     * 
     * @param agId the internal model ID of the agent
     * @return true if successfully stepped off, false if blocked
     */
    boolean stepOff(int agId);

    /**
     * Retrieves the name of the agent currently holding a workstation's lock.
     *
     * @param station the name of the workstation
     * @return the agent's name, or null if unlocked
     */
    String getLockOwner(String station);

    /**
     * Starts cooking a task on the workstation currently used by the agent.
     * 
     * @param agId the internal model ID of the agent
     * @param task the task to cook
     * @param timeMs the duration in milliseconds
     * @return true if successful
     */
    boolean startCooking(int agId, String task, int timeMs);

    /**
     * Sets a listener to be notified when the environment state changes asynchronously (e.g. timers).
     */
    void setEnvironmentListener(Runnable listener);
    
    /**
     * Adds a new order to the system with its task breakdown.
     * 
     * @param orderId the order ID
     * @param dish the dish name
     * @param taskNames the list of atomic task names composing the order
     */
    void addOrder(int orderId, String dish, List<String> taskNames);

    /**
     * Updates the status of an existing order.
     * 
     * @param orderId the order ID
     * @param status the new status
     */
    void updateOrderStatus(int orderId, String status);

    /**
     * Marks a task within an order as assigned to a specific chef.
     * 
     * @param orderId the order ID
     * @param task the task name
     * @param chef the name of the assigned chef
     */
    void assignTask(int orderId, String task, String chef);

    /**
     * Marks a task within an order as completed.
     * 
     * @param orderId the order ID
     * @param task the task name
     */
    void completeTask(int orderId, String task);

    /**
     * Retrieves all tracked orders.
     * 
     * @return a collection of OrderRecord
     */
    Collection<OrderRecord> getOrders();

    /**
     * Gets the current location of the agent inside the grid.
     *
     * @param agId the internal model ID of the agent
     * @return the Location object representing (X, Y) coordinates
     */
    Location getAgPos(int agId);

    /**
     * Sets the decoupling view for UI notifications.
     *
     * @param view the concrete implementation of the KitchenView interface
     */
    void setView(KitchenView view);
}
