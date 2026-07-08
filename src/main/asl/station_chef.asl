/* Initial beliefs and rules */

workload(0).
pending_bids(0).
max_attempts(3).

// Task to workstation mappings with prep time
task_workstation(grill_patty, grill, 7000).
task_workstation(toast_bun, grill, 3000).
task_workstation(assemble_burger, prep_counter, 1500).
task_workstation(heat_piadina, oven, 200).
task_workstation(add_squacquerone, prep_counter, 1000).
task_workstation(add_prosciutto, prep_counter, 1000).

task_workstation(slice_tomatoes, salad_bar, 700).
task_workstation(slice_mozzarella, salad_bar, 700).
task_workstation(plate_salad, prep_counter, 1000).

task_workstation(bread_calamari, prep_counter, 1500).
task_workstation(fry_calamari, fryer, 3000).
task_workstation(plate_calamari, prep_counter, 1000).

task_workstation(prepare_coffee, stove, 2000).
task_workstation(layer_mascarpone, dessert_station, 1500).
task_workstation(dust_cocoa, dessert_station, 500).

task_workstation(boil_pasta, stove, 4000).
task_workstation(fry_guanciale, stove, 2500).
task_workstation(mix_egg_cheese, prep_counter, 1500).


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
    ?task_workstation(Task, Station, _);
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
    ?task_workstation(Task, Station, _);
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
    
    ?task_workstation(Task, Station, _);
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
    : task_workstation(Task, Station, Time) & workstation(Station, X, Y) 
    <-  
        .print("Moving adjacent to ", Station, " at (", X, ", ", Y, ")");
        !go_to_adjacent(X, Y);
        
        .print("Locking ", Station);
        lock(Station); 
        
        .print("Stepping onto ", Station);
        !go_to(X, Y);
        
        .print("Executing ", Task, " on ", Station, " for ", Time, "ms...");
        start_cooking(Task, Time);
        
        .wait(cooked(Task));
        
        .print("Stepping off ", Station);
        !step_off;
        
        .print("Unlocking ", Station);
        unlock(Station);
        
        .send(head_chef, tell, task_completed(OrderId, Task)).

// --- Fallback & Error Handling ---
// BDI failure recovery: retries on lock failures before giving up completely

-!perform_task(AuctionId, OrderId, Task, Attempts) : max_attempts(Max) & Attempts < Max <-
    .print("Action failed for ", Task, " (Order ", OrderId, "). Congestion or locked? Retrying... (Attempt ", Attempts + 1, ")");
    .wait(1000 + math.random(1000));
    !perform_task(AuctionId, OrderId, Task, Attempts + 1).

-!perform_task(AuctionId, OrderId, Task, Attempts) : max_attempts(Max) & Attempts >= Max <-
    .print("Max attempts reached for ", Task, " (Order ", OrderId, "). Aborting task.");
    .send(head_chef, tell, task_failed(OrderId, Task)).

// --- Movement ---

// Goal achieved: you are adjacent to TargetX, TargetY (distance <= 1) and NOT on it
+!go_to_adjacent(TargetX, TargetY) 
    : .my_name(Name) & at(Name, MyX, MyY) 
    & math.abs(MyX - TargetX) <= 1 & math.abs(MyY - TargetY) <= 1 
    & (MyX \== TargetX | MyY \== TargetY) 
    <- true.

// Recursive step towards adjacent
+!go_to_adjacent(TargetX, TargetY) <-
    step_towards(TargetX, TargetY);
    .wait(300);
    !go_to_adjacent(TargetX, TargetY).

// Obstacle handling in route
-!go_to_adjacent(TargetX, TargetY) <-
    .print("Path blocked! Waiting for traffic to clear...");
    .wait(300 + math.random(500));
    !go_to_adjacent(TargetX, TargetY).

// Exact movement to the center of the workstation (allowed if lock owned)
+!go_to(TargetX, TargetY) : .my_name(Name) & at(Name, TargetX, TargetY) <- 
    true.

+!go_to(TargetX, TargetY) <-
    step_towards(TargetX, TargetY);
    .wait(300);
    !go_to(TargetX, TargetY).

-!go_to(TargetX, TargetY) <-
    .wait(300 + math.random(500));
    !go_to(TargetX, TargetY).

// Stepping off
+!step_off <-
    step_off. 

-!step_off <- 
    .wait(500);
    !step_off.
