# Emotion Analysis API

## Goal
Provide a REST API that analyzes dialog messages and returns emotion temperature.

---

## Project Structure

```text
api/             → HTTP layer (controllers, DTOs)
application/     → use cases (orchestration)
domain/          → core logic (models, rules)
infrastructure/  → external systems (ML, Redis, lexicons)
mapper/          → DTO ↔ domain mapping (MapStruct)
config/          → configuration
```
---

## Layer Responsibilities

### api/

* REST endpoints
* request/response DTOs

### application/

* Use cases (AnalyzeMessage, AnalyzeDialogue, etc.)
* Orchestrates flow

### domain/

* Business logic only
* No Spring, no DB, no ML implementations

### infrastructure/

* ML models
* Redis cache
* external integrations

### mapper/

* All mapping via MapStruct (mandatory)

---

## Request Flow

```text
Controller → UseCase → Domain → Infrastructure → Response
```

---

## API (v1)

### 1. Analyze Single Message

POST /api/v1/emotion/message

**Request**

```json
{
  "speaker": "user",
  "text": "This is so annoying",
  "timestamp": "2026-05-01T12:00:00Z"
}
```

**Response**

```json
{
  "analysis": {
    "speaker": "user",
    "temperature": -0.72,
    "sentiment": -0.8,
    "emotion": "anger",
    "intensity": 0.9,
    "confidence": 0.92
  }
}
```

---

### 2. Analyze Dialogue

POST /api/v1/emotion/dialogue

**Request**

```json
{
  "dialogId": "dialog-1",
  "messages": [
    { "speaker": "user", "text": "I am upset" },
    { "speaker": "bot", "text": "Let me help you" }
  ]
}
```

**Response**

```json
{
  "dialogId": "dialog-1",
  "overallTemperature": -0.3,
  "participants": [
    {
      "speaker": "user",
      "temperature": -0.6,
      "dominantEmotion": "sadness"
    }
  ],
  "messages": [
    {
      "speaker": "user",
      "temperature": -0.7,
      "sentiment": -0.65,
      "emotion": "sadness",
      "intensity": 0.8,
      "confidence": 0.91
    }
  ]
}
```

---

### 3. Analyze With Context

POST /api/v1/emotion/message/with-context

**Request**

```json
{
  "dialogId": "dialog-1",
  "messages": [
    { "speaker": "user", "text": "Still not working..." }
  ]
}
```

**Behavior**

* Load previous messages from cache
* Append new messages
* Analyze combined context
* Update cache

**Response**

```json
{
  "dialogId": "dialog-1",
  "overallTemperature": -0.45,
  "contextUsed": true,
  "windowSize": 12,
  "participants": [
    {
      "speaker": "user",
      "temperature": -0.7,
      "dominantEmotion": "frustration"
    }
  ],
  "messages": [
    {
      "speaker": "user",
      "temperature": -0.8,
      "sentiment": -0.75,
      "emotion": "anger",
      "intensity": 0.85,
      "confidence": 0.93
    }
  ]
}
```

---

## Core Concept

### Emotion Temperature

Range: [-1.0, 1.0]

```text
temperature = sentiment * intensity
```

---

## DTOs

### MessageAnalysisDto

```java
class MessageAnalysisDto {
    String speaker;
    Double temperature;
    Double sentiment;
    String emotion;
    Double intensity;
    Double confidence;
}
```

### ParticipantAnalysisDto

```java
class ParticipantAnalysisDto {
    String speaker;
    Double temperature;
    String dominantEmotion;
}
```

---

## Rules

* No client-side message IDs
* Same entity → same DTO
* Aggregated entity → separate DTO
* API is stateless (except context endpoint)

### Mapping

* ALL mapping must use MapStruct
* No manual mapping in services/controllers

### Architecture

* Do not mix domain and infrastructure
* ML must be accessed via interface
* No business logic in controllers

---

## Metric Usage

* `messages[].temperature` → real-time reactions
* `overallTemperature` → global behavior

---

## Future

* Replace cache with DB
* Improve ML model
* Add streaming / real-time updates

