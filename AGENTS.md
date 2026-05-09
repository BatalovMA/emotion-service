## Architecture

```text
Controller → UseCase → Domain → Infrastructure → Response
```

---

## Project Structure

```text
api/             → controllers + DTOs
application/     → use cases
domain/          → business logic + models
infrastructure/  → ML, Redis, lexicons
mapper/          → MapStruct mappers
config/          → application configuration
```

---

## Rules
- ALL mapping must use MapStruct
- No manual mapping in controllers/services
- No business logic in controllers
- ML must be accessed via interfaces
- Keep domain independent from infrastructure
- DTOs should use Java records
- Same entity → same DTO
- Aggregated entity → separate DTO
- Context endpoint is the only stateful endpoint
- Update README.md when public API changes
- Keep AGENTS.md implementation-focused

---

## Emotion Pipeline

Primary inference:
```text
ONNX transformer
```

Secondary signals:
```text
NRC + DepecheMood
```

---

## Fusion Weights

Default:

```text
transformer = 0.85
nrc = 0.05
depecheMood = 0.10
```

Short messages:

```text
transformer = 0.70
lexicons = 0.30
```

---

## Lexicon Rules

- Use weighted emotion maps internally
- Filter weak emotions
- Return top 3 emotions maximum
- Apply negation handling
- Transformer remains dominant

---

## Redis Rules

Redis stores:
- active dialogue context only

Redis does NOT store:
- analytics history
- reports
- permanent records

Use rolling context windows:
```text
20-50 messages
```

---

## Context Endpoint Flow

```text
load context
    ↓
append new message
    ↓
trim context
    ↓
analyze dialogue
    ↓
save updated context
```

---

## Emotion Temperature

Range: [-1.0, 1.0]

Formula:

```
temperature = sentiment * intensity
```
