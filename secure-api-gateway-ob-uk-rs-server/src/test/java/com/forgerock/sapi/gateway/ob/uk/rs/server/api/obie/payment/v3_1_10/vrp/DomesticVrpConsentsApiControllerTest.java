package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.vrp;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.api.HttpHeadersTestDataFactory;

import uk.org.openbanking.datamodel.vrp.OBVRPFundsConfirmationRequest;
import uk.org.openbanking.datamodel.vrp.OBVRPFundsConfirmationRequestData;
import uk.org.openbanking.datamodel.vrp.OBVRPFundsConfirmationResponse;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class DomesticVrpConsentsApiControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private String getFundsConfirmationUrl(String consentId) {
        return "http://localhost:" + port + "/open-banking/v3.1.10/pisp/domestic-vrp-consents/" + consentId + "/funds-confirmation";
    }

    @Test
    public void testFundsConfirmation() {
        final String consentId = "123";
        // TODO build complete request obj
        final OBVRPFundsConfirmationRequest obvrpFundsConfirmationRequest = new OBVRPFundsConfirmationRequest().data(new OBVRPFundsConfirmationRequestData().consentId(consentId));

        HttpEntity<OBVRPFundsConfirmationRequest> request = new HttpEntity<>(obvrpFundsConfirmationRequest, HttpHeadersTestDataFactory.requiredVrpPaymentHttpHeaders());

        ResponseEntity<OBVRPFundsConfirmationResponse> response = restTemplate.postForEntity(getFundsConfirmationUrl(consentId), request, OBVRPFundsConfirmationResponse.class);

        // TODO validate the response
    }

}