package com.rendaflex.demo.mapper;

import com.rendaflex.demo.dto.internal.InternalExpenseSimulationRequest;
import com.rendaflex.demo.dto.internal.InternalExpenseSimulationResponse;
import com.rendaflex.demo.dto.internal.InternalExpenseTransaction;
import com.rendaflex.demo.dto.internal.InternalFinancialMetrics;
import com.rendaflex.demo.dto.internal.InternalFinancialScenario;
import com.rendaflex.demo.dto.internal.InternalNewExpense;
import com.rendaflex.demo.dto.internal.InternalRecommendation;
import com.rendaflex.demo.dto.internal.QuantitativeImpact;
import com.rendaflex.demo.dto.request.IncomeHistoryItem;
import com.rendaflex.demo.dto.request.NewExpenseInput;
import com.rendaflex.demo.dto.request.SimulationRequest;
import com.rendaflex.demo.dto.request.TransactionInput;
import com.rendaflex.demo.dto.response.FinancialMetrics;
import com.rendaflex.demo.dto.response.FinancialScenario;
import com.rendaflex.demo.dto.response.Recommendation;
import com.rendaflex.demo.dto.response.SimulatedExpenseResult;
import com.rendaflex.demo.dto.response.SimulationResponse;
import com.rendaflex.demo.enums.FinancialProfile;
import com.rendaflex.demo.enums.ImpactLevel;
import com.rendaflex.demo.enums.TransactionType;
import com.rendaflex.demo.exception.ApiErrorCode;
import com.rendaflex.demo.exception.ModelServiceException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

@Component
public class SimulationMapper {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;

    /*
     * MVP ImpactLevel thresholds.
     * Internal financial ratios use decimal representation:
     * 0.05 = 5 percentage points and 0.15 = 15 percentage points.
     */
    private static final BigDecimal MODERATE_IMPACT_THRESHOLD = new BigDecimal("0.05");
    private static final BigDecimal HIGH_IMPACT_THRESHOLD = new BigDecimal("0.15");

    private static final String DEBT_RATIO = "debtRatio";
    private static final String FIXED_COMMITMENT = "fixedCommitment";

    public InternalExpenseSimulationRequest toInternalRequest(SimulationRequest request) {
        Objects.requireNonNull(request, "SimulationRequest must not be null.");

        List<BigDecimal> incomeHistory = request.incomeHistory().stream()
                .sorted(Comparator.comparing(item -> YearMonth.parse(item.month())))
                .map(IncomeHistoryItem::amount)
                .toList();

        List<InternalExpenseTransaction> expenses = IntStream.range(0, request.transactions().size())
                .filter(index -> request.transactions().get(index).type() == TransactionType.EXPENSE)
                .mapToObj(index -> toInternalExpense(index, request.transactions().get(index)))
                .toList();

        InternalNewExpense newExpense = toInternalNewExpense(request.newExpense());

        return new InternalExpenseSimulationRequest(
                incomeHistory,
                request.monthlyDebtPayments(),
                request.otherFixedMonthlyExpenses(),
                request.savingFrequency(),
                expenses,
                newExpense
        );
    }

    public SimulationResponse toPublicResponse(
            SimulationRequest originalRequest,
            InternalExpenseSimulationResponse internalResponse
    ) {
        Objects.requireNonNull(originalRequest, "Original SimulationRequest must not be null.");

        if (internalResponse == null) {
            throw invalidInternalResponse();
        }

        InternalFinancialScenario currentInternal = requireScenario(internalResponse.currentScenario());
        InternalFinancialScenario projectedInternal = requireScenario(internalResponse.projectedScenario());

        FinancialScenario currentScenario = toPublicScenario(currentInternal);
        FinancialScenario projectedScenario = toPublicScenario(projectedInternal);

        FinancialProfile currentProfile = currentScenario.financialProfile();
        FinancialProfile projectedProfile = projectedScenario.financialProfile();

        boolean profileChanged = currentProfile != projectedProfile;

        int currentSeverity = severity(currentProfile);
        int projectedSeverity = severity(projectedProfile);

        boolean financialHealthWorsened = projectedSeverity > currentSeverity;

        ImpactLevel impactLevel = calculateImpactLevel(
                currentSeverity,
                projectedSeverity,
                financialHealthWorsened,
                internalResponse.quantitativeImpact()
        );

        return new SimulationResponse(
                toPublicNewExpense(originalRequest.newExpense()),
                currentScenario,
                projectedScenario,
                profileChanged,
                financialHealthWorsened,
                impactLevel,
                toPublicRecommendations(internalResponse.recommendations())
        );
    }

    private InternalExpenseTransaction toInternalExpense(
            int sourceIndex,
            TransactionInput transaction
    ) {
        return new InternalExpenseTransaction(
                sourceIndex,
                transaction.description(),
                transaction.amount()
        );
    }

    private InternalNewExpense toInternalNewExpense(NewExpenseInput newExpense) {
        Objects.requireNonNull(newExpense, "NewExpenseInput must not be null.");

        BigDecimal installmentAmount = calculateInstallmentAmount(
                newExpense.totalAmount(),
                newExpense.installmentCount()
        );

        return new InternalNewExpense(
                newExpense.description(),
                newExpense.totalAmount(),
                newExpense.installmentCount(),
                installmentAmount
        );
    }

    private SimulatedExpenseResult toPublicNewExpense(NewExpenseInput newExpense) {
        Objects.requireNonNull(newExpense, "NewExpenseInput must not be null.");

        return new SimulatedExpenseResult(
                newExpense.description(),
                money(newExpense.totalAmount()),
                newExpense.installmentCount(),
                calculateInstallmentAmount(
                        newExpense.totalAmount(),
                        newExpense.installmentCount()
                )
        );
    }

    private BigDecimal calculateInstallmentAmount(
            BigDecimal totalAmount,
            Integer installmentCount
    ) {
        if (totalAmount == null || installmentCount == null || installmentCount <= 0) {
            throw new IllegalArgumentException("Invalid expense installment data.");
        }

        return totalAmount.divide(
                BigDecimal.valueOf(installmentCount),
                2,
                RoundingMode.HALF_UP
        );
    }

    private FinancialScenario toPublicScenario(InternalFinancialScenario scenario) {
        InternalFinancialScenario validScenario = requireScenario(scenario);

        validateProbability(validScenario.probability());

        return new FinancialScenario(
                requireFinancialProfile(validScenario.financialProfile()),
                validScenario.probability(),
                toPublicMetrics(validScenario.metrics())
        );
    }

    private FinancialMetrics toPublicMetrics(InternalFinancialMetrics metrics) {
        if (metrics == null) {
            throw invalidInternalResponse();
        }

        validateNonNegative(metrics.averageIncome());
        validateNonNegative(metrics.incomeVariationCoefficient());
        validateNonNegative(metrics.debtRatio());
        validateNonNegative(metrics.fixedCommitment());
        validateNonNegative(metrics.monthlyReference());
        validateNonNegative(metrics.monthlyCommitments());
        validatePresent(metrics.availableMargin());

        return new FinancialMetrics(
                money(metrics.averageIncome()),
                percentage(metrics.incomeVariationCoefficient()),
                percentage(metrics.debtRatio()),
                percentage(metrics.fixedCommitment()),
                money(metrics.monthlyReference()),
                money(metrics.monthlyCommitments()),
                money(metrics.availableMargin())
        );
    }

    private ImpactLevel calculateImpactLevel(
            int currentSeverity,
            int projectedSeverity,
            boolean financialHealthWorsened,
            QuantitativeImpact quantitativeImpact
    ) {
        BigDecimal maxIncrease = maxRelevantMetricIncrease(quantitativeImpact);

        boolean projectedAtRisk = projectedSeverity == severity(FinancialProfile.AT_RISK);

        if ((projectedAtRisk && financialHealthWorsened)
                || maxIncrease.compareTo(HIGH_IMPACT_THRESHOLD) >= 0) {
            return ImpactLevel.HIGH;
        }

        int worseningSteps = projectedSeverity - currentSeverity;

        if (worseningSteps == 1
                || maxIncrease.compareTo(MODERATE_IMPACT_THRESHOLD) >= 0) {
            return ImpactLevel.MODERATE;
        }

        return ImpactLevel.LOW;
    }

    private BigDecimal maxRelevantMetricIncrease(QuantitativeImpact quantitativeImpact) {
        if (quantitativeImpact == null || quantitativeImpact.metricVariations() == null) {
            throw invalidInternalResponse();
        }

        Map<String, BigDecimal> variations = quantitativeImpact.metricVariations();

        BigDecimal debtRatioVariation = variations.get(DEBT_RATIO);
        BigDecimal fixedCommitmentVariation = variations.get(FIXED_COMMITMENT);

        if (debtRatioVariation == null || fixedCommitmentVariation == null) {
            throw invalidInternalResponse();
        }

        return ZERO
                .max(debtRatioVariation)
                .max(fixedCommitmentVariation);
    }

    private List<Recommendation> toPublicRecommendations(
            List<InternalRecommendation> internalRecommendations
    ) {
        if (internalRecommendations == null) {
            throw invalidInternalResponse();
        }

        List<Recommendation> result = new ArrayList<>(internalRecommendations.size());

        for (InternalRecommendation recommendation : internalRecommendations) {
            if (recommendation == null
                    || recommendation.priority() == null
                    || recommendation.message() == null
                    || recommendation.message().isBlank()) {
                throw invalidInternalResponse();
            }

            result.add(new Recommendation(
                    recommendation.priority(),
                    recommendation.message()
            ));
        }

        return List.copyOf(result);
    }

    private InternalFinancialScenario requireScenario(InternalFinancialScenario scenario) {
        if (scenario == null) {
            throw invalidInternalResponse();
        }
        return scenario;
    }

    private FinancialProfile requireFinancialProfile(FinancialProfile financialProfile) {
        if (financialProfile == null) {
            throw invalidInternalResponse();
        }
        return financialProfile;
    }

    private int severity(FinancialProfile profile) {
        return switch (requireFinancialProfile(profile)) {
            case HEALTHY -> 0;
            case UNDER_OBSERVATION -> 1;
            case AT_RISK -> 2;
        };
    }

    private BigDecimal percentage(BigDecimal ratio) {
        return ratio.multiply(ONE_HUNDRED).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateProbability(BigDecimal probability) {
        if (probability == null
                || probability.compareTo(ZERO) < 0
                || probability.compareTo(ONE) > 0) {
            throw invalidInternalResponse();
        }
    }

    private void validateNonNegative(BigDecimal value) {
        if (value == null || value.compareTo(ZERO) < 0) {
            throw invalidInternalResponse();
        }
    }

    private void validatePresent(BigDecimal value) {
        if (value == null) {
            throw invalidInternalResponse();
        }
    }

    private ModelServiceException invalidInternalResponse() {
        return new ModelServiceException(
                ApiErrorCode.MODEL_SERVICE_INVALID_RESPONSE,
                "O serviço de simulação retornou uma resposta inválida."
        );
    }
}
