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
mapper/          → DTO ↔ domain mapping (MapStruct)І
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
  "text": "This is so annoying"
}
```

**Response**

```json
{
  "speaker": "user",
  "temperature": -0.4488999999999999,
  "emotion": [
    "annoyance"
  ],
  "confidence": 0.8935558199882507
}
```

**Notes**

* Uses hybrid analysis (ONNX transformer + NRC lexicon emotions)
* Emotion lists are capped to top 3 entries

---

### 2. Analyze Dialogue

POST /api/v1/emotion/dialogue

**Request**

```json
{
  "messages": [
    {
      "speaker": "user",
      "text": "I am upset"
    },
    {
      "speaker": "bot",
      "text": "Let me help you"
    }
  ]
}
```

**Response**

```json
{
  "overallTemperature": -0.20801250000000002,
  "dominantDialogueEmotion": "caring",
  "participants": [
    {
      "speaker": "user",
      "temperature": -0.41602500000000003,
      "dominantEmotion": "negative",
      "emotionalTrend": "stable"
    },
    {
      "speaker": "bot",
      "temperature": 0,
      "dominantEmotion": "caring",
      "emotionalTrend": "stable"
    }
  ],
  "messages": [
    {
      "speaker": "user",
      "temperature": -0.41602500000000003,
      "emotion": [
        "negative",
        "sadness",
        "anger"
      ],
      "confidence": 0.5305424928665161
    },
    {
      "speaker": "bot",
      "temperature": 0,
      "emotion": [
        "caring"
      ],
      "confidence": 0.9614611268043518
    }
  ],
  "trajectory": {
    "startTemperature": -0.41602500000000003,
    "endTemperature": 0,
    "volatility": 0.41602500000000003,
    "trend": "positive"
  }
}
```

---

### 3. Analyze With Context

POST /api/v1/emotion/message/with-context

**Request**

```json
{
  "sessionId": "d84b1292-e142-446a-b47a-58b1df85ca7a",
  "speaker": "user",
  "text": "Still not working..."
}
```

**Behavior**

* If `sessionId` is missing, the server generates a UUID and returns it.
* Loads previous messages from cache (if present)
* Appends the new message
* Analyzes combined context
* Updates cache

**Response**

```json
{
  "sessionId": "d84b1292-e142-446a-b47a-58b1df85ca7a",
  "message": {
    "speaker": "user",
    "temperature": -0.33152499999999996,
    "emotion": [
      "disappointment",
      "neutral",
      "sadness"
    ],
    "confidence": 0.6389271020889282
  },
  "overallTemperature": -0.11050833333333332,
  "dominantDialogueEmotion": "neutral",
  "trajectory": {
    "startTemperature": 0,
    "endTemperature": -0.33152499999999996,
    "volatility": 0.16576249999999998,
    "trend": "negative"
  }
}
```

---

### 4. Get session history

GET /api/v1/emotion/message/with-context/session/{sessionId}

**Response**

```json
[
  {
    "speaker": "user",
    "text": "Hi",
    "timestamp": "2026-05-08T23:40:20.256260200Z"
  },
  {
    "speaker": "bot",
    "text": "Hello",
    "timestamp": "2026-05-08T23:40:43.781213Z"
  },
  {
    "speaker": "user",
    "text": "Still not working...",
    "timestamp": "2026-05-08T23:42:56.932662400Z"
  }
]
```
---

## TODO:
- Resolve issue with Jackson2JsonRedisSerializer being deprecated
- Make ONNX return top 3 emotions if dominant one has similar confidence or return 1 dominant emotion

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
record MessageAnalysisDto(
        String speaker,
        Double temperature,
        java.util.List<String> emotion,
        Double confidence
) {
}
```

### ParticipantAnalysisDto

```java
record ParticipantAnalysisDto(
        String speaker,
        Double temperature,
        String dominantEmotion,
        String emotionalTrend
) {
}
```

### TrajectoryDto

```java
record TrajectoryDto(
        Double startTemperature,
        Double endTemperature,
        Double volatility,
        String trend
) {
}
```

---

## Rules

* No client-side message IDs
* Same entity → same DTO
* Aggregated entity → separate DTO
* API is stateless (except context endpoint)
* Duplication tracking lives in `DUPLICATION_NOTES.md`

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

---

## Lexicon Analyzer

* Dictionary-based scoring (TBI)
* Returns a ranked list of emotions; the first entry is dominant

---

## ONNX Setup

Download `model.onnx` and `tokenizer.json` from:

```
SamLowe/roberta-base-go_emotions-onnx
```

Place the files here:

```
src/main/resources/model/
```

Model assets should be tracked with Git LFS so they are available on checkout without bloating Git history.

The ONNX inference engine is enabled by default.
