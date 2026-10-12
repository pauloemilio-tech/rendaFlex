<div align="center">

<img src="./frontend/public/assets/rendaFlex.logo1.png" alt="RendaFlex" width="240" />

# RendaFlex

### Sua renda muda. Seu planejamento pode acompanhar.

[![Project status](https://img.shields.io/badge/status-Functional%20MVP%20%2F%20In%20Active%20Development-F2765E?style=flat-square)](#project-status)
[![Frontend](https://img.shields.io/badge/frontend-React%20%2B%20TypeScript-315B8C?style=flat-square)](#technology-stack)
[![Backend](https://img.shields.io/badge/backend-Spring%20Boot-413333?style=flat-square)](#technology-stack)
[![ML service](https://img.shields.io/badge/ML-FastAPI%20%2B%20scikit--learn-315B8C?style=flat-square)](#machine-learning)

[Live demo](https://rendaflex.vercel.app/) · [Original Hackathon repository](https://github.com/No-Country-simulation/RendaFlex__G9-BR-Team05) · [Current repository](https://github.com/pauloemilio-tech/rendaFlex)

</div>

> [!IMPORTANT]
> RendaFlex began as a collaborative MVP at the **Programa ONE / Oracle Next Education Hackathon**. After the MVP was completed and presented, **Paulo Emilio** continued the project independently in this repository. The original work remains credited to the full Hackathon team; this repository represents the project's post-Hackathon evolution, not a project created from scratch by one person.

## About

RendaFlex is a financial decision-support application for people whose income changes from month to month. It combines recent income, recurring commitments, saving frequency and transaction context to help users understand their current financial position before taking on a new expense.

The application provides an informational analysis—not financial advice—and currently works without user accounts or bank connections. Frontend results are kept only for the browser session.

## Project history

### Phase 1 — ONE Hackathon

The original MVP was designed and developed collaboratively for the **Programa ONE / Oracle Next Education Hackathon** by:

| Contributor | Area |
| --- | --- |
| Deane Carvalho | Data |
| Junior Ribeiro | Data |
| Millena Belo | Backend |
| Paulo Emilio | Full Stack |
| Raul Oliveira | Backend |

That phase established the product concept, data and ML work, Java backend, web experience and integrated MVP. Its source and history remain available in the [original team repository](https://github.com/No-Country-simulation/RendaFlex__G9-BR-Team05).

### Phase 2 — Independent continuation

After the Hackathon, I created this repository as the official home of his independent continuation of RendaFlex. Post-MVP work includes the current visual identity, a redesigned Home and result experience, responsive refinements, the **Faixa Flex**, margin comparison, product and UX improvements, and continued evolution of the architecture and financial metrics.

## Current features

- Variable-income analysis based on a 3–6 month history
- Monthly debt payments, other fixed commitments and saving frequency
- Recent income and expense transactions
- ML-assisted transaction classification and category summaries
- Financial profile, profile probability and financial indicators
- Contextual recommendations
- **Faixa Flex** visualization for income rhythm, monthly reference, commitments and available margin
- New-expense simulation by description, total amount and installment count
- Current-versus-projected profile, metrics, margin, impact and recommendations
- Responsive interface with light and dark themes
- Real integration between the React frontend, Spring Boot API and Python service
- Dockerized backend and ML services, with deployments using Vercel and OCI

## Product flow

```text
Income history
    → monthly reference
    → monthly commitments
    → available margin
    → financial profile
    → transaction context
    → recommendations
    → new-expense simulation
    → current vs. projected scenario
```

## Architecture

```text
┌──────────────────────────┐
│ React + TypeScript       │
│ Vite · Vercel            │
└────────────┬─────────────┘
             │ Public REST API (/api/v1)
             ▼
┌──────────────────────────┐
│ Java + Spring Boot       │
│ Docker · OCI Compute     │
└────────────┬─────────────┘
             │ Internal HTTP API (/internal/v1)
             ▼
┌──────────────────────────┐
│ Python + FastAPI         │
│ Pandas · scikit-learn    │
│ Serialized ML models     │
└──────────────────────────┘
```

The frontend calls the Spring Boot API; it does **not** call Python directly. Spring Boot validates public requests, maps the public and internal contracts, orchestrates the use cases and handles integration failures. FastAPI owns feature calculation and model inference.

Docker Compose runs the Java and Python services together. The current Python image packages the serialized `.pkl` artifacts so it can start independently; OCI Object Storage is used as supporting artifact infrastructure rather than as a required runtime download step.

### Public API

The implemented public contract uses the `/api/v1` base path:

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/financial-analyses` | Generate the financial analysis |
| `POST` | `/api/v1/expense-simulations` | Compare current and projected scenarios |
| `POST` | `/api/v1/transactions/classify` | Classify transaction descriptions |

The current contract reference is [`docs/api/rendaflex_api_contract_v2.0.0.json`](./docs/api/rendaflex_api_contract_v2.0.0.json).

## Frontend

The frontend is a React single-page application with routes for Home, financial analysis, analysis results, expense simulation and simulation results. It includes form validation, loading and error states, responsive layouts, theme persistence and session-based result persistence.

The post-MVP redesign uses **Manrope** and a visual system centered on `#F5EBDD`, `#F2765E`, `#315B8C` and `#413333`, with asymmetric desktop compositions, a custom mascot and dedicated result visualizations.

## Backend

The Java 17 / Spring Boot API provides request validation, business rules, standardized errors and the public REST boundary. Dedicated gateways and HTTP clients connect it to the Python service with configured timeouts, retry behavior and response validation.

The backend also adapts internal decimal ratios to public percentages, calculates installment values and derives simulation fields such as profile change, financial-health worsening and impact level.

## Machine learning

The Python service loads the model artifacts generated during the Data/ML work and exposes internal endpoints consumed by Spring Boot:

- **Financial profile:** Random Forest
- **Transaction classification:** TF-IDF vectorization + Multinomial Naive Bayes
- **Data processing and inference:** Pandas, NumPy, scikit-learn and joblib

The financial profile returned by the model is one of `HEALTHY`, `UNDER_OBSERVATION` or `AT_RISK`. Transaction expenses are organized into the public categories `FOOD`, `TRANSPORT`, `HEALTH`, `HOUSING`, `EDUCATION`, `ENTERTAINMENT`, `SERVICES` and `OTHER`.

## Financial metrics and Faixa Flex

The analysis currently exposes:

- `averageIncome`
- `incomeVariationCoefficientPercentage`
- `debtRatioPercentage`
- `fixedCommitmentPercentage`
- `monthlyReference`
- `monthlyCommitments`
- `availableMargin`

For the current MVP:

```text
monthlyReference = averageIncome

monthlyCommitments = monthlyDebtPayments
                   + otherFixedMonthlyExpenses

availableMargin = monthlyReference - monthlyCommitments
```

Recent transactions provide spending and classification context and contribute to the profile analysis, but they are intentionally excluded from `monthlyCommitments` and `availableMargin`.

The **Faixa Flex**, introduced during the independent continuation, turns these values into a visual reading of recent income rhythm, monthly reference, committed share and remaining margin. Negative margins are preserved and highlighted rather than hidden.

## Expense simulation

The simulation accepts a description, total amount and number of installments. The backend calculates the monthly installment, and the system evaluates the expense as an additional monthly commitment.

The result compares:

- current and projected available margin;
- current and projected financial profile and probability;
- current and projected financial indicators;
- whether the profile changed or financial health worsened;
- impact level and recommendations.

## Technology stack

| Layer | Technologies |
| --- | --- |
| Frontend | React, TypeScript, Vite, React Router, lucide-react, CSS |
| Backend | Java 17, Spring Boot, Maven |
| Data / ML | Python, FastAPI, Pandas, NumPy, scikit-learn, joblib |
| Models | Random Forest, TF-IDF, Multinomial Naive Bayes |
| Infrastructure | Docker, Docker Compose, OCI Compute, OCI Object Storage, Terraform, Vercel |

## Testing and quality

The repository contains:

- Java unit, controller, validation, mapper, integration-client, gateway and end-to-end tests;
- Python/FastAPI tests for analysis metrics, negative margin behavior, simulations and transaction classification;
- frontend linting and TypeScript/Vite production-build checks.

Useful verification commands:

```bash
./mvnw test

cd python-service
pytest

cd ../frontend
npm run lint
npm run build
```

No fixed test count is documented here because the suites continue to evolve with the project.

## Running locally

Start the backend and ML service:

```bash
docker compose up --build
```

Then configure and start the frontend:

```bash
cd frontend
npm install
npm run dev
```

For local integration, set `VITE_API_BASE_URL=http://localhost:8080`. The optional `VITE_USE_MOCK_API=true` switch is available for isolated frontend work; the integrated flow uses the real Spring Boot API.

## Project status

**Functional MVP / In Active Development**

### Working today

- End-to-end financial analysis and expense-simulation flows
- Spring Boot API and Python model service integration
- Financial metrics, transaction classification and recommendations
- Faixa Flex and current-versus-projected margin comparison
- Responsive interface and light/dark themes
- Automated backend and Python coverage
- Docker-based service deployment, OCI infrastructure and Vercel frontend

### Still evolving

- Visual refinement of forms and result pages
- Fine-grained responsiveness and deployment synchronization
- Observability and operational visibility
- Authenticated users and persistent financial history
- Product validation and broader product capabilities
- Future infrastructure and model-delivery improvements

The existence of a deployed, functional MVP should not be read as a claim that the product is finished or generally production-ready.

## Credits and repositories

RendaFlex's foundation belongs to the collaborative Hackathon phase and to the five-person team credited above. The independent continuation is maintained by **Paulo Emilio**, while preserving that original authorship and project history.

- **Original Hackathon repository:** [No-Country-simulation/RendaFlex__G9-BR-Team05](https://github.com/No-Country-simulation/RendaFlex__G9-BR-Team05)
- **Independent continuation:** [pauloemilio-tech/rendaFlex](https://github.com/pauloemilio-tech/rendaFlex)
