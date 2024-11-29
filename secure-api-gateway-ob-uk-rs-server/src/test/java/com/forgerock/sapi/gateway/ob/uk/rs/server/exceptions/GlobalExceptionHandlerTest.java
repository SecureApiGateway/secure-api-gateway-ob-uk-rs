/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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

import static com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory.requiredAccountConsentApiHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;

import uk.org.openbanking.datamodel.error.OBStandardErrorCodes1;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.error.OBErrorResponse1;

/**
 * Tests for the GlobalExceptionHandler, uses the {@link com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1_10.accounts.AccountAccessConsentsApi}
 * as the endpoint to test the cross-cutting error handling done by the GlobalExceptionHandler.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    public static final HttpHeaders REQUIRED_ACCOUNT_CONSENT_API_HEADERS = requiredAccountConsentApiHeaders("client-123");
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
        assertThat(firstError.getMessage()).isEqualTo("Required request header 'Authorization' for method parameter type String is not present");
    }

    @Test
    void testErrorDueToMethodNotSupported() {
        final ResponseEntity<OBErrorResponse1> errorResponse = restTemplate.exchange(getAccountAccessConsentApiUri("12234"),
                HttpMethod.PUT, new HttpEntity<>(REQUIRED_ACCOUNT_CONSENT_API_HEADERS), OBErrorResponse1.class);

        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        final OBErrorResponse1 errorResponseBody = errorResponse.getBody();
        assertThat(errorResponseBody.getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(errorResponseBody.getErrors()).hasSize(1);
        final OBError1 firstError = errorResponseBody.getErrors().get(0);
        assertThat(firstError.getErrorCode()).isEqualTo(OBRIErrorType.REQUEST_METHOD_NOT_SUPPORTED.getCode().toString());
        assertThat(firstError.getMessage()).startsWith("Method 'PUT' is not supported for this request");
    }

    @Test
    void testMediaTypeNotSupported() {
        final HttpHeaders headers = new HttpHeaders(REQUIRED_ACCOUNT_CONSENT_API_HEADERS);
        headers.setContentType(MediaType.APPLICATION_XML);
        final ResponseEntity<OBErrorResponse1> errorResponse = restTemplate.exchange(accountAccessConsentApiUri(),
                HttpMethod.POST, new HttpEntity<>(headers), OBErrorResponse1.class);

        assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);

        final OBErrorResponse1 errorResponseBody = errorResponse.getBody();
        assertThat(errorResponseBody.getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(errorResponseBody.getErrors()).hasSize(1);
        final OBError1 firstError = errorResponseBody.getErrors().get(0);
        assertThat(firstError.getErrorCode()).isEqualTo(OBRIErrorType.REQUEST_MEDIA_TYPE_NOT_SUPPORTED.getCode().toString());
        assertThat(firstError.getMessage())
                .isEqualTo("Media type 'application/xml' is not supported for this request. " +
                        "Supported media type are 'application/json;charset=utf-8' 'application/jose+jwe' ");
    }
}