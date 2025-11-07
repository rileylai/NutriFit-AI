# NutriFit AI

## Introduction / Overview
- Designed as a full-stack fitness and nutrition platform combining a secure Spring Boot backend with a responsive React + TypeScript frontend.
- Provides a unified environment for workout tracking, nutrition management, and AI-driven coaching through seamless API integration.
- Demonstrates enterprise-grade orchestration and AI-assisted insights capable of scaling from individual users to high-volume deployments.

## Tech Stack
| Layer | Technology | Purpose |
| --- | --- | --- |
| Core Framework | Spring Boot 3, Java 17 | Production-grade REST services with actuator, validation, and modular starters |
| Data | PostgreSQL + Spring Data JPA + HikariCP | Reliable relational storage, optimized pooling, and schema-driven models |
| Security | Spring Security, JWT, BCrypt, Mailgun SMTP | Tokenized auth, strong password hashing, outbound email flows |
| AI & Analytics | Gemini API, custom inference services, iText PDF | AI calorie prediction, conversational insights, formalized PDF coaching packs |
| Tooling | Maven Wrapper, Lombok, JaCoCo | Reproducible builds, trimmed boilerplate, automated coverage reporting |
| Frontend | React + TypeScript + Vite + Tailwind CSS + Supabase | Responsive SPA interface for user interaction, AI visualization, and session management |

## Key Features
- **JWT-authenticated account lifecycle** covering registration, verification, login, refresh, and secure password resets through Mailgun-powered notifications.
- **Profile + body metrics module** capturing BMI/BMR calculations, paginated histories, and insight-ready telemetry.
- **Workout orchestration** with CRUD endpoints, structured metadata, filters, and AI-suggested burn estimates for fast planning.
- **Nutrition intelligence** that ingests meals, aggregates macros, and surfaces daily summaries for data-driven eating decisions.
- **Dashboard + reporting** pipelines that deliver JSON snapshots and branded PDF reports via iText for executive-ready sharing.
- **Operational readiness** through profile-based configuration, environment overrides, and cloud-friendly containerization defaults.

## AI Integration
- Integrates Gemini-backed services for calorie prediction, workout generation, and contextual nutrition guidance.
- Wraps AI prompts in a policy layer to sanitize inputs, enforce role-based scopes, and log inference metadata for traceability.
- Feeds inference results back into the metrics store, enabling adaptive planning based on user adherence trends.

## Architecture Highlights
- **Modular packages** (auth, profile, workout, nutrition, insights) keep domain logic isolated and easy to extend.
- **Layered service + repository pattern** encourages unit-testable business rules and consistent transaction handling.
- **Asynchronous mail + notification gateways** decouple outbound communication and protect the request thread pool.
- **Centralized security filters** enforce JWT validation, CORS policy, and least-privilege access across every endpoint.
- **Cloud-readiness**: stateless services, environment-driven secrets, and PostgreSQL compatibility with managed offerings like RDS.

## Testing & Code Quality
- Achieved 87% line coverage with JUnit 5, Mockito, and JaCoCo instrumentation.
- Segregated unit tests by layer (security, repository, controller) to accelerate CI feedback.
- Automated build verification through Maven Wrapper for reproducible test pipelines.
- Key commands:
  ```bash
  ./mvnw clean verify            # full build + unit tests + JaCoCo report
  ./mvnw jacoco:report           # regenerate HTML coverage dashboard
  ```

## Setup Instructions (concise)
Environment variables are managed via a `.env` file.  
Copy `.env.example` to `.env` and fill in your credentials before running locally or deploying.  
All sensitive keys (JWT, Mailgun, Gemini, database credentials) are loaded dynamically through environment variables.

1. **Prerequisites**: Install JDK 17+, Maven Wrapper, PostgreSQL 14+, and export credentials via environment variables (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `GEMINI_API_KEY`, `MAILGUN_API_KEY`).
2. **Configure profiles**: Update `src/main/resources/application-dev.yml` or supply overrides for datasource, JWT secrets, and Mailgun domains.
3. **Run locally**:
   ```bash
   ./mvnw spring-boot:run
   # or
   java -jar target/nutrifit-app-0.0.1-SNAPSHOT.jar
   ```
4. **Access**: API defaults to `http://localhost:8080` with the `dev` profile; set `SPRING_PROFILES_ACTIVE` for staging/production parity.

## Future Improvements
Future iterations aim to enhance scalability, intelligence, and real-time analytics across multiple deployment environments.
- Expand AI coaching with reinforcement signals from wearable integrations.
- Introduce multi-tenant org controls for enterprises running corporate wellness programs.
- Add WebSocket push channels for real-time dashboard updates and anomaly alerts.
- Harden observability via OpenTelemetry traces, SLIs, and auto-scaling policies for peak training seasons.

## Feature Preview

### 1. Dashboard & Insights

Frontend UI showcases demonstrate how the React SPA visualizes comprehensive health overviews with AI-generated recommendations, calorie tracking, and workout analytics sourced from Spring Boot services.
Displays progress summaries, daily goals, and personalized insights from Gemini-powered models.

<img src="docs/images/1_home_page_1.png" width="1000"/>
<img src="docs/images/1_home_page_2.png" width="1000"/>
<img src="docs/images/1_home_page_3.png" width="1000"/>

### 2. Profile Management

Frontend UI showcases enable intuitive updates to physical metrics while visualizing BMI/BMR trends through interactive charts.
Data synchronization ensures progress tracking across sessions and devices.

<img src="docs/images/2_profile.png" width="1000"/>

### 3. Workout Tracking

Frontend UI showcases support both manual and AI-assisted workout logging with calorie burn estimation backed by backend intelligence.
Summarizes history, streaks, and weekly frequency analytics for consistent training habits.

<img src="docs/images/3_workout.png" width="1000"/>

### 4. Nutrition & Meal Estimation

Frontend UI showcases process meals via text or image input to estimate calories and macros automatically using backend AI inference.
Provides daily summaries, macro distribution charts, and nutrient gap analytics for personalized diet optimization.

<img src="docs/images/4_meal.png" width="1000"/>
