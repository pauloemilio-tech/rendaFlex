package com.rendaflex.demo.integration;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class FinancialAnalysisEndToEndTest {

    private static final String INTERNAL_PATH = "/internal/v1/financial-analyses";

    private static final String SUCCESS_RESPONSE = """
            {
              "financialProfile": "HEALTHY",
              "probability": 0.91,
              "metrics": {
                "averageIncome": 3300.00,
                "incomeVariationCoefficient": 0.0247,
                "debtRatio": 0.1818,
                "fixedCommitment": 0.4545,
                "monthlyReference": 3300.00,
                "monthlyCommitments": 1500.00,
                "availableMargin": 1800.00
              },
              "classifiedTransactions": [
                {
                  "sourceIndex": 1,
                  "predictedCategory": "TRANSPORT",
                  "classificationProbability": 0.98
                }
              ],
              "categorySummary": {
                "TRANSPORT": 51.00
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

    private static final String MODEL_ERROR_RESPONSE = """
            {
              "code": "MODEL_INTERNAL_ERROR",
              "message": "Internal model failure.",
              "fieldErrors": []
            }
            """;

    private static final AtomicReference<String> LAST_INTERNAL_REQUEST = new AtomicReference<>();
    private static final AtomicInteger INTERNAL_REQUEST_COUNT = new AtomicInteger();
    private static final AtomicInteger STUB_STATUS = new AtomicInteger(200);
    private static final AtomicReference<String> STUB_BODY = new AtomicReference<>(SUCCESS_RESPONSE);

    private static final HttpServer PYTHON_STUB = startPythonStub();

    @Autowired
    private WebApplicationContext applicationContext;

    private MockMvc mockMvc;

    @DynamicPropertySource
    static void pythonServiceProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "rendaflex.python.base-url",
                () -> "http://127.0.0.1:" + PYTHON_STUB.getAddress().getPort()
        );
    }

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(applicationContext).build();
        LAST_INTERNAL_REQUEST.set(null);
        INTERNAL_REQUEST_COUNT.set(0);
        STUB_STATUS.set(200);
        STUB_BODY.set(SUCCESS_RESPONSE);
    }

    @AfterAll
    static void stopPythonStub() {
        PYTHON_STUB.stop(0);
    }

    @Test
    void shouldCompleteFinancialAnalysisThroughRealSpringHttpBoundary() throws Exception {
        mockMvc.perform(post("/api/v1/financial-analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publicRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.financialProfile").value("HEALTHY"))
                .andExpect(jsonPath("$.probability").value(0.91))
                .andExpect(jsonPath("$.metrics.averageIncome").value(3300.00))
                .andExpect(jsonPath("$.metrics.incomeVariationCoefficientPercentage").value(2.47))
                .andExpect(jsonPath("$.metrics.debtRatioPercentage").value(18.18))
                .andExpect(jsonPath("$.metrics.fixedCommitmentPercentage").value(45.45))
                .andExpect(jsonPath("$.metrics.monthlyReference").value(3300.00))
                .andExpect(jsonPath("$.metrics.monthlyCommitments").value(1500.00))
                .andExpect(jsonPath("$.metrics.availableMargin").value(1800.00))
                .andExpect(jsonPath("$.classifiedTransactions[0].description").value("Client payment"))
                .andExpect(jsonPath("$.classifiedTransactions[0].type").value("INCOME"))
                .andExpect(jsonPath("$.classifiedTransactions[0].predictedCategory").doesNotExist())
                .andExpect(jsonPath("$.classifiedTransactions[0].classificationProbability").doesNotExist())
                .andExpect(jsonPath("$.classifiedTransactions[1].description").value("Uber"))
                .andExpect(jsonPath("$.classifiedTransactions[1].type").value("EXPENSE"))
                .andExpect(jsonPath("$.classifiedTransactions[1].predictedCategory").value("TRANSPORT"))
                .andExpect(jsonPath("$.classifiedTransactions[1].classificationProbability").value(0.98))
                .andExpect(jsonPath("$.categorySummary.TRANSPORT").value(51.00))
                .andExpect(jsonPath("$.categoryPercentages.TRANSPORT").value(100.00))
                .andExpect(jsonPath("$.recommendations[0].priority").value("HIGH"))
                .andExpect(jsonPath("$.recommendations[0].message")
                        .value("Revise os compromissos mensais antes de assumir novas despesas."));

        String internalRequest = LAST_INTERNAL_REQUEST.get();

        assertNotNull(internalRequest);
        assertEquals(1, INTERNAL_REQUEST_COUNT.get());
        assertTrue(internalRequest.contains("\"sourceIndex\":1"));
        assertTrue(internalRequest.contains("\"description\":\"Uber\""));
        assertFalse(internalRequest.contains("Client payment"));
        assertTrue(internalRequest.contains("\"savingFrequency\":\"OFTEN\""));
    }

    @Test
    void shouldMapPythonUnavailabilityToSafePublicErrorAfterRetry() throws Exception {
        STUB_STATUS.set(503);
        STUB_BODY.set(MODEL_ERROR_RESPONSE);

        mockMvc.perform(post("/api/v1/financial-analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publicRequest()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("MODEL_SERVICE_UNAVAILABLE"))
                .andExpect(jsonPath("$.message")
                        .value("O serviço de análise financeira está temporariamente indisponível."));

        assertEquals(2, INTERNAL_REQUEST_COUNT.get());
    }

    private static HttpServer startPythonStub() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext(INTERNAL_PATH, FinancialAnalysisEndToEndTest::handlePythonRequest);
            server.start();
            return server;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static void handlePythonRequest(HttpExchange exchange) throws IOException {
        try (exchange) {
            INTERNAL_REQUEST_COUNT.incrementAndGet();

            String requestBody = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            );
            LAST_INTERNAL_REQUEST.set(requestBody);

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            byte[] responseBody = STUB_BODY.get().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(STUB_STATUS.get(), responseBody.length);
            exchange.getResponseBody().write(responseBody);
        }
    }

    private String publicRequest() {
        return """
                {
                  "incomeHistory": [
                    {"month": "2026-03", "amount": 3200.00},
                    {"month": "2026-04", "amount": 3400.00},
                    {"month": "2026-05", "amount": 3300.00}
                  ],
                  "monthlyDebtPayments": 600.00,
                  "otherFixedMonthlyExpenses": 900.00,
                  "savingFrequency": "OFTEN",
                  "transactions": [
                    {
                      "description": "Client payment",
                      "amount": 900.00,
                      "date": "2026-05-20",
                      "type": "INCOME"
                    },
                    {
                      "description": "Uber",
                      "amount": 51.00,
                      "date": "2026-05-10",
                      "type": "EXPENSE"
                    }
                  ]
                }
                """;
    }
}
