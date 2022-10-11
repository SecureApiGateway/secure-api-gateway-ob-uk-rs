/*
 * Copyright © 2020-2022 ForgeRock AS (obst@forgerock.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgerock.securebanking.rs.platform.client.services;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.share.IntentType;
import com.forgerock.securebanking.rs.platform.client.configuration.ConfigurationPropertiesClient;
import com.forgerock.securebanking.rs.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.rs.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.rs.platform.client.model.ClientRequest;
import com.forgerock.securebanking.rs.platform.client.test.support.DomesticPaymentConsentDetailsTestFactory;
import com.forgerock.securebanking.rs.platform.client.utils.url.UrlContext;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;

/**
 * Unit test for {@link CloudPlatformClientService}
 */
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PlatformClientServiceTest {

    @InjectMocks
    private CloudPlatformClientService consentService;

    @Mock
    protected ConfigurationPropertiesClient configurationPropertiesClient;

    @Mock
    protected RestTemplate restTemplate;

    protected MockedStatic<UrlContext> urlContextMockedStatic;

    @BeforeEach
    public void setup() {
        urlContextMockedStatic = Mockito.mockStatic(UrlContext.class);
        urlContextMockedStatic.when(
                () -> UrlContext.replaceParameterContextIntentId(anyString(), anyString())
        ).thenReturn("http://a.domain/context/intent-id-xxxx");
    }

    @AfterEach
    public void close() {
        urlContextMockedStatic.close();
    }

    @Test
    public void shouldGetIntentFromPlatform() throws ExceptionClient {
        // Given
        ClientRequest clientRequest = ClientRequest.builder()
                .intentId(IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId())
                .apiClientId(UUID.randomUUID().toString())
                .build();
        JsonObject intentResponse = DomesticPaymentConsentDetailsTestFactory.aValidDomesticPaymentConsentDetails(
                clientRequest.getIntentId(),
                clientRequest.getApiClientId()
        );
        when(restTemplate.exchange(
                        anyString(),
                        eq(GET),
                        isNull(),
                        eq(String.class)
                )
        ).thenReturn(ResponseEntity.ok(intentResponse.toString()));

        // When
        JsonObject idmIntent = consentService.getIntentAsJsonObject(clientRequest);

        // Then
        assertThat(idmIntent).isNotNull();
        assertThat(idmIntent).isEqualTo(intentResponse.getAsJsonObject("OBIntentObject"));
    }

    @Test
    public void shouldGetInvalidRequestConsentDetails() {
        // Given
        ClientRequest clientRequest = ClientRequest.builder()
                .intentId(IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId())
                .apiClientId(UUID.randomUUID().toString())
                .build();
        JsonObject intentResponse = DomesticPaymentConsentDetailsTestFactory.aValidDomesticPaymentConsentDetails(
                clientRequest.getIntentId(),
                UUID.randomUUID().toString()
        );
        when(restTemplate.exchange(
                        anyString(),
                        eq(GET),
                        isNull(),
                        eq(String.class)
                )
        ).thenReturn(ResponseEntity.ok(intentResponse.toString()));

        // When
        ExceptionClient exception = catchThrowableOfType(() -> consentService.getIntentAsJsonObject(clientRequest), ExceptionClient.class);

        // Then
        assertThat(exception.getErrorClient().getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST);
        assertThat(exception.getErrorClient().getErrorType().getErrorCode()).isEqualTo(ErrorType.INVALID_REQUEST.getErrorCode());
        assertThat(exception.getErrorClient().getErrorType().getInternalCode()).isEqualTo(ErrorType.INVALID_REQUEST.getInternalCode());
    }

    @Test
    public void shouldGetNotFoundConsentDetails() {
        // Given
        ClientRequest clientRequest = ClientRequest.builder()
                .intentId(IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId())
                .apiClientId(UUID.randomUUID().toString())
                .build();
        when(restTemplate.exchange(
                        anyString(),
                        eq(GET),
                        isNull(),
                        eq(String.class)
                )
        ).thenReturn(null);

        // When
        ExceptionClient exception = catchThrowableOfType(() -> consentService.getIntentAsJsonObject(clientRequest), ExceptionClient.class);

        // Then
        assertThat(exception.getErrorClient().getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        assertThat(exception.getErrorClient().getErrorType().getErrorCode()).isEqualTo(ErrorType.NOT_FOUND.getErrorCode());
        assertThat(exception.getErrorClient().getErrorType().getInternalCode()).isEqualTo(ErrorType.NOT_FOUND.getInternalCode());
    }
}
