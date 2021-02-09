/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_1_1.internationalstandingorders;

import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.FRInternationalStandingOrderPaymentSubmission;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.InternationalStandingOrderPaymentSubmissionRepository;
import com.forgerock.securebanking.openbanking.uk.rs.service.standingorder.StandingOrderService;
import com.forgerock.securebanking.openbanking.uk.rs.validator.PaymentSubmissionValidator;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteDataInternationalStandingOrderResponse3;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrder3;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderResponse3;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;

import static com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.LinksHelper.createInternationalStandingOrderPaymentLink;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRSubmissionStatusConverter.toOBExternalStatus1Code;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRWriteInternationalStandingOrderConsentConverter.toOBInternationalStandingOrder3;

@Controller("InternationalStandingOrdersApiV3.1.1")
@Slf4j
public class InternationalStandingOrdersApiController implements InternationalStandingOrdersApi {

    private final InternationalStandingOrderPaymentSubmissionRepository standingOrderPaymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;
    private final StandingOrderService standingOrderService;

    public InternationalStandingOrdersApiController(
            InternationalStandingOrderPaymentSubmissionRepository standingOrderPaymentSubmissionRepository,
            PaymentSubmissionValidator paymentSubmissionValidator,
            StandingOrderService standingOrderService) {
        this.standingOrderPaymentSubmissionRepository = standingOrderPaymentSubmissionRepository;
        this.paymentSubmissionValidator = paymentSubmissionValidator;
        this.standingOrderService = standingOrderService;
    }

    @Override
    public ResponseEntity<OBWriteInternationalStandingOrderResponse3> createInternationalStandingOrders(
            @Valid OBWriteInternationalStandingOrder3 OBWriteInternationalStandingOrder3,
            String xFapiFinancialId,
            String authorization,
            String xIdempotencyKey,
            String xJwsSignature,
            String xAccountId,
            DateTime xFapiCustomerLastLoggedTime,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal) throws OBErrorResponseException {
        return null;
    }

    @Override
    public ResponseEntity<OBWriteInternationalStandingOrderResponse3> getInternationalStandingOrdersInternationalStandingOrderPaymentId(
            String internationalStandingOrderPaymentId,
            String xFapiFinancialId,
            String authorization,
            DateTime xFapiCustomerLastLoggedTime,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal) throws OBErrorResponseException {
        return null;
    }

    private OBWriteInternationalStandingOrderResponse3 responseEntity(FRInternationalStandingOrderPaymentSubmission frPaymentSubmission) {
        return new OBWriteInternationalStandingOrderResponse3()
                .data(new OBWriteDataInternationalStandingOrderResponse3()
                        .internationalStandingOrderId(frPaymentSubmission.getId())
                        .initiation(toOBInternationalStandingOrder3(frPaymentSubmission.getStandingOrder().getData().getInitiation()))
                        .creationDateTime(frPaymentSubmission.getCreated())
                        .statusUpdateDateTime(frPaymentSubmission.getUpdated())
                        .status(toOBExternalStatus1Code(frPaymentSubmission.getStatus()))
                        .consentId(frPaymentSubmission.getStandingOrder().getData().getConsentId())
                )
                .links(createInternationalStandingOrderPaymentLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }
}
