/* Initial beliefs and rules */

workload(0).

// Task to workstation mappings
task_workstation(grill_patty, grill).
task_workstation(toast_bun, grill).
task_workstation(assemble_burger, prep_counter).
task_workstation(heat_piadina, oven).
task_workstation(add_squacquerone, prep_counter).
task_workstation(add_prosciutto, prep_counter).


/* Initial goals */

!report_duty.


/* Plans */

+!report_duty <-
    .my_name(Name);
    register;
    .df_register("station_chef");
    .print("Ready at station: ", Name).


/* ContractNet Protocol */

// --- Bidding Phase ---
// Computes bid based on spatial distance and current workload

+cfp(AuctionId, OrderId, Task)[source(head_chef)] <-
    ?workload(W);
    .my_name(Name);
    ?at(Name, MyX, MyY);
    ?task_workstation(Task, Station);
    ?workstation(Station, StatX, StatY);
    Distance = math.abs(MyX - StatX) + math.abs(MyY - StatY);
    Bid = Distance + (W * 5);
    .print("Evaluating CFP ", AuctionId, " for task: ", Task, " (Order ", OrderId, "). Bid: ", Bid);
    .send(head_chef, tell, propose(AuctionId, OrderId, Bid)).

// --- Task Execution Pipeline ---
// Manages the sequential process of moving to, locking, and using a workstation

+accept_proposal(AuctionId, OrderId, Task)[source(head_chef)] <-
    .print("Won contract ", AuctionId, " for Order ", OrderId, "! Executing: ", Task);
    ?workload(W);
    -+workload(W + 1);
    
    !perform_task(AuctionId, OrderId, Task, 0);
    
    ?workload(CurrentW);
    -+workload(CurrentW - 1).


+!perform_task(AuctionId, OrderId, Task, Attempts) 
    : task_workstation(Task, Station) & workstation(Station, X, Y) 
    <-  .print("Moving to ", Station, " at (", X, ", ", Y, ")");
        move_towards(X, Y);
        
        .print("Locking ", Station);
        lock(Station); 
        
        .print("Executing ", Task, " on ", Station, "...");
        .wait(1500);
        
        .print("Unlocking ", Station);
        unlock(Station);
        
        .send(head_chef, tell, task_completed(OrderId, Task)).

// --- Fallback & Error Handling ---
// BDI failure recovery: retries on lock failures before giving up completely

-!perform_task(AuctionId, OrderId, Task, Attempts) : Attempts < 3 <-
    .print("Lock failed for ", Task, " (Order ", OrderId, "). Retrying in 2s... (Attempt ", Attempts + 1, ")");
    .wait(2000);
    !perform_task(AuctionId, OrderId, Task, Attempts + 1).

-!perform_task(AuctionId, OrderId, Task, Attempts) : Attempts >= 3 <-
    .print("Max lock attempts reached for ", Task, " (Order ", OrderId, "). Aborting.");
    .send(head_chef, tell, task_failed(OrderId, Task)).
