# Smart Medicine Management and Reminder System

## Overview
A production-grade full-stack mobile application for managing medicines, inventories, and reminders.

## Architecture
- **Frontend**: React Native (TypeScript)
- **Backend**: Spring Boot (Java 17+)
- **Database**: 
  - MySQL (Inventory, Auth, Groups)
  - MongoDB (Medicines, Reminders)

## Setup
### Prerequisites
- Docker & Docker Compose
- Java 17+
- Node.js 18+
- Android Studio / Xcode

### Running Infrastructure
```bash
docker-compose up -d
```

### Backend
1. Copy `.env.example` to `.env` if it doesn't exist.
2. Ensure Docker infrastructure is running.
3. Run with environment variables loaded:
```powershell
./scripts/load-env.ps1
```

Or manually:
```bash
# Set variables in your terminal, then:
cd backend
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run android # or ios
```
