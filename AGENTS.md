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
  "text": "This is so annoying"
}
```

**Response**

```json
{
  "analysis": {
    "speaker": "user",
    "temperature": -0.72,
    "emotion": [
      "anger",
      "negative"
    ],
    "confidence": 0.6
  }
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
  "overallTemperature": -0.3,
  "dominantDialogueEmotion": "sadness",
  "participants": [
    {
      "speaker": "user",
      "temperature": -0.6,
      "dominantEmotion": "sadness",
      "emotionalTrend": "escalating"
    }
  ],
  "messages": [
    {
      "speaker": "user",
      "temperature": -0.7,
      "emotion": [
        "sadness"
      ],
      "confidence": 0.91
    }
  ],
  "trajectory": {
    "startTemperature": -0.15,
    "endTemperature": -0.72,
    "volatility": 0.44,
    "trend": "negative"
  }
}
```

---

### 3. Analyze With Context

POST /api/v1/emotion/message/with-context

**Request**

```json
{
  "sessionId": "0c6190b4-4d8d-4d4f-9af0-5089372c03a8",
  "speaker": "user",
  "text": "Still not working..."
}
```

**Behavior**

* If `sessionId` is missing, the server generates a UUID and returns it
* Load previous messages from cache
* Append new message
* Analyze using combined context
* Update cache

**Response**

```json
{
  "sessionId": "0c6190b4-4d8d-4d4f-9af0-5089372c03a8",
  "message": {
    "speaker": "user",
    "temperature": -0.8,
    "emotion": [
      "anger",
      "negative"
    ],
    "confidence": 0.93
  },
  "overallTemperature": -0.45,
  "dominantDialogueEmotion": "frustration",
  "trajectory": {
    "startTemperature": -0.15,
    "endTemperature": -0.72,
    "volatility": 0.44,
    "trend": "negative"
  }
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
* Returns a ranked list of emotions; the first entry is dominant

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
* DTOs should be Java records (keep DTOs as records)
* Update README.md and AGENTS.md if related code is changed
* All DTO <-> domain mapping MUST use MapStruct
* Duplication tracking lives in `DUPLICATION_NOTES.md`

---

## Metric Usage

* `messages[].temperature` → real-time reactions
* `overallTemperature` → global behavior

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
