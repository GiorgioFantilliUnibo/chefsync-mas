---
layout: default
title: Implementation Details
nav_order: 6
---

# 5. Implementation Details & Tech Stack

Translating the conceptual Multi-Agent System into a concrete, reproducible software requires a robust technology stack and a strictly organized codebase. This section outlines the core implementation decisions, the specific technologies adopted, and the structural organization of the ChefSync repository.

## 5.1 Physical Layer Details

### Physical Model & State Management

At the core of the physical layer, the state of orders and tasks is managed by `KitchenModelImpl`. Because multiple agent threads and the Swing UI thread access and mutate this state concurrently, thread safety and structural clarity are enforced via:

* **Concurrent Data Structures:** The active orders are stored in a `ConcurrentHashMap<Integer, OrderRecord>` to prevent concurrency issues during concurrent reads and writes from the agent threads.
* **Data Transfer Objects (Records):** To clean up data representation, order and task details are modeled as Java records. Records provide a concise syntax for data carriers, facilitating structured updates and state transfers:
  ```java
  public record TaskRecord(String name, String assignedTo, boolean completed) {}
  public record OrderRecord(int id, String dish, String status, List<TaskRecord> tasks) {}
  ```
* **Functional-Style State Replacement:** State transitions (like assigning a task or marking it complete) are performed by atomically replacing the existing record in the map with a newly instantiated record using stream mapping:
  ```java
  @Override
  public void assignTask(int orderId, String task, String chef) {
      orders.computeIfPresent(orderId, (id, order) -> {
          List<TaskRecord> updated = order.tasks().stream()
              .map(t -> t.name().equals(task) ? new TaskRecord(t.name(), chef, t.completed()) : t)
              .collect(Collectors.toList());
          return new OrderRecord(id, order.dish(), order.status(), updated);
      });
      if (customView != null) customView.updateView();
  }
  ```

### Asynchronous Resource Execution (Cooking Timers)

Culinary execution requires physical time. Instead of blocking the agent's main execution thread during cooking (which would freeze the BDI reasoning loop), the environment delegates cooking tasks to background timers:

* **Timer Delegation:** When a Station Chef agent invokes the `start_cooking(Task, Time)` action, the `WorkstationImpl` schedules a `java.util.TimerTask`:
  ```java
  public synchronized void startCooking(String task, int timeMs, Runnable onComplete) {
      this.completedTask = null;
      timer.schedule(new TimerTask() {
          @Override
          public void run() {
              synchronized (WorkstationImpl.this) {
                  completedTask = task;
              }
              if (onComplete != null) onComplete.run();
          }
      }, timeMs);
  }
  ```
* **Decoupled Notification & Asynchronous Repainting:** Once the timer expires, the workstation updates its state and executes the `onComplete` callback. This callback does two things: it triggers the asynchronous repainting of the Swing GUI to keep the visual monitor updated, and it invokes `informAgsEnvironmentChanged()`. This signals the Jason interpreter to perform a new reasoning cycle and update the agent's percepts, adding the belief `cooked(Task)` so the agent can resume its plan.

## 5.2 Technology Stack

The project relies on a modern, standard toolchain designed to guarantee cross-platform execution, ease of integration, and reproducibility without relying on specific IDE configurations:

* **Java 21 (LTS):** Serves as the foundational programming language for the physical environment, the concurrent data structures, and the Swing GUI. The choice of Java 21 allows the use of modern language features while ensuring long-term support and high performance for the real-time asynchronous execution and thread-safe operations.
* **Jason 3.2.1:** The core BDI framework. It acts as the interpreter for the AgentSpeak(L) language. Rather than relying on legacy or unstable IDE plugins, Jason is integrated as a library dependency, allowing it to be seamlessly booted and managed programmatically.
* **Gradle:** Used as the build automation and dependency management tool. By providing a Gradle Wrapper (`gradlew`), the project guarantees full reproducibility. The MAS can be built, tested, and run from the command line without needing to manually configure Jason classpaths or install additional software.

## 5.3 Repository Structure

To enforce the Separation of Concerns (SoC) discussed in Chapter 2, the source code strictly separates the cognitive "Mind" from the physical "Body" using a footprint directory layout:

* **`src/main/asl/` (The Mind):** This directory encapsulates the cognitive layer. It contains all the AgentSpeak source files (`.asl`) that define the beliefs, plans, and reasoning cycles of the agents (`waiter.asl`, `head_chef.asl`, `station_chef.asl`).
* **`src/main/java/` (The Body):** This directory encapsulates the physical and infrastructural layers. It is further modularized following a Model-View-Controller (MVC) inspired pattern:
  * `env/`: Contains the `KitchenEnv.java` bridge that translates AgentSpeak actions into Java methods and generates percepts.
  * `model/`: Contains the core business logic of the simulation, including the `KitchenModel`, `Workstation` locks, and spatial tracking.
  * `view/`: Contains the Java Swing components (`KitchenView`) responsible for passively rendering the state of the model.
  * `utils/`: Contains custom internal actions and utility helper classes (such as `calculate_distance.java`) that extend the AgentSpeak language capabilities.
* **`src/test/`:** Contains the JUnit suites for environment and E2E MAS integration testing.

## 5.4 System Bootstrapping: `chefsync.mas2j`

The entry point of the Multi-Agent System is the `chefsync.mas2j` configuration file. This file acts as the primary orchestrator for the Jason framework during the boot phase. Its role is fundamental for establishing the system boundaries:

1. **Environment Binding:** It explicitly links the logical MAS to the Java physical layer by declaring the custom environment class (`environment: env.KitchenEnv`).
2. **Agent Instantiation:** It defines the exact composition of the kitchen brigade, specifying which `.asl` files to compile and how many instances of each agent to spawn (e.g., spawning exactly one `head_chef` but a dynamically configurable number of `station_chef` agents).
3. **Infrastructure Management:** It configures the internal Jason infrastructure (using the Centralised infrastructure) and manages the classpath mappings required to bridge the `.asl` files with the compiled Java classes.
