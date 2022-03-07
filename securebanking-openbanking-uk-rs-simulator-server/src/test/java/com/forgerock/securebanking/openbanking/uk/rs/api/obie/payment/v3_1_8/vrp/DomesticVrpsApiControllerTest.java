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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_1_8.vrp;

import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.DomesticVrpPaymentSubmissionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import uk.org.openbanking.datamodel.vrp.*;
import uk.org.openbanking.testsupport.vrp.OBDomesticVrpRequestTestDataFactory;

import static com.forgerock.securebanking.openbanking.uk.rs.testsupport.api.HttpHeadersTestDataFactory.requiredVrpPaymentHttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * A SpringBoot test for the {@link DomesticVrpsApiController}.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class DomesticVrpsApiControllerTest {

    private static final HttpHeaders HTTP_HEADERS = requiredVrpPaymentHttpHeaders();
    private static final String BASE_URL = "http://localhost:";
    private static final String PAYMENTS_URI = "/open-banking/v3.1.9/pisp";
    private static final String VRP_PAYMENTS_URI = "/domestic-vrps";
    private static final String VRP_PAYMENTS_DETAILS_URI = "/payment-details";

    @LocalServerPort
    private int port;

    @Autowired
    private DomesticVrpPaymentSubmissionRepository paymentSubmissionRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void removeData() {
        paymentSubmissionRepository.deleteAll();
    }

    @Test
    public void shouldCreateDomesticVrpPayment() {
        // Given
        OBDomesticVRPRequest obDomesticVRPRequest = OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest();
        HttpEntity<OBDomesticVRPRequest> request = new HttpEntity<>(obDomesticVRPRequest, HTTP_HEADERS);

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
        HttpHeaders headers = requiredVrpPaymentHttpHeaders();
        headers.add("x-read-refund-account", "Yes");
        HttpEntity<OBDomesticVRPRequest> request = new HttpEntity<>(obDomesticVRPRequest, headers);

        // When
        ResponseEntity<OBDomesticVRPResponse> response = restTemplate.postForEntity(vrpPaymentsUrl(), request, OBDomesticVRPResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OBDomesticVRPResponseData responseData = response.getBody().getData();
        assertThat(responseData.getConsentId()).isEqualTo(obDomesticVRPRequest.getData().getConsentId());
        assertThat(responseData.getRefund()).isNotNull();
        // convert from new to old before comparing (due to missing fields on older versions)
        assertThat(responseData.getInitiation()).isEqualTo(obDomesticVRPRequest.getData().getInitiation());
        assertThat(response.getBody().getLinks().getSelf().getPath().endsWith(VRP_PAYMENTS_URI + "/" + responseData.getDomesticVRPId())).isTrue();
    }

    @Test
    public void shouldGetDomesticVrpPaymentById() {
        // Given
        OBDomesticVRPRequest obDomesticVRPRequest = OBDomesticVrpRequestTestDataFactory.aValidOBDomesticVRPRequest();
        HttpEntity<OBDomesticVRPRequest> request = new HttpEntity<>(obDomesticVRPRequest, HTTP_HEADERS);
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
        ResponseEntity<OBDomesticVRPResponse> vrpCreated = restTemplate.postForEntity(vrpPaymentsUrl(), request, OBDomesticVRPResponse.class);
        String url = vrpPaymentIdUrl(vrpCreated.getBody().getData().getDomesticVRPId());
        // When
        ResponseEntity<OBDomesticVRPDetails> response = restTemplate.exchange(url + VRP_PAYMENTS_DETAILS_URI, HttpMethod.GET, new HttpEntity<>(HTTP_HEADERS), OBDomesticVRPDetails.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        OBDomesticVRPDetailsData responseData = response.getBody().getData();
        assertThat(responseData.getPaymentStatus().get(0).getStatus()).isEqualTo(OBDomesticVRPDetailsDataPaymentStatus.StatusEnum.PENDING);
    }

    private String vrpPaymentsUrl() {
        return BASE_URL + port + PAYMENTS_URI + VRP_PAYMENTS_URI;
    }

    private String vrpPaymentIdUrl(String id) {
        return vrpPaymentsUrl() + "/" + id;
    }
}
