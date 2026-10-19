# GlamourCuts

[![Android CI](https://github.com/st10445279/GlamourCuts/actions/workflows/android.yml/badge.svg)](https://github.com/st10445279/GlamourCuts/actions/workflows/android.yml)

An Android app for [one line: Booking and managing salon appointments].

## Walkthrough Video

https://www.youtube.com/watch?v=dH6Mx49g2mU

## Overview

GlamourCuts lets clients [what clients do] and gives salon admins and staff tools to [what admins do]. It solves [problem].

## Features

- User registration and login, with admin approval of new users
- Browse services and book appointments
- View and manage your bookings, with a confirmation screen
- Reviews and feedback
- In-app messaging and communication
- Inventory management for staff and admins
- Staff management
- Analytics dashboard
- Help and support articles
- Settings and notifications
- Local data storage with Room, plus Firebase

## Tech Stack

- Kotlin, Android, Gradle Kotlin DSL
- Room database (KSP)
- Firebase (Google Services, Crashlytics)
- GitHub Actions for automated builds

## Project Structure

```
GlamourCuts/
├── app/
│   └── src/
│       ├── main/java/com/example/glamourcuts/   Activities, adapters, data models, DAOs
│       ├── main/res/                             Layouts, drawables, strings
│       ├── test/                                 Unit tests
│       └── androidTest/                          Instrumented tests
├── gradle/                                       Gradle wrapper
├── .github/workflows/android.yml                 CI build
└── README.md
```

## Getting Started

1. Clone the repo.
2. Open it in Android Studio and sync Gradle.
3. Run on an emulator or device.

Command-line build: `gradlew.bat assembleDebug`

## Team

| TIYASE NTESENG | Role |
|--------|------|
| [Name] | [Role] |