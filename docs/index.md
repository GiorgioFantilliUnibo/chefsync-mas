---
layout: default
title: Intro
nav_order: 1
---

# ChefSync: A Multi-Agent Kitchen Orchestrator

**Author:** Giorgio Fantilli  
**Course:** Intelligent Systems Engineering  
**Institution:** Alma Mater Studiorum – Università di Bologna (Cesena Campus)  
**Academic Year:** 2025/2026  


## Abstract

ChefSync is a simulated, grid-based orchestrator designed to manage highly concurrent culinary environments through decentralized multi-agent coordination. The project addresses the complex challenges of modern restaurant kitchens, specifically focusing on dynamic workload balancing, spatial pathfinding, and the resolution of physical resource conflicts. To achieve this, the system enforces a strict architectural dichotomy. The physical layer, implemented in Java, provides a discrete-time 2D spatial simulator that manages environmental state, hardware constraints (workstations), and mutually exclusive resource locks. The logical layer encapsulates the system's intelligence utilizing BDI (Belief-Desire-Intention) agents programmed in Jason and AgentSpeak(L). These autonomous agents—acting as Waiters, Head Chefs, and Station Chefs—communicate via the FIPA Agent Communication Language (ACL). By leveraging the ContractNet Protocol (CNP) for task negotiation, the agents evaluate real-time spatial heuristics and operational availability to effectively orchestrate the decomposition, assignment, and execution of unpredictable order streams.


## Table of Contents
1. [Domain & Core Concepts](01-domain-core-concepts.md)
2. [System Architecture & Design](02-system-architecture-design.md)
3. [The Environment](03-the-environment.md)
4. [Multi-Agent System: BDI Logic & Interaction](04-multi-agent-system-bdi-logic-interaction.md)
5. [Implementation Details & Tech Stack](05-implementation-details-tech-stack.md)
6. [Testing, Validation & Reproducibility](06-testing-validation-reproducibility.md)
7. [Deployment & Execution](07-deployment-execution.md)
8. [Conclusions & Future Work](08-conclusions-future-work.md)
