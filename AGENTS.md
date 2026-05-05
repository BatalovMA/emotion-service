# AGENTS.md — Emotion Analysis API

## Goal
Provide a REST API that analyzes dialog messages and returns emotion temperature.

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
* Analyze using combined context
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

## Core Concepts

### Emotion Temperature

Range: [-1.0, 1.0]

Formula:

```
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

## Architecture

```
Controller → UseCase → Inference → Lexicon → Aggregation
```

---

## Components

### Inference Engine

* Interface-based
* ML model (primary signal)

### Lexicon Analyzer

* Dictionary-based scoring (TBI)

### Aggregation Service

* Per-message temperature
* Per-speaker aggregation
* Overall temperature

### Context Service

* Stores recent messages (Redis)
* Sliding window (10–30 messages)

---

## Rules

* No client-side message IDs
* Server may hash messages internally
* Same entity → same DTO (messages reuse MessageAnalysisDto)
* Aggregated entity → separate DTO (participants)
* Keep API stateless (except context endpoint)
* Separate domain from DTOs
* Do not couple business logic to ML implementation
* Update README.md and AGENTS.md if related  code is changed
* All DTO ↔ domain mapping MUST use MapStruct

---

## Metric Usage

* `messages[].temperature` → real-time reactions
* `overallTemperature` → global behavior
