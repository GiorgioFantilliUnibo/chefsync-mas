# ChefSync

**ChefSync** is a simulated, grid-based orchestrator for culinary environments, focusing on distributed task delegation, spatial coordination, and dynamic workload balancing among autonomous entities. 

Developed for the Intelligent Systems Engineering course (University of Bologna - Cesena), the project models a highly concurrent restaurant kitchen where a stream of unpredictable orders must be fulfilled by a coordinated brigade of agents.

## System Architecture

The project enforces a strict architectural split between the physical simulation (Java) and the distributed intelligence/reasoning layer (Jason/AgentSpeak).

### 1. The Environment (Java)
* **Discrete-time Spatial Simulator:** Manages the physical 2D grid, coordinates hardware locks (mutual exclusion for shared equipment like Ovens or Grills), and handles simulation ticks.
* **Visual Monitor & Logger:** Built-in grid views to observe spatial coordination and agent movements in real-time, coupled with console tracing for FIPA ACL negotiations.

### 2. The Mind (Jason/AgentSpeak)
* **Logical Knowledge Base:** Culinary dependencies (e.g., preparing a smash burger requires grilling the patty, toasting the bun, and assembling) are internalized as BDI beliefs and rules, rather than external Java data structures.
* **Distributed Orchestration:** Agents communicate strictly via **FIPA ACL** messages to negotiate tasks and update statuses.



## Agent Roles

The system intelligence is distributed across three distinct BDI roles:

* **Waiter Agent (Order Ingestion):** Acts as the environmental stimulus. Asynchronously generates and injects complex, multi-item orders into the system, simulating real-world unpredictable demand.
* **Head Chef Agent (Task Orchestration):** Functions as the central planner. Evaluates incoming orders against its internal knowledge base, decomposes them into atomic sub-tasks, and initiates a **ContractNet Protocol (CNP)**. It acts as the *Initiator*, assigning tasks based on the brigade's current load and the received proposals.
* **Station Chef Agents (Execution & Spatial Reasoning):** Autonomous BDI workers acting as CNP *Participants*. They evaluate CFPs based on their availability and spatial distance to the required workstation. Once a task is assigned, they handle pathfinding in the Java grid, acquire physical resources, execute the cooking task, and employ failure-recovery plans (e.g., dynamically dropping an intention and notifying the Head Chef if a workstation breaks or is blocked).



## Repository Structure

```text
chefsync/
├── chefsync.mas2j                # Main Multi-Agent System configuration
├── src/
│   ├── main/
│   │   ├── asl/                  # BDI Logic (AgentSpeak)
│   │   │   ├── waiter.asl
│   │   │   ├── head_chef.asl
│   │   │   └── station_chef.asl
│   │   └── java/                 # Simulator & Infrastructure
│   │       ├── env/              # 2D Grid, Lock mechanisms, Environment extensions
│   │       ├── model/            # Order structures, Grid coordinate models
│   │       └── view/             # Java Swing Visual Monitor
│   └── test/
│       └── java/                 # JUnit Integration and Unit Tests