package com.rendaflex.demo.dto.response;

import java.math.BigDecimal;

public record FinancialMetrics(
        BigDecimal averageIncome,
        BigDecimal incomeVariationCoefficientPercentage,
        BigDecimal debtRatioPercentage,
        BigDecimal fixedCommitmentPercentage,
        BigDecimal monthlyReference,
        BigDecimal monthlyCommitments,
        BigDecimal availableMargin
) {
}
