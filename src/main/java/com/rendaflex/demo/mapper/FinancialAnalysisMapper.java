package com.rendaflex.demo.mapper;

import com.rendaflex.demo.dto.internal.InternalClassifiedTransaction;
import com.rendaflex.demo.dto.internal.InternalExpenseTransaction;
import com.rendaflex.demo.dto.internal.InternalFinancialAnalysisRequest;
import com.rendaflex.demo.dto.internal.InternalFinancialAnalysisResponse;
import com.rendaflex.demo.dto.internal.InternalFinancialMetrics;
import com.rendaflex.demo.dto.internal.InternalRecommendation;
import com.rendaflex.demo.dto.request.FinancialAnalysisRequest;
import com.rendaflex.demo.dto.request.IncomeHistoryItem;
import com.rendaflex.demo.dto.request.TransactionInput;
import com.rendaflex.demo.dto.response.ClassifiedTransaction;
import com.rendaflex.demo.dto.response.FinancialAnalysisResponse;
import com.rendaflex.demo.dto.response.FinancialMetrics;
import com.rendaflex.demo.dto.response.Recommendation;
import com.rendaflex.demo.enums.FinancialProfile;
import com.rendaflex.demo.enums.TransactionCategory;
import com.rendaflex.demo.enums.TransactionType;
import com.rendaflex.demo.exception.ApiErrorCode;
import com.rendaflex.demo.exception.ModelServiceException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

@Component
public class FinancialAnalysisMapper {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;

    public InternalFinancialAnalysisRequest toInternalRequest(FinancialAnalysisRequest request) {
        Objects.requireNonNull(request, "FinancialAnalysisRequest must not be null.");

        List<BigDecimal> incomeHistory = request.incomeHistory().stream()
                .sorted(Comparator.comparing(item -> YearMonth.parse(item.month())))
                .map(IncomeHistoryItem::amount)
                .toList();

        List<InternalExpenseTransaction> expenses = IntStream.range(0, request.transactions().size())
                .filter(index -> request.transactions().get(index).type() == TransactionType.EXPENSE)
                .mapToObj(index -> toInternalExpense(index, request.transactions().get(index)))
                .toList();

        return new InternalFinancialAnalysisRequest(
                incomeHistory,
                request.monthlyDebtPayments(),
                request.otherFixedMonthlyExpenses(),
                request.savingFrequency(),
                expenses
        );
    }

    public FinancialAnalysisResponse toPublicResponse(
            FinancialAnalysisRequest originalRequest,
            InternalFinancialAnalysisResponse internalResponse
    ) {
        Objects.requireNonNull(originalRequest, "Original FinancialAnalysisRequest must not be null.");
        if (internalResponse == null) {
            throw invalidInternalResponse();
        }

        validateProbability(internalResponse.probability());

        return new FinancialAnalysisResponse(
                requireFinancialProfile(internalResponse.financialProfile()),
                internalResponse.probability(),
                toPublicMetrics(internalResponse.metrics()),
                toPublicTransactions(originalRequest.transactions(), internalResponse.classifiedTransactions()),
                toPublicCategorySummary(internalResponse.categorySummary()),
                toPublicCategoryPercentages(internalResponse.categoryPercentages()),
                toPublicRecommendations(internalResponse.recommendations())
        );
    }

    private InternalExpenseTransaction toInternalExpense(int sourceIndex, TransactionInput transaction) {
        return new InternalExpenseTransaction(
                sourceIndex,
                transaction.description(),
                transaction.amount()
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

    private List<ClassifiedTransaction> toPublicTransactions(
            List<TransactionInput> originalTransactions,
            List<InternalClassifiedTransaction> internalClassifiedTransactions
    ) {
        Objects.requireNonNull(originalTransactions, "Original transactions must not be null.");
        if (internalClassifiedTransactions == null) {
            throw invalidInternalResponse();
        }

        Map<Integer, InternalClassifiedTransaction> classificationsBySourceIndex = new HashMap<>();

        for (InternalClassifiedTransaction classified : internalClassifiedTransactions) {
            validateInternalClassification(classified, originalTransactions);

            InternalClassifiedTransaction previous = classificationsBySourceIndex.put(
                    classified.sourceIndex(),
                    classified
            );

            if (previous != null) {
                throw invalidInternalResponse();
            }
        }

        List<ClassifiedTransaction> result = new ArrayList<>(originalTransactions.size());

        for (int index = 0; index < originalTransactions.size(); index++) {
            TransactionInput original = originalTransactions.get(index);

            if (original.type() == TransactionType.INCOME) {
                result.add(new ClassifiedTransaction(
                        original.description(),
                        original.amount(),
                        original.date(),
                        original.type(),
                        null,
                        null
                ));
                continue;
            }

            InternalClassifiedTransaction classification = classificationsBySourceIndex.get(index);
            if (classification == null) {
                throw invalidInternalResponse();
            }

            result.add(new ClassifiedTransaction(
                    original.description(),
                    original.amount(),
                    original.date(),
                    original.type(),
                    classification.predictedCategory(),
                    classification.classificationProbability()
            ));
        }

        return List.copyOf(result);
    }

    private List<Recommendation> toPublicRecommendations(List<InternalRecommendation> internalRecommendations) {
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

    private void validateInternalClassification(
            InternalClassifiedTransaction classified,
            List<TransactionInput> originalTransactions
    ) {
        if (classified == null) {
            throw invalidInternalResponse();
        }

        Integer sourceIndex = classified.sourceIndex();
        if (sourceIndex == null || sourceIndex < 0 || sourceIndex >= originalTransactions.size()) {
            throw invalidInternalResponse();
        }

        if (originalTransactions.get(sourceIndex).type() != TransactionType.EXPENSE) {
            throw invalidInternalResponse();
        }

        if (classified.predictedCategory() == null) {
            throw invalidInternalResponse();
        }

        validateProbability(classified.classificationProbability());
    }

    private Map<TransactionCategory, BigDecimal> toPublicCategorySummary(
            Map<TransactionCategory, BigDecimal> internalSummary
    ) {
        if (internalSummary == null) {
            throw invalidInternalResponse();
        }

        Map<TransactionCategory, BigDecimal> result = new EnumMap<>(TransactionCategory.class);
        internalSummary.forEach((category, amount) -> {
            if (category == null) {
                throw invalidInternalResponse();
            }
            validateNonNegative(amount);
            result.put(category, money(amount));
        });
        return Map.copyOf(result);
    }

    private Map<TransactionCategory, BigDecimal> toPublicCategoryPercentages(
            Map<TransactionCategory, BigDecimal> internalPercentages
    ) {
        if (internalPercentages == null) {
            throw invalidInternalResponse();
        }

        Map<TransactionCategory, BigDecimal> result = new EnumMap<>(TransactionCategory.class);
        internalPercentages.forEach((category, value) -> {
            if (category == null) {
                throw invalidInternalResponse();
            }
            validateRatioBetweenZeroAndOne(value);
            result.put(category, percentage(value));
        });
        return Map.copyOf(result);
    }

    private BigDecimal percentage(BigDecimal ratio) {
        return ratio.multiply(ONE_HUNDRED).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateProbability(BigDecimal probability) {
        validateRatioBetweenZeroAndOne(probability);
    }

    private void validateRatioBetweenZeroAndOne(BigDecimal value) {
        if (value == null || value.compareTo(ZERO) < 0 || value.compareTo(ONE) > 0) {
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

    private FinancialProfile requireFinancialProfile(FinancialProfile financialProfile) {
        if (financialProfile == null) {
            throw invalidInternalResponse();
        }
        return financialProfile;
    }

    private ModelServiceException invalidInternalResponse() {
        return new ModelServiceException(
                ApiErrorCode.MODEL_SERVICE_INVALID_RESPONSE,
                "O serviço de análise retornou uma resposta inválida."
        );
    }
}
