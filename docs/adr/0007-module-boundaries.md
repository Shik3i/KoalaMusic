# ADR 0007: Module boundaries

Status: Accepted — 2026-07-30

Use coherent core platform/data modules and workflow-sized features. The model
module stays Android-free. Features depend inward and never on each other.
Convention plugins centralize Android configuration. Avoid per-entity modules,
single-implementation ceremony, and a generic provider hierarchy.
