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
package com.forgerock.sapi.gateway.ob.uk.rs.server.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;

import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.error.OBStandardErrorCodes1;

/**
 * Tests for the GlobalExceptionHandler, uses the {@link com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1_10.accounts.AccountAccessConsentsApi}
 * as the endpoint to test the cross-cutting error handling done by the GlobalExceptionHandler.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")

class GlobalExceptionHandlerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String accountAccessConsentApiUri() {
        return "http://localhost:" + port + "/open-banking/v3.1.10/aisp/account-access-consents";
    }

    private String getAccountAccessConsentApiUri(String consentId) {
        return accountAccessConsentApiUri() + "/" + consentId;
    }

    @Test
    void testErrorDueToMissingHeader() {
        final HttpEntity<Object> requestWithNoHeaders = new HttpEntity<>(null);
        final ResponseEntity<OBErrorResponse1> errorResponse = restTemplate.exchange(getAccountAccessConsentApiUri("12234"),
                HttpMethod.GET, requestWithNoHeaders, OBErrorResponse1.class);

        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        final OBErrorResponse1 errorResponseBody = errorResponse.getBody();
        assertThat(errorResponseBody.getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(errorResponseBody.getErrors()).hasSize(1);
        final OBError1 firstError = errorResponseBody.getErrors().get(0);
        assertThat(firstError.getErrorCode()).isEqualTo(OBStandardErrorCodes1.UK_OBIE_HEADER_MISSING.toString());
        assertThat(firstError.getMessage()).isEqualTo("Missing request header 'Authorization' for method parameter of type String");
    }
}