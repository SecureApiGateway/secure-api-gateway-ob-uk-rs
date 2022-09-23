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
import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.payment.*;

import java.net.URI;

import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.api.HttpHeadersTestDataFactory.requiredBackofficeHttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticScheduledConsentTestDataFactory.aValidOBWriteDomesticScheduledConsent4;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticStandingOrderConsentTestDataFactory.aValidOBWriteDomesticStandingOrderConsent5;
import static uk.org.openbanking.testsupport.payment.OBWriteInternationalConsentTestDataFactory.aValidOBWriteInternationalConsent5;
import static uk.org.openbanking.testsupport.payment.OBWriteInternationalScheduledConsentTestDataFactory.aValidOBWriteInternationalScheduledConsent5;
import static uk.org.openbanking.testsupport.payment.OBWriteInternationalStandingOrderConsentTestDataFactory.aValidOBWriteInternationalStandingOrderConsent6;


@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class CalculateResponseElementsControllerTest {

    public static final String API_VERSION_DESCRIPTION = "Api version";
    public static final String INTENT_TYPE_DESCRIPTION = "Intent type";

    private static final HttpHeaders HTTP_HEADERS = requiredBackofficeHttpHeaders();
    private static final String BASE_URL = "http://localhost:";
    private static final String INVALID_CURRENCY = "Invalid currency";
    private static final String REST_CONTEXT = "/backoffice/{0}/{1}/calculate-elements";

    private static final String PDC_CONTEXT = "domestic-payment-consents";
    private static final String PDSC_CONTEXT = "domestic-scheduled-payment-consents";
    private static final String PDSOC_CONTEXT = "domestic-standing-order-consents";
    private static final String PIC_CONTEXT = "international-payment-consents";
    private static final String PISC_CONTEXT = "international-scheduled-payment-consents";
    private static final String PISOC_CONTEXT = "international-standing-order-consents";
    private static final String PFC_CONTEXT = "file-payment-consents";
    private static final String FCC_CONTEXT = "funds-confirmation-consents";
    private static final String DVRP_CONTEXT = "domestic-vrp-consents";


    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    /*
    Domestic payments v3.1.4
     */
    @Test
    public void shouldCalculateResponseElements4PDC_v3_1_4() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();
        OBWriteDomesticConsent4 consentRequest = aValidOBWriteDomesticConsent4();

        // When
        ResponseEntity<OBWriteDomesticConsentResponse4> response = restTemplate.exchange(
                getUri(intent, OBVersion.v3_1_4.getCanonicalName(), PDC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteDomesticConsentResponse4.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getInitiation()).isEqualTo(consentRequest.getData().getInitiation());
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    @Test
    public void shouldCalculateResponseElements4PDSC_v3_1_4() throws JsonProcessingException {
        String consentId = IntentType.PAYMENT_DOMESTIC_SCHEDULED_CONSENT.generateIntentId();
        OBWriteDomesticScheduledConsent4 consentRequest = aValidOBWriteDomesticScheduledConsent4();

        // When
        ResponseEntity<OBWriteDomesticScheduledConsentResponse4> response = restTemplate.exchange(
                getUri(consentId, OBVersion.v3_1_4.getCanonicalName(), PDSC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteDomesticScheduledConsentResponse4.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mapper.writeValueAsString(response.getBody().getData().getInitiation())).isEqualTo(mapper.writeValueAsString(consentRequest.getData().getInitiation()));
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    @Test
    public void shouldCalculateResponseElements4PDSOC_v3_1_4() throws JsonProcessingException {
        String consentId = IntentType.PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT.generateIntentId();
        OBWriteDomesticStandingOrderConsent5 consentRequest = aValidOBWriteDomesticStandingOrderConsent5();

        // When
        ResponseEntity<OBWriteDomesticStandingOrderConsentResponse5> response = restTemplate.exchange(
                getUri(consentId, OBVersion.v3_1_4.getCanonicalName(), PDSOC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteDomesticStandingOrderConsentResponse5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mapper.writeValueAsString(response.getBody().getData().getInitiation())).isEqualTo(mapper.writeValueAsString(consentRequest.getData().getInitiation()));
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    @Test
    public void shouldCalculateResponseElements4PIC_v3_1_4() throws JsonProcessingException {
        String consentId = IntentType.PAYMENT_INTERNATIONAL_CONSENT.generateIntentId();
        OBWriteInternationalConsent5 consentRequest = aValidOBWriteInternationalConsent5();

        // When
        ResponseEntity<OBWriteInternationalConsentResponse5> response = restTemplate.exchange(
                getUri(consentId, OBVersion.v3_1_4.getCanonicalName(), PIC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteInternationalConsentResponse5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mapper.writeValueAsString(response.getBody().getData().getInitiation())).isEqualTo(mapper.writeValueAsString(consentRequest.getData().getInitiation()));
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    @Test
    public void shouldCalculateResponseElementsWithoutExchangeRateInformation4PIC_v3_1_4() throws JsonProcessingException {
        String consentId = IntentType.PAYMENT_INTERNATIONAL_CONSENT.generateIntentId();
        OBWriteInternationalConsent5 consentRequest = aValidOBWriteInternationalConsent5();
        consentRequest.getData().getInitiation().setExchangeRateInformation(null);

        // When
        ResponseEntity<OBWriteInternationalConsentResponse5> response = restTemplate.exchange(
                getUri(consentId, OBVersion.v3_1_4.getCanonicalName(), PIC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteInternationalConsentResponse5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getInitiation().getExchangeRateInformation()).isNotNull();
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    @Test
    public void shouldCalculateResponseElements4PISC_v3_1_4() throws JsonProcessingException {
        String consentId = IntentType.PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT.generateIntentId();
        OBWriteInternationalScheduledConsent5 consentRequest = aValidOBWriteInternationalScheduledConsent5();

        // When
        ResponseEntity<OBWriteInternationalScheduledConsentResponse5> response = restTemplate.exchange(
                getUri(consentId, OBVersion.v3_1_4.getCanonicalName(), PISC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteInternationalScheduledConsentResponse5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mapper.writeValueAsString(response.getBody().getData().getInitiation())).isEqualTo(mapper.writeValueAsString(consentRequest.getData().getInitiation()));
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    @Test
    public void shouldCalculateResponseElementsWithoutExchangeRateInformation4PISC_v3_1_4() throws JsonProcessingException {
        String consentId = IntentType.PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT.generateIntentId();
        OBWriteInternationalScheduledConsent5 consentRequest = aValidOBWriteInternationalScheduledConsent5();
        consentRequest.getData().getInitiation().setExchangeRateInformation(null);

        // When
        ResponseEntity<OBWriteInternationalScheduledConsentResponse5> response = restTemplate.exchange(
                getUri(consentId, OBVersion.v3_1_4.getCanonicalName(), PISC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteInternationalScheduledConsentResponse5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getInitiation().getExchangeRateInformation()).isNotNull();
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    @Test
    public void shouldCalculateResponseElements4PISOC_v3_1_4() throws JsonProcessingException {
        String consentId = IntentType.PAYMENT_INTERNATIONAL_STANDING_ORDERS_CONSENT.generateIntentId();
        OBWriteInternationalStandingOrderConsent6 consentRequest = aValidOBWriteInternationalStandingOrderConsent6();

        // When
        ResponseEntity<OBWriteInternationalStandingOrderConsentResponse6> response = restTemplate.exchange(
                getUri(consentId, OBVersion.v3_1_4.getCanonicalName(), PISOC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteInternationalStandingOrderConsentResponse6.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mapper.writeValueAsString(response.getBody().getData().getInitiation())).isEqualTo(mapper.writeValueAsString(consentRequest.getData().getInitiation()));
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    /*@Test
    public void shouldCalculateResponseElements4PFC_v3_1_4() throws JsonProcessingException {
        String consentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();
        OBWriteFileConsent3 consentRequest = aValidOBWriteFileConsent3();

        // When
        ResponseEntity<OBWriteFileConsentResponse3> response = restTemplate.exchange(
                getUri(consentId, OBVersion.v3_1_4.getCanonicalName(), PFC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteFileConsentResponse3.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }*/

    /*
    Domestic payments v3.1.8
     */
    @Test
    public void shouldCalculateResponseElements4PDC_v3_1_8() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();
        OBWriteDomesticConsent4 consentRequest = aValidOBWriteDomesticConsent4();

        // When
        ResponseEntity<OBWriteDomesticConsentResponse5> response = restTemplate.exchange(
                getUri(intent, OBVersion.v3_1_8.getCanonicalName(), PDC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteDomesticConsentResponse5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getInitiation()).isEqualTo(consentRequest.getData().getInitiation());
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    @Test
    public void shouldCalculateResponseElements4PDSC_v3_1_8() throws JsonProcessingException {
        String consentId = IntentType.PAYMENT_DOMESTIC_SCHEDULED_CONSENT.generateIntentId();
        OBWriteDomesticScheduledConsent4 consentRequest = aValidOBWriteDomesticScheduledConsent4();

        // When
        ResponseEntity<OBWriteDomesticScheduledConsentResponse5> response = restTemplate.exchange(
                getUri(consentId, OBVersion.v3_1_8.getCanonicalName(), PDSC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteDomesticScheduledConsentResponse5.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mapper.writeValueAsString(response.getBody().getData().getInitiation())).isEqualTo(mapper.writeValueAsString(consentRequest.getData().getInitiation()));
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    @Test
    public void shouldCalculateResponseElements4PDSOC_v3_1_8() throws JsonProcessingException {
        String consentId = IntentType.PAYMENT_DOMESTIC_STANDING_ORDERS_CONSENT.generateIntentId();
        OBWriteDomesticStandingOrderConsent5 consentRequest = aValidOBWriteDomesticStandingOrderConsent5();

        // When
        ResponseEntity<OBWriteDomesticStandingOrderConsentResponse6> response = restTemplate.exchange(
                getUri(consentId, OBVersion.v3_1_8.getCanonicalName(), PDSOC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteDomesticStandingOrderConsentResponse6.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mapper.writeValueAsString(response.getBody().getData().getInitiation())).isEqualTo(mapper.writeValueAsString(consentRequest.getData().getInitiation()));
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    @Test
    public void shouldCalculateResponseElements4PIC_v3_1_8() throws JsonProcessingException {
        String consentId = IntentType.PAYMENT_INTERNATIONAL_CONSENT.generateIntentId();
        OBWriteInternationalConsent5 consentRequest = aValidOBWriteInternationalConsent5();

        // When
        ResponseEntity<OBWriteInternationalConsentResponse6> response = restTemplate.exchange(
                getUri(consentId, OBVersion.v3_1_8.getCanonicalName(), PIC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteInternationalConsentResponse6.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mapper.writeValueAsString(response.getBody().getData().getInitiation())).isEqualTo(mapper.writeValueAsString(consentRequest.getData().getInitiation()));
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    @Test
    public void shouldCalculateResponseElementsWithoutExchangeRateInformation4PIC_v3_1_8() throws JsonProcessingException {
        String consentId = IntentType.PAYMENT_INTERNATIONAL_CONSENT.generateIntentId();
        OBWriteInternationalConsent5 consentRequest = aValidOBWriteInternationalConsent5();
        consentRequest.getData().getInitiation().setExchangeRateInformation(null);

        // When
        ResponseEntity<OBWriteInternationalConsentResponse6> response = restTemplate.exchange(
                getUri(consentId, OBVersion.v3_1_8.getCanonicalName(), PIC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteInternationalConsentResponse6.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getInitiation().getExchangeRateInformation()).isNotNull();
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    @Test
    public void shouldCalculateResponseElements4PISC_v3_1_8() throws JsonProcessingException {
        String consentId = IntentType.PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT.generateIntentId();
        OBWriteInternationalScheduledConsent5 consentRequest = aValidOBWriteInternationalScheduledConsent5();

        // When
        ResponseEntity<OBWriteInternationalScheduledConsentResponse6> response = restTemplate.exchange(
                getUri(consentId, OBVersion.v3_1_8.getCanonicalName(), PISC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteInternationalScheduledConsentResponse6.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mapper.writeValueAsString(response.getBody().getData().getInitiation())).isEqualTo(mapper.writeValueAsString(consentRequest.getData().getInitiation()));
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    @Test
    public void shouldCalculateResponseElementsWithoutExchangeRateInformation4PISC_v3_1_8() throws JsonProcessingException {
        String consentId = IntentType.PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT.generateIntentId();
        OBWriteInternationalScheduledConsent5 consentRequest = aValidOBWriteInternationalScheduledConsent5();
        consentRequest.getData().getInitiation().setExchangeRateInformation(null);

        // When
        ResponseEntity<OBWriteInternationalScheduledConsentResponse6> response = restTemplate.exchange(
                getUri(consentId, OBVersion.v3_1_8.getCanonicalName(), PISC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteInternationalScheduledConsentResponse6.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getInitiation().getExchangeRateInformation()).isNotNull();
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    @Test
    public void shouldCalculateResponseElements4PISOC_v3_1_8() throws JsonProcessingException {
        String consentId = IntentType.PAYMENT_INTERNATIONAL_STANDING_ORDERS_CONSENT.generateIntentId();
        OBWriteInternationalStandingOrderConsent6 consentRequest = aValidOBWriteInternationalStandingOrderConsent6();

        // When
        ResponseEntity<OBWriteInternationalStandingOrderConsentResponse7> response = restTemplate.exchange(
                getUri(consentId, OBVersion.v3_1_8.getCanonicalName(), PISOC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteInternationalStandingOrderConsentResponse7.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mapper.writeValueAsString(response.getBody().getData().getInitiation())).isEqualTo(mapper.writeValueAsString(consentRequest.getData().getInitiation()));
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }

    /*@Test
    public void shouldCalculateResponseElements4PFC_v3_1_8() throws JsonProcessingException {
        String consentId = IntentType.PAYMENT_FILE_CONSENT.generateIntentId();
        OBWriteFileConsent3 consentRequest = aValidOBWriteFileConsent3();

        // When
        ResponseEntity<OBWriteFileConsentResponse4> response = restTemplate.exchange(
                getUri(consentId, OBVersion.v3_1_8.getCanonicalName(), PFC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBWriteFileConsentResponse4.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getCharges()).isNotEmpty();
    }*/

    @Test
    public void cannotDetermineTheVersion() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();
        OBWriteDomesticConsent4 consentRequest = aValidOBWriteDomesticConsent4();

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri(intent, "v99.00.334", PDC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    public void cannotDetermineTheIntentType() throws JsonProcessingException {
        OBWriteDomesticConsent4 consentRequest = aValidOBWriteDomesticConsent4();

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri("WRONG_INTENT", OBVersion.v3_1_8.getCanonicalName(), PDC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(response.getBody().getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        assertThat(response.getBody().getErrors()).containsExactly(
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                        String.format("It has not been possible to determine the value of '%s'", INTENT_TYPE_DESCRIPTION)
                )
        );
    }

    @Test
    public void shouldFailInstructedAmount() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();
        OBWriteDomesticConsent4 consentRequest = aValidOBWriteDomesticConsent4();
        consentRequest.getData().getInitiation().setInstructedAmount(null);
        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri(intent, OBVersion.v3_1_8.getCanonicalName(), PDC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(response.getBody().getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        assertThat(response.getBody().getErrors()).containsExactly(
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                        String.format("'%s' cannot be null to be validate", "InstructedAmount")
                )
        );
    }

    @Test
    public void shouldFailInstructedAmountAmount() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();
        OBWriteDomesticConsent4 consentRequest = aValidOBWriteDomesticConsent4();
        consentRequest.getData().getInitiation().getInstructedAmount().setAmount("0");
        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri(intent, OBVersion.v3_1_8.getCanonicalName(), PDC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(response.getBody().getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        assertThat(response.getBody().getErrors()).containsExactly(
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                        String.format(
                                "The amount %s provided must be greater than 0",
                                consentRequest.getData().getInitiation().getInstructedAmount().getAmount()
                        )
                )
        );
    }

    @Test
    public void shouldFailInstructedAmountCurrency() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();
        OBWriteDomesticConsent4 consentRequest = aValidOBWriteDomesticConsent4();
        consentRequest.getData().getInitiation().getInstructedAmount().setCurrency(INVALID_CURRENCY);
        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri(intent, OBVersion.v3_1_8.getCanonicalName(), PDC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(response.getBody().getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        assertThat(response.getBody().getErrors()).containsExactly(
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                        String.format(
                                "The currency %s provided is not supported",
                                consentRequest.getData().getInitiation().getInstructedAmount().getCurrency()
                        )
                )
        );
    }

    @Test
    public void shouldFailInstructedAmountAmountAndCurrency() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();
        OBWriteDomesticConsent4 consentRequest = aValidOBWriteDomesticConsent4();
        consentRequest.getData().getInitiation().getInstructedAmount().setAmount("0");
        consentRequest.getData().getInitiation().getInstructedAmount().setCurrency(INVALID_CURRENCY);
        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri(intent, OBVersion.v3_1_8.getCanonicalName(), PDC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(response.getBody().getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        assertThat(response.getBody().getErrors()).containsExactlyInAnyOrder(
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                        String.format(
                                "The currency %s provided is not supported",
                                consentRequest.getData().getInitiation().getInstructedAmount().getCurrency()
                        )
                ),
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                        String.format(
                                "The amount %s provided must be greater than 0",
                                consentRequest.getData().getInitiation().getInstructedAmount().getAmount()
                        )
                )
        );
    }

    @Test
    public void shouldFailExchangeRateInformationRateType() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_INTERNATIONAL_CONSENT.generateIntentId();
        OBWriteInternationalConsent5 consentRequest = aValidOBWriteInternationalConsent5();
        consentRequest.getData().getInitiation().getExchangeRateInformation().setRateType(null);
        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri(intent, OBVersion.v3_1_8.getCanonicalName(), PIC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(response.getBody().getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        assertThat(response.getBody().getErrors()).containsExactly(
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                        String.format("'%s' cannot be null to be validate", "RateType")
                )
        );
    }

    @Test
    public void shouldFailExchangeRateInformationUnitCurrency() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_INTERNATIONAL_CONSENT.generateIntentId();
        OBWriteInternationalConsent5 consentRequest = aValidOBWriteInternationalConsent5();
        consentRequest.getData().getInitiation().getExchangeRateInformation().setUnitCurrency(null);
        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri(intent, OBVersion.v3_1_8.getCanonicalName(), PIC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(response.getBody().getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        assertThat(response.getBody().getErrors()).containsExactly(
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                        String.format("'%s' cannot be null to be validate", "UnitCurrency")
                )
        );
    }

    @Test
    public void shouldFailExchangeRateInformationUnitCurrencyWRONG() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_INTERNATIONAL_CONSENT.generateIntentId();
        OBWriteInternationalConsent5 consentRequest = aValidOBWriteInternationalConsent5();
        consentRequest.getData().getInitiation().getInstructedAmount().setCurrency(INVALID_CURRENCY);

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri(intent, OBVersion.v3_1_8.getCanonicalName(), PIC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(response.getBody().getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        assertThat(response.getBody().getErrors()).containsExactly(
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                        String.format("The currency %s provided is not supported", INVALID_CURRENCY)
                )
        );
    }

    @Test
    public void shouldFailExchangeRateInformationRateType_Agreed() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_INTERNATIONAL_CONSENT.generateIntentId();
        OBWriteInternationalConsent5 consentRequest = aValidOBWriteInternationalConsent5();
        consentRequest.getData().getInitiation().setCurrencyOfTransfer("EUR");

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri(intent, OBVersion.v3_1_8.getCanonicalName(), PIC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(response.getBody().getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        assertThat(response.getBody().getErrors()).containsExactly(
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1("The currency of transfer should be the same with the exchange unit currency.")
        );
    }

    @Test
    public void shouldFailExchangeRateInformationRateType_Actual() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_INTERNATIONAL_CONSENT.generateIntentId();
        OBWriteInternationalConsent5 consentRequest = aValidOBWriteInternationalConsent5();
        consentRequest.getData().getInitiation().getExchangeRateInformation().setRateType(OBExchangeRateType2Code.ACTUAL);

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri(intent, OBVersion.v3_1_8.getCanonicalName(), PIC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(response.getBody().getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        assertThat(response.getBody().getErrors()).containsExactly(
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                        String.format("A PISP must not specify ExchangeRate and/or ContractIdentification when requesting an %s RateType.", OBExchangeRateType2Code.ACTUAL)
                )
        );
    }

    @Test
    public void shouldFailExchangeRateInformationRateType_Indicative() throws JsonProcessingException {
        String intent = IntentType.PAYMENT_INTERNATIONAL_CONSENT.generateIntentId();
        OBWriteInternationalConsent5 consentRequest = aValidOBWriteInternationalConsent5();
        consentRequest.getData().getInitiation().getExchangeRateInformation().setRateType(OBExchangeRateType2Code.INDICATIVE);

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.exchange(
                getUri(intent, OBVersion.v3_1_8.getCanonicalName(), PIC_CONTEXT),
                HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(consentRequest), HTTP_HEADERS),
                OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(response.getBody().getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        assertThat(response.getBody().getErrors()).containsExactly(
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                        String.format("A PISP must not specify ExchangeRate and/or ContractIdentification when requesting an %s RateType.", OBExchangeRateType2Code.INDICATIVE)
                )
        );
    }

    private URI getUri(String intent, String version, String intentPath) {

        String restContext = java.text.MessageFormat.format(REST_CONTEXT, version, intentPath);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(BASE_URL + port + restContext);
        builder.queryParam("intent", intent);
        builder.queryParam("version", version);
        return builder.build().encode().toUri();
    }
}
