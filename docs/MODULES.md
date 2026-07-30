# Modules

```text
app
├─ feature:{setup,home,library,search,player,settings}
├─ core:{designsystem,ui,playback,data}
core:data -> core:{database,opensubsonic,security,model,common}
core:playback -> core:{database,opensubsonic,security,model,common}
core:{database,opensubsonic} -> core:{model,common}
feature:* -> core:{model,ui,designsystem}; selected features -> core:data/playback
```

- `common`: error and redaction primitives.
- `model`: Android-free immutable domain types.
- `opensubsonic`: request construction, auth, parsing, capability negotiation.
- `database`: Room schema, DAO, transactions.
- `data`: profile, sync, and library repositories.
- `security`: Android Keystore credential vault.
- `playback`: service, authenticated data source, queue persistence/controller.
- `designsystem`: color, typography, reusable components.
- `ui`: common presentation models and formatting.
- `testing`: fixtures and test utilities only.

Feature modules are workflow-sized, not entity-sized. Dependency cycles and
direct feature-to-feature dependencies are forbidden.
