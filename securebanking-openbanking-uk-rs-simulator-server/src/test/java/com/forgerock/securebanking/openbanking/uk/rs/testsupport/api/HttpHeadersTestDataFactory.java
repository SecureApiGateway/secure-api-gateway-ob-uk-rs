/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.testsupport.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

/**
 * A test data factory for {@link HttpHeaders} that are sent in each HTTP request.
 */
public class HttpHeadersTestDataFactory {

    private static final String ALL_NON_BASIC_PERMISSIONS = Arrays.stream(OBExternalPermissions1Code.values())
            .map(Enum::name)
            .filter(name -> !name.endsWith("BASIC"))
            .collect(Collectors.joining(","));

    /**
     * Provides an instance of {@link HttpHeaders} with the minimal set of required headers for the Accounts API.
     *
     * @param resourceUrl The URL to retrieve the resource in question.
     * @param accountId The ID of the account to be returned.
     * @return the {@link HttpHeaders} instance.
     */
    public static HttpHeaders requiredAccountHttpHeaders(String resourceUrl, String accountId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("dummyAuthToken");
        headers.add("x-fapi-financial-id", UUID.randomUUID().toString());
        headers.add("x-idempotency-key", UUID.randomUUID().toString());
        headers.add("x-fapi-interaction-id", UUID.randomUUID().toString());
        headers.add("x-ob-url", resourceUrl);
        headers.add("x-ob-permissions", ALL_NON_BASIC_PERMISSIONS);
        headers.add("x-ob-account-ids", accountId);
        return headers;
    }

    /**
     * Provides an instance of {@link HttpHeaders} with the minimal set of required headers for the Accounts API.
     *
     * @param resourceUrl The URL to retrieve the resource in question.
     * @param acceptHeader The value to set 'Accept' header.
     *
     * @return the {@link HttpHeaders} instance.
     */
    public static HttpHeaders requiredAccountStatementFileHttpHeaders(String resourceUrl, MediaType acceptHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(acceptHeader));
        headers.setBearerAuth("dummyAuthToken");
        headers.add("x-fapi-financial-id", UUID.randomUUID().toString());
        headers.add("x-idempotency-key", UUID.randomUUID().toString());
        headers.add("x-fapi-interaction-id", UUID.randomUUID().toString());
        headers.add("x-ob-url", resourceUrl);
        headers.add("x-ob-permissions", ALL_NON_BASIC_PERMISSIONS);
        return headers;
    }

    /**
     * @return an instance of {@link HttpHeaders} with the minimal set of required headers for the Payments API.
     */
    public static HttpHeaders requiredPaymentHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("dummyAuthToken");
        headers.add("x-fapi-financial-id", UUID.randomUUID().toString());
        headers.add("x-fapi-interaction-id", UUID.randomUUID().toString());
        headers.add("x-idempotency-key", UUID.randomUUID().toString());
        headers.add("x-jws-signature", "dummyJwsSignature");
        headers.add("x-ob-account-id", UUID.randomUUID().toString());
        return headers;
    }

    /**
     * @return an instance of {@link HttpHeaders} with the minimal set of required headers for the Payment Funds Confirmation API.
     */
    public static HttpHeaders requiredPaymentFundsConfirmationHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("dummyAuthToken");
        headers.add("x-fapi-financial-id", UUID.randomUUID().toString());
        headers.add("x-fapi-interaction-id", UUID.randomUUID().toString());
        headers.add("x-idempotency-key", UUID.randomUUID().toString());
        headers.add("x-ob-account-id", UUID.randomUUID().toString());
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
        headers.add("x-fapi-financial-id", UUID.randomUUID().toString());
        headers.add("x-fapi-interaction-id", UUID.randomUUID().toString());
        headers.add("x-ob-tpp-id", tppId);
        headers.add("x-ob-url", resourceUrl);
        return headers;
    }

    public static HttpHeaders requiredFundsHttpHeaders(String resourceUrl, String accountId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("dummyAuthToken");
        headers.add("x-fapi-financial-id", UUID.randomUUID().toString());
        headers.add("x-fapi-interaction-id", UUID.randomUUID().toString());
        headers.add("x-account-id", accountId);
        headers.add("x-ob-url", resourceUrl);
        return headers;
    }

    /**
     * @return an instance of {@link HttpHeaders} with the minimal set of required headers for the Payments API.
     */
    public static HttpHeaders requiredVrpPaymentHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("dummyAuthToken");
        headers.add("x-fapi-financial-id", UUID.randomUUID().toString());
        headers.add("x-fapi-interaction-id", UUID.randomUUID().toString());
        headers.add("x-idempotency-key", UUID.randomUUID().toString());
        headers.add("x-jws-signature", "dummyJwsSignature");
        headers.add("x-ob-account-id", UUID.randomUUID().toString());
        return headers;
    }

    /**
     * @return an instance of {@link HttpHeaders} with the minimal set of required headers for backoffice payments API.
     */
    public static HttpHeaders requiredBackofficeHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-fapi-financial-id", UUID.randomUUID().toString());
        headers.add("x-fapi-interaction-id", UUID.randomUUID().toString());
        return headers;
    }
}
