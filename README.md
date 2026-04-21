# InsightInvest

InsightInvest is a stock analysis platform with two parts:

- `backend-java`: Spring Boot REST backend for forecasting, sentiment analysis, risk scoring, reports, authentication, and portfolio APIs.
- `investchat`: Next.js frontend that acts as the UI for analysis and chat-style interaction.

## How To Run

### Prerequisites

- Java 17
- Maven
- Node.js 20+ and npm
- PostgreSQL
- Optional: a Groq API key for AI-powered sentiment and report generation

### 1) Start the backend

1. Open a terminal in `backend-java`.
2. Configure your environment variables if needed:
   - `DB_URL`
   - `DB_USERNAME`
   - `DB_PASSWORD`
   - `JWT_SECRET`
   - `JWT_EXPIRATION`
   - `GROQ_API_KEY`
   - `GROQ_MODEL`
3. Run the backend:

```bash
mvn spring-boot:run
```

The backend starts on port `8000` by default.

### 2) Start the frontend

1. Open a second terminal in `investchat`.
2. Install dependencies:

```bash
npm install
```

3. Start the development server:

```bash
npm run dev
```

The frontend runs on the default Next.js port, usually `3000`.

## Project Structure

- `backend-java/src/main/java/com/insightinvest/controller` - REST controllers
- `backend-java/src/main/java/com/insightinvest/service` - business logic
- `backend-java/src/main/java/com/insightinvest/repository` - database access layer
- `backend-java/src/main/java/com/insightinvest/entity` - JPA entities
- `backend-java/src/main/java/com/insightinvest/dto` - request/response DTOs
- `backend-java/src/main/java/com/insightinvest/factory` - object creation helpers
- `investchat/app` - Next.js pages and API routes
- `investchat/components` - reusable UI components

## OOAD Design Summary

The table below shows where the main design principles and patterns are used, how they appear in the code, and why they matter.

| Category | Principle / Pattern | Where it is used | How it is used | Why it matters |
| --- | --- | --- | --- | --- |
| SOLID | Single Responsibility Principle | `AnalysisFacadeService`, `ForecastService`, `NewsService`, `RiskAssessmentService`, `ReportGenerationService`, `ForecastResponseFactory` | `AnalysisFacadeService` only orchestrates the end-to-end flow; `ForecastService` only forecasts; `NewsService` only fetches news; `RiskAssessmentService` only computes risk; `ReportGenerationService` only saves reports; `ForecastResponseFactory` only builds the response object. | Each class has one focused reason to change, so the code is easier to maintain and explain in a presentation. |
| GRASP | High Cohesion | `ForecastService`, `NewsService`, `AIAnalysisService`, `RiskAssessmentService`, `ReportGenerationService`, `UserService`, `PortfolioService`, `WatchlistService` | Each service class groups closely related logic instead of mixing unrelated responsibilities. | This keeps each class easy to understand, test, and reuse. |
| GRASP | Low Coupling | Controllers depend on services; services depend on repositories and helpers | `CompanyController` calls `AnalysisFacadeService` instead of directly calling every lower-level class. Services use repository interfaces and helper services instead of hardcoding everything in one place. | Changes stay localized and the system becomes easier to extend without breaking other layers. |
| MVC | Controller Layer | `CompanyController`, `PortfolioController`, `UserController`, `WatchlistController`, `ReportController`, `ScreenerController`, `AdminController` | Controllers receive HTTP requests, validate inputs, call services, and return responses. | This is the request-handling layer of MVC. |
| MVC | Model Layer | `entity` classes and `dto` classes | Entities such as `User`, `Portfolio`, `Holding`, and `Watchlist` represent stored data. DTOs such as `ForecastResponse`, `AuthResponse`, and `ForecastResult` carry structured data to and from the API. | It separates domain data from HTTP handling and response formatting. |
| MVC | View Layer | `investchat` frontend | The Next.js UI displays the results returned by the backend and acts as the user-facing view. | It keeps presentation separate from backend business logic. |
| Creational Pattern | Factory Pattern | `ForecastResponseFactory` | This class builds the final `ForecastResponse` object in one place instead of constructing it directly inside the controller or facade. | Object creation is centralized, making the response assembly cleaner and easier to change. |
| Architectural Style | Layered Architecture | Backend folders: `controller`, `service`, `repository`, `entity`, `dto`, `factory` | The backend is organized in layers, with each layer talking to the next one. | This gives a clear structure for the project and maps well to OOAD concepts. |
| Security Design | Filter / Chain of Responsibility style | `JwtAuthenticationFilter`, `SecurityConfig` | Incoming requests pass through a security filter before reaching protected endpoints. | Authentication is handled in one dedicated place rather than scattered across controllers. |

## Quick Presentation Script

If you need to explain the project in a viva or presentation, you can say:

- "This project follows MVC in the backend and uses the Next.js frontend as the view layer."
- "We demonstrate SOLID through Single Responsibility, especially by splitting orchestration, forecasting, reporting, and response-building into separate classes."
- "We demonstrate GRASP through high cohesion and low coupling because each service handles one focused task and controllers depend only on service abstractions."
- "We use the Factory pattern through `ForecastResponseFactory` to build the final API response."

## Main API

- `GET /forecast/{symbol}` - returns the full analysis report
- Query parameters:
  - `steps` - forecast horizon, default `10`
  - `period` - historical period, default `6mo`
  - `newsItems` - number of news items, default `15`

## Notes

- The backend runs on `http://localhost:8000`.
- The frontend runs on `http://localhost:3000`.
- Make sure PostgreSQL is running before starting the backend.
- If you want AI-powered sentiment and reports, set `GROQ_API_KEY`; otherwise the backend falls back to heuristic logic.
