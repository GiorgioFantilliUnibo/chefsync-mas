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
    .df_register("station_chef");
    .print("Ready at station: ", Name).


/* ContractNet Protocol - Participant Logic */

+cfp(Id, Task)[source(head_chef)] <-
    ?workload(W);
    .my_name(Name);
    .print("[", Name, "] Evaluating CFP ", Id, " for task: ", Task, ". My workload is ", W);
    .send(head_chef, tell, propose(Id, W)).

+accept_proposal(Id, Task)[source(head_chef)] <-
    .my_name(Name);
    .print("[", Name, "] won contract ", Id, "! Executing task: ", Task, "...");
    
    ?workload(W);
    -+workload(W + 1);
    
    !perform_task(Task);
    .print("[", Name, "] completed task: ", Task);

    ?workload(CurrentW);
    -+workload(CurrentW - 1).


+!perform_task(Task)[source(head_chef)] 
    : task_workstation(Task, Station) & workstation(Station, X, Y) 
    <-  .my_name(Name);
        .print("[", Name, "] received task: ", Task, " -> Starting preparation on ", Station, "...");
        
        .print("[", Name, "] moving to ", Station, " at (", X, ", ", Y, ")");
        move_towards(X, Y);
        
        .print("[", Name, "] locking ", Station);
        lock(Station);
        
        .print("[", Name, "] executing ", Task, " on ", Station, "...");
        .wait(1500);
        
        .print("[", Name, "] unlocking ", Station);
        unlock(Station);
        
        .print("[", Name, "] completed task: ", Task).

+!perform_task(Task) <-
    .my_name(Name);
    .print("[", Name, "] Error: unable to perform task: ", Task).
