# DevTrack — AI-Powered Project Management Platform

DevTrack is a full-stack project and issue management platform designed to help developers organize projects, manage development tasks, collaborate with team members, and use AI assistance throughout the development workflow.

It combines a Java Spring Boot backend with a Next.js frontend and integrates Google Gemini to provide AI-powered issue analysis, project analysis, and development task breakdowns.

## Live Application

- **Frontend:** https://devtrack-frontend-i5ze.onrender.com
- **Backend:** https://devtrack-lk4y.onrender.com
- **Backend Health:** https://devtrack-lk4y.onrender.com/api/health

## Features

### 1. Authentication and Account Management

- Email and password registration and login.
- Email verification using OTP.
- OTP resend functionality with a cooldown period and expiry.
- JWT-based authentication.
- Google OAuth 2.0 login.
- GitHub OAuth 2.0 login.
- Forgot-password and password-reset workflows.
- Change password and update profile.

### 2. Project Management

- Create, view, update, and delete projects.
- Store project descriptions and primary programming languages.
- Categorize projects.
- Associate GitHub repository URLs with projects.
- Track multiple technologies used by each project.
- Categorize technologies as languages, frameworks, databases, tools, and others.

### 3. Issue and Task Management

- Create and update project issues.
- Track issue status:
  - OPEN
  - IN_PROGRESS
  - CLOSED
- Set issue priority:
  - LOW
  - MEDIUM
  - HIGH
- Categorize issues using labels.
- Assign issues to team members.
- Filter issues by status and priority.
- Retrieve issues using pagination.

### 4. Team Collaboration

- Add registered users to project teams.
- View project members.
- Remove team members.
- Track project membership and roles.
- Enforce project membership checks for protected operations.

### 5. Gemini AI Integration

DevTrack integrates Google Gemini through the Java Spring Boot backend.

AI capabilities include:

- **AI Issue Analysis:** Analyzes issue descriptions and provides technical insights, suggested priority, complexity, possible solutions, and next steps.
- **AI Project Analysis:** Examines project details, technologies, and issues to provide an overview, identify risks, and suggest improvements.
- **AI Task Breakdown:** Generates structured development tasks from a project or a user-provided goal.
- **Project-Aware AI:** Supplies relevant project information and issue context to Gemini when generating project-specific recommendations.

AI-generated tasks are suggestions; they are not automatically created as project issues.

The Gemini API key is stored in the backend environment and is not exposed to the frontend.

### 6. User Interface

- Responsive dashboard interface.
- Separate sections for projects, issues, technologies, team, profile, and settings.
- Dark and light theme support.
- Reusable React components.
- Loading and error states for API operations.

## Technology Stack

### Backend

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL
- H2 for test configuration
- JWT authentication
- OAuth 2.0
- Maven
- Google Gemini API

### Frontend

- Next.js 16
- React 19
- TypeScript
- Tailwind CSS
- Axios
- next-themes
- Lucide React
- Sonner

### Deployment and Services

- Render — backend and frontend hosting
- Render PostgreSQL — relational database
- Google Gemini — AI integration
- Google OAuth — authentication
- GitHub OAuth — authentication
- Email services — verification and password-reset workflows

## Architecture

DevTrack follows a frontend-backend architecture.

```text
                User
                 |
                 v
       Next.js / React Frontend
                 |
           Axios REST Calls
                 |
                 v
        Spring Boot REST API
                 |
       +---------+----------+
       |         |          |
       v         v          v
  Spring       JWT /     Gemini AI
  Security     OAuth      Service
       |         |          |
       +---------+----------+
                 |
                 v
          Spring Data JPA
                 |
                 v
            PostgreSQL
```

The frontend communicates with the backend through REST APIs. Spring Security protects authenticated endpoints, while Spring Data JPA manages persistence. AI requests are handled by the backend, which communicates with Google Gemini.

## Project Structure

The repository contains separate backend and frontend applications.

```text
DevTrack/
|
└── Project/
    |
    ├── DevTrack-Backend/
    |   |
    |   └── Backend/
    |       ├── src/
    |       │   ├── main/
    |       │   │   ├── java/
    |       │   │   │   └── in/strikes/devTrack/
    |       │   │   │       ├── config/
    |       │   │   │       ├── controller/
    |       │   │   │       ├── dto/
    |       │   │   │       ├── entity/
    |       │   │   │       ├── exception/
    |       │   │   │       ├── repository/
    |       │   │   │       ├── security/
    |       │   │   │       └── service/
    |       │   │   └── resources/
    |       │   └── test/
    |       └── pom.xml
    |
    └── DevTrack-Frontend/
        |
        └── Frontend/
            ├── app/
            ├── components/
            │   └── devtrack/
            ├── lib/
            │   └── api/
            ├── public/
            └── package.json
```

## Local Setup

### Prerequisites

Install the following:

- Java 21
- Maven
- Node.js
- pnpm
- PostgreSQL
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/lalithamba-mailavarapu/DevTrack.git
cd DevTrack
```

### 2. Backend Configuration

Navigate to the backend directory:

```bash
cd Project/DevTrack-Backend/Backend
```

Create a PostgreSQL database for DevTrack.

Configure the required environment variables:

| Variable | Purpose |
|---|---|
| DB_URL | PostgreSQL JDBC connection URL |
| DB_USERNAME | Database username |
| DB_PASSWORD | Database password |
| JWT_SECRET | Secret used to sign JWTs |
| FRONTEND_URL | Frontend application URL |
| RESEND_API_KEY | Resend API credential |
| RESEND_FROM_EMAIL | Verified sender email |
| EMAILJS_SERVICE_ID | EmailJS service identifier |
| EMAILJS_TEMPLATE_ID | EmailJS template identifier |
| EMAILJS_PUBLIC_KEY | EmailJS public key |
| EMAILJS_PRIVATE_KEY | EmailJS private key |
| GOOGLE_CLIENT_ID | Google OAuth client ID |
| GOOGLE_CLIENT_SECRET | Google OAuth client secret |
| GITHUB_CLIENT_ID | GitHub OAuth client ID |
| GITHUB_CLIENT_SECRET | GitHub OAuth client secret |
| GEMINI_API_KEY | Google Gemini API key |

Configure the Gemini model in `application.properties` using the `gemini.model` property.

Do not commit actual credentials or API keys to GitHub.

### 3. Run the Backend

Using the Maven wrapper:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend runs on port 8080 by default.

Check the health endpoint:

```text
http://localhost:8080/api/health
```

### 4. Frontend Configuration

Navigate to the frontend directory:

```bash
cd Project/DevTrack-Frontend/Frontend
```

Create a `.env.local` file:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

Open:

```text
http://localhost:3000
```

## Main REST API Endpoints

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register an account |
| POST | `/api/auth/verify-email` | Verify email using OTP |
| POST | `/api/auth/resend-otp` | Resend verification OTP |
| POST | `/api/auth/login` | Authenticate user |
| GET | `/api/auth/me` | Get current user |
| PUT | `/api/auth/me` | Update profile |
| POST | `/api/auth/change-password` | Change password |
| POST | `/api/auth/forgot-password` | Request password reset |
| POST | `/api/auth/reset-password` | Reset password |

### Projects

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/projects` | Get user's projects |
| POST | `/api/projects` | Create project |
| PUT | `/api/projects/{projectId}` | Update project |
| DELETE | `/api/projects/{projectId}` | Delete project |

### Issues

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/projects/{projectId}/issues` | List project issues |
| POST | `/api/projects/{projectId}/issues` | Create issue |
| GET | `/api/projects/{projectId}/issues/filter` | Filter issues |
| PATCH | `/api/projects/{projectId}/issues/{issueId}` | Update issue |
| PATCH | `/api/projects/{projectId}/issues/{issueId}/assignee` | Assign issue |

Issue listing supports pagination through `page` and `size` query parameters. Filtering supports status and priority.

### Team Management

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/projects/{projectId}/members` | List project members |
| POST | `/api/projects/{projectId}/members` | Add a project member |
| DELETE | `/api/projects/{projectId}/members/{userId}` | Remove a member |

### AI Assistance

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/ai/analyze-issue` | Analyze an issue |
| POST | `/api/ai/breakdown-task` | Break down a development goal |
| POST | `/api/ai/projects/{projectId}/analyze` | Analyze a project |
| POST | `/api/ai/projects/{projectId}/breakdown-tasks` | Generate project development tasks |

AI endpoints require authentication and use the configured Gemini API key on the backend.

## Security

- Passwords are hashed using BCrypt.
- JWT-based authentication protects application APIs.
- Google and GitHub OAuth 2.0 provide alternative sign-in methods.
- Email verification is required during normal registration.
- Project membership is checked before allowing protected project operations.
- Backend secrets are supplied through environment variables.
- Gemini credentials remain on the backend.

## Deployment

The application is deployed using Render.

- Frontend: Next.js application hosted on Render.
- Backend: Spring Boot service hosted on Render.
- Database: Render PostgreSQL.

Production environment variables must be configured in the respective Render services. OAuth callback URLs and frontend/backend origins must match the production configuration.

The free Render instance may experience a cold start after inactivity, so the first request can take longer.

Gemini may also temporarily return HTTP 503 during periods of high demand. This is an upstream service availability issue rather than necessarily an application defect.

## Future Improvements

- Automatic retry handling for temporary Gemini API failures.
- Improved cold-start experience and backend availability.
- Automated end-to-end authentication and AI workflow tests.
- Safe linking of existing accounts with Google and GitHub identities.
- Further performance optimization for project and issue queries.
- More AI-assisted development workflows.

## Author

**DevTrack — Full-Stack Project Management Platform**

Built using Java, Spring Boot, PostgreSQL, Next.js, TypeScript, and Google Gemini.
