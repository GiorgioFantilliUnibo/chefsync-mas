/* Initial beliefs and rules */


/* Initial goals */


/* Plans */

+ready_for_orders[source(head_chef)] <-
    .print("Head Chef gave the green light. I'm starting to take orders at the tables.");
    !send_orders.

+!send_orders <-
    .print("Sending first orders to the kitchen...");
    .send(head_chef, achieve, prepare_order(smash_burger, 1));
    .send(head_chef, achieve, prepare_order(romagnola_piadina, 2)).