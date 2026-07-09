package model;

import java.util.List;

/**
 * A record representing an order with its associated tasks.
 *
 * @param id the unique order identifier.
 * @param dish the dish name.
 * @param status the order status.
 * @param tasks the list of atomic tasks composing this order.
 */
public record OrderRecord(
    int id, 
    String dish, 
    String status, 
    List<TaskRecord> tasks
) {}
