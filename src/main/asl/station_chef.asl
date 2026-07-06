/* Initial beliefs and rules */

workload(0).


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
    
    .wait(1500);
    .print("[", Name, "] completed task: ", Task);

    ?workload(CurrentW);
    -+workload(CurrentW - 1).


+!perform_task(Task)[source(head_chef)] <-
    .my_name(Name);
    .print("[", Name, "] received task: ", Task, " -> Starting preparation...");
    
    // TODO: Spatial reasoning (move to workstation, lock resource, execute)
    
    .print("[", Name, "] completed task: ", Task).
