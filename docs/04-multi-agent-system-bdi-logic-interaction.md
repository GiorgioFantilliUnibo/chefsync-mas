---
layout: default
title: Multi-Agent System
nav_order: 5
---

# 4. Multi-Agent System: BDI Logic & Interaction

The logical layer of ChefSync represents the cognitive "Mind" of the orchestrator. It is implemented using **Jason**, an interpreter for an extended version of the **AgentSpeak(L)** programming language. This architecture encapsulates control within autonomous entities that act proactively based on their internal mental states (Beliefs, Desires, and Intentions).

This section explores the specific roles assigned to the agents within the brigade, detailing their individual reasoning cycles and how they handle environmental unpredictability and execution failures.

## 4.1 Agent Roles & Reasoning

The system's intelligence is distributed across three specialized agent roles. Each role is designed with a specific set of initial beliefs and plans, allowing the emergence of complex, coordinated behavior.

### 4.1.1 The Waiter Agent: Asynchronous Demand Ingestion
In a professional kitchen, the arrival of orders is asynchronous and outside the control of the cooking brigade. The **Waiter Agent** models this unpredictable workload.
* **Behavior:** The Waiter is programmed to simulate dining table events by asynchronously generating orders (macro-goals) and delegating them to the kitchen staff.
* **Interaction:** It uses the FIPA ACL `achieve` performative to send a `prepare_order(Dish, Table)` request directly to the Head Chef. By using the `achieve` act (which requests the receiver to adopt a goal) rather than a simple `tell` (which just updates a belief), the Waiter delegates the entire goal of preparing the dish, abstracting the physical fulfillment details.

### 4.1.2 The Head Chef Agent: Coordination & Barrier Synchronization
The **Head Chef** acts as the central coordinator and planner of the brigade. It holds the cognitive responsibility of decomposing complex recipes and managing the order lifecycle.
* **Recipe Ontology:** The Head Chef's Belief Base is initialized with a menu ontology (`recipe(DishName, TaskList)`). When the Waiter delegates a goal, the addition event `+!prepare_order(Dish, Table)` triggers the main planning process.
* **Task Decomposition:** The Head Chef resolves the recipe to decompose the macro-goal (the dish) into a flat list of atomic, parallelizable sub-tasks (e.g., `grill_patty`, `toast_bun`, `assemble_burger`).
* **ContractNet Initiation:** The Head Chef initiates the ContractNet Protocol (CNP). It performs a directory facilitator search (`.df_search`) for agents registered with the `station_chef` role and issues a **multicast** Call for Proposal (CFP) to that specific group of candidates.
* **Workstation Claiming:** When a Station Chef is awarded a task, it issues a **broadcast** message (`claiming(OrderId, Station)`) to all other agents in the platform, allowing peer-to-peer cognitive coordination to avoid workstation conflicts before physical navigation starts.
* **Barrier Synchronization:** The Head Chef acts as a synchronization barrier. It maintains order completion state via the `pending_tasks(OrderId, Dish, Table, Remaining)` belief. Every completed task decrements this count. When the remaining task count reaches zero, the Head Chef triggers the `ring_bell(OrderId)` action in the environment to finalize the order.

### 4.1.3 The Station Chef Agents: Spatial Navigation & Failure Recovery
The **Station Chef Agents** represent the autonomous workforce. They evaluate task proposals, coordinate their movements physically, and recover from failures in a highly concurrent environment.
* **Negotiation (Bid Evaluation):** Upon receiving a `cfp(AuctionId, OrderId, Task)`, a Station Chef evaluates its local commitments (`workload` and `pending_bids`). If free, it computes a bid based on its Manhattan distance to the workstation required for that task, replying with a `propose(Bid)` FIPA ACL message.
* **Spatial Navigation:** Once awarded a task (`accept_proposal`), the agent executes a plan to reach the workstation coordinates. It issues sequential `step_towards` actions in the environment, which uses a localized greedy heuristic to step closer while avoiding collision with other agents.
* **Proximity Locking:** Upon reaching the destination, the agent must be adjacent to the workstation to execute the `lock(Station)` action. Once locked, it steps onto the station, executes `start_cooking(Task, Time)`, waits for the environment percept `cooked(Task)`, steps off, and unlocks the workstation.
* **BDI Fallback & Recovery:** If movement or lock acquisition fails (due to congestion, lock contention, or equipment breakdown), the BDI engine triggers a plan failure handler: `-!perform_task(AuctionId, OrderId, Task, Attempts)`. The agent increments its attempt counter and retries after a random delay. If the attempts exceed `max_attempts`, it aborts and notifies the Head Chef by sending a `task_failed(OrderId, Task)` message, triggering a re-auction of the task.

## 4.2 FIPA ContractNet Protocol (CNP) Implementation

The distribution of cooking tasks in ChefSync is dynamically negotiated using a FIPA-compliant ContractNet Protocol (CNP). This ensures that task allocation is not hardcoded, but emerges at runtime based on the state of the system.

### 4.2.1 The Negotiation Process

The protocol is divided into distinct phases involving the Head Chef (as the **Initiator**) and the Station Chefs (as the **Participants**):

1. **Call for Proposal (CFP):**
   The Head Chef launches an asynchronous intention (`!start_asynchronous_auction`) for a specific task. It queries the Directory Facilitator to find the active Station Chefs and sends a multicast `cfp(AuctionId, OrderId, Task)` message. The Head Chef then waits for a deterministic timeout (e.g., 1.5 seconds) to allow participants to reply.
   
2. **Propose or Refuse:**
   Station Chefs receive the CFP and evaluate it. Based on their local state, they reply either with a `propose(Bid)` containing a heuristic cost score, or a `refuse` message.
   
3. **Evaluation & Award:**
   Once the timeout expires, the Head Chef collects all proposals using `.findall`.
   * **Success Case:** If proposals are received, the Head Chef selects the optimal proposal using `.min`. It sends `accept_proposal` to the winner, updates the physical dashboard via `assign_task`, and sends `reject_proposal` to the remaining bidders.
   * **Failure/Retry Case:** If no proposals are received (e.g., all chefs are busy or workstations are contested), the Head Chef waits for a short period and spawns a retry auction.

### 4.2.2 Utility & Heuristic Evaluation Function

To decide whether to bid and how to calculate the cost, the Station Chefs evaluate the incoming CFP against their local cognitive and physical context:

#### 1. Cognitive Concurrency Gate
Before calculating a bid, the Station Chef validates its capacity. To prevent resource over-commitment, an agent will only bid if it is completely free:

```text
workload == 0  AND  pending_bids == 0
```

If the chef is currently cooking a task (`workload > 0`) or waiting for the outcome of another auction (`pending_bids > 0`), it replies with a `refuse`.

#### 2. Physical Contention Gate
The agent checks if the workstation required for the task is already occupied by another agent or has been claimed by a peer at the cognitive level:

```text
Occupied(Station)  OR  Claimed(Station)
```

If the workstation is unavailable, the chef refuses to prevent spatial conflicts and lock contention.

#### 3. Heuristic Utility Score
If both gates are passed, the chef computes a cost score based on spatial distance. Since moving through the grid takes time and steps, the utility of a task is inversely proportional to the distance:

```text
Manhattan_Distance = |X_chef - X_station| + |Y_chef - Y_station|
```

The calculated `Bid` represents the Manhattan distance (total grid step actions required to reach the destination). The Head Chef's minimization function (`min`) naturally awards the task to the closest available chef, minimizing movement latency and maximizing kitchen throughput.

To guarantee encapsulation and high software modularity, the calculation is delegated to a **custom Java Internal Action** (`utils.calculate_distance`). By decoupling this heuristic from the AgentSpeak plan logic, the distance calculation remains easily customizable for the future.
