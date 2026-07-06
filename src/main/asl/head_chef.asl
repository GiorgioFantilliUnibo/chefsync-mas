/* Initial beliefs and rules */

available_workstations(3).
cnp_counter(0).

// Recipe Ontology: recipe(DishName, [ListOfAtomicTasks])
recipe(smash_burger, [grill_patty, toast_bun, assemble_burger]).
recipe(romagnola_piadina, [heat_piadina, add_squacquerone, add_prosciutto]).


/* Initial goals */

!start_shift.


/* Plans */

+!start_shift : kitchen_status(open) <-
    .print("Kitchen Shift - Head Chef started.");
    .send(waiter, tell, ready_for_orders).


+!prepare_order(Dish, Table)[source(waiter)] : recipe(Dish, Tasks) <-
    .print("Order accepted: ", Dish, " (Table ", Table, ").");
    .print("Decomposing into tasks: ", Tasks);
    !delegate_tasks(Tasks).

+!prepare_order(Dish, Table)[source(waiter)] <-
    .print("Error! Unknown recipe for dish: ", Dish).


/* ContractNet Protocol - Initiator Logic */

+!delegate_tasks([]) <-
    .print("All sub-tasks requested! Dish assembly completed.");
    ring_bell.

+!delegate_tasks([Task | Rest]) <-
    ?cnp_counter(Id);
    -+cnp_counter(Id + 1);
    
    .df_search("station_chef", Chefs);
    .print("CFP: Requesting bids for task '", Task, "' from ", Chefs, " (CNP ", Id, ")");
    .send(Chefs, tell, cfp(Id, Task));
    
    .wait(2000);
    !evaluate_bids(Id, Task);
    !delegate_tasks(Rest).


+!evaluate_bids(Id, Task) <-
    .findall(offer(Bid, Agent), propose(Id, Bid)[source(Agent)], Offers);
    
    if (Offers \== []) {
        .print("Bids received for CNP ", Id, ": ", Offers);
        
        .min(Offers, offer(BestBid, Winner));
        .print("Awarding task '", Task, "' to ", Winner, " (Bid: ", BestBid, ")");
        
        .send(Winner, tell, accept_proposal(Id, Task));
        
        .abolish(propose(Id, _));
    } else {
        .print("No bids received for task ", Task, "! Brigade is unresponsive.");
    }.
