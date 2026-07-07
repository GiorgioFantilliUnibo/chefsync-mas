/* Initial beliefs and rules */

cnp_counter(0).
order_counter(0).

// Recipe Ontology: recipe(DishName, [ListOfAtomicTasks])
recipe(smash_burger, [grill_patty, toast_bun, assemble_burger]).
recipe(romagnola_piadina, [heat_piadina, add_squacquerone, add_prosciutto]).


/* Initial goals */

!start_shift.


/* Plans */

+!start_shift : kitchen_status(open) <-
    .print("Kitchen Shift - Head Chef started.");
    .send(waiter, tell, ready_for_orders).

// --- Order Reception & IDs Generation ---
// Atomic counters ensure unique IDs across concurrent orders and auctions

@get_order_id[atomic]
+!get_next_order_id(NextId) <-
    ?order_counter(Current);
    NextId = Current + 1;
    -+order_counter(NextId).


@get_auction_id[atomic]
+!get_next_auction_id(NextId) <-
    ?cnp_counter(Current);
    NextId = Current + 1;
    -+cnp_counter(NextId).


+!prepare_order(Dish, Table)[source(waiter)] : recipe(Dish, Tasks) <-
    !get_next_order_id(OrderId);
    .length(Tasks, TotalTasks);
    
    +pending_tasks(OrderId, Dish, Table, TotalTasks);
    .print("Order ", OrderId, " accepted: ", Dish, " (Table ", Table, "). Total sub-tasks: ", TotalTasks);
    
    !dispatch_parallel_auctions(OrderId, Tasks).

+!prepare_order(Dish, Table)[source(waiter)] <-
    .print("Error! Unknown recipe for dish: ", Dish).

// --- Auction Orchestration ---
// Spawns independent intentions for each task to avoid blocking the main thread

+!dispatch_parallel_auctions(_, []).
+!dispatch_parallel_auctions(OrderId, [Task | Rest]) <-
    !!start_asynchronous_auction(OrderId, Task);
    !dispatch_parallel_auctions(OrderId, Rest).


+!start_asynchronous_auction(OrderId, Task) <-
    !get_next_auction_id(AuctionId);
    .df_search("station_chef", Chefs);
    .print("CNP ", AuctionId, " launched for task '", Task, "' (Order ", OrderId, ") targeting chefs: ", Chefs);
    
    .send(Chefs, tell, cfp(AuctionId, OrderId, Task));
    .wait(2000);
    !evaluate_bids(AuctionId, OrderId, Task).

// --- Bid Evaluation ---
// Collects proposals after the timeout and awards the task to the lowest bidder

+!evaluate_bids(AuctionId, OrderId, Task) <-
    .findall(offer(Bid, Agent), propose(AuctionId, OrderId, Bid)[source(Agent)], Offers);
    if (Offers \== []) {
        .min(Offers, offer(BestBid, Winner));
        .print("Awarding task '", Task, "' (Order ", OrderId, ") to ", Winner, " (Bid: ", BestBid, ")");
        .send(Winner, tell, accept_proposal(AuctionId, OrderId, Task));
        .abolish(propose(AuctionId, OrderId, _));
    } else {
        .print("No bids received for task ", Task, " (Order ", OrderId, ")! Retrying auction in 1s...");
        .wait(1000);
        !!start_asynchronous_auction(OrderId, Task);
    }.

// --- Synchronization Barrier ---
// Tracks the completion of all sub-tasks for a specific order. 
// The bell rings only when the barrier counter reaches exactly zero.

@task_completion_handler[atomic]
+task_completed(OrderId, Task)[source(Chef)] <-
    ?pending_tasks(OrderId, Dish, Table, Remaining);
    -pending_tasks(OrderId, Dish, Table, Remaining);
    NewRemaining = Remaining - 1;
    +pending_tasks(OrderId, Dish, Table, NewRemaining);
    .print("Chef ", Chef, " completed '", Task, "' for Order ", OrderId, ". Remaining tasks in barrier: ", NewRemaining);
    
    .abolish(task_completed(OrderId, Task)[source(Chef)]);
    !check_barrier_status(OrderId).


+!check_barrier_status(OrderId) : pending_tasks(OrderId, Dish, Table, 0) <-
    .print("SUCCESS: Order ", OrderId, " (", Dish, ") for Table ", Table, " is fully completed!");
    ring_bell;
    .abolish(pending_tasks(OrderId, _, _, _)).

+!check_barrier_status(_).

// --- Error Recovery ---
// Responds to task failures by immediately re-auctioning the aborted task

+task_failed(OrderId, Task)[source(Chef)] <-
    .print("CRITICAL: Chef ", Chef, " aborted task '", Task, "' for Order ", OrderId, ". Re-auctioning task immediately.");
    .abolish(task_failed(OrderId, Task)[source(Chef)]);
    !!start_asynchronous_auction(OrderId, Task).
