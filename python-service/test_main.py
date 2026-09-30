from fastapi.testclient import TestClient

from main import app


client = TestClient(app)


PAYLOAD_ANALISE = {
    "incomeHistory": [3200.0, 3400.0, 3300.0],
    "monthlyDebtPayments": 600.0,
    "otherFixedMonthlyExpenses": 900.0,
    "savingFrequency": "OFTEN",
    "transactions": [
        {"sourceIndex": 0, "description": "Uber", "amount": 51.0}
    ],
}


def test_financial_analysis():
    response = client.post("/internal/v1/financial-analyses", json=PAYLOAD_ANALISE)

    assert response.status_code == 200
    body = response.json()
    assert "financialProfile" in body
    assert "probability" in body
    assert "metrics" in body
    assert "classifiedTransactions" in body
    assert "categorySummary" in body
    assert "categoryPercentages" in body
    assert "recommendations" in body
    assert body["classifiedTransactions"][0]["sourceIndex"] == 0
    assert body["metrics"]["monthlyReference"] == 3300.0
    assert body["metrics"]["monthlyCommitments"] == 1500.0
    assert body["metrics"]["availableMargin"] == 1800.0


def test_financial_margin_metrics():
    payload = {
        **PAYLOAD_ANALISE,
        "incomeHistory": [4000.0, 4000.0, 4000.0],
        "monthlyDebtPayments": 500.0,
        "otherFixedMonthlyExpenses": 800.0,
    }

    response = client.post("/internal/v1/financial-analyses", json=payload)

    assert response.status_code == 200
    metrics = response.json()["metrics"]
    assert metrics["averageIncome"] == 4000.0
    assert metrics["monthlyReference"] == 4000.0
    assert metrics["monthlyCommitments"] == 1300.0
    assert metrics["availableMargin"] == 2700.0


def test_financial_margin_metrics_with_zero_commitments():
    payload = {
        **PAYLOAD_ANALISE,
        "incomeHistory": [4000.0, 4000.0, 4000.0],
        "monthlyDebtPayments": 0.0,
        "otherFixedMonthlyExpenses": 0.0,
    }

    response = client.post("/internal/v1/financial-analyses", json=payload)

    assert response.status_code == 200
    metrics = response.json()["metrics"]
    assert metrics["monthlyCommitments"] == 0.0
    assert metrics["availableMargin"] == 4000.0


def test_financial_margin_metrics_preserve_negative_available_margin():
    payload = {
        **PAYLOAD_ANALISE,
        "incomeHistory": [3000.0, 3000.0, 3000.0],
        "monthlyDebtPayments": 1800.0,
        "otherFixedMonthlyExpenses": 1500.0,
    }

    response = client.post("/internal/v1/financial-analyses", json=payload)

    assert response.status_code == 200
    metrics = response.json()["metrics"]
    assert metrics["monthlyCommitments"] == 3300.0
    assert metrics["availableMargin"] == -300.0


def test_expense_simulation():
    payload = {
        **PAYLOAD_ANALISE,
        "newExpense": {
            "description": "Notebook",
            "totalAmount": 3600.0,
            "installmentCount": 12,
            "installmentAmount": 300.0,
        },
    }

    response = client.post("/internal/v1/expense-simulations", json=payload)

    assert response.status_code == 200
    body = response.json()
    assert "currentScenario" in body
    assert "projectedScenario" in body
    assert "quantitativeImpact" in body
    assert "recommendations" in body
    assert set(body["quantitativeImpact"]["metricVariations"]) == {
        "debtRatio",
        "fixedCommitment",
    }


def test_expense_simulation_updates_commitments_and_available_margin():
    payload = {
        **PAYLOAD_ANALISE,
        "incomeHistory": [4000.0, 4000.0, 4000.0],
        "monthlyDebtPayments": 500.0,
        "otherFixedMonthlyExpenses": 800.0,
        "newExpense": {
            "description": "Notebook",
            "totalAmount": 320.0,
            "installmentCount": 1,
            "installmentAmount": 320.0,
        },
    }

    response = client.post("/internal/v1/expense-simulations", json=payload)

    assert response.status_code == 200
    body = response.json()
    current = body["currentScenario"]["metrics"]
    projected = body["projectedScenario"]["metrics"]
    assert current["monthlyCommitments"] == 1300.0
    assert current["availableMargin"] == 2700.0
    assert projected["monthlyCommitments"] == 1620.0
    assert projected["availableMargin"] == 2380.0


def test_transaction_classification():
    payload = {
        "transactions": [
            {"sourceIndex": 0, "description": "Netflix"},
            {"sourceIndex": 1, "description": "Posto de gasolina"},
        ]
    }

    response = client.post("/internal/v1/transactions/classify", json=payload)

    assert response.status_code == 200
    body = response.json()
    assert len(body["transactions"]) == 2
    assert [item["sourceIndex"] for item in body["transactions"]] == [0, 1]
