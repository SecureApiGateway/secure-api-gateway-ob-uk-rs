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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_0.domesticstandingorders;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticStandingOrder;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.rs.validator.PaymentSubmissionValidator;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.VersionPathExtractor;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.FRDomesticStandingOrderPaymentSubmission;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.IdempotentRepositoryAdapter;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.DomesticStandingOrderPaymentSubmissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteDataDomesticStandingOrderResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticStandingOrder1;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticStandingOrderResponse1;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

import static com.forgerock.securebanking.openbanking.uk.rs.api.obie.LinksHelper.createDomesticStandingOrderPaymentLink;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRSubmissionStatusConverter.toOBExternalStatus1Code;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRWriteDomesticStandingOrderConsentConverter.toOBDomesticStandingOrder1;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRWriteDomesticStandingOrderConverter.toFRWriteDomesticStandingOrder;
import static com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.FRSubmissionStatus.INITIATIONPENDING;

@Controller("DomesticStandingOrdersApiV3.0")
@Slf4j
public class DomesticStandingOrdersApiController implements DomesticStandingOrdersApi {

    private final DomesticStandingOrderPaymentSubmissionRepository standingOrderPaymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;

    public DomesticStandingOrdersApiController(
            DomesticStandingOrderPaymentSubmissionRepository standingOrderPaymentSubmissionRepository,
            PaymentSubmissionValidator paymentSubmissionValidator) {
        this.standingOrderPaymentSubmissionRepository = standingOrderPaymentSubmissionRepository;
        this.paymentSubmissionValidator = paymentSubmissionValidator;
    }

    @Override
    public ResponseEntity<OBWriteDomesticStandingOrderResponse1> createDomesticStandingOrders(
            @Valid OBWriteDomesticStandingOrder1 obWriteDomesticStandingOrder1,
            String xFapiFinancialId,
            String authorization,
            String xIdempotencyKey,
            String xJwsSignature,
            DateTime xFapiCustomerLastLoggedTime,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal) throws OBErrorResponseException {
        log.debug("Received payment submission: '{}'", obWriteDomesticStandingOrder1);

        // TODO - before we get this far, the IG will need to:
        //      - verify the consent status
        //      - verify the payment details match those in the payment consent
        //      - verify security concerns (e.g. detached JWS, access token, roles, MTLS etc.)

        paymentSubmissionValidator.validateIdempotencyKeyAndRisk(xIdempotencyKey, obWriteDomesticStandingOrder1.getRisk());

        FRWriteDomesticStandingOrder frStandingOrder = toFRWriteDomesticStandingOrder(obWriteDomesticStandingOrder1);
        log.trace("Converted to: '{}'", frStandingOrder);

        FRDomesticStandingOrderPaymentSubmission frPaymentSubmission = FRDomesticStandingOrderPaymentSubmission.builder()
                // TODO - openbanking-aspsp uses the consent id - is this for convenience? Could it be a problem?
                .id(frStandingOrder.getData().getConsentId())
                .domesticStandingOrder(frStandingOrder)
                .status(INITIATIONPENDING)
                .created(new DateTime())
                .updated(new DateTime())
                .idempotencyKey(xIdempotencyKey)
                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                .build();
        frPaymentSubmission = new IdempotentRepositoryAdapter<>(standingOrderPaymentSubmissionRepository)
                .idempotentSave(frPaymentSubmission);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseEntity(frPaymentSubmission));
    }

    @Override
    public ResponseEntity getDomesticStandingOrdersDomesticStandingOrderId(
            String domesticStandingOrderId,
            String xFapiFinancialId,
            String authorization,
            DateTime xFapiCustomerLastLoggedTime,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal) {
        Optional<FRDomesticStandingOrderPaymentSubmission> isPaymentSubmission = standingOrderPaymentSubmissionRepository.findById(domesticStandingOrderId);
        if (!isPaymentSubmission.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment submission '" + domesticStandingOrderId + "' can't be found");
        }

        return ResponseEntity.ok(responseEntity(isPaymentSubmission.get()));
    }

    private OBWriteDomesticStandingOrderResponse1 responseEntity(FRDomesticStandingOrderPaymentSubmission frPaymentSubmission) {
        return new OBWriteDomesticStandingOrderResponse1().data(new OBWriteDataDomesticStandingOrderResponse1()
                .domesticStandingOrderId(frPaymentSubmission.getId())
                .initiation(toOBDomesticStandingOrder1(frPaymentSubmission.getDomesticStandingOrder().getData().getInitiation()))
                .creationDateTime(frPaymentSubmission.getCreated())
                .statusUpdateDateTime(frPaymentSubmission.getUpdated())
                .status(toOBExternalStatus1Code(frPaymentSubmission.getStatus()))
                .consentId(frPaymentSubmission.getDomesticStandingOrder().getData().getConsentId()))
                .links(createDomesticStandingOrderPaymentLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }
}
