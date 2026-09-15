# SAHAY-AI 🫂

### AI-Powered Dynamic Mental Health Monitoring & Distress Prediction System

SAHAY-AI is an AI-assisted wellbeing monitoring and early-warning platform designed to support victims during the long and stressful journey of investigation, trial, rehabilitation, and recovery.

The system continuously monitors wellbeing through periodic check-ins and AI-assisted conversations, identifies changes in distress levels, and provides early warnings to authorized human support teams.

> **Reactive Support → Proactive Wellbeing Monitoring**

---

## 🎯 Problem Statement

Victims involved in atrocity-related cases may experience prolonged emotional distress, fear, anxiety, stress, and safety concerns throughout the legal process.

Existing support mechanisms primarily focus on legal assistance, compensation, protection, relief, and rehabilitation.

However, there is a gap in:

- Continuous wellbeing monitoring
- Early identification of increasing distress
- Tracking psychological wellbeing over time
- Prioritizing cases that may require human attention

SAHAY-AI addresses this gap by introducing an **AI-assisted early-warning layer**.

---

## 💡 Our Solution

SAHAY-AI provides a continuous monitoring loop:

```text
Victim
   ↓
Periodic Check-in / AI Chat
   ↓
AI Language Analysis
   ↓
Structured Distress Indicators
   ↓
Deterministic Scoring Engine
   ↓
Dynamic Distress Score (0–100)
   ↓
Trend & Escalation Detection
   ↓
Human Review Alert
   ↓
Appropriate Support

The system is designed to assist human decision-making rather than replace it.
```

🧠 How the AI Works

SAHAY-AI does not directly ask an LLM to determine whether a victim is "high risk."

Instead, the AI extracts structured signals from natural-language responses.

AI extracts indicators such as:
Emotional distress
Fear / anxiety / stress
Safety or threat indicators
Engagement / response behavior
Changes from previous check-ins

These indicators are passed to a deterministic scoring engine.

Distress Score

The system generates a Dynamic Distress Score from 0 to 100.

Indicator	Weight
Emotional Distress	40%
Fear / Anxiety / Stress	25%
Safety Indicators	15%
Engagement	10%
Change from Previous Check-ins	10%
Risk Levels
Score	Risk Level
0–24	Low
25–49	Moderate
50–74	High
75–100	Critical

The score is non-diagnostic and is intended only as an early-warning indicator.

📈 Dynamic Trend Monitoring

A single check-in may not represent a person's overall wellbeing.

Therefore, SAHAY-AI stores historical check-ins and distress scores.

For example:

42 → 48 → 61 → 74

The system can identify that distress is increasing over time.

This enables:

Historical trend visualization
Rapid deterioration detection
Increasing distress identification
Better prioritization of cases

If there is insufficient historical data, the system does not fabricate a score or trend.

🚨 Early Warning System

When predefined conditions are detected, SAHAY-AI can generate an alert for authorized human review.

Examples include:

Critical distress score
Rapid increase in distress
Strong safety-related indicators
Significant deterioration across check-ins

The system does not automatically diagnose the victim or make legal decisions.
```text
Instead:

AI Detection
     ↓
Early Warning
     ↓
Human Review
     ↓
Appropriate Support
```

📞 Automated Follow-up

To reduce dependence on users continuously opening the application, SAHAY-AI can support an automated voice check-in workflow.

If a victim does not complete a check-in for a defined period, such as two days:
```text
No Check-in
     ↓
Backend Inactivity Detection
     ↓
Automated Voice Call
     ↓
AI Voice Conversation
     ↓
Speech-to-Text
     ↓
Existing AI Analysis Pipeline
     ↓
Distress Assessment
     ↓
Human Review if Required
```

This voice-intervention module can be integrated using telephony platforms such as Exotel or Twilio.

👥 Scalable Case Monitoring

SAHAY-AI is not intended to make officers manually monitor every victim.

For a large number of cases, the system prioritizes cases based on:

Current distress level
Distress trend
Safety indicators
Recent deterioration
Follow-up status

The authority dashboard therefore works as a priority-based work queue.

Example:

CRITICAL
Case A     86 ↑
Case B     81 ↑

HIGH
Case C     72 ↑
Case D     68 →

MODERATE
Case E     43 →

This allows authorized personnel to focus attention where it is most needed.
```text
🏗️ System Architecture
┌──────────────────────────┐
│      Android App         │
│ Kotlin + Jetpack Compose │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│     Application Layer    │
│ ViewModel + Repository   │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│        Supabase          │
│ Auth + PostgreSQL + RLS  │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│    Supabase Edge         │
│       Functions          │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│       Groq API           │
│      Llama 3.1 8B        │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│ Structured AI Indicators │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│ Deterministic Scoring    │
│        Engine            │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│   Distress Score +       │
│   Trend + Alerts         │
└──────────────────────────┘
```
🛠️ Technology Stack
Mobile Application
Kotlin
Jetpack Compose
Android SDK
MVVM Architecture
Backend
Supabase
PostgreSQL
Supabase Authentication
Supabase Edge Functions
Row Level Security (RLS)
Artificial Intelligence
Llama 3.1 8B Instant
Groq API
Structured AI output
Deterministic weighted scoring engine
Networking
Retrofit
REST APIs
Visualization
Jetpack Compose Canvas
Historical distress trend graphs
Planned Voice Module
Exotel / Twilio
Speech-to-Text
AI Voice Agent

🔐 Security & Privacy

SAHAY-AI handles highly sensitive victim information, so security is a core architectural requirement.

Key principles include:

Server-side API key protection
Authentication
Role-Based Access Control
PostgreSQL Row Level Security
Restricted access to case information
Secure backend communication
Minimal exposure of sensitive information
Human review for consequential decisions

AI-generated analysis is treated as an assistive signal, not as an authoritative medical or legal decision.

🌐 Multilingual Support

SAHAY-AI supports communication in:

English
Tamil

The AI analysis pipeline is designed to understand the user's preferred language while maintaining a structured internal representation for scoring.

This allows victims to communicate naturally without requiring them to interact only in English.

📱 Key Features
Victim Application
Secure login
Periodic wellbeing check-ins
AI-assisted conversation
Multilingual interaction
Dynamic distress score
Risk-level indicator
Distress trend graph
Score explanation
Case status
Support workflow
Authority / Support Dashboard
Case overview
Priority-based cases
Distress score
Trend indicators
Early-warning alerts
Reason for alert
Follow-up status
Role-based access

🔄 Example User Journey
```text
Victim is onboarded
        ↓
Completes wellbeing check-in
        ↓
AI analyzes response
        ↓
Distress indicators extracted
        ↓
Scoring engine calculates score
        ↓
Score stored in database
        ↓
Trend updated
        ↓
System checks escalation conditions
        ↓
No concern → Continue monitoring
```
⚖️ Human-in-the-Loop Design

SAHAY-AI follows a human-in-the-loop architecture.

The AI:

Analyzes language
Extracts indicators
Calculates an explainable score
Identifies potential concerns
Prioritizes cases

The AI does not:

Diagnose mental health conditions
Make legal decisions
Decide punishment
Automatically take police action
Replace counselors or authorized personnel

The final intervention decision remains with authorized human personnel.

🎯 Why SAHAY-AI?

Traditional systems often provide support when a problem has already become visible.

SAHAY-AI adds a continuous early-warning layer.

Traditional Approach
```text
Problem occurs
      ↓
Victim reports
      ↓
Support is initiated
SAHAY-AI
Continuous Check-ins
      ↓
AI Monitoring
      ↓
Distress Trend
      ↓
Early Warning
      ↓
Human Support
```

This shifts the approach from:

Reactive Support → Proactive Wellbeing Monitoring

🚀 Future Scope

Potential future extensions include:

Automated AI voice check-ins
Additional Indian language support
Improved longitudinal prediction models
Advanced anomaly detection
Integration with authorized case-management systems
Offline-first check-in support
Explainable AI dashboards
Federated/privacy-preserving learning
Improved accessibility for users without smartphones
⚠️ Responsible AI

SAHAY-AI is designed as an early-warning and support-coordination system, not as a medical diagnostic platform.

The system should always:

Treat AI output as probabilistic.
Use deterministic rules for consequential scoring.
Provide explainable indicators.
Keep humans involved in intervention decisions.
Protect sensitive victim information.
Avoid unnecessary data collection.
```text
📂 Project Structure
SAHAY-AI/
│
├── android/
│   ├── ui/
│   ├── viewmodel/
│   ├── repository/
│   ├── model/
│   └── localization/
│
├── supabase/
│   ├── functions/
│   │   └── chat-analysis/
│   └── migrations/
│
├── docs/
│   ├── architecture/
│   └── screenshots/
│
└── README.md
```
🧪 Project Status
Current Prototype
 Android application
 Kotlin + Jetpack Compose
 AI chat interface
 English/Tamil interaction
 Supabase backend
 Check-in persistence
 Dynamic distress scoring
 Historical distress tracking
 Distress trend visualization
 Score explanation
 Early-warning architecture
 Role-based access concept
Planned
 Automated voice check-ins
 Production telephony integration
 Advanced prediction models
 Production deployment
 Integration with authorized government workflows
 
🏆 Core Innovation

SAHAY-AI combines:

LLM-based Language Understanding

Explainable Deterministic Distress Scoring

Longitudinal Trend Monitoring

Automated Early Warning

Human-in-the-Loop Support

to create a proactive wellbeing monitoring system.

👨‍💻 Project

SAHAY-AI

AI-Powered Dynamic Mental Health Monitoring & Distress Prediction System

From Reactive Support to Proactive Wellbeing Monitoring.


One thing I'd strongly recommend: **don't put “mental health prediction” everywhere in the README as if the AI diagnoses people.** Your strongest and safest technical positioning is **“AI-assisted wellbeing monitoring and distress early warning.”** That also makes your architecture much easier to defend when judges start asking difficult questions.
