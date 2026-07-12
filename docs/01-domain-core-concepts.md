---
layout: default
title: Domain & Core Concepts
nav_order: 2
---

# 1. Domain & Core Concepts

The domain of ChefSync is modeled around the operational dynamics of a professional restaurant kitchen, conceptualized as a highly concurrent, spatially constrained, and unpredictable environment. The kitchen brigade serves as an ideal metaphor for a complex socio-technical system. It requires distributed problem-solving, dynamic workload balancing, and coordination among autonomous entities to achieve a common goal: fulfilling customer orders efficiently.

Unlike a deterministic setup, the kitchen has to deal with constant, unpredictable changes. The agents need to be reactive enough to pick up new tasks as they arrive, but also proactive in navigating the kitchen, sharing workstations, and resolving bottlenecks on their own.

## 1.1 The Kitchen Brigade (The Agents)

Before delving into the technical modeling, it is essential to introduce the actors that populate this environment. The brigade is composed of autonomous agents, each with specific roles, responsibilities, and behaviors:

* **The Waiter (1 agent):** The entry point of the system. The Waiter acts as a simple interface with the external environment, injecting a predefined sequence of orders into the kitchen.
* **The Head Chef (1 agent):** The central coordinator of the kitchen. The Head Chef receives macro-orders from the Waiter, decomposes them into actionable atomic sub-preparations, and delegates these tasks to the available Station Chefs. It monitors the completion of these sub-preparations to assemble the final dish.
* **The Station Chefs (N agents):** The workforce of the kitchen. These agents perform the physical labor. They navigate the kitchen grid, acquire necessary physical resources (workstations like stoves or prep counters), execute the assigned sub-tasks, and report back to the Head Chef upon completion or failure.

## 1.2 Core Domain Entities

To abstract the physical kitchen into a computable Multi-Agent System (MAS), the domain is formalized around three primary entities:

### 1.2.1 Orders as BDI Goals
Orders represent requests for dishes injected into the system. From a MAS perspective, rather than passive environmental stimuli, orders are represented as high-level goals. They are initiated by the Waiter agent and communicated to the Head Chef agent via FIPA ACL messages. An order represents a macro-goal that needs to be satisfied, representing a complex workflow that must be orchestrated and synchronized to guarantee correct and timely service.

### 1.2.2 Recipes and Task Decomposition
In this domain, a recipe represents the declarative knowledge of how a macro-goal (a dish) is decomposed into atomic sub-goals (tasks).
* **Macro-goals:** Preparing a complete dish (e.g., a `smash_burger` or a `caprese_salad`) represents a complex goal.
* **Atomic Sub-tasks:** The macro-goal is decomposed by the Head Chef into a set of indivisible tasks (e.g., `grill_patty`, `toast_bun`, `assemble_burger`) that can be executed independently.
This task decomposition allows the system to distribute the execution load across the brigade, enabling Station Chefs to work in parallel on the components of the same order. The synchronization of these sub-tasks is handled via a barrier mechanism before the final dish is declared completed.

### 1.2.3 Workstations (Physical Resources and Constraints)
The physical layout of the kitchen introduces spatial and resource-based constraints into the simulation. Workstations (e.g., stoves, ovens, prep counters) are situated at specific coordinates within a discrete 2D grid. They introduce two fundamental engineering challenges:
1. **Spatial Navigation:** Agents must physically navigate the environment to reach a workstation before a task can be executed, requiring spatial reasoning and pathfinding capabilities.
2. **Mutual Exclusion:** Workstations are finite hardware resources. They cannot be shared simultaneously by multiple agents for different tasks. Consequently, they require strict concurrency control mechanisms to ensure exclusive access.

## 1.3 Mapping the Domain to MAS Characteristics

The modeling of the kitchen brigade as a Multi-Agent System maps directly to the key dimensions of agenthood and distributed coordination:

* **Autonomy:** Each agent (e.g., a Station Chef) maintains its own internal state (such as its active commitments) and determines its own actions. There is no centralized scheduler deciding pathfinding or lock acquisition; agents autonomously negotiate, navigate, and execute tasks.
* **Social Ability & Coordination:** The resolution of the global problem (fulfilling the stream of orders) emerges from social interaction. Agents coordinate dynamically using FIPA ACL communication and a structured negotiation protocol (ContractNet Protocol), matching tasks to the most suitable agents based on localized heuristics (e.g., spatial distance and current workload).
* **Reactivity and Proactivity:** The agents are goal-directed (proactive), constantly working towards completing their assigned tasks and coordinating movement. At the same time, they are reactive to a dynamic environment: they perceive workstation availability, handle pathfinding blockages, and respond to failures (such as task abortions) by triggering recovery behaviors.
* **Situatedness:** The agents are situated in a shared physical environment (the 2D grid). They do not just compute logical states, but must physically interact with spatial constraints, coordinate their movements to avoid collisions, and acquire mutually exclusive locks on workstations, coupling cognitive reasoning with physical limitations.

