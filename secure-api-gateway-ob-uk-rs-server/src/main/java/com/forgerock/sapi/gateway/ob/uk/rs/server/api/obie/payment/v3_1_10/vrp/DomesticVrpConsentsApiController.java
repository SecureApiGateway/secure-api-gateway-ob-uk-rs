package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.vrp;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_10.vrp.DomesticVrpConsentsApi;

import uk.org.openbanking.datamodel.vrp.OBVRPFundsConfirmationRequest;
import uk.org.openbanking.datamodel.vrp.OBVRPFundsConfirmationResponse;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
@Controller("DomesticVrpConsentApiV3.1.10")
public class DomesticVrpConsentsApiController implements DomesticVrpConsentsApi {


    @Override
    public ResponseEntity<OBVRPFundsConfirmationResponse> domesticVrpConsentsFundsConfirmation(String consentId,
                                                                                               String authorization,
                                                                                               String xJwsSignature,
                                                                                               OBVRPFundsConfirmationRequest obVRPFundsConfirmationRequest,
                                                                                               String xFapiAuthDate,
                                                                                               String xFapiCustomerIpAddress,
                                                                                               String xFapiInteractionId,
                                                                                               String xCustomerUserAgent,
                                                                                               HttpServletRequest request,
                                                                                               Principal principal) throws OBErrorResponseException {
        throw new UnsupportedOperationException("implement me");
    }
}
