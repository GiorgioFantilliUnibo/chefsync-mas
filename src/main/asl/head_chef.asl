/* Initial beliefs and rules */

available_workstations(3).


/* Initial goals */

!start_shift.


/* Plans */

+!start_shift : kitchen_status(open) <-
    .print("Kitchen Shift - Head Chef started.");
    .send(waiter, tell, ready_for_orders).

+!prepare_order(Dish, Table)[source(waiter)] <-
    .print("Head Chef received order for Table ", Table, ": ", Dish);
    .print("Decomposing recipe for ", Dish, "...");
    
    // TODO: ContractNet verso station_chef

    ring_bell.