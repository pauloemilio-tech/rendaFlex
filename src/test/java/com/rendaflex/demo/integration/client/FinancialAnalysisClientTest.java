package com.rendaflex.demo.integration.client;

import com.rendaflex.demo.config.PythonServiceProperties;
import com.rendaflex.demo.dto.internal.InternalFinancialAnalysisRequest;
import com.rendaflex.demo.dto.internal.InternalFinancialAnalysisResponse;
import com.rendaflex.demo.enums.RecommendationPriority;
import com.rendaflex.demo.enums.SavingFrequency;
import com.rendaflex.demo.exception.ApiErrorCode;
import com.rendaflex.demo.exception.BusinessRuleException;
import com.rendaflex.demo.exception.ModelServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FinancialAnalysisClientTest {

    private static final String BASE_URL = "http://python.test";
    private static final String ENDPOINT = BASE_URL + FinancialAnalysisClient.FINANCIAL_ANALYSIS_PATH;

    private MockRestServiceServer server;
    private FinancialAnalysisClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();

        PythonServiceProperties properties = new PythonServiceProperties();
        properties.setBaseUrl(URI.create(BASE_URL));
        properties.setMaximumRetries(1);

        client = new FinancialAnalysisClient(builder.build(), properties);
    }

    @Test
    void shouldCallInternalFinancialAnalysisEndpointAndDeserializeResponse() {
        server.expect(requestTo(ENDPOINT))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedRequestJson()))
                .andRespond(withSuccess(validResponseJson(), MediaType.APPLICATION_JSON));

        InternalFinancialAnalysisResponse response = client.analyze(request());

        assertThat(response.financialProfile().name()).isEqualTo("HEALTHY");
        assertThat(response.probability()).isEqualByComparingTo("0.91");
        assertThat(response.metrics().monthlyReference()).isEqualByComparingTo("3300.0");
        assertThat(response.metrics().monthlyCommitments()).isEqualByComparingTo("1500.0");
        assertThat(response.metrics().availableMargin()).isEqualByComparingTo("1800.0");
        assertThat(response.classifiedTransactions()).hasSize(1);
        assertThat(response.categoryPercentages()).containsEntry(
                com.rendaflex.demo.enums.TransactionCategory.TRANSPORT,
                new BigDecimal("1.0")
        );
        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().get(0).priority()).isEqualTo(RecommendationPriority.HIGH);
        assertThat(response.recommendations().get(0).message())
                .isEqualTo("Revise os compromissos mensais antes de assumir novas despesas.");
        server.verify();
    }

    @Test
    void shouldRetryOnceFor503AndThenReturnSuccess() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess(validResponseJson(), MediaType.APPLICATION_JSON));

        InternalFinancialAnalysisResponse response = client.analyze(request());

        assertThat(response.financialProfile().name()).isEqualTo("HEALTHY");
        server.verify();
    }

    @Test
    void shouldRetryTimeoutOnceAndThenMapToTimeout() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withException(new SocketTimeoutException("timeout")));
        server.expect(requestTo(ENDPOINT))
                .andRespond(withException(new SocketTimeoutException("timeout")));

        assertThatThrownBy(() -> client.analyze(request()))
                .isInstanceOfSatisfying(ModelServiceException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ApiErrorCode.MODEL_SERVICE_TIMEOUT)
                );

        server.verify();
    }

    @Test
    void shouldRetryConnectionFailureOnceAndThenMapToUnavailable() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withException(new ConnectException("connection refused")));
        server.expect(requestTo(ENDPOINT))
                .andRespond(withException(new ConnectException("connection refused")));

        assertThatThrownBy(() -> client.analyze(request()))
                .isInstanceOfSatisfying(ModelServiceException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ApiErrorCode.MODEL_SERVICE_UNAVAILABLE)
                );

        server.verify();
    }

    @Test
    void shouldNotRetry500AndShouldMapToUnavailable() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.analyze(request()))
                .isInstanceOfSatisfying(ModelServiceException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ApiErrorCode.MODEL_SERVICE_UNAVAILABLE)
                );

        server.verify();
    }

    @Test
    void shouldMap400ToInvalidResponseWithoutRetry() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> client.analyze(request()))
                .isInstanceOfSatisfying(ModelServiceException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ApiErrorCode.MODEL_SERVICE_INVALID_RESPONSE)
                );

        server.verify();
    }

    @Test
    void shouldMap422ToPublicBusinessRuleWithoutRetry() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_CONTENT));

        assertThatThrownBy(() -> client.analyze(request()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Não foi possível concluir a análise financeira com os dados informados.");

        server.verify();
    }

    @Test
    void shouldMapMalformed200ResponseToInvalidResponse() {
        server.expect(requestTo(ENDPOINT))
                .andRespond(withSuccess("{not-valid-json", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.analyze(request()))
                .isInstanceOfSatisfying(ModelServiceException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo(ApiErrorCode.MODEL_SERVICE_INVALID_RESPONSE)
                );

        server.verify();
    }

    @Test
    void shouldRejectNullRequestBeforeCallingPython() {
        assertThatThrownBy(() -> client.analyze(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("InternalFinancialAnalysisRequest must not be null.");

        server.verify();
    }

    private InternalFinancialAnalysisRequest request() {
        return new InternalFinancialAnalysisRequest(
                List.of(
                        new BigDecimal("3200.00"),
                        new BigDecimal("3400.00"),
                        new BigDecimal("3300.00")
                ),
                new BigDecimal("600.00"),
                new BigDecimal("900.00"),
                SavingFrequency.OFTEN,
                List.of(new com.rendaflex.demo.dto.internal.InternalExpenseTransaction(
                        0,
                        "Uber",
                        new BigDecimal("51.00")
                ))
        );
    }

    private String expectedRequestJson() {
        return """
                {
                  "incomeHistory": [3200.00, 3400.00, 3300.00],
                  "monthlyDebtPayments": 600.00,
                  "otherFixedMonthlyExpenses": 900.00,
                  "savingFrequency": "OFTEN",
                  "transactions": [
                    {
                      "sourceIndex": 0,
                      "description": "Uber",
                      "amount": 51.00
                    }
                  ]
                }
                """;
    }

    private String validResponseJson() {
        return """
                {
                  "financialProfile": "HEALTHY",
                  "probability": 0.91,
                  "metrics": {
                    "averageIncome": 3300.0,
                    "incomeVariationCoefficient": 0.0247,
                    "debtRatio": 0.1818,
                    "fixedCommitment": 0.4545,
                    "monthlyReference": 3300.0,
                    "monthlyCommitments": 1500.0,
                    "availableMargin": 1800.0
                  },
                  "classifiedTransactions": [
                    {
                      "sourceIndex": 0,
                      "predictedCategory": "TRANSPORT",
                      "classificationProbability": 0.98
                    }
                  ],
                  "categorySummary": {
                    "TRANSPORT": 51.0
                  },
                  "categoryPercentages": {
                    "TRANSPORT": 1.0
                  },
                  "recommendations": [
                    {
                      "priority": "HIGH",
                      "message": "Revise os compromissos mensais antes de assumir novas despesas."
                    }
                  ]
                }
                """;
    }
}
