# GlamourCuts

[![Android CI](https://github.com/st10445279/GlamourCuts/actions/workflows/android.yml/badge.svg)](https://github.com/st10445279/GlamourCuts/actions/workflows/android.yml)

An Android app for [one line: Booking and managing salon appointments].

## Walkthrough Video

https://www.youtube.com/watch?v=dH6Mx49g2mU

## Overview
GlamourCutsApp is an Android app for booking and managing salon appointments. Clients can register, browse services such as precision cuts, colour highlights, keratin and scalp treatments and bridal packages, book appointments, track their bookings, chat with the salon, and leave reviews and feedback. Salon admins and staff get tools to approve new users, manage staff and inventory, communicate with clients, and view analytics. It solves the problem of salons handling bookings, stock and client communication by phone and paper, by putting everything in one app that supports English, Afrikaans, Spanish, French and Zulu.

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

| TIYASE NTESENG | Technical Lead / QA |
|--------|------|
| MABELEBELE ALICIA |  |
