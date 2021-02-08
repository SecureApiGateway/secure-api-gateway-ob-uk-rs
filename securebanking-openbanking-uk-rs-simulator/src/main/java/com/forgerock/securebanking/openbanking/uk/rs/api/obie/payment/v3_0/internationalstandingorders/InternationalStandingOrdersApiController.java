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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_0.internationalstandingorders;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRStandingOrderData;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalStandingOrder;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalStandingOrderDataInitiation;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.VersionPathExtractor;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.FRInternationalStandingOrderPaymentSubmission;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.IdempotentRepositoryAdapter;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.InternationalStandingOrderPaymentSubmissionRepository;
import com.forgerock.securebanking.openbanking.uk.rs.service.frequency.FrequencyService;
import com.forgerock.securebanking.openbanking.uk.rs.service.standingorder.StandingOrderService;
import com.forgerock.securebanking.openbanking.uk.rs.validator.PaymentSubmissionValidator;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.account.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteDataInternationalStandingOrderResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrder1;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderResponse1;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRStandingOrderData.FRStandingOrderStatus.ACTIVE;
import static com.forgerock.securebanking.openbanking.uk.rs.api.obie.LinksHelper.createInternationalStandingOrderPaymentLink;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRSubmissionStatusConverter.toOBExternalStatus1Code;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRWriteInternationalStandingOrderConsentConverter.toOBInternationalStandingOrder1;
import static com.forgerock.securebanking.openbanking.uk.rs.converter.payment.FRWriteInternationalStandingOrderConverter.toFRWriteInternationalStandingOrder;
import static com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.FRSubmissionStatus.PENDING;

@Controller("InternationalStandingOrdersApiV3.0")
@Slf4j
public class InternationalStandingOrdersApiController implements InternationalStandingOrdersApi {

    private final InternationalStandingOrderPaymentSubmissionRepository standingOrderPaymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;
    private final FrequencyService frequencyService;
    private final StandingOrderService standingOrderService;

    public InternationalStandingOrdersApiController(
            InternationalStandingOrderPaymentSubmissionRepository standingOrderPaymentSubmissionRepository,
            PaymentSubmissionValidator paymentSubmissionValidator,
            FrequencyService frequencyService,
            StandingOrderService standingOrderService) {
        this.standingOrderPaymentSubmissionRepository = standingOrderPaymentSubmissionRepository;
        this.paymentSubmissionValidator = paymentSubmissionValidator;
        this.frequencyService = frequencyService;
        this.standingOrderService = standingOrderService;
    }

    @Override
    public ResponseEntity<OBWriteInternationalStandingOrderResponse1> createInternationalStandingOrders(
            @Valid OBWriteInternationalStandingOrder1 obWriteInternationalStandingOrder1,
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
            Principal principal
    ) throws OBErrorResponseException {
        log.debug("Received payment submission: '{}'", obWriteInternationalStandingOrder1);

        // TODO - before we get this far, the IG will need to:
        //      - verify the consent status
        //      - verify the payment details match those in the payment consent
        //      - verify security concerns (e.g. detached JWS, access token, roles, MTLS etc.)

        paymentSubmissionValidator.validateIdempotencyKeyAndRisk(xIdempotencyKey, obWriteInternationalStandingOrder1.getRisk());

        FRWriteInternationalStandingOrder frStandingOrder = toFRWriteInternationalStandingOrder(obWriteInternationalStandingOrder1);
        log.trace("Converted to: '{}'", frStandingOrder);

        FRInternationalStandingOrderPaymentSubmission frPaymentSubmission = FRInternationalStandingOrderPaymentSubmission.builder()
                .id(UUID.randomUUID().toString())
                .standingOrder(frStandingOrder)
                .status(PENDING)
                .created(new DateTime())
                .updated(new DateTime())
                .idempotencyKey(xIdempotencyKey)
                .version(VersionPathExtractor.getVersionFromPath(request))
                .build();

        // Save the international standing order
        frPaymentSubmission = new IdempotentRepositoryAdapter<>(standingOrderPaymentSubmissionRepository)
                .idempotentSave(frPaymentSubmission);

        // Save the standing order data for the Accounts API
        FRStandingOrderData standingOrderData = frStandingOrderData(frStandingOrder.getData().getInitiation(), xAccountId);
        standingOrderService.createStandingOrder(standingOrderData);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseEntity(frPaymentSubmission));
    }

    @Override
    public ResponseEntity getInternationalStandingOrdersInternationalStandingOrderPaymentId(
            String internationalStandingOrderPaymentId,
            String xFapiFinancialId,
            String authorization,
            DateTime xFapiCustomerLastLoggedTime,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal
    ) {
        Optional<FRInternationalStandingOrderPaymentSubmission> isPaymentSubmission = standingOrderPaymentSubmissionRepository.findById(internationalStandingOrderPaymentId);
        if (!isPaymentSubmission.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment submission '" + internationalStandingOrderPaymentId + "' can't be found");
        }

        return ResponseEntity.ok(responseEntity(isPaymentSubmission.get()));
    }

    private FRStandingOrderData frStandingOrderData(FRWriteInternationalStandingOrderDataInitiation initiation, String accountId) {
        FRStandingOrderData standingOrderData = FRStandingOrderData.builder()
                .standingOrderId(UUID.randomUUID().toString())
                .accountId(accountId)
                .standingOrderStatusCode(ACTIVE)
                .creditorAccount(initiation.getCreditorAccount())
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .firstPaymentDateTime(initiation.getFirstPaymentDateTime())
                .firstPaymentAmount(initiation.getInstructedAmount())
                .nextPaymentAmount(initiation.getInstructedAmount())
                .nextPaymentDateTime(frequencyService.getNextDateTime(
                        initiation.getFirstPaymentDateTime(),
                        initiation.getFrequency()))
                .finalPaymentDateTime(initiation.getFinalPaymentDateTime())
                .finalPaymentAmount(initiation.getInstructedAmount())
                .frequency(initiation.getFrequency())
                .reference(initiation.getReference())
                .numberOfPayments(initiation.getNumberOfPayments())
                .build();
        return standingOrderData;
    }

    private OBWriteInternationalStandingOrderResponse1 responseEntity(FRInternationalStandingOrderPaymentSubmission frPaymentSubmission) {
        return new OBWriteInternationalStandingOrderResponse1()
                .data(new OBWriteDataInternationalStandingOrderResponse1()
                        .internationalStandingOrderId(frPaymentSubmission.getId())
                        .initiation(toOBInternationalStandingOrder1(frPaymentSubmission.getStandingOrder().getData().getInitiation()))
                        .creationDateTime(frPaymentSubmission.getCreated())
                        .statusUpdateDateTime(frPaymentSubmission.getUpdated())
                        .status(toOBExternalStatus1Code(frPaymentSubmission.getStatus()))
                        .consentId(frPaymentSubmission.getStandingOrder().getData().getConsentId())
                )
                .links(createInternationalStandingOrderPaymentLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }
}
