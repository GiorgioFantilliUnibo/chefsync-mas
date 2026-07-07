/* Initial beliefs and rules */


/* Initial goals */


/* Plans */

+ready_for_orders[source(head_chef)] <-
    .print("Head Chef gave the green light. I'm starting to take orders at the tables.");
    !send_orders.

+!send_orders <-
    .print("Sending first orders to the kitchen...");
    .send(head_chef, achieve, prepare_order(smash_burger, 1));
    .send(head_chef, achieve, prepare_order(romagnola_piadina, 2));
    .send(head_chef, achieve, prepare_order(caprese_salad, 3));
    .send(head_chef, achieve, prepare_order(fried_calamari, 4));
    .send(head_chef, achieve, prepare_order(tiramisu, 5));
    .send(head_chef, achieve, prepare_order(spaghetti_carbonara, 6)).
    