<div align="center">

# RendaFlex

### Financial decision support designed for variable-income realities

[![Status](https://img.shields.io/badge/status-MVP%20Functional%20%2F%20In%20Development-F59E0B?style=flat-square)](#project-status)
[![Current Development](https://img.shields.io/badge/development-Solo%20Continuation-315B8C?style=flat-square)](#project-history)

**Originally created during the Programa ONE Hackathon · Oracle Next Education**

</div>

> [!IMPORTANT]
> RendaFlex originally started as a collaborative project developed by a five-person team during the **Programa ONE Hackathon**, part of **Oracle Next Education**.
>
> After the team completed and presented the original MVP, **Paulo Emilio created this personal repository and continued the project independently**.
>
> The original Hackathon work remains credited to the full team. All post-MVP evolution in this repository represents the project's new phase as an independently maintained and continuously evolving product.

---

## Live Prototype

[Open the RendaFlex frontend](https://rendaflex.vercel.app/)

> The application already has a functional MVP and an integrated architecture, but the project is still under active development. Some interface, infrastructure, deployment, and product improvements may therefore evolve continuously.

---

## About

**RendaFlex** is a financial decision-support application designed for people whose income changes from month to month.

Traditional financial tools are often structured around a predictable salary. RendaFlex approaches the problem differently: instead of treating monthly income as fixed, it analyzes recent income behavior, financial commitments, transactions, and spending patterns to help users understand their current financial scenario before making new decisions.

The project currently provides:

- financial-profile analysis;
- variable-income history analysis;
- financial indicators;
- monthly commitment and available-margin calculations;
- transaction classification using Machine Learning;
- spending summaries by category;
- personalized financial recommendations;
- expense simulation;
- comparison between current and projected financial scenarios;
- responsive frontend with light and dark themes;
- an integrated Java + Python backend architecture.

RendaFlex already has a **functional MVP**, but this repository represents an ongoing evolution of the product.

---

# Project History

## Phase 1 — ONE Hackathon

RendaFlex was originally created during the **Programa ONE Hackathon**, part of **Oracle Next Education**, in collaboration with **Alura**.

The first version of the project was developed collaboratively by:

- **Deane Carvalho** — Data
- **Junior Ribeiro** — Data
- **Millena Belo** — Backend
- **Paulo Emilio** — Full Stack
- **Raul Oliveira** — Backend

The team worked together to define the problem, architecture, Machine Learning models, backend, frontend, infrastructure, integrations, and the first functional MVP.

The original team repository is available at:

**https://github.com/No-Country-simulation/RendaFlex__G9-BR-Team05**

The contribution areas above describe the team's general areas of work during the Hackathon and are not intended as formal organizational titles.

---

## Phase 2 — Independent Continuation

After the original MVP was completed and presented during the Hackathon, **Paulo Emilio decided to continue developing RendaFlex independently**.

This repository:

**https://github.com/pauloemilio-tech/rendaFlex**

was created from the original Hackathon codebase to serve as the new development home for the project.

From this point forward, RendaFlex became an independently maintained project, with continued work focused on:

- frontend redesign;
- UI/UX improvements;
- responsive behavior;
- clearer financial visualization;
- new financial metrics;
- improved expense simulation;
- backend evolution;
- API contract improvements;
- infrastructure and deployment refinement;
- product positioning;
- code quality;
- testing;
- future product capabilities.

The goal is not to erase or replace the collaborative origin of the project, but to clearly separate:

```text
Original Hackathon MVP
        ↓
Collaborative team development
        ↓
MVP presentation
        ↓
Personal repository created
        ↓
Independent post-MVP evolution
        ↓
Current RendaFlex

Project Status
RendaFlex currently has a functional MVP, but the project is still actively being developed.
Functional today
- [x] Financial-analysis flow
- [x] Variable-income history input
- [x] Financial-profile prediction
- [x] Transaction classification
- [x] Category summaries
- [x] Financial recommendations
- [x] Expense simulation
- [x] Current-versus-projected scenario comparison
- [x] Spring Boot REST API
- [x] Python/FastAPI Machine Learning service
- [x] React + TypeScript frontend
- [x] Dockerized backend services
- [x] OCI infrastructure
- [x] Vercel frontend deployment
- [x] Light and dark themes
- [x] Responsive interface
- [x] Automated backend and ML tests
- [x] End-to-end integration between frontend, Java backend, and Python service
Currently evolving
- [ ] Continued UI/UX refinement
- [ ] Financial-analysis form redesign
- [ ] Expense-simulation form redesign
- [ ] Wider and more adaptive result-page layouts
- [ ] Further mobile and tablet refinement
- [ ] Production deployment synchronization
- [ ] README and portfolio documentation improvements
- [ ] Additional observability and infrastructure improvements
- [ ] Persistent authenticated user history
- [ ] Continuous financial tracking
- [ ] Future post-MVP product features
Product Concept
RendaFlex is built around a simple idea:
A person's financial planning should adapt to the rhythm of their income instead of assuming that every month looks the same.

The current product flow is:
Recent income history
        ↓
Monthly reference
        ↓
Monthly commitments
        ↓
Available margin
        ↓
Financial profile
        ↓
Transaction context
        ↓
Recommendations
        ↓
New expense simulation
        ↓
Current scenario vs projected scenario

Financial Analysis
The analysis form collects information such as:
- three to six months of recent income;
- saving frequency;
- recent transactions;
- monthly debt payments;
- other fixed monthly expenses.
The application validates the information before sending the analysis request to the backend.
The financial-analysis flow is:
FinancialAnalysisPage
        ↓
financialAnalysisService
        ↓
financialAnalysisApi
        ↓
Spring Boot API
        ↓
Python / FastAPI
        ↓
Machine Learning models
        ↓
Financial analysis result

Financial Metrics
The application currently works with several financial indicators.
Among them:
- average income;
- income variation coefficient;
- debt ratio;
- fixed commitment percentage;
- monthly reference;
- monthly commitments;
- available margin.
For the current MVP:
monthlyReference = averageIncome

monthlyCommitments =
monthlyDebtPayments
+
otherFixedMonthlyExpenses

availableMargin =
monthlyReference
-
monthlyCommitments

Recent transactions provide financial context and category information, but they are not directly added to monthlyCommitments.
Flex Range
One of the main post-Hackathon product evolutions is the Flex Range.
The Flex Range provides a visual representation of how recent income behaves across the months informed by the user and how that income relates to the user's financial commitments.
It combines:
- recent income history;
- monthly reference;
- monthly commitments;
- available margin;
- fixed commitment percentage.
The visualization is intended to help users understand their financial rhythm rather than reducing their situation to a single isolated number.
Expense Simulation
After completing a financial analysis, the user can simulate a new expense.
The simulation receives:
- expense description;
- total amount;
- number of installments.
The backend recalculates the projected scenario and compares it with the current financial situation.
The flow is:
Current financial scenario
        ↓
New monthly installment
        ↓
Projected financial scenario

The interface compares:
- current margin;
- new installment;
- projected margin;
- current financial profile;
- projected financial profile;
- probabilities;
- debt indicators;
- fixed commitment;
- impact level;
- recommendations.
Architecture
The current application architecture is:
React + TypeScript
        │
        ▼
Spring Boot REST API
        │
        ▼
Python / FastAPI
        │
        ▼
Machine Learning models

The deployed architecture uses:
React + TypeScript
        │
      Vercel
        │
        ▼
Spring Boot
 OCI Compute / Docker
        │
        ▼
Python / FastAPI
 OCI Compute / Docker
        │
        └──────────────► OCI Object Storage
                         ML artifacts

The frontend never communicates directly with the Python service.
Frontend
The frontend is built with:
- React 19;
- TypeScript;
- Vite;
- React Router;
- lucide-react;
- CSS Custom Properties.
The frontend is responsible for:
- collecting user input;
- client-side validation;
- API communication;
- presenting financial analysis;
- displaying classified transactions;
- presenting the Flex Range;
- displaying recommendations;
- expense simulation;
- scenario comparison;
- responsive interaction;
- accessibility-oriented feedback.
The frontend does not calculate Machine Learning features or call the Python service directly.
UI / UX Evolution
The post-MVP phase introduced a major visual redesign.
The current visual identity uses:
- cream: #F5EBDD
- coral: #F2765E
- blue: #315B8C
- dark brown: #413333
The main typography is based on Manrope.
The design direction focuses on:
- asymmetric layouts;
- broader use of desktop space;
- responsive behavior;
- controlled visual contrast;
- financial information hierarchy;
- data-driven visualizations;
- accessible interactions;
- light and dark themes.
The main Home message is:
Your income changes. Your planning can keep up.

The redesign also introduced a visual identity and mascot specifically developed for RendaFlex.
Spring Boot Backend
The Spring Boot API is responsible for:
- public REST endpoints;
- request validation;
- business rules;
- DTO mapping;
- API contracts;
- orchestration between frontend and Python;
- expense-simulation support;
- error handling;
- CORS configuration;
- integration with the Python service.
Main public operations include:
POST /api/v1/financial-analyses
POST /api/v1/expense-simulations
POST /api/v1/transactions/classify

Python and Machine Learning
The Python service is built with FastAPI and is responsible for feature engineering and Machine Learning inference.
The project uses:
Financial-profile model
- Random Forest
Transaction-classification model
- TF-IDF
- Multinomial Naive Bayes
The Python service calculates and returns financial features, classifications, probabilities, and derived information required by the backend.
The final public transaction categories include:
FOOD
TRANSPORT
HEALTH
HOUSING
EDUCATION
ENTERTAINMENT
SERVICES
OTHER

Model Artifacts
For the MVP, serialized .pkl Machine Learning artifacts are packaged directly into the Python Docker image.
OCI Object Storage is also used as an artifact repository and infrastructure proof of concept.
A future evolution may dynamically load model artifacts from Object Storage at runtime.
API Contract
The public API contract is versioned and documented at:
docs/api/rendaflex_api_contract_v2.0.0.json

The contract defines:
- public endpoints;
- internal integration schemas;
- request and response structures;
- validation rules;
- enums;
- financial metrics;
- error structures;
- timeout behavior;
- Java-to-Python integration policies.
Technology Stack
Frontend
- React 19
- TypeScript
- Vite
- React Router
- lucide-react
- CSS Custom Properties
Backend
- Java
- Spring Boot
Data and Machine Learning
- Python
- FastAPI
- Pandas
- Scikit-learn
Modeling
- Random Forest
- TF-IDF
- Multinomial Naive Bayes
Infrastructure
- Docker
- Docker Compose
- Oracle Cloud Infrastructure
- OCI Compute
- OCI Object Storage
- Vercel
Engineering Workflow
- Git
- GitHub
- feature branches
- Pull Requests
- automated tests
- API contract versioning
Testing and Quality
The project includes automated and manual validation across multiple layers.
Current quality practices include:
- Java unit tests;
- Java integration tests;
- Python tests;
- Spring Boot–Python integration validation;
- frontend TypeScript checks;
- ESLint;
- Vite production builds;
- git diff --check;
- responsive interface validation;
- light and dark theme validation;
- error, loading, and empty states;
- accessibility-oriented labels and keyboard focus;
- end-to-end API validation.
Privacy
RendaFlex currently does not require:
- user registration;
- bank-account connection;
- financial institution credentials.
Analysis data is handled within the current application session.
The product is intended as a financial organization and decision-support tool and does not replace professional financial advice.
Repository Structure
rendaFlex/
├── frontend/
│   └── React + TypeScript application
│
├── src/
│   └── Spring Boot backend
│
├── python-service/
│   └── FastAPI + Machine Learning service
│
├── docs/
│   └── API contracts and examples
│
├── infra/
│   └── Infrastructure configuration
│
├── Dockerfile
└── docker-compose.yml

Roadmap
The next development stages include:
1. continue refining the analysis and simulation interfaces;
2. improve wide-screen and responsive layouts;
3. evolve financial visualizations;
4. improve deployment and observability;
5. introduce authenticated users;
6. persist financial history;
7. support continuous financial tracking;
8. evolve Machine Learning artifact loading;
9. expand product analytics;
10. continue developing RendaFlex beyond the original Hackathon MVP.
Credits and Authorship
Original ONE Hackathon MVP
The original RendaFlex MVP was collaboratively developed by:
- Deane Carvalho — Data
- Junior Ribeiro — Data
- Millena Belo — Backend
- Paulo Emilio — Full Stack
- Raul Oliveira — Backend
Original repository:
https://github.com/No-Country-simulation/RendaFlex__G9-BR-Team05
Current Project Development
After completion and presentation of the Hackathon MVP, the project entered a new development phase.
Paulo Emilio created this repository and took responsibility for the continued independent development of RendaFlex.
Current repository:
https://github.com/pauloemilio-tech/rendaFlex
The current repository therefore contains both:
- the collaborative foundation created during the ONE Hackathon;
- the subsequent independent evolution developed after the MVP.
<div align="center">

RendaFlex
Your income changes. Your planning can keep up.
Functional MVP · Actively evolving
</div>
```
