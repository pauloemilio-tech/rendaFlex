package com.rendaflex.demo.controller;

import com.rendaflex.demo.dto.response.ClassifiedTransaction;
import com.rendaflex.demo.dto.response.FinancialAnalysisResponse;
import com.rendaflex.demo.dto.response.FinancialMetrics;
import com.rendaflex.demo.dto.response.Recommendation;
import com.rendaflex.demo.enums.FinancialProfile;
import com.rendaflex.demo.enums.RecommendationPriority;
import com.rendaflex.demo.enums.TransactionCategory;
import com.rendaflex.demo.enums.TransactionType;
import com.rendaflex.demo.exception.ApiErrorCode;
import com.rendaflex.demo.exception.BusinessRuleException;
import com.rendaflex.demo.exception.ModelServiceException;
import com.rendaflex.demo.service.FinancialAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class FinancialAnalysisControllerTest {

    private static final String ALLOWED_ORIGIN = "http://localhost:5173";

    @MockitoBean
    private FinancialAnalysisService service;

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(applicationContext).build();
    }

    @Test
    void shouldReturnFinancialAnalysis() throws Exception {
        when(service.analyze(any())).thenReturn(response());

        mockMvc.perform(post("/api/v1/financial-analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.financialProfile").value("HEALTHY"))
                .andExpect(jsonPath("$.probability").value(0.80))
                .andExpect(jsonPath("$.metrics.averageIncome").value(3000))
                .andExpect(jsonPath("$.metrics.monthlyReference").value(3000))
                .andExpect(jsonPath("$.metrics.monthlyCommitments").value(1200))
                .andExpect(jsonPath("$.metrics.availableMargin").value(1800))
                .andExpect(jsonPath("$.categorySummary.FOOD").value(500))
                .andExpect(jsonPath("$.categoryPercentages.FOOD").value(100))
                .andExpect(jsonPath("$.recommendations[0].priority").value("MEDIUM"));

        verify(service).analyze(any());
    }

    @Test
    void shouldAllowPreflightFromFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/financial-analyses")
                        .header("Origin", ALLOWED_ORIGIN)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN))
                .andExpect(header().string(
                        "Access-Control-Allow-Methods",
                        org.hamcrest.Matchers.containsString("POST")
                ))
                .andExpect(header().string(
                        "Access-Control-Allow-Headers",
                        org.hamcrest.Matchers.containsString("Content-Type")
                ));

        verifyNoInteractions(service);
    }

    @Test
    void shouldAllowPostFromFrontendOrigin() throws Exception {
        when(service.analyze(any())).thenReturn(response());

        mockMvc.perform(post("/api/v1/financial-analyses")
                        .header("Origin", ALLOWED_ORIGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ALLOWED_ORIGIN))
                .andExpect(jsonPath("$.financialProfile").value("HEALTHY"))
                .andExpect(jsonPath("$.categoryPercentages.FOOD").value(100));

        verify(service).analyze(any());
    }

    @Test
    void shouldAllowPreflightFromAnyOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/financial-analyses")
                        .header("Origin", "http://localhost:9999")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin",
                        "http://localhost:9999"
                ));

        verifyNoInteractions(service);
    }

    @Test
    void shouldReturnValidationErrorForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/financial-analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestWithInsufficientIncomeHistory()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Existem campos inválidos na requisição."))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'incomeHistory')]").isNotEmpty());

        verify(service, never()).analyze(any());
    }

    @Test
    void shouldReturnValidationErrorForInvalidEnum() throws Exception {
        mockMvc.perform(post("/api/v1/financial-analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().replace("SOMETIMES", "INVALID")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("A requisição possui formato ou valores inválidos."))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());

        verify(service, never()).analyze(any());
    }

    @Test
    void shouldReturnBusinessRuleError() throws Exception {
        when(service.analyze(any())).thenThrow(new BusinessRuleException("Regra de negócio violada."));

        mockMvc.perform(post("/api/v1/financial-analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_ERROR"))
                .andExpect(jsonPath("$.message").value("Regra de negócio violada."));
    }

    @Test
    void shouldReturnModelServiceUnavailable() throws Exception {
        when(service.analyze(any())).thenThrow(new ModelServiceException(
                ApiErrorCode.MODEL_SERVICE_UNAVAILABLE,
                "Serviço de modelo indisponível."
        ));

        mockMvc.perform(post("/api/v1/financial-analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("MODEL_SERVICE_UNAVAILABLE"));
    }

    @Test
    void shouldReturnSafeInternalError() throws Exception {
        String technicalMessage = "Detalhe técnico confidencial";
        when(service.analyze(any())).thenThrow(new RuntimeException(technicalMessage));

        mockMvc.perform(post("/api/v1/financial-analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Ocorreu um erro interno inesperado."))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString(technicalMessage)
                )));
    }

    private FinancialAnalysisResponse response() {
        return new FinancialAnalysisResponse(
                FinancialProfile.HEALTHY,
                new BigDecimal("0.80"),
                new FinancialMetrics(
                        new BigDecimal("3000"),
                        new BigDecimal("10"),
                        new BigDecimal("20"),
                        new BigDecimal("35"),
                        new BigDecimal("3000"),
                        new BigDecimal("1200"),
                        new BigDecimal("1800")
                ),
                List.of(new ClassifiedTransaction(
                        "Supermercado",
                        new BigDecimal("500"),
                        LocalDate.of(2026, 7, 15),
                        TransactionType.EXPENSE,
                        TransactionCategory.FOOD,
                        new BigDecimal("0.90")
                )),
                Map.of(TransactionCategory.FOOD, new BigDecimal("500")),
                Map.of(TransactionCategory.FOOD, new BigDecimal("100")),
                List.of(new Recommendation(
                        RecommendationPriority.MEDIUM,
                        "Mantenha uma reserva financeira."
                ))
        );
    }

    private String validRequest() {
        return """
                {
                  "incomeHistory": [
                    {"month": "2026-05", "amount": 2800},
                    {"month": "2026-06", "amount": 3000},
                    {"month": "2026-07", "amount": 3200}
                  ],
                  "monthlyDebtPayments": 500,
                  "otherFixedMonthlyExpenses": 700,
                  "savingFrequency": "SOMETIMES",
                  "transactions": [
                    {
                      "description": "Supermercado",
                      "amount": 500,
                      "date": "2026-07-15",
                      "type": "EXPENSE"
                    }
                  ]
                }
                """;
    }

    private String requestWithInsufficientIncomeHistory() {
        return """
                {
                  "incomeHistory": [
                    {"month": "2026-07", "amount": 3200}
                  ],
                  "monthlyDebtPayments": 500,
                  "otherFixedMonthlyExpenses": 700,
                  "savingFrequency": "SOMETIMES",
                  "transactions": [
                    {
                      "description": "Supermercado",
                      "amount": 500,
                      "date": "2026-07-15",
                      "type": "EXPENSE"
                    }
                  ]
                }
                """;
    }
}
