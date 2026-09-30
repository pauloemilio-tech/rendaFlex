from __future__ import annotations

import os
from pathlib import Path
from typing import Any

import joblib
import numpy as np
import pandas as pd
from fastapi import FastAPI


BASE_DIR = Path(__file__).resolve().parent
MODEL_DIR = Path(os.getenv("RENDAFLEX_MODEL_DIR", BASE_DIR))

# Artefatos definitivos exportados pelo notebook final.
modelo_perfil = joblib.load(MODEL_DIR / "modelo_perfil.pkl")
modelo_categoria = joblib.load(MODEL_DIR / "modelo_categoria.pkl")
vetorizador_tfidf = joblib.load(MODEL_DIR / "vetorizador_tfidf.pkl")
colunas_features = joblib.load(MODEL_DIR / "colunas_features.pkl")


# Mapeamento das categorias traduzidas do dataset para as 8 categorias finais do MVP.
# Mantido no serviço para que a inferência não dependa dos CSVs de treinamento.
DE_PARA_CATEGORIA_FINAL = {
    "Supermercados": "Alimentação",
    "Fast food": "Alimentação",
    "Restaurantes": "Alimentação",
    "Táxi": "Transporte",
    "Carsharing": "Transporte",
    "Combustível": "Transporte",
    "Aluguel de carro": "Transporte",
    "Serviços automotivos": "Transporte",
    "Transporte": "Transporte",
    "Passagens aéreas": "Transporte",
    "Passagens de trem": "Transporte",
    "Farmácias": "Saúde",
    "Beleza": "Saúde",
    "Casa e reforma": "Moradia",
    "Educação": "Educação",
    "Livros": "Educação",
    "Música": "Lazer",
    "Cinema": "Lazer",
    "Entretenimento": "Lazer",
    "Artigos esportivos": "Lazer",
    "Foto e vídeo": "Lazer",
    "Duty free": "Lazer",
    "Souvenirs": "Lazer",
    "Arte": "Lazer",
    "Flores": "Lazer",
    "Roupas e calçados": "Serviços",
    "Pet shop": "Serviços",
    "Outros": "Outras",
}

CATEGORIAS_CONHECIDAS = set(DE_PARA_CATEGORIA_FINAL.keys()) | set(
    DE_PARA_CATEGORIA_FINAL.values()
)

CHAVES_ENTRADA_CONTRATO = {
    "historico_renda": "incomeHistory",
    "pagamentos_mensais_dividas": "monthlyDebtPayments",
    "outras_despesas_fixas_mensais": "otherFixedMonthlyExpenses",
    "frequencia_poupanca": "savingFrequency",
    "transacoes": "transactions",
    "descricao": "description",
    "valor": "amount",
    "source_index": "sourceIndex",
}

MAPEAMENTO_CATEGORIAS_CONTRATO = {
    "Alimentação": "FOOD",
    "Transporte": "TRANSPORT",
    "Saúde": "HEALTH",
    "Moradia": "HOUSING",
    "Educação": "EDUCATION",
    "Lazer": "ENTERTAINMENT",
    "Serviços": "SERVICES",
    "Outras": "OTHER",
}

MAPEAMENTO_PERFIL_FINANCEIRO_CONTRATO = {
    "Saudável": "HEALTHY",
    "Em observação": "UNDER_OBSERVATION",
    "Em risco": "AT_RISK",
}

MAPEAMENTO_FREQUENCIA_POUPANCA_CONTRATO = {
    "Alta": "OFTEN",
    "Media": "SOMETIMES",
    "Baixa": "RARELY",
}

MAPEAMENTO_PRIORIDADE_CONTRATO = {
    "Alta": "HIGH",
    "Media": "MEDIUM",
    "Baixa": "LOW",
}


def _nome_contrato(mapeamento: dict[str, str], nome_interno: str) -> str:
    return mapeamento.get(nome_interno) or nome_interno


def _ler_campo_entrada(
    dados: dict[str, Any], nome_interno: str, padrao: Any = None
) -> Any:
    nome_externo = CHAVES_ENTRADA_CONTRATO.get(nome_interno)
    if nome_externo and nome_externo in dados:
        return dados[nome_externo]
    return dados.get(nome_interno, padrao)


def _converter_frequencia_para_interno(valor: str) -> str:
    if valor in MAPEAMENTO_FREQUENCIA_POUPANCA_CONTRATO:
        return valor

    mapa_inverso = {
        externo: interno
        for interno, externo in MAPEAMENTO_FREQUENCIA_POUPANCA_CONTRATO.items()
        if externo is not None
    }
    return mapa_inverso.get(valor, valor)


def analisar_financas(dados_entrada: dict[str, Any]) -> dict[str, Any]:
    historico_renda = _ler_campo_entrada(dados_entrada, "historico_renda", [])
    pagamentos_mensais_dividas = (
        _ler_campo_entrada(dados_entrada, "pagamentos_mensais_dividas", 0) or 0
    )
    outras_despesas_fixas_mensais = (
        _ler_campo_entrada(dados_entrada, "outras_despesas_fixas_mensais", 0) or 0
    )
    frequencia_recebida = _ler_campo_entrada(
        dados_entrada, "frequencia_poupanca", "Baixa"
    )
    frequencia_poupanca = _converter_frequencia_para_interno(frequencia_recebida)
    transacoes = _ler_campo_entrada(dados_entrada, "transacoes", [])

    renda_media = float(np.mean(historico_renda)) if historico_renda else 0
    renda_desvio = float(np.std(historico_renda)) if len(historico_renda) > 1 else 0
    coef_variacao_renda = (renda_desvio / renda_media) if renda_media > 0 else 0

    resumo_gastos: dict[str, float] = {}
    transacoes_classificadas: list[dict[str, Any]] = []

    for transacao in transacoes:
        descricao = _ler_campo_entrada(transacao, "descricao")
        valor = _ler_campo_entrada(transacao, "valor", 0)

        if descricao in CATEGORIAS_CONHECIDAS:
            categoria = descricao
            probabilidade_categoria = 1.0
        else:
            vetor = vetorizador_tfidf.transform([descricao])
            categoria = modelo_categoria.predict(vetor)[0]
            probabilidade_categoria = modelo_categoria.predict_proba(vetor).max()

        resumo_gastos[categoria] = round(resumo_gastos.get(categoria, 0) + valor, 2)
        transacoes_classificadas.append(
            {
                "sourceIndex": _ler_campo_entrada(transacao, "source_index"),
                "predictedCategory": _nome_contrato(
                    MAPEAMENTO_CATEGORIAS_CONTRATO, categoria
                ),
                "classificationProbability": round(
                    float(probabilidade_categoria), 4
                ),
            }
        )

    despesa_total = sum(
        _ler_campo_entrada(transacao, "valor", 0) for transacao in transacoes
    )
    nivel_endividamento = (
        pagamentos_mensais_dividas / renda_media * 100 if renda_media > 0 else 0
    )
    comprometimento_fixo = (
        (pagamentos_mensais_dividas + outras_despesas_fixas_mensais) / renda_media
        if renda_media > 0
        else 1
    )
    referencia_mensal = renda_media
    compromissos_mensais = (
        pagamentos_mensais_dividas + outras_despesas_fixas_mensais
    )
    margem_disponivel = referencia_mensal - compromissos_mensais
    saldo_estimado = renda_media - despesa_total

    valores_features = {
        "renda_media": renda_media,
        "coef_variacao_renda": coef_variacao_renda,
        "despesa_media_mensal": despesa_total,
        "comprometimento_fixo": min(comprometimento_fixo, 1),
        "saldo_medio_mensal": saldo_estimado,
    }

    entrada_modelo = pd.DataFrame(
        [
            {
                coluna: valor
                for coluna, valor in valores_features.items()
                if coluna in colunas_features
            }
        ]
    )[colunas_features]

    perfil_previsto = modelo_perfil.predict(entrada_modelo)[0]
    probabilidade = modelo_perfil.predict_proba(entrada_modelo).max()

    recomendacoes: list[dict[str, str]] = []

    if nivel_endividamento > 30:
        recomendacoes.append(
            {
                "mensagem": "Priorizar redução do nível de endividamento",
                "prioridade": "Alta",
            }
        )

    if (
        renda_media > 0
        and despesa_total / renda_media > 0.6
        and perfil_previsto != "Saudável"
    ):
        recomendacoes.append(
            {
                "mensagem": "Rever gastos recorrentes, o comprometimento da renda está alto",
                "prioridade": "Alta",
            }
        )

    if frequencia_poupanca == "Baixa":
        recomendacoes.append(
            {
                "mensagem": "Aumentar a frequência de poupança mensal",
                "prioridade": "Media",
            }
        )

    if coef_variacao_renda > 0.4:
        recomendacoes.append(
            {
                "mensagem": "Construir uma reserva de emergência maior, dado o histórico de renda instável",
                "prioridade": "Media",
            }
        )

    if not recomendacoes:
        recomendacoes.append(
            {
                "mensagem": "Manter os hábitos financeiros atuais",
                "prioridade": "Baixa",
            }
        )

    category_percentages = {
        categoria: round(valor / despesa_total, 4) if despesa_total > 0 else 0
        for categoria, valor in resumo_gastos.items()
    }

    return {
        "financialProfile": MAPEAMENTO_PERFIL_FINANCEIRO_CONTRATO.get(
            perfil_previsto, perfil_previsto
        ),
        "probability": round(float(probabilidade), 2),
        "metrics": {
            "averageIncome": round(renda_media, 2),
            "incomeVariationCoefficient": round(coef_variacao_renda, 4),
            "debtRatio": round(nivel_endividamento / 100, 4),
            "fixedCommitment": round(comprometimento_fixo, 4),
            "monthlyReference": round(referencia_mensal, 2),
            "monthlyCommitments": round(compromissos_mensais, 2),
            "availableMargin": round(margem_disponivel, 2),
        },
        "classifiedTransactions": transacoes_classificadas,
        "categorySummary": {
            _nome_contrato(MAPEAMENTO_CATEGORIAS_CONTRATO, categoria): valor
            for categoria, valor in resumo_gastos.items()
        },
        "categoryPercentages": {
            _nome_contrato(MAPEAMENTO_CATEGORIAS_CONTRATO, categoria): valor
            for categoria, valor in category_percentages.items()
        },
        "recommendations": [
            {
                "priority": MAPEAMENTO_PRIORIDADE_CONTRATO.get(
                    recomendacao["prioridade"], recomendacao["prioridade"]
                ),
                "message": recomendacao["mensagem"],
            }
            for recomendacao in recomendacoes
        ],
    }


def classificar_transacoes(transacoes: list[dict[str, Any]]) -> dict[str, Any]:
    resultados = []

    for transacao in transacoes:
        source_index = transacao.get("sourceIndex")
        descricao = transacao.get("description")

        vetor = vetorizador_tfidf.transform([descricao])
        categoria = modelo_categoria.predict(vetor)[0]
        probabilidade = modelo_categoria.predict_proba(vetor).max()

        resultados.append(
            {
                "sourceIndex": source_index,
                "predictedCategory": _nome_contrato(
                    MAPEAMENTO_CATEGORIAS_CONTRATO, categoria
                ),
                "classificationProbability": round(float(probabilidade), 4),
            }
        )

    return {"transactions": resultados}


def simular_nova_despesa(
    dados_entrada: dict[str, Any], nova_despesa: dict[str, Any]
) -> dict[str, Any]:
    resultado_atual = analisar_financas(dados_entrada)

    valor_parcela = nova_despesa["installmentAmount"]

    dados_projetados = dict(dados_entrada)

    divida_atual = dados_entrada.get(
        "monthlyDebtPayments",
        dados_entrada.get("pagamentos_mensais_dividas", 0),
    )

    dados_projetados["monthlyDebtPayments"] = divida_atual + valor_parcela

    resultado_projetado = analisar_financas(dados_projetados)

    metricas_atuais = resultado_atual["metrics"]
    metricas_projetadas = resultado_projetado["metrics"]

    return {
        "currentScenario": {
            "financialProfile": resultado_atual["financialProfile"],
            "probability": resultado_atual["probability"],
            "metrics": metricas_atuais,
        },
        "projectedScenario": {
            "financialProfile": resultado_projetado["financialProfile"],
            "probability": resultado_projetado["probability"],
            "metrics": metricas_projetadas,
        },
        "quantitativeImpact": {
            "metricVariations": {
                "debtRatio": round(
                    metricas_projetadas["debtRatio"] - metricas_atuais["debtRatio"],
                    4,
                ),
                "fixedCommitment": round(
                    metricas_projetadas["fixedCommitment"]
                    - metricas_atuais["fixedCommitment"],
                    4,
                ),
            }
        },
        "recommendations": resultado_projetado["recommendations"],
    }


app = FastAPI(title="RendaFlex Internal Model API")


@app.post("/internal/v1/financial-analyses")
def financial_analysis(payload: dict[str, Any]) -> dict[str, Any]:
    return analisar_financas(payload)


@app.post("/internal/v1/expense-simulations")
def expense_simulation(payload: dict[str, Any]) -> dict[str, Any]:
    nova_despesa = payload["newExpense"]
    contexto = {chave: valor for chave, valor in payload.items() if chave != "newExpense"}
    return simular_nova_despesa(contexto, nova_despesa)


@app.post("/internal/v1/transactions/classify")
def transaction_classification(payload: dict[str, Any]) -> dict[str, Any]:
    return classificar_transacoes(payload["transactions"])
