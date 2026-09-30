package com.rendaflex.demo.mapper;

import com.rendaflex.demo.dto.internal.InternalClassifiedTransaction;
import com.rendaflex.demo.dto.internal.InternalFinancialAnalysisResponse;
import com.rendaflex.demo.dto.internal.InternalFinancialMetrics;
import com.rendaflex.demo.dto.internal.InternalRecommendation;
import com.rendaflex.demo.dto.request.FinancialAnalysisRequest;
import com.rendaflex.demo.dto.request.IncomeHistoryItem;
import com.rendaflex.demo.dto.request.TransactionInput;
import com.rendaflex.demo.dto.response.FinancialAnalysisResponse;
import com.rendaflex.demo.enums.FinancialProfile;
import com.rendaflex.demo.enums.RecommendationPriority;
import com.rendaflex.demo.enums.SavingFrequency;
import com.rendaflex.demo.enums.TransactionCategory;
import com.rendaflex.demo.enums.TransactionType;
import com.rendaflex.demo.exception.ApiErrorCode;
import com.rendaflex.demo.exception.ModelServiceException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinancialAnalysisMapperTest {

    private final FinancialAnalysisMapper mapper = new FinancialAnalysisMapper();

    @Test
    void shouldBuildInternalRequestWithChronologicalIncomeAndOnlyExpenses() {
        FinancialAnalysisRequest request = new FinancialAnalysisRequest(
                List.of(
                        income("2026-07", "3300.00"),
                        income("2026-05", "3200.00"),
                        income("2026-06", "3400.00")
                ),
                bd("600.00"),
                bd("900.00"),
                SavingFrequency.OFTEN,
                List.of(
                        transaction("Pagamento de cliente", "850.00", TransactionType.INCOME),
                        transaction("Uber", "51.00", TransactionType.EXPENSE),
                        transaction("Supermercado", "200.00", TransactionType.EXPENSE)
                )
        );

        var internal = mapper.toInternalRequest(request);

        assertEquals(List.of(bd("3200.00"), bd("3400.00"), bd("3300.00")), internal.incomeHistory());
        assertEquals(2, internal.transactions().size());
        assertEquals(1, internal.transactions().get(0).sourceIndex());
        assertEquals("Uber", internal.transactions().get(0).description());
        assertEquals(2, internal.transactions().get(1).sourceIndex());
    }

    @Test
    void shouldPreserveOriginalTransactionOrderAndKeepIncomeUnclassified() {
        FinancialAnalysisRequest request = requestWithIncomeBetweenExpenses();
        InternalFinancialAnalysisResponse internal = response(
                List.of(
                        classified(0, TransactionCategory.TRANSPORT, "0.98"),
                        classified(2, TransactionCategory.FOOD, "0.87")
                ),
                Map.of(TransactionCategory.TRANSPORT, bd("51.00"), TransactionCategory.FOOD, bd("200.00")),
                Map.of(TransactionCategory.TRANSPORT, bd("0.2032"), TransactionCategory.FOOD, bd("0.7968")),
                List.of(new InternalRecommendation(RecommendationPriority.MEDIUM, "Mantenha uma reserva."))
        );

        FinancialAnalysisResponse publicResponse = mapper.toPublicResponse(request, internal);

        assertEquals(3, publicResponse.classifiedTransactions().size());
        assertEquals("Uber", publicResponse.classifiedTransactions().get(0).description());
        assertEquals("Pagamento de cliente", publicResponse.classifiedTransactions().get(1).description());
        assertEquals(TransactionType.INCOME, publicResponse.classifiedTransactions().get(1).type());
        assertNull(publicResponse.classifiedTransactions().get(1).predictedCategory());
        assertNull(publicResponse.classifiedTransactions().get(1).classificationProbability());
        assertEquals("Supermercado", publicResponse.classifiedTransactions().get(2).description());
        assertEquals(1, publicResponse.recommendations().size());
        assertEquals(RecommendationPriority.MEDIUM, publicResponse.recommendations().get(0).priority());
        assertEquals("Mantenha uma reserva.", publicResponse.recommendations().get(0).message());
    }

    @Test
    void shouldConvertInternalRatiosToPublicPercentagesAndRoundMoneyHalfUp() {
        FinancialAnalysisResponse publicResponse = mapper.toPublicResponse(
                requestWithSingleExpense(),
                new InternalFinancialAnalysisResponse(
                        FinancialProfile.HEALTHY,
                        bd("0.91"),
                        new InternalFinancialMetrics(
                                bd("3300.005"),
                                bd("0.0247"),
                                bd("0.1818"),
                                bd("1.23456"),
                                bd("3300.005"),
                                bd("1500.005"),
                                bd("1800.005")
                        ),
                        List.of(classified(0, TransactionCategory.TRANSPORT, "0.98")),
                        Map.of(TransactionCategory.TRANSPORT, bd("51.005")),
                        Map.of(TransactionCategory.TRANSPORT, bd("1.0")),
                        List.of()
                )
        );

        assertEquals(bd("3300.01"), publicResponse.metrics().averageIncome());
        assertEquals(bd("2.47"), publicResponse.metrics().incomeVariationCoefficientPercentage());
        assertEquals(bd("18.18"), publicResponse.metrics().debtRatioPercentage());
        assertEquals(bd("123.46"), publicResponse.metrics().fixedCommitmentPercentage());
        assertEquals(bd("3300.01"), publicResponse.metrics().monthlyReference());
        assertEquals(bd("1500.01"), publicResponse.metrics().monthlyCommitments());
        assertEquals(bd("1800.01"), publicResponse.metrics().availableMargin());
        assertEquals(bd("51.01"), publicResponse.categorySummary().get(TransactionCategory.TRANSPORT));
        assertEquals(bd("100.00"), publicResponse.categoryPercentages().get(TransactionCategory.TRANSPORT));
    }

    @Test
    void shouldPreserveNegativeAvailableMargin() {
        FinancialAnalysisResponse publicResponse = mapper.toPublicResponse(
                requestWithSingleExpense(),
                new InternalFinancialAnalysisResponse(
                        FinancialProfile.AT_RISK,
                        bd("0.91"),
                        new InternalFinancialMetrics(
                                bd("3000"),
                                bd("0.0247"),
                                bd("0.6000"),
                                bd("1.1000"),
                                bd("3000"),
                                bd("3300"),
                                bd("-300")
                        ),
                        List.of(classified(0, TransactionCategory.TRANSPORT, "0.98")),
                        Map.of(TransactionCategory.TRANSPORT, bd("51")),
                        Map.of(TransactionCategory.TRANSPORT, bd("1.0")),
                        List.of()
                )
        );

        assertEquals(bd("-300.00"), publicResponse.metrics().availableMargin());
    }

    @Test
    void shouldRejectClassificationThatPointsToIncome() {
        FinancialAnalysisRequest request = requestWithIncomeBetweenExpenses();
        InternalFinancialAnalysisResponse internal = response(
                List.of(
                        classified(0, TransactionCategory.TRANSPORT, "0.98"),
                        classified(1, TransactionCategory.FOOD, "0.80"),
                        classified(2, TransactionCategory.FOOD, "0.87")
                ),
                Map.of(TransactionCategory.TRANSPORT, bd("51.00")),
                Map.of(TransactionCategory.TRANSPORT, bd("1.0")),
                List.of()
        );

        ModelServiceException exception = assertThrows(
                ModelServiceException.class,
                () -> mapper.toPublicResponse(request, internal)
        );
        assertEquals(ApiErrorCode.MODEL_SERVICE_INVALID_RESPONSE, exception.getCode());
    }

    @Test
    void shouldRejectMissingExpenseClassification() {
        FinancialAnalysisRequest request = requestWithIncomeBetweenExpenses();
        InternalFinancialAnalysisResponse internal = response(
                List.of(classified(0, TransactionCategory.TRANSPORT, "0.98")),
                Map.of(TransactionCategory.TRANSPORT, bd("51.00")),
                Map.of(TransactionCategory.TRANSPORT, bd("1.0")),
                List.of()
        );

        ModelServiceException exception = assertThrows(
                ModelServiceException.class,
                () -> mapper.toPublicResponse(request, internal)
        );
        assertEquals(ApiErrorCode.MODEL_SERVICE_INVALID_RESPONSE, exception.getCode());
    }

    @Test
    void shouldRejectDuplicateSourceIndex() {
        InternalFinancialAnalysisResponse internal = response(
                List.of(
                        classified(0, TransactionCategory.TRANSPORT, "0.98"),
                        classified(0, TransactionCategory.FOOD, "0.80")
                ),
                Map.of(TransactionCategory.TRANSPORT, bd("51.00")),
                Map.of(TransactionCategory.TRANSPORT, bd("1.0")),
                List.of()
        );

        ModelServiceException exception = assertThrows(
                ModelServiceException.class,
                () -> mapper.toPublicResponse(requestWithSingleExpense(), internal)
        );
        assertEquals(ApiErrorCode.MODEL_SERVICE_INVALID_RESPONSE, exception.getCode());
    }

    @Test
    void shouldRejectCategoryPercentageOutsideInternalZeroToOneScale() {
        InternalFinancialAnalysisResponse internal = response(
                List.of(classified(0, TransactionCategory.TRANSPORT, "0.98")),
                Map.of(TransactionCategory.TRANSPORT, bd("51.00")),
                Map.of(TransactionCategory.TRANSPORT, bd("1.01")),
                List.of()
        );

        ModelServiceException exception = assertThrows(
                ModelServiceException.class,
                () -> mapper.toPublicResponse(requestWithSingleExpense(), internal)
        );
        assertEquals(ApiErrorCode.MODEL_SERVICE_INVALID_RESPONSE, exception.getCode());
    }

    @Test
    void shouldRejectNullRecommendationsFromPython() {
        InternalFinancialAnalysisResponse internal = response(
                List.of(classified(0, TransactionCategory.TRANSPORT, "0.98")),
                Map.of(TransactionCategory.TRANSPORT, bd("51.00")),
                Map.of(TransactionCategory.TRANSPORT, bd("1.0")),
                null
        );

        ModelServiceException exception = assertThrows(
                ModelServiceException.class,
                () -> mapper.toPublicResponse(requestWithSingleExpense(), internal)
        );
        assertEquals(ApiErrorCode.MODEL_SERVICE_INVALID_RESPONSE, exception.getCode());
    }

    @Test
    void shouldRejectBlankRecommendationMessageFromPython() {
        InternalFinancialAnalysisResponse internal = response(
                List.of(classified(0, TransactionCategory.TRANSPORT, "0.98")),
                Map.of(TransactionCategory.TRANSPORT, bd("51.00")),
                Map.of(TransactionCategory.TRANSPORT, bd("1.0")),
                List.of(new InternalRecommendation(RecommendationPriority.HIGH, "   "))
        );

        ModelServiceException exception = assertThrows(
                ModelServiceException.class,
                () -> mapper.toPublicResponse(requestWithSingleExpense(), internal)
        );
        assertEquals(ApiErrorCode.MODEL_SERVICE_INVALID_RESPONSE, exception.getCode());
    }

    private FinancialAnalysisRequest requestWithSingleExpense() {
        return new FinancialAnalysisRequest(
                List.of(income("2026-05", "3200"), income("2026-06", "3400"), income("2026-07", "3300")),
                bd("600"),
                bd("900"),
                SavingFrequency.OFTEN,
                List.of(transaction("Uber", "51", TransactionType.EXPENSE))
        );
    }

    private FinancialAnalysisRequest requestWithIncomeBetweenExpenses() {
        return new FinancialAnalysisRequest(
                List.of(income("2026-05", "3200"), income("2026-06", "3400"), income("2026-07", "3300")),
                bd("600"),
                bd("900"),
                SavingFrequency.OFTEN,
                List.of(
                        transaction("Uber", "51", TransactionType.EXPENSE),
                        transaction("Pagamento de cliente", "850", TransactionType.INCOME),
                        transaction("Supermercado", "200", TransactionType.EXPENSE)
                )
        );
    }

    private InternalFinancialAnalysisResponse response(
            List<InternalClassifiedTransaction> classifiedTransactions,
            Map<TransactionCategory, BigDecimal> categorySummary,
            Map<TransactionCategory, BigDecimal> categoryPercentages,
            List<InternalRecommendation> recommendations
    ) {
        return new InternalFinancialAnalysisResponse(
                FinancialProfile.HEALTHY,
                bd("0.91"),
                new InternalFinancialMetrics(
                        bd("3300"),
                        bd("0.0247"),
                        bd("0.1818"),
                        bd("0.4545"),
                        bd("3300"),
                        bd("1500"),
                        bd("1800")
                ),
                classifiedTransactions,
                categorySummary,
                categoryPercentages,
                recommendations
        );
    }

    private InternalClassifiedTransaction classified(
            int sourceIndex,
            TransactionCategory category,
            String probability
    ) {
        return new InternalClassifiedTransaction(sourceIndex, category, bd(probability));
    }

    private IncomeHistoryItem income(String month, String amount) {
        return new IncomeHistoryItem(month, bd(amount));
    }

    private TransactionInput transaction(String description, String amount, TransactionType type) {
        return new TransactionInput(
                description,
                bd(amount),
                LocalDate.of(2026, 7, 15),
                type
        );
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
