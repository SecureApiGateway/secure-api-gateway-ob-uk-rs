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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.vrp;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRFinancialAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRReadRefundAccount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.vrp.FRDomesticVRPConsentConverters;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRDomesticVRPConsent;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.vrp.v3_1_10.DomesticVRPConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.vrp.v3_1_10.DomesticVRPConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.DomesticVrpPaymentSubmissionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.v3.common.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;
import uk.org.openbanking.datamodel.v3.vrp.*;
import uk.org.openbanking.testsupport.vrp.OBDomesticVrpConsentRequestTestDataFactory;
import uk.org.openbanking.testsupport.vrp.OBDomesticVrpRequestTestDataFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory.requiredPaymentHttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * A SpringBoot test for the {@link DomesticVrpConsentsApiController} <br/>
 * Coverage versions v3.1.9 to v3.1.10.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DomesticVrpsApiControllerTest {

    private static final String TEST_API_CLIENT = "client-54354";
    private static final HttpHeaders HTTP_HEADERS = requiredPaymentHttpHeaders(TEST_API_CLIENT);
    private static final String BASE_URL = "http://localhost:";
    private static final String PAYMENTS_URI = "/open-banking/v3.1.10/pisp";
    private static final String VRP_PAYMENTS_URI = "/domestic-vrps";
    private static final String VRP_PAYMENTS_DETAILS_URI = "/payment-details";

    @LocalServerPort
    private int port;

    @Autowired
    private DomesticVrpPaymentSubmissionRepository paymentSubmissionRepository;

    @MockBean
    private FRAccountRepository frAccountRepository;

    @MockBean
    private DomesticVRPConsentStoreClient consentStoreClient;

    @Autowired
    private TestRestTemplate restTemplate;

    private FRAccount readRefundAccount;

    @BeforeEach
    void setup() {
        readRefundAccount = FRAccount.builder()
                .account(
                        FRFinancialAccount.builder().accounts(
                                List.of(
                                        FRAccountIdentifier.builder()
                                                .identification("08080021325698")
                                                .name("ACME Inc")
                                                .schemeName("UK.OBIE.SortCodeAccountNumber")
                                                .secondaryIdentification("0002")
                                                .build()
                                )
                        ).build()
                )
                .build();

        given(frAccountRepository.byAccountId(anyString())).willReturn(readRefundAccount);
    }

    @AfterEach
    void removeData() {
        paymentSubmissionRepository.deleteAll();
    }

    /**
     * Test to check that Bean validation configuration is enabled.
     *
     * Attempt to submit an empty payment request, and verify we get an error response stating risk and data fields
     * must not be null.
     */
    @Test
    public void testBeanValidation() {
        OBDomesticVRPRequest badRequest = new OBDomesticVRPRequest();
        HttpEntity<OBDomesticVRPRequest> request = new HttpEntity<>(badRequest, HTTP_HEADERS);

        ResponseEntity<OBErrorResponse1> response = restTemplate.postForEntity(vrpPaymentsUrl(), request, OBErrorResponse1.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final OBErrorResponse1 errorResponse = response.getBody();
        assertThat(errorResponse.getCode()).isEqualTo(OBRIErrorResponseCategory.ARGUMENT_INVALID.getId());
        assertThat(errorResponse.getMessage()).isEqualTo(OBRIErrorResponseCategory.ARGUMENT_INVALID.getDescription());
        assertThat(errorResponse.getErrors()).hasSize(2);
        assertThat(errorResponse.getErrors()).containsExactlyInAnyOrder(
                new OBError1().errorCode("UK.OBIE.Field.Invalid").message("The field received is invalid. Reason 'must not be null'").path("risk"),
                new OBError1().errorCode("UK.OBIE.Field.Invalid").message("The field received is invalid. Reason 'must not be null'").path("data"));
    }

    @Test
    public void testFailToCreatePaymentWhenConsentNotAuthorised() {
        OBDomesticVRPRequest obDomesticVRPRequest = OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest();
        HttpEntity<OBDomesticVRPRequest> request = new HttpEntity<>(obDomesticVRPRequest, HTTP_HEADERS);

        final DomesticVRPConsent consent = createAwaitingAuthorisationConsent(obDomesticVRPRequest.getData().getConsentId());

        given(consentStoreClient.getConsent(eq(obDomesticVRPRequest.getData().getConsentId()), eq(TEST_API_CLIENT))).willReturn(consent);

        ResponseEntity<OBErrorResponse1> response = restTemplate.postForEntity(vrpPaymentsUrl(), request, OBErrorResponse1.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final OBErrorResponse1 errorResponse = response.getBody();
        assertThat(errorResponse.getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(errorResponse.getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        assertThat(errorResponse.getErrors()).hasSize(1);
        assertThat(errorResponse.getErrors().get(0)).isEqualTo(
                new OBError1().errorCode("UK.OBIE.Resource.InvalidConsentStatus").message("Action can only be performed on consents with status: Authorised. Currently, the consent is: AwaitingAuthorisation"));
    }

    @Test
    public void testFailToCreatePaymentWhenMaxIndividualAmountIsBreached() {
        OBDomesticVRPRequest obDomesticVRPRequest = OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest();
        HttpEntity<OBDomesticVRPRequest> request = new HttpEntity<>(obDomesticVRPRequest, HTTP_HEADERS);

        final DomesticVRPConsent consent = createAuthorisedConsent(obDomesticVRPRequest.getData().getConsentId());
        final OBActiveOrHistoricCurrencyAndAmount instructedAmount = obDomesticVRPRequest.getData().getInstruction().getInstructedAmount();
        // set MaxIndividualAmount < Instructed Amount
        consent.getRequestObj().getData().getControlParameters().setMaximumIndividualAmount(new FRAmount(new BigDecimal(
                instructedAmount.getAmount()).divide(BigDecimal.valueOf(2), RoundingMode.DOWN).toPlainString(),
                instructedAmount.getCurrency()));

        given(consentStoreClient.getConsent(eq(obDomesticVRPRequest.getData().getConsentId()), eq(TEST_API_CLIENT))).willReturn(consent);

        ResponseEntity<OBErrorResponse1> response = restTemplate.postForEntity(vrpPaymentsUrl(), request, OBErrorResponse1.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final OBErrorResponse1 errorResponse = response.getBody();
        assertThat(errorResponse.getCode()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getId());
        assertThat(errorResponse.getMessage()).isEqualTo(OBRIErrorResponseCategory.REQUEST_INVALID.getDescription());
        assertThat(errorResponse.getErrors()).hasSize(1);
        assertThat(errorResponse.getErrors().get(0)).isEqualTo(
                new OBError1().errorCode("UK.OBIE.Rules.FailsControlParameters").message("The field 'InstructedAmount' breaches a limitation set by 'MaximumIndividualAmount'"));
    }


    @Test
    public void shouldCreateDomesticVrpPayment_refundYes() {
        // Given
        OBDomesticVRPRequest obDomesticVRPRequest = OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest();
        HttpEntity<OBDomesticVRPRequest> request = new HttpEntity<>(obDomesticVRPRequest, HTTP_HEADERS);

        final DomesticVRPConsent consent = createAuthorisedConsent(obDomesticVRPRequest.getData().getConsentId());
        consent.getRequestObj().getData().setReadRefundAccount(FRReadRefundAccount.YES);

        given(consentStoreClient.getConsent(eq(obDomesticVRPRequest.getData().getConsentId()), eq(TEST_API_CLIENT))).willReturn(consent);

        // When
        ResponseEntity<OBDomesticVRPResponse> response = restTemplate.postForEntity(vrpPaymentsUrl(), request, OBDomesticVRPResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBDomesticVRPResponseData responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(obDomesticVRPRequest.getData().getConsentId());
        assertThat(responseData.getRefund()).isNotNull();
        FRAccountIdentifier frAccountIdentifier = readRefundAccount.getAccount().getFirstAccount();
        OBCashAccountDebtorWithName refund = responseData.getRefund();
        assertThat(refund.getIdentification()).isEqualTo(frAccountIdentifier.getIdentification());
        assertThat(refund.getName()).isEqualTo(frAccountIdentifier.getName());
        assertThat(refund.getSchemeName()).isEqualTo(frAccountIdentifier.getSchemeName());
        assertThat(responseData.getInitiation()).isEqualTo(obDomesticVRPRequest.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().getPath().endsWith(VRP_PAYMENTS_URI + "/" + responseData.getDomesticVRPId())).isTrue();
    }

    @Test
    public void shouldCreateDomesticVrpPayment_refundNo() {
        // Given
        OBDomesticVRPRequest obDomesticVRPRequest = OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest();
        HttpEntity<OBDomesticVRPRequest> request = new HttpEntity<>(obDomesticVRPRequest, HTTP_HEADERS);

        final DomesticVRPConsent consent = createAuthorisedConsent(obDomesticVRPRequest.getData().getConsentId());
        consent.getRequestObj().getData().setReadRefundAccount(FRReadRefundAccount.NO);

        given(consentStoreClient.getConsent(eq(obDomesticVRPRequest.getData().getConsentId()), eq(TEST_API_CLIENT))).willReturn(consent);

        // When
        ResponseEntity<OBDomesticVRPResponse> response = restTemplate.postForEntity(vrpPaymentsUrl(), request, OBDomesticVRPResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBDomesticVRPResponseData responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(obDomesticVRPRequest.getData().getConsentId());
        assertThat(responseData.getRefund()).isNull();
        assertThat(responseData.getInitiation()).isEqualTo(obDomesticVRPRequest.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().getPath().endsWith(VRP_PAYMENTS_URI + "/" + responseData.getDomesticVRPId())).isTrue();
    }

    @Test
    public void shouldGetDomesticVrpPaymentById() {
        // Given
        OBDomesticVRPRequest obDomesticVRPRequest = OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest();
        HttpEntity<OBDomesticVRPRequest> request = new HttpEntity<>(obDomesticVRPRequest, HTTP_HEADERS);

        final DomesticVRPConsent consent = createAuthorisedConsent(obDomesticVRPRequest.getData().getConsentId());
        given(consentStoreClient.getConsent(eq(obDomesticVRPRequest.getData().getConsentId()), eq(TEST_API_CLIENT))).willReturn(consent);

        ResponseEntity<OBDomesticVRPResponse> vrpCreated = restTemplate.postForEntity(vrpPaymentsUrl(), request, OBDomesticVRPResponse.class);
        String url = vrpPaymentIdUrl(vrpCreated.getBody().getData().getDomesticVRPId());
        // When
        ResponseEntity<OBDomesticVRPResponse> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBDomesticVRPResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBDomesticVRPResponseData responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(obDomesticVRPRequest.getData().getConsentId());
        assertThat(responseData.getInitiation()).isEqualTo(obDomesticVRPRequest.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().getPath().endsWith(VRP_PAYMENTS_URI + "/" + responseData.getDomesticVRPId())).isTrue();
    }

    private static DomesticVRPConsent createAwaitingAuthorisationConsent(String consentId) {
        final FRDomesticVRPConsent consentRequest = FRDomesticVRPConsentConverters.toFRDomesticVRPConsent(OBDomesticVrpConsentRequestTestDataFactory.aValidOBDomesticVRPConsentRequest());
        final DomesticVRPConsent consent = new DomesticVRPConsent();
        consentRequest.getData().setReadRefundAccount(FRReadRefundAccount.NO);
        consent.setRequestObj(consentRequest);
        consent.setId(consentId);
        consent.setStatus(OBPaymentConsentStatus.AWAITINGAUTHORISATION.toString());
        return consent;
    }

    private static DomesticVRPConsent createAuthorisedConsent(String consentId) {
        final FRDomesticVRPConsent consentRequest = FRDomesticVRPConsentConverters.toFRDomesticVRPConsent(OBDomesticVrpConsentRequestTestDataFactory.aValidOBDomesticVRPConsentRequest());
        final DomesticVRPConsent consent = new DomesticVRPConsent();
        consentRequest.getData().setReadRefundAccount(FRReadRefundAccount.NO);
        consent.setRequestObj(consentRequest);
        consent.setId(consentId);
        consent.setStatus(OBPaymentConsentStatus.AUTHORISED.toString());
        consent.setAuthorisedDebtorAccountId("acc-1321243");
        return consent;
    }

    @Test
    public void shouldGetDomesticVrpPaymentDetails() {
        // Given
        OBDomesticVRPRequest obDomesticVRPRequest = OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest();
        HttpEntity<OBDomesticVRPRequest> request = new HttpEntity<>(obDomesticVRPRequest, HTTP_HEADERS);

        final DomesticVRPConsent consent = createAuthorisedConsent(obDomesticVRPRequest.getData().getConsentId());
        given(consentStoreClient.getConsent(eq(obDomesticVRPRequest.getData().getConsentId()), eq(TEST_API_CLIENT))).willReturn(consent);

        ResponseEntity<OBDomesticVRPResponse> vrpCreated = restTemplate.postForEntity(vrpPaymentsUrl(), request, OBDomesticVRPResponse.class);
        String url = vrpPaymentIdUrl(vrpCreated.getBody().getData().getDomesticVRPId());
        // When
        ResponseEntity<OBDomesticVRPDetails> response = restTemplate.exchange(url + VRP_PAYMENTS_DETAILS_URI, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBDomesticVRPDetails.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBDomesticVRPDetailsData responseData = response.getBody().getData();
        assertThat(responseData.getPaymentStatus().get(0).getStatus()).isEqualTo(OBDomesticVRPDetailsDataPaymentStatusInnerStatus.PENDING);
    }

    @Test
    public void shouldReturnLimitBreachWhenCreateDomesticVrpPayment() {
        // Given
        OBDomesticVRPRequest obDomesticVRPRequest = OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest();
        OBDomesticVRPConsentRequest consentRequest = OBDomesticVrpConsentRequestTestDataFactory.aValidOBDomesticVRPConsentRequest();
        HttpHeaders headers = requiredPaymentHttpHeaders(TEST_API_CLIENT);
        String periodType = consentRequest.getData().getControlParameters().getPeriodicLimits().get(0).getPeriodType().getValue();
        String periodAlignment = consentRequest.getData().getControlParameters().getPeriodicLimits().get(0).getPeriodAlignment().getValue();
        headers.add("x-vrp-limit-breach-response-simulation", periodType + "-" + periodAlignment);
        HttpEntity<OBDomesticVRPRequest> request = new HttpEntity<>(obDomesticVRPRequest, headers);

        final DomesticVRPConsent consent = createAuthorisedConsent(obDomesticVRPRequest.getData().getConsentId());
        given(consentStoreClient.getConsent(eq(obDomesticVRPRequest.getData().getConsentId()), eq(TEST_API_CLIENT))).willReturn(consent);

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.postForEntity(vrpPaymentsUrl(), request, OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        OBError1 error = response.getBody().getErrors().get(0);
        assertEquals(OBRIErrorType.REQUEST_VRP_CONTROL_PARAMETERS_PAYMENT_PERIODIC_LIMIT_BREACH.getCode().getValue(),
                error.getErrorCode());
        String amount = consentRequest.getData().getControlParameters().getPeriodicLimits().get(0).getAmount();
        String currency = consentRequest.getData().getControlParameters().getPeriodicLimits().get(0).getCurrency();
        assertEquals("Unable to complete payment due to payment limit breach, periodic limit of '" + amount +
                        "' '" + currency + "' for period '" + periodType + "' '" + periodAlignment + "' has been breached",
                error.getMessage());
    }

    @Test
    public void testPaymentSubmissionIsIdempotent() {
        OBDomesticVRPRequest obDomesticVRPRequest = OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest();
        HttpEntity<OBDomesticVRPRequest> request = new HttpEntity<>(obDomesticVRPRequest, HTTP_HEADERS);

        final DomesticVRPConsent consent = createAuthorisedConsent(obDomesticVRPRequest.getData().getConsentId());
        consent.getRequestObj().getData().setReadRefundAccount(FRReadRefundAccount.YES);

        given(consentStoreClient.getConsent(eq(obDomesticVRPRequest.getData().getConsentId()), eq(TEST_API_CLIENT))).willReturn(consent);

        ResponseEntity<OBDomesticVRPResponse> firstResponse = restTemplate.postForEntity(vrpPaymentsUrl(), request, OBDomesticVRPResponse.class);
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Send the same request with the same idempotencyKey, verify we get the same response
        ResponseEntity<OBDomesticVRPResponse> secondResponse = restTemplate.postForEntity(vrpPaymentsUrl(), request, OBDomesticVRPResponse.class);
        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(secondResponse.getBody()).isEqualTo(firstResponse.getBody());
    }

    private String vrpPaymentsUrl() {
        return BASE_URL + port + PAYMENTS_URI + VRP_PAYMENTS_URI;
    }

    private String vrpPaymentIdUrl(String id) {
        return vrpPaymentsUrl() + "/" + id;
    }
}
