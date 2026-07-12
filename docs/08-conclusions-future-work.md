---
layout: default
title: Conclusions & Future Work
nav_order: 9
---

# 8. Conclusions & Future Work

In conclusion, ChefSync successfully demonstrates how a multi-agent system can coordinate a busy restaurant kitchen. Using the BDI paradigm and the Jason framework, we modeled the kitchen staff as autonomous agents that can plan, react to orders, negotiate tasks via FIPA ContractNet, and handle execution failures dynamically.

The current implementation has some limits. Physical simulation calculations (such as coordinates and movement steps) are computed centrally in Java, which would slow down the simulation if we scaled up to a much larger kitchen. Additionally, agents must manage workstation locks and queue priorities cognitively by constantly exchanging beliefs and messages.

To improve this, a future development would be integrating the **Agents & Artifacts (A&A)** paradigm. Instead of custom Java models and direct communication for locking, workstations and order queues would be modeled as external "artifacts". Agents would interact with these artifacts indirectly (stigmergy) by reading their public properties and calling their operations (like `lock` or `cook`). This would simplify agent code, reduce communication overhead, and make the whole system much more modular and scalable.
