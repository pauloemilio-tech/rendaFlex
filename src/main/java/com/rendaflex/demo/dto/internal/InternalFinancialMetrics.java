package com.rendaflex.demo.dto.internal;

import java.math.BigDecimal;

public record InternalFinancialMetrics(
        BigDecimal averageIncome,
        BigDecimal incomeVariationCoefficient,
        BigDecimal debtRatio,
        BigDecimal fixedCommitment,
        BigDecimal monthlyReference,
        BigDecimal monthlyCommitments,
        BigDecimal availableMargin
) {
}
