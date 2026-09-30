package com.rendaflex.demo.mapper;

import com.rendaflex.demo.dto.internal.InternalExpenseSimulationResponse;
import com.rendaflex.demo.dto.internal.InternalFinancialMetrics;
import com.rendaflex.demo.dto.internal.InternalFinancialScenario;
import com.rendaflex.demo.dto.internal.InternalRecommendation;
import com.rendaflex.demo.dto.internal.QuantitativeImpact;
import com.rendaflex.demo.dto.request.IncomeHistoryItem;
import com.rendaflex.demo.dto.request.NewExpenseInput;
import com.rendaflex.demo.dto.request.SimulationRequest;
import com.rendaflex.demo.dto.request.TransactionInput;
import com.rendaflex.demo.dto.response.SimulationResponse;
import com.rendaflex.demo.enums.FinancialProfile;
import com.rendaflex.demo.enums.ImpactLevel;
import com.rendaflex.demo.enums.RecommendationPriority;
import com.rendaflex.demo.enums.SavingFrequency;
import com.rendaflex.demo.enums.TransactionType;
import com.rendaflex.demo.exception.ApiErrorCode;
import com.rendaflex.demo.exception.ModelServiceException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimulationMapperTest {

    private final SimulationMapper mapper = new SimulationMapper();

    @Test
    void shouldBuildInternalRequestWithChronologicalIncomeOnlyExpensesAndCalculatedInstallment() {
        SimulationRequest request = new SimulationRequest(
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
                ),
                new NewExpenseInput("Notebook", bd("3600.00"), 12)
        );

        var internal = mapper.toInternalRequest(request);

        assertEquals(
                List.of(bd("3200.00"), bd("3400.00"), bd("3300.00")),
                internal.incomeHistory()
        );

        assertEquals(2, internal.transactions().size());
        assertEquals(1, internal.transactions().get(0).sourceIndex());
        assertEquals("Uber", internal.transactions().get(0).description());
        assertEquals(2, internal.transactions().get(1).sourceIndex());

        assertEquals("Notebook", internal.newExpense().description());
        assertEquals(bd("3600.00"), internal.newExpense().totalAmount());
        assertEquals(12, internal.newExpense().installmentCount());
        assertEquals(bd("300.00"), internal.newExpense().installmentAmount());
    }

    @Test
    void shouldRoundInstallmentAmountHalfUpToTwoDecimalPlaces() {
        SimulationRequest request = request(
                FinancialProfile.HEALTHY,
                new NewExpenseInput("Teste", bd("1.00"), 8)
        );

        var internal = mapper.toInternalRequest(request);

        assertEquals(bd("0.13"), internal.newExpense().installmentAmount());
    }

    @Test
    void shouldMapOfficialExampleToModerateImpactAndPublicPercentages() {
        SimulationRequest request = request(
                FinancialProfile.HEALTHY,
                new NewExpenseInput("Notebook", bd("3600.00"), 12)
        );

        InternalExpenseSimulationResponse internal = response(
                FinancialProfile.HEALTHY,
                FinancialProfile.UNDER_OBSERVATION,
                "0.0909",
                "0.091"
        );

        SimulationResponse publicResponse = mapper.toPublicResponse(request, internal);

        assertTrue(publicResponse.profileChanged());
        assertTrue(publicResponse.financialHealthWorsened());
        assertEquals(ImpactLevel.MODERATE, publicResponse.impactLevel());

        assertEquals(
                bd("18.18"),
                publicResponse.currentScenario().metrics().debtRatioPercentage()
        );
        assertEquals(
                bd("45.45"),
                publicResponse.currentScenario().metrics().fixedCommitmentPercentage()
        );
        assertEquals(
                bd("27.27"),
                publicResponse.projectedScenario().metrics().debtRatioPercentage()
        );
        assertEquals(
                bd("54.55"),
                publicResponse.projectedScenario().metrics().fixedCommitmentPercentage()
        );
        assertEquals(bd("1500.00"), publicResponse.currentScenario().metrics().monthlyCommitments());
        assertEquals(bd("1800.00"), publicResponse.currentScenario().metrics().availableMargin());
        assertEquals(bd("1800.00"), publicResponse.projectedScenario().metrics().monthlyCommitments());
        assertEquals(bd("1500.00"), publicResponse.projectedScenario().metrics().availableMargin());

        assertEquals(bd("300.00"), publicResponse.newExpense().installmentAmount());
        assertEquals(1, publicResponse.recommendations().size());
        assertEquals(
                RecommendationPriority.HIGH,
                publicResponse.recommendations().get(0).priority()
        );
    }

    @Test
    void shouldReturnLowImpactBelowFivePercentagePointsWithoutProfileWorsening() {
        SimulationResponse response = mapper.toPublicResponse(
                request(FinancialProfile.HEALTHY, defaultExpense()),
                response(
                        FinancialProfile.HEALTHY,
                        FinancialProfile.HEALTHY,
                        "0.0499",
                        "0.0200"
                )
        );

        assertFalse(response.profileChanged());
        assertFalse(response.financialHealthWorsened());
        assertEquals(ImpactLevel.LOW, response.impactLevel());
    }

    @Test
    void shouldReturnModerateImpactAtFivePercentagePoints() {
        SimulationResponse response = mapper.toPublicResponse(
                request(FinancialProfile.HEALTHY, defaultExpense()),
                response(
                        FinancialProfile.HEALTHY,
                        FinancialProfile.HEALTHY,
                        "0.0500",
                        "0.0100"
                )
        );

        assertEquals(ImpactLevel.MODERATE, response.impactLevel());
    }

    @Test
    void shouldReturnModerateImpactBelowFifteenPercentagePoints() {
        SimulationResponse response = mapper.toPublicResponse(
                request(FinancialProfile.HEALTHY, defaultExpense()),
                response(
                        FinancialProfile.HEALTHY,
                        FinancialProfile.HEALTHY,
                        "0.1499",
                        "0.0300"
                )
        );

        assertEquals(ImpactLevel.MODERATE, response.impactLevel());
    }

    @Test
    void shouldReturnHighImpactAtFifteenPercentagePoints() {
        SimulationResponse response = mapper.toPublicResponse(
                request(FinancialProfile.HEALTHY, defaultExpense()),
                response(
                        FinancialProfile.HEALTHY,
                        FinancialProfile.HEALTHY,
                        "0.1500",
                        "0.0100"
                )
        );

        assertEquals(ImpactLevel.HIGH, response.impactLevel());
    }

    @Test
    void shouldReturnModerateWhenProfileWorsensOneLevelEvenBelowFivePercentagePoints() {
        SimulationResponse response = mapper.toPublicResponse(
                request(FinancialProfile.HEALTHY, defaultExpense()),
                response(
                        FinancialProfile.HEALTHY,
                        FinancialProfile.UNDER_OBSERVATION,
                        "0.0100",
                        "0.0200"
                )
        );

        assertTrue(response.profileChanged());
        assertTrue(response.financialHealthWorsened());
        assertEquals(ImpactLevel.MODERATE, response.impactLevel());
    }

    @Test
    void shouldReturnHighWhenProjectedProfileReachesAtRiskWithWorsening() {
        SimulationResponse response = mapper.toPublicResponse(
                request(FinancialProfile.UNDER_OBSERVATION, defaultExpense()),
                response(
                        FinancialProfile.UNDER_OBSERVATION,
                        FinancialProfile.AT_RISK,
                        "0.0100",
                        "0.0200"
                )
        );

        assertTrue(response.financialHealthWorsened());
        assertEquals(ImpactLevel.HIGH, response.impactLevel());
    }

    @Test
    void shouldNotTreatNegativeVariationAsPositiveImpact() {
        SimulationResponse response = mapper.toPublicResponse(
                request(FinancialProfile.HEALTHY, defaultExpense()),
                response(
                        FinancialProfile.HEALTHY,
                        FinancialProfile.HEALTHY,
                        "-0.0800",
                        "0.0200"
                )
        );

        assertEquals(ImpactLevel.LOW, response.impactLevel());
    }

    @Test
    void shouldDetectProfileImprovementWithoutMarkingFinancialHealthAsWorsened() {
        SimulationResponse response = mapper.toPublicResponse(
                request(FinancialProfile.UNDER_OBSERVATION, defaultExpense()),
                response(
                        FinancialProfile.UNDER_OBSERVATION,
                        FinancialProfile.HEALTHY,
                        "0.0100",
                        "0.0200"
                )
        );

        assertTrue(response.profileChanged());
        assertFalse(response.financialHealthWorsened());
        assertEquals(ImpactLevel.LOW, response.impactLevel());
    }

    @Test
    void shouldRejectNullInternalResponse() {
        ModelServiceException exception = assertThrows(
                ModelServiceException.class,
                () -> mapper.toPublicResponse(
                        request(FinancialProfile.HEALTHY, defaultExpense()),
                        null
                )
        );

        assertEquals(ApiErrorCode.MODEL_SERVICE_INVALID_RESPONSE, exception.getCode());
    }

    @Test
    void shouldRejectMissingRelevantMetricVariation() {
        InternalExpenseSimulationResponse internal = new InternalExpenseSimulationResponse(
                scenario(FinancialProfile.HEALTHY, "0.91", false),
                scenario(FinancialProfile.HEALTHY, "0.90", true),
                new QuantitativeImpact(
                        Map.of("debtRatio", bd("0.02"))
                ),
                recommendations()
        );

        ModelServiceException exception = assertThrows(
                ModelServiceException.class,
                () -> mapper.toPublicResponse(
                        request(FinancialProfile.HEALTHY, defaultExpense()),
                        internal
                )
        );

        assertEquals(ApiErrorCode.MODEL_SERVICE_INVALID_RESPONSE, exception.getCode());
    }

    private SimulationRequest request(
            FinancialProfile ignoredProfile,
            NewExpenseInput newExpense
    ) {
        return new SimulationRequest(
                List.of(
                        income("2026-05", "3200"),
                        income("2026-06", "3400"),
                        income("2026-07", "3300")
                ),
                bd("600"),
                bd("900"),
                SavingFrequency.OFTEN,
                List.of(
                        transaction("Uber", "51", TransactionType.EXPENSE)
                ),
                newExpense
        );
    }

    private NewExpenseInput defaultExpense() {
        return new NewExpenseInput("Notebook", bd("3600"), 12);
    }

    private InternalExpenseSimulationResponse response(
            FinancialProfile currentProfile,
            FinancialProfile projectedProfile,
            String debtRatioVariation,
            String fixedCommitmentVariation
    ) {
        return new InternalExpenseSimulationResponse(
                scenario(currentProfile, "0.91", false),
                scenario(projectedProfile, "0.78", true),
                new QuantitativeImpact(
                        Map.of(
                                "debtRatio", bd(debtRatioVariation),
                                "fixedCommitment", bd(fixedCommitmentVariation)
                        )
                ),
                recommendations()
        );
    }

    private InternalFinancialScenario scenario(
            FinancialProfile profile,
            String probability,
            boolean projected
    ) {
        return new InternalFinancialScenario(
                profile,
                bd(probability),
                projected
                        ? new InternalFinancialMetrics(
                                bd("3300"),
                                bd("0.0247"),
                                bd("0.2727"),
                                bd("0.5455"),
                                bd("3300"),
                                bd("1800"),
                                bd("1500")
                        )
                        : new InternalFinancialMetrics(
                                bd("3300"),
                                bd("0.0247"),
                                bd("0.1818"),
                                bd("0.4545"),
                                bd("3300"),
                                bd("1500"),
                                bd("1800")
                        )
        );
    }

    private List<InternalRecommendation> recommendations() {
        return List.of(
                new InternalRecommendation(
                        RecommendationPriority.HIGH,
                        "Considere uma parcela menor ou adiar a compra."
                )
        );
    }

    private IncomeHistoryItem income(String month, String amount) {
        return new IncomeHistoryItem(month, bd(amount));
    }

    private TransactionInput transaction(
            String description,
            String amount,
            TransactionType type
    ) {
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
