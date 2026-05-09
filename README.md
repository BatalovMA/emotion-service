# Emotion Analysis API

REST API for conversational emotion analysis using:
- ONNX transformer inference
- NRC Emotion Lexicon
- DepecheMood

Supports:
- single message analysis
- dialogue analysis
- contextual conversation memory
- emotional trajectory tracking

---

# Features

- Hybrid ML + lexicon emotion inference
- Real-time emotion temperature scoring
- Context-aware conversation analysis
- Redis-backed conversational memory
- Swagger/OpenAPI support
- ONNX runtime inference
- MapStruct-based DTO mappin

---

# Tech Stack

- Java 21
- Spring Boot
- Gradle
- ONNX Runtime
- Redis
- MapStruct
- Docker

---

# Swagger

Swagger UI:

```text
http://localhost:8080/api/v1/swagger-ui/index.html
```

OpenAPI docs:

```text
http://localhost:8080/api/v1/v3/api-docs
```

---

## API

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

* Uses hybrid analysis (ONNX transformer + NRC + DepecheMood lexicons)
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
    "text": "Hi"
  },
  {
    "speaker": "bot",
    "text": "Hello"
  },
  {
    "speaker": "user",
    "text": "Still not working..."
  }
]
```

---

# Emotion Temperature

Range:

```text
[-1.0, 1.0]
```

Interpretation:
- negative → negative emotional state
- positive → positive emotional state
- near zero → neutral/stable

---

# Hybrid Inference

The system combines:
- ONNX transformer inference
- NRC categorical emotions
- DepecheMood emotional intensity

Transformer inference remains the primary signal.

---

# Contextual Analysis

The contextual endpoint uses Redis-backed rolling memory:
- restores previous messages
- tracks emotional trajectory
- improves short-message interpretation
- enables emotionally continuous conversations

---

# Notes

- Emotion lists are capped to top 3 entries
- Short messages increase lexicon influence
- Context endpoint keeps rolling message windows
- Redis is used only for active conversational memory
- See AGENTS.md for more implementation-sided notes