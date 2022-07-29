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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.obie.OBVersion;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.share.IntentType;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorResponseCategory;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.payment.*;

import java.net.URI;

import static com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.CalculateResponseElementsController.*;
import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.api.HttpHeadersTestDataFactory.requiredBackofficeHttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent3;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4;

/**
 * Unit test for {@link CalculateResponseElementsController}
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class CalculateResponseElementsControllerTest {

    private static final HttpHeaders HTTP_HEADERS = requiredBackofficeHttpHeaders();
    private static final String BASE_URL = "http://localhost:";

    private static final String REST_CONTEXT = "/backoffice/payment-consent/calculate-elements";
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    /*
    Domestic payments v3.1.2
     */
    @Test
    public void shouldCalculateResponseElements4PDC_v3_1_2() throws JsonProcessingException {
        String consentId = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();
        OBWriteDomesticConsent3 consentRequest = aValidOBWriteDomesticConsent3();

        // When
        ResponseEntity<OBWriteDomesticConsentResponse3> response = restTemplate.exchange(
                getUri(consentId, OBVersion.v3_1_2.getCanonicalName()),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteDomesticConsentResponse3.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getInitiation()).isEqualTo(consentRequest.getData().getInitiation());
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    /*
    Domestic payments v3.1.4
     */
    @Test
    public void shouldCalculateResponseElements4PDC_v3_1_4() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();
        OBWriteDomesticConsent4 consentRequest = aValidOBWriteDomesticConsent4();

        // When
        ResponseEntity<OBWriteDomesticConsentResponse4> response = restTemplate.exchange(
                getUri(intent, OBVersion.v3_1_4.getCanonicalName()),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteDomesticConsentResponse4.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getInitiation()).isEqualTo(consentRequest.getData().getInitiation());
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    /*
    Domestic payments v3.1.8
     */
    @Test
    public void shouldCalculateResponseElements4PDC_v3_1_8() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();
        OBWriteDomesticConsent4 consentRequest = aValidOBWriteDomesticConsent4();

        // When
        ResponseEntity<OBWriteDomesticConsentResponse5> response = restTemplate.exchange(
                getUri(intent, OBVersion.v3_1_8.getCanonicalName()),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteDomesticConsentResponse5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getInitiation()).isEqualTo(consentRequest.getData().getInitiation());
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    @Test
    public void cannotDetermineTheVersion() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();
        OBWriteDomesticConsent4 consentRequest = aValidOBWriteDomesticConsent4();

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri(intent, "v99.00.334"),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(response.getBody().getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        OBError1 error = response.getBody().getErrors().get(0);
        assertThat(error.getErrorCode()).isEqualTo(OBRIErrorType.DATA_INVALID_REQUEST.getCode().getValue());
        assertThat(error.getMessage()).contains(String.format("It has not been possible to determine the value of '%s'", API_VERSION_DESCRIPTION));
    }

    @Test
    public void cannotDetermineTheIntentType() throws JsonProcessingException {
        OBWriteDomesticConsent4 consentRequest = aValidOBWriteDomesticConsent4();

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri("WRONG_INTENT", OBVersion.v3_1_8.getCanonicalName()),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(response.getBody().getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        OBError1 error = response.getBody().getErrors().get(0);
        assertThat(error.getErrorCode()).isEqualTo(OBRIErrorType.DATA_INVALID_REQUEST.getCode().getValue());
        assertThat(error.getMessage()).contains(String.format("It has not been possible to determine the value of '%s'", INTENT_TYPE_DESCRIPTION));
    }

    @Test
    public void shouldFailsInstructedAmount() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();
        OBWriteDomesticConsent4 consentRequest = aValidOBWriteDomesticConsent4();
        consentRequest.getData().getInitiation().setInstructedAmount(null);
        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri(intent, OBVersion.v3_1_8.getCanonicalName()),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(response.getBody().getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        OBError1 error = response.getBody().getErrors().get(0);
        assertThat(error.getErrorCode()).isEqualTo(OBRIErrorType.DATA_INVALID_REQUEST.getCode().getValue());
        assertThat(error.getMessage()).contains(String.format("'%s' cannot be null to be validate", "InstructedAmount"));
    }

    @Test
    public void shouldFailsInstructedAmountAmount() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();
        OBWriteDomesticConsent4 consentRequest = aValidOBWriteDomesticConsent4();
        consentRequest.getData().getInitiation().getInstructedAmount().setAmount("0");
        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri(intent, OBVersion.v3_1_8.getCanonicalName()),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(response.getBody().getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        OBError1 error = response.getBody().getErrors().get(0);
        assertThat(error.getErrorCode()).isEqualTo(OBRIErrorType.DATA_INVALID_REQUEST.getCode().getValue());
        assertThat(error.getMessage()).contains(
                String.format("The amount %s provided must be greater than 0", consentRequest.getData().getInitiation().getInstructedAmount().getAmount())
        );
    }

    @Test
    public void shouldFailsInstructedAmountCurrency() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();
        OBWriteDomesticConsent4 consentRequest = aValidOBWriteDomesticConsent4();
        consentRequest.getData().getInitiation().getInstructedAmount().setCurrency("BITCOIN");
        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri(intent, OBVersion.v3_1_8.getCanonicalName()),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(response.getBody().getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        OBError1 error = response.getBody().getErrors().get(0);
        assertThat(error.getErrorCode()).isEqualTo(OBRIErrorType.DATA_INVALID_REQUEST.getCode().getValue());
        assertThat(error.getMessage()).contains(
                String.format("The currency %s provided is not supported.", consentRequest.getData().getInitiation().getInstructedAmount().getCurrency())
        );
    }

    private URI getUri(String intent, String version) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + port + REST_CONTEXT);
        builder.queryParam("intent", intent);
        builder.queryParam("version", version);
        return builder.build().encode().toUri();
    }
}
