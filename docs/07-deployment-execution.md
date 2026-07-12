---
layout: default
title: Deployment & Execution
nav_order: 8
---

# 7. Deployment & Execution

This chapter provides step-by-step, reproducible instructions to build, execute, and validate the ChefSync Multi-Agent System on different operating systems.

## 7.1 Prerequisites

To run the simulation and compile the codebase, the host environment must satisfy the following minimal requirements:

* **Java Development Kit (JDK):** Version **21** or higher must be installed and properly configured in the system's path.

*Note: No local installation of Gradle is required, as the project includes the Gradle Wrapper (`gradlew` / `gradlew.bat`).*

## 7.2 Clone the Repository

Clone the project from the version control system and navigate into the root directory:

```bash
git clone https://github.com/GiorgioFantilliUnibo/chefsync-mas.git
cd chefsync-mas
```

## 7.3 Executing the Multi-Agent System

To launch the Multi-Agent System (which boots the Swing visual monitor, the Jason MAS Console, and spawns the Waiter, Head Chef, and Station Chef agents), execute the custom Gradle task:

### On Linux / macOS:
```bash
./gradlew runChefsyncMas
```

### On Windows:
```bash
.\gradlew.bat runChefsyncMas
```

### Expected Output & Interfaces
Once the boot phase completes, the following components will launch automatically:
1. **The Visual Monitor (Java Swing GUI):** Renders the 2D grid depicting workstations and chefs moving in real-time, side-by-side with the *Order & Task Management* dashboard JTable tracking order states.
2. **The Jason MAS Console:** Spawns a Java Swing window showing aggregated real-time agent output prints and coordination logs, with buttons to pause, step, or stop execution.

## 7.4 Running Automated Tests

To execute the entire test suite—including the physical layer unit tests (`KitchenModelTest` and `KitchenEnvTest`) and the headless integration test (`ChefsyncEndToEndTest`):

### On Linux / macOS:
```bash
./gradlew test
```

### On Windows:
```bash
.\gradlew.bat test
```
