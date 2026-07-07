/* Initial beliefs and rules */

workload(0).
pending_bids(0).

// Task to workstation mappings
task_workstation(grill_patty, grill).
task_workstation(toast_bun, grill).
task_workstation(assemble_burger, prep_counter).
task_workstation(heat_piadina, oven).
task_workstation(add_squacquerone, prep_counter).
task_workstation(add_prosciutto, prep_counter).

task_workstation(slice_tomatoes, salad_bar).
task_workstation(slice_mozzarella, salad_bar).
task_workstation(plate_salad, prep_counter).

task_workstation(bread_calamari, prep_counter).
task_workstation(fry_calamari, fryer).
task_workstation(plate_calamari, prep_counter).

task_workstation(prepare_coffee, stove).
task_workstation(layer_mascarpone, dessert_station).
task_workstation(dust_cocoa, dessert_station).

task_workstation(boil_pasta, stove).
task_workstation(fry_guanciale, stove).
task_workstation(mix_egg_cheese, prep_counter).


/* Initial goals */

!report_duty.


/* Plans */

+!report_duty <-
    .my_name(Name);
    register;
    .df_register("station_chef");
    .print("Ready at station chef: ", Name).


/* ContractNet Protocol */

// --- Bidding Phase ---
// Computes bid based on spatial distance. Can bid on multiple auctions concurrently.

+cfp(AuctionId, OrderId, Task)[source(head_chef)] : workload(0) <-
    .my_name(Name);
    ?task_workstation(Task, Station);
    ?workstation(Station, StatX, StatY);
    
    if (at(OtherAgent, StatX, StatY) & OtherAgent \== Name | claiming(_, Station)[source(OtherAgent)]) {
        .print("Refusing CFP ", AuctionId, " for task: ", Task, " because workstation ", Station, " is occupied or claimed by ", OtherAgent);
        .send(head_chef, tell, refuse(AuctionId, OrderId));
    } else {
        ?at(Name, MyX, MyY);
        Distance = math.abs(MyX - StatX) + math.abs(MyY - StatY);
        Bid = Distance;
        
        -+pending_bids(1);
        .print("Evaluating CFP ", AuctionId, " for task: ", Task, " (Order ", OrderId, "). Bid: ", Bid);
        .send(head_chef, tell, propose(AuctionId, OrderId, Bid));
    }.

+cfp(AuctionId, OrderId, Task)[source(head_chef)] <-
    .print("Refusing CFP ", AuctionId, " for task: ", Task, " (I am busy)");
    .send(head_chef, tell, refuse(AuctionId, OrderId)).

// --- Task Execution Pipeline ---
// Manages the sequential process of moving to, locking, and using a workstation

+accept_proposal(AuctionId, OrderId, Task)[source(head_chef)] <-
    ?task_workstation(Task, Station);
    .broadcast(tell, claiming(OrderId, Station));
    
    .print("Won contract ", AuctionId, " for Order ", OrderId, "! Queuing: ", Task);
    ?workload(W);
    -+workload(W + 1);
    
    +queued_task(AuctionId, OrderId, Task);
    !!process_queue.

+reject_proposal(AuctionId, OrderId, Task)[source(head_chef)] <-
    -+pending_bids(0);
    .print("Lost contract ", AuctionId, " for task ", Task, ". I am free to bid again.").

// --- Task Queue Processor ---
// Ensures tasks are executed sequentially without interleaving movement

+!process_queue : not busy & queued_task(AuctionId, OrderId, Task) <-
    +busy;
    -queued_task(AuctionId, OrderId, Task);
    
    !perform_task(AuctionId, OrderId, Task, 0);
    
    ?task_workstation(Task, Station);
    .broadcast(untell, claiming(OrderId, Station));
    
    ?workload(CurrentW);
    -+workload(CurrentW - 1);
    
    -busy;
    !!process_queue.

+!process_queue : busy <- true.
+!process_queue : not queued_task(_,_,_) <- true.

// Fallback to avoid deadlock if a task completely fails and throws an exception
-!process_queue <-
    -busy;
    ?workload(W);
    if (W > 0) { -+workload(W - 1); }
    .print("A task execution critically failed. Unblocking queue...");
    !!process_queue.


+!perform_task(AuctionId, OrderId, Task, Attempts) 
    : task_workstation(Task, Station) & workstation(Station, X, Y) 
    <-  .print("Moving to ", Station, " at (", X, ", ", Y, ")");
        !go_to(X, Y);
        
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

// --- Movement ---
// Step-by-step recursive movement to prevent teleportation

+!go_to(TargetX, TargetY) : .my_name(Name) & at(Name, TargetX, TargetY) <- 
    true.

+!go_to(TargetX, TargetY) : .my_name(Name) & at(Name, CurrX, CurrY) & (CurrX \== TargetX | CurrY \== TargetY) <-
    step_towards(TargetX, TargetY); 
    !go_to(TargetX, TargetY).
