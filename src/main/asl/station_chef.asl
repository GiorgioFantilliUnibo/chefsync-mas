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


/* ContractNet Protocol - Participant Logic */

+cfp(Id, Task)[source(head_chef)] <-
    ?workload(W);
    .my_name(Name);
    ?at(Name, MyX, MyY);
    ?task_workstation(Task, Station);
    ?workstation(Station, StatX, StatY);
    Distance = math.abs(MyX - StatX) + math.abs(MyY - StatY);
    Bid = Distance + (W * 5);
    .print("Evaluating CFP ", Id, " for task: ", Task, ". Dist: ", Distance, ", WL: ", W, " -> Bid: ", Bid);
    .send(head_chef, tell, propose(Id, Bid)).

+accept_proposal(Id, Task)[source(head_chef)] <-
    .print("won contract ", Id, "! Executing task: ", Task, "...");
    
    ?workload(W);
    -+workload(W + 1);
    
    !perform_task(Task, 0);
    .print("completed task: ", Task);

    ?workload(CurrentW);
    -+workload(CurrentW - 1).


+!perform_task(Task, Attempts) 
    : task_workstation(Task, Station) & workstation(Station, X, Y) 
    <-  .print("moving to ", Station, " at (", X, ", ", Y, ")");
        move_towards(X, Y);
        
        .print("locking ", Station);
        lock(Station); 
        
        .print("executing ", Task, " on ", Station, "...");
        .wait(1500);
        
        .print("unlocking ", Station);
        unlock(Station);
        .print("completed task: ", Task).

-!perform_task(Task, Attempts) : Attempts < 3 <-
    .print("Lock failed for ", Task, ". Retrying in a bit... (Attempt ", Attempts + 1, ")");
    .wait(2000);
    !perform_task(Task, Attempts + 1).

-!perform_task(Task, Attempts) : Attempts >= 3 <-
    .print("Unable to complete ", Task, ". Station inaccessible. Notifying the Head Chef.");
    
    ?workload(CurrentW);
    -+workload(CurrentW - 1);
    
    .send(head_chef, tell, task_failed(Task)).
