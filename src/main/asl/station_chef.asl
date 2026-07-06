/* Initial beliefs and rules */


/* Initial goals */

!report_duty.


/* Plans */

+!report_duty <-
    .my_name(Name);
    .print("Ready at station: ", Name).
