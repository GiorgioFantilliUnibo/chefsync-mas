package model;

/**
 * A record representing an atomic task within an order.
 *
 * @param name the task identifier.
 * @param assignedTo the name of the chef assigned, or null if unassigned.
 * @param completed whether the task has been completed.
 */
public record TaskRecord(
    String name, 
    String assignedTo, 
    boolean completed
) {}
