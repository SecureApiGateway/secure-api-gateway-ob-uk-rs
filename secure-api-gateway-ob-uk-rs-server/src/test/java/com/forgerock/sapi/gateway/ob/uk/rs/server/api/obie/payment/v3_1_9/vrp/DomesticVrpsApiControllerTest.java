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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_9.vrp;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.ConsentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_8.vrp.DomesticVrpsApiController;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.payments.DomesticVrpPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.error.OBErrorResponse1;
import uk.org.openbanking.datamodel.vrp.*;
import uk.org.openbanking.testsupport.vrp.OBDomesticVrpRequestTestDataFactory;

import static com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.test.support.DomesticVrpPaymentConsentDetailsTestFactory.aValidDomesticVrpPaymentConsentDetails;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.org.openbanking.testsupport.vrp.OBDomesticVRPConsentResponseTestDataFactory.aValidOBDomesticVRPConsentResponse;
import static uk.org.openbanking.testsupport.vrp.OBDomesticVrpCommonTestDataFactory.aValidOBCashAccountDebtorWithName;

/**
 * A SpringBoot test for the {@link DomesticVrpsApiController} v[3.1.9, 3.1.10].
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DomesticVrpsApiControllerTest {

    private static final HttpHeaders HTTP_HEADERS = HttpHeadersTestDataFactory.requiredVrpPaymentHttpHeaders();
    private static final String BASE_URL = "http://localhost:";
    private static final String PAYMENTS_URI = "/open-banking/v3.1.9/pisp";
    private static final String VRP_PAYMENTS_URI = "/domestic-vrps";
    private static final String VRP_PAYMENTS_DETAILS_URI = "/payment-details";

    @LocalServerPort
    private int port;

    @Autowired
    private DomesticVrpPaymentSubmissionRepository paymentSubmissionRepository;

    @MockBean
    private ConsentService consentService;

    @Autowired
    private TestRestTemplate restTemplate;
    ;

    @AfterEach
    void removeData() {
        paymentSubmissionRepository.deleteAll();
    }

    @Test
    public void shouldCreateDomesticVrpPayment() {
        // Given
        OBDomesticVRPRequest obDomesticVRPRequest = OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest();
        HttpEntity<OBDomesticVRPRequest> request = new HttpEntity<>(obDomesticVRPRequest, HTTP_HEADERS);

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(aValidDomesticVrpPaymentConsentDetails(
                obDomesticVRPRequest.getData().getConsentId()
        ));

        given(consentService.deserialize(any(), any(JsonObject.class), anyString())).willReturn(
                aValidOBDomesticVRPConsentResponse(obDomesticVRPRequest)
        );

        // When
        ResponseEntity<OBDomesticVRPResponse> response = restTemplate.postForEntity(vrpPaymentsUrl(), request, OBDomesticVRPResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBDomesticVRPResponseData responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(obDomesticVRPRequest.getData().getConsentId());
        assertThat(responseData.getRefund()).isNull();
        // convert from new to old before comparing (due to missing fields on older versions)
        assertThat(responseData.getInitiation()).isEqualTo(obDomesticVRPRequest.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().getPath().endsWith(VRP_PAYMENTS_URI + "/" + responseData.getDomesticVRPId())).isTrue();
    }

    @Test
    public void shouldCreateDomesticVrpPaymentWithRefund() {
        // Given
        OBDomesticVRPRequest obDomesticVRPRequest = OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest();
        // the debtor account is mandatory in initiation to fill the refund object in the response
        obDomesticVRPRequest.getData().getInitiation().debtorAccount(aValidOBCashAccountDebtorWithName());
        HttpHeaders headers = HttpHeadersTestDataFactory.requiredVrpPaymentHttpHeaders();
        headers.add("x-read-refund-account", "Yes");
        HttpEntity<OBDomesticVRPRequest> request = new HttpEntity<>(obDomesticVRPRequest, headers);
        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(aValidDomesticVrpPaymentConsentDetails(
                obDomesticVRPRequest.getData().getConsentId()
        ));

        given(consentService.deserialize(any(), any(JsonObject.class), anyString())).willReturn(
                aValidOBDomesticVRPConsentResponse(obDomesticVRPRequest)
        );
        // When
        ResponseEntity<OBDomesticVRPResponse> response = restTemplate.postForEntity(vrpPaymentsUrl(), request, OBDomesticVRPResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBDomesticVRPResponseData responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(obDomesticVRPRequest.getData().getConsentId());
        assertThat(responseData.getRefund()).isNotNull();
        OBCashAccountDebtorWithName debtorWithName = obDomesticVRPRequest.getData().getInitiation().getDebtorAccount();
        assertThat(responseData.getRefund().getIdentification()).isEqualTo(debtorWithName.getIdentification());
        assertThat(responseData.getRefund().getName()).isEqualTo(debtorWithName.getName());
        assertThat(responseData.getRefund().getSchemeName()).isEqualTo(debtorWithName.getSchemeName());
        // convert from new to old before comparing (due to missing fields on older versions)
        assertThat(responseData.getInitiation()).isEqualTo(obDomesticVRPRequest.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().getPath().endsWith(VRP_PAYMENTS_URI + "/" + responseData.getDomesticVRPId())).isTrue();
    }

    @Test
    public void shouldGetDomesticVrpPaymentById() {
        // Given
        OBDomesticVRPRequest obDomesticVRPRequest = OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest();
        HttpEntity<OBDomesticVRPRequest> request = new HttpEntity<>(obDomesticVRPRequest, HTTP_HEADERS);
        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(aValidDomesticVrpPaymentConsentDetails(
                obDomesticVRPRequest.getData().getConsentId()
        ));

        given(consentService.deserialize(any(), any(JsonObject.class), anyString())).willReturn(
                aValidOBDomesticVRPConsentResponse(obDomesticVRPRequest)
        );
        ResponseEntity<OBDomesticVRPResponse> vrpCreated = restTemplate.postForEntity(vrpPaymentsUrl(), request, OBDomesticVRPResponse.class);
        String url = vrpPaymentIdUrl(vrpCreated.getBody().getData().getDomesticVRPId());
        // When
        ResponseEntity<OBDomesticVRPResponse> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBDomesticVRPResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBDomesticVRPResponseData responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(obDomesticVRPRequest.getData().getConsentId());
        // convert from new to old before comparing (due to missing fields on older versions)
        assertThat(responseData.getInitiation()).isEqualTo(obDomesticVRPRequest.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().getPath().endsWith(VRP_PAYMENTS_URI + "/" + responseData.getDomesticVRPId())).isTrue();
    }

    @Test
    public void shouldGetDomesticVrpPaymentDetails() {
        // Given
        OBDomesticVRPRequest obDomesticVRPRequest = OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest();
        HttpEntity<OBDomesticVRPRequest> request = new HttpEntity<>(obDomesticVRPRequest, HTTP_HEADERS);
        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(aValidDomesticVrpPaymentConsentDetails(
                obDomesticVRPRequest.getData().getConsentId()
        ));

        given(consentService.deserialize(any(), any(JsonObject.class), anyString())).willReturn(
                aValidOBDomesticVRPConsentResponse(obDomesticVRPRequest)
        );
        ResponseEntity<OBDomesticVRPResponse> vrpCreated = restTemplate.postForEntity(vrpPaymentsUrl(), request, OBDomesticVRPResponse.class);
        String url = vrpPaymentIdUrl(vrpCreated.getBody().getData().getDomesticVRPId());
        // When
        ResponseEntity<OBDomesticVRPDetails> response = restTemplate.exchange(url + VRP_PAYMENTS_DETAILS_URI, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBDomesticVRPDetails.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBDomesticVRPDetailsData responseData = response.getBody().getData();
        assertThat(responseData.getPaymentStatus().get(0).getStatus()).isEqualTo(OBDomesticVRPDetailsDataPaymentStatus.StatusEnum.PENDING);
    }

    @Test
    public void shouldReturnLimitBreachWhenCreateDomesticVrpPayment() {
        // Given
        OBDomesticVRPRequest obDomesticVRPRequest = OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest();
        OBDomesticVRPConsentResponse consentResponse = aValidOBDomesticVRPConsentResponse(obDomesticVRPRequest);
        HttpHeaders headers = HttpHeadersTestDataFactory.requiredVrpPaymentHttpHeaders();
        String periodType = consentResponse.getData().getControlParameters().getPeriodicLimits().get(0).getPeriodType().getValue();
        String periodAlignment = consentResponse.getData().getControlParameters().getPeriodicLimits().get(0).getPeriodAlignment().getValue();
        headers.add("x-vrp-limit-breach-response-simulation", periodType + "-" + periodAlignment);
        HttpEntity<OBDomesticVRPRequest> request = new HttpEntity<>(obDomesticVRPRequest, headers);

        given(consentService.getIDMIntent(anyString(), anyString())).willReturn(aValidDomesticVrpPaymentConsentDetails(
                obDomesticVRPRequest.getData().getConsentId()
        ));

        given(consentService.deserialize(any(), any(JsonObject.class), anyString())).willReturn(
                consentResponse
        );

        // When
        ResponseEntity<OBErrorResponse1> response = restTemplate.postForEntity(vrpPaymentsUrl(), request, OBErrorResponse1.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        OBError1 error = response.getBody().getErrors().get(0);
        assertEquals(OBRIErrorType.REQUEST_VRP_CONTROL_PARAMETERS_PAYMENT_PERIODIC_LIMIT_BREACH.getCode().getValue(),
                error.getErrorCode());
        String amount = consentResponse.getData().getControlParameters().getPeriodicLimits().get(0).getAmount();
        String currency = consentResponse.getData().getControlParameters().getPeriodicLimits().get(0).getCurrency();
        assertEquals("Unable to complete payment due to payment limit breach, periodic limit of '" + amount +
                        "' '" + currency + "' for period '" + periodType + "' '" + periodAlignment + "' has been breached",
                error.getMessage());
    }

    private String vrpPaymentsUrl() {
        return BASE_URL + port + PAYMENTS_URI + VRP_PAYMENTS_URI;
    }

    private String vrpPaymentIdUrl(String id) {
        return vrpPaymentsUrl() + "/" + id;
    }
}
