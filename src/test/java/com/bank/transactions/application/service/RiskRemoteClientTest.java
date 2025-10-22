package com.bank.transactions.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bank.transactions.config.LogContext;
import java.math.BigDecimal;
import java.net.URI;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class RiskRemoteClientTest {

  private WebClient.RequestHeadersUriSpec uriSpec;
  private WebClient.RequestHeadersSpec headersSpec;
  private WebClient.ResponseSpec responseSpec;
  private WebClient webClient;
  private RiskService legacyRiskService;
  private LogContext logContext;
  private RiskRemoteClient riskRemoteClient;

  @BeforeEach
  void setup() {
    // ⚙️ Crear mocks con tipos "raw" (sin <?>)
    uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
    headersSpec = mock(WebClient.RequestHeadersSpec.class);
    responseSpec = mock(WebClient.ResponseSpec.class);
    webClient = mock(WebClient.class);
    legacyRiskService = mock(RiskService.class);
    logContext = mock(LogContext.class);

    riskRemoteClient = new RiskRemoteClient(webClient, logContext, legacyRiskService);

    // ⚙️ Configurar la cadena completa WebClient -> uri -> retrieve -> bodyToMono
    when(webClient.get()).thenReturn(uriSpec);
    when(uriSpec.uri(any(Function.class))).thenReturn(headersSpec);
    when(headersSpec.retrieve()).thenReturn(responseSpec);
  }

  @Test
  void isAllowed_success_executesUriBuilder() {
    when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.just(true));

    // Ejecutar el builder URI para cubrir el código de queryParam(...)
    when(uriSpec.uri(any(Function.class))).thenAnswer(invocation -> {
      Function<UriBuilder, URI> func = invocation.getArgument(0);
      URI built = func.apply(new DefaultUriBuilderFactory().builder());
      assert built.toString().contains("/allow");
      assert built.toString().contains("currency=USD");
      assert built.toString().contains("amount=100");
      return headersSpec;
    });

    StepVerifier.create(riskRemoteClient.isAllowed("USD", "DEBIT", new BigDecimal("100")))
      .expectNext(true)
      .verifyComplete();

    verify(responseSpec, times(1)).bodyToMono(Boolean.class);
  }

  @Test
  void fallback_usesLegacyService() {
    when(legacyRiskService.isAllowedLegacy(anyString(), anyString(), any()))
      .thenReturn(false);

    StepVerifier.create(riskRemoteClient.fallback("USD", "DEBIT", new BigDecimal("100"),
        new RuntimeException("Simulated")))
      .expectNext(false)
      .verifyComplete();

    verify(legacyRiskService, times(1)).isAllowedLegacy("USD", "DEBIT", new BigDecimal("100"));
  }
}
