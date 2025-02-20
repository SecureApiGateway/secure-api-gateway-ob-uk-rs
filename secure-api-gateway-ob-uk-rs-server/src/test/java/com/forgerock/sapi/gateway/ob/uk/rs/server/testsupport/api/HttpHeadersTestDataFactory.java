/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api;

import static java.util.Collections.singletonList;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.ApiConstants;

/**
 * A test data factory for {@link HttpHeaders} that are sent in each HTTP request.
 */
public class HttpHeadersTestDataFactory {

    private static final DateTimeFormatter FAPI_AUTH_DATE_FORMATTER = DateTimeFormatter.ofPattern(ApiConstants.HTTP_DATE_FORMAT);

    public static HttpHeaders requiredAccountConsentApiHeaders(String apiClientId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("dummyAuthToken");
        headers.add("x-fapi-interaction-id", UUID.randomUUID().toString());
        headers.add("x-api-client-id", apiClientId);
        headers.add("x-fapi-auth-date", FAPI_AUTH_DATE_FORMATTER.format(ZonedDateTime.now()));
        return headers;
    }


    /**
     * Provides an instance of {@link HttpHeaders} with the minimal set of required headers for the Accounts API.
     *
     * @return the {@link HttpHeaders} instance.
     */
    public static HttpHeaders requiredAccountApiHeaders(String consentId, String apiClientId) {
        return requiredAccountApiHeaders(consentId, apiClientId, MediaType.APPLICATION_JSON);
    }

    /**
     * Provides an instance of {@link HttpHeaders} with the minimal set of required headers for the Accounts API.
     *
     * @return the {@link HttpHeaders} instance.
     */
    public static HttpHeaders requiredAccountApiHeaders(String consentId, String apiClientId, MediaType acceptHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(acceptHeader));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("dummyAuthToken");
        headers.add("x-fapi-interaction-id", UUID.randomUUID().toString());
        headers.add("x-api-client-id", apiClientId);
        headers.add("x-intent-id", consentId);
        return headers;
    }

    /**
     * @return an instance of {@link HttpHeaders} with the minimal set of required headers for the Payments API.
     */
    public static HttpHeaders requiredPaymentHttpHeaders(String apiClientId) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("dummyAuthToken");
        headers.add("x-fapi-interaction-id", UUID.randomUUID().toString());
        headers.add("x-idempotency-key", UUID.randomUUID().toString());
        headers.add("x-jws-signature", "dummyJwsSignature");
        headers.add("x-api-client-id", apiClientId);

        return headers;
    }

    public static HttpHeaders requiredFundsConsentApiHeaders(String apiClientId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("dummyAuthToken");
        headers.add("x-fapi-interaction-id", UUID.randomUUID().toString());
        headers.add("x-api-client-id", apiClientId);
        return headers;
    }

    /**
     * Provides an instance of {@link HttpHeaders} with the minimal set of required headers for the Events API.
     *
     * @param resourceUrl The URL to retrieve the resource in question.
     * @param tppId The ID of the TPP in question.
     * @return the {@link HttpHeaders} instance.
     */
    public static HttpHeaders requiredEventHttpHeaders(String resourceUrl, String tppId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("dummyAuthToken");
        headers.add("x-fapi-interaction-id", UUID.randomUUID().toString());
        headers.add("x-ob-tpp-id", tppId);
        headers.add("x-ob-url", resourceUrl);
        return headers;
    }

    /**
     * Provides an instance of {@link HttpHeaders} with the minimal set of required headers for the Events API.
     *
     * @param apiClientId The ID of the TPP in question.
     * @return the {@link HttpHeaders} instance.
     */
    public static HttpHeaders requiredEventNotificationsHttpHeaders(String apiClientId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("dummyAuthToken");
        headers.add("x-fapi-interaction-id", UUID.randomUUID().toString());
        headers.add("x-api-client-id", apiClientId);
        return headers;
    }

    public static HttpHeaders requiredFundsHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("dummyAuthToken");
        headers.add("x-fapi-interaction-id", UUID.randomUUID().toString());
        return headers;
    }

    public static HttpHeaders requiredFundsHttpHeadersWithApiClientId(String apiClientId) {
        final HttpHeaders httpHeaders = requiredFundsHttpHeaders();
        httpHeaders.add("x-api-client-id", apiClientId);
        return httpHeaders;
    }
}
