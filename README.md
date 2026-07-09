# ChefSync - Multi-Agent Kitchen Orchestrator

**ChefSync** is a simulated, grid-based orchestrator for culinary environments, developed as a project for the **Intelligent Systems Engineering** course (University of Bologna).

It focuses on distributed task delegation, spatial coordination, and dynamic workload balancing among autonomous entities. The project models a highly concurrent restaurant kitchen where unpredictable orders are fulfilled by a coordinated brigade of BDI agents.

## Technologies

* **Jason / AgentSpeak(L)** for BDI agent models and reasoning.
* **Java 21** for the discrete-time spatial simulator and physical grid constraints.
* **FIPA ContractNet Protocol (CNP)** for distributed task orchestration.
* **Gradle** for build automation.
* **JUnit 4** for End-to-End integration testing.

---

## Quick Start

### Requirements
* **Java 21** installed on your machine.
* *No local Gradle installation is required (the repository includes the Gradle Wrapper).*

### Clone and Run
1. Clone the repository:
   ```bash
   git clone [https://github.com/TuoUsername/chefsync.git](https://github.com/TuoUsername/chefsync.git)
   cd chefsync
   ```

2. Start the Multi-Agent System (this will open the Jason MAS Console and the Grid GUI):
   ```bash
   # On Linux / macOS
   ./gradlew runChefsyncMas

   # On Windows
   .\gradlew.bat runChefsyncMas
   ```

---

## System Architecture

The intelligence of the system is distributed across three distinct roles communicating via FIPA ACL:

* **Waiter Agent (`waiter.asl`)**: Acts as the environmental stimulus. Asynchronously generates and injects complex, multi-item orders into the kitchen.
* **Head Chef Agent (`head_chef.asl`)**: The central planner. Evaluates incoming orders against its internal recipe knowledge base, decomposes them into atomic sub-tasks, and initiates a **ContractNet Protocol (CNP)** to assign tasks based on the brigade's current workload and distance.
* **Station Chef Agents (`station_chef.asl`)**: Autonomous BDI workers. They evaluate CFPs based on their spatial distance to the required workstation, handle A* grid pathfinding, acquire physical resource locks, execute cooking tasks, and manage failure-recovery plans.

---

## Testing

The repository includes an End-to-End (E2E) integration test that runs a headless execution of the MAS to verify the full lifecycle (boot -> order injection -> CNP negotiation -> cooking -> completion).

To run the automated tests:
```bash
./gradlew test
```

---

## Project Structure

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
```