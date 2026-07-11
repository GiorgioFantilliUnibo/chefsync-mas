---
layout: default
title: Testing & Validation
nav_order: 7
---

# 6. Testing, Validation & Reproducibility

Validating a decentralized system of autonomous agents presents unique challenges compared to standard procedural software, as the control flow is non-deterministic and emerges from asynchronous social interactions. 

ChefSync addresses this by combining isolated unit testing of the physical layer, automated End-to-End (E2E) integration testing of the Multi-Agent System (MAS), and real-time observability of spatial and social dynamics.

## 6.1 Unit Testing (Physical Layer Validation)

Before evaluating the cognitive coordination of the agents, the physical layer must be proven correct. Because the Java codebase enforces the rules of the simulation, the project includes isolated unit tests using JUnit to validate the model and environment controller in isolation.

### 6.1.1 Model State & Rules Verification (`KitchenModelTest`)
The spatial and resource constraints are validated in the `KitchenModelTest` suite. These tests ensure the simulation's integrity:
* **Spatial Constraints:** Verifies coordinate bounds, grid positioning, and that agents cannot occupy cells occupied by other agents.
* **Workstation Mutex Locks:** Validates that a workstation can only be locked by one agent at a time, and that locking is proximity-restricted (the locking agent must be adjacent).
* **Order & Task State Tracking:** Verifies that orders and their sub-tasks are correctly registered, assigned to chefs, and marked as completed, ensuring that the model updates and tracks state transitions properly.

### 6.1.2 Environment Bridge & RBAC Validation (`KitchenEnvTest`)
The gateway connecting the agents to the environment is validated in `KitchenEnvTest`. These tests focus on action parsing and security:
* **Role-Based Access Control (RBAC):** Verifies that only authorized agent roles can execute specific actions (e.g., verifying that a `station_chef` is blocked from executing `register_order`, while the `head_chef` is allowed).
* **Action Execution Translation:** Assures that AgentSpeak actions are correctly parsed, mapped to the correct model calls, and return the correct boolean status to the agent interpreter.

## 6.2 End-to-End (E2E) Integration Testing

To programmatically verify the emergent behavior of the brigade, the project includes an automated test suite built with JUnit 4. The core of this suite is the `ChefsyncEndToEndTest`, which evaluates the entire Multi-Agent System lifecycle without requiring human intervention.

### 6.2.1 Headless MAS Execution
Instead of launching the Jason MAS with its graphical Swing interface, the JUnit test utilizes the `RunLocalMAS` wrapper to boot the Jason infrastructure in **headless mode**. This allows the test suite to programmatically instantiate the `chefsync.mas2j` configuration within a Continuous Integration (CI) environment.

### 6.2.2 Synchronization Latch (`testLatch`)
To bridge the JUnit runner thread with the asynchronous Jason agent threads without consuming CPU cycles in polling loops, the environment includes a synchronization hook:
* A static `CountDownLatch` (`testLatch`) is initialized by JUnit before starting the MAS.
* When the Head Chef successfully completes the first order (invoking the `ring_bell` action), the environment decrements the latch (`testLatch.countDown()`), instantly waking up the JUnit thread.

### 6.2.3 E2E Integration Assertions
The test verifies the full, unbroken chain of events:
1. **Bootstrapping:** Checks that all agents (Waiter, Head Chef, Station Chefs) spawn and register.
2. **Social Interaction:** Verifies that the Waiter sends the order request, the Head Chef decomposes the recipe, and the Station Chefs dynamically negotiate via FIPA ACL.
3. **Physical Action Integration:** Verifies that chefs successfully navigate the grid, lock the workstations, cook, and notify completion.
4. **Safety & Liveness:** Confirms the order completes within the 45-second safety timeout, asserting that the system is free of deadlocks and logical stalls.

## 6.3 Visual and Console Validation

While automated tests ensure structural correctness, manual and visual validation remains essential for analyzing the complex social dynamics and spatial heuristics of the agents.

### 6.3.1 Social & Log Traceability

To monitor the cognitive coordination and internal states of the agents, the system provides two distinct debugging interfaces:

* **Jason MAS Console (GUI):** A Swing-based console window automatically spawned by the Jason framework at startup. It aggregates the stdout logs, logging outputs, and `.print` instructions from all active agents into a single centralized view. It also provides basic controls (such as pause, resume, and stop) to inspect the execution flow step-by-step.
* **Agent Mind Inspector (Web Interface):** Jason hosts a local web server (typically at port `3272`) running the Mind Inspector. This web interface allows evaluators to inspect the internal BDI state of any agent dynamically. It shows their active belief base (e.g., current position, held locks, claims), active desires (goals), and the exact execution stack of their current intentions.

### 6.3.2 Spatial Validation (The Swing GUI)
Simultaneously, the Swing GUI provides a passive window into the physical consequences of those cognitive decisions. It allows the user to visually validate:
* **Spatial Coordination:** Observing the Station Chefs physically moving step-by-step across the grid, confirming that they avoid static obstacles and dynamically choose the shortest path using the model's greedy Manhattan distance minimization heuristic.
* **Concurrency Safety:** Observing the color-coded workstation locks (green/red), ensuring that no two agents ever occupy or cook at the same critical hardware resource simultaneously.
* **Order Tracking Panel:** Monitoring the real-time JTable, validating that order states and task assignment progress update immediately in response to execution events.