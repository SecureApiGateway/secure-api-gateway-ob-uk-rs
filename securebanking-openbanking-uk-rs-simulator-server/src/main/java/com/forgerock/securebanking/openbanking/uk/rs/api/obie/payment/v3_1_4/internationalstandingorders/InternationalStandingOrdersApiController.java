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
/**
 * NOTE: This class is auto generated by the swagger code generator program (2.3.1).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_1_4.internationalstandingorders;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRStandingOrderData;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.common.FRResponseDataRefundConverter;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRInternationalResponseDataRefund;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRReadRefundAccount;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalStandingOrder;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteInternationalStandingOrderData;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.OBVersion;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.VersionPathExtractor;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.FRInternationalStandingOrderPaymentSubmission;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.IdempotentRepositoryAdapter;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.InternationalStandingOrderPaymentSubmissionRepository;
import com.forgerock.securebanking.openbanking.uk.rs.service.standingorder.StandingOrderService;
import com.forgerock.securebanking.openbanking.uk.rs.validator.PaymentSubmissionValidator;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrder4;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderResponse6;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderResponse6Data;
import uk.org.openbanking.datamodel.payment.OBWritePaymentDetailsResponse1;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.common.FRSubmissionStatusConverter.toOBWriteInternationalStandingOrderResponse6DataStatus;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRWriteInternationalStandingOrderConsentConverter.toOBWriteInternationalStandingOrder4DataInitiation;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRWriteInternationalStandingOrderConverter.toFRWriteInternationalStandingOrder;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRSubmissionStatus.PENDING;
import static com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.factories.FRStandingOrderDataFactory.createFRStandingOrderData;
import static com.forgerock.securebanking.openbanking.uk.rs.common.refund.FRReadRefundAccountFactory.frReadRefundAccount;
import static com.forgerock.securebanking.openbanking.uk.rs.common.refund.FRResponseDataRefundFactory.frInternationalResponseDataRefund;
import static com.forgerock.securebanking.openbanking.uk.rs.common.util.PaymentApiResponseUtil.resourceConflictResponse;
import static com.forgerock.securebanking.openbanking.uk.rs.common.util.link.LinksHelper.createInternationalStandingOrderPaymentLink;
import static com.forgerock.securebanking.openbanking.uk.rs.validator.ResourceVersionValidator.isAccessToResourceAllowed;
import static org.springframework.http.HttpStatus.*;

@Controller("InternationalStandingOrdersApiV3.1.4")
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
    public ResponseEntity<OBWriteInternationalStandingOrderResponse6> createInternationalStandingOrders(
            @Valid OBWriteInternationalStandingOrder4 obWriteInternationalStandingOrder4,
            String authorization,
            String xIdempotencyKey,
            String xJwsSignature,
            String xAccountId,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String xReadRefundAccount,
            HttpServletRequest request,
            Principal principal) throws OBErrorResponseException {
        log.debug("Received payment submission: '{}'", obWriteInternationalStandingOrder4);

        paymentSubmissionValidator.validateIdempotencyKeyAndRisk(xIdempotencyKey, obWriteInternationalStandingOrder4.getRisk());

        FRWriteInternationalStandingOrder frStandingOrder = toFRWriteInternationalStandingOrder(obWriteInternationalStandingOrder4);
        log.trace("Converted to: '{}'", frStandingOrder);

        FRInternationalStandingOrderPaymentSubmission frPaymentSubmission = FRInternationalStandingOrderPaymentSubmission.builder()
                .id(obWriteInternationalStandingOrder4.getData().getConsentId())
                .standingOrder(frStandingOrder)
                .status(PENDING)
                .created(new DateTime())
                .updated(new DateTime())
                .idempotencyKey(xIdempotencyKey)
                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                .build();

        // Save the international standing order
        frPaymentSubmission = new IdempotentRepositoryAdapter<>(standingOrderPaymentSubmissionRepository)
                .idempotentSave(frPaymentSubmission);

        // Save the standing order data for the Accounts API
        FRStandingOrderData standingOrderData = createFRStandingOrderData(frStandingOrder, xAccountId);
        standingOrderService.createStandingOrder(standingOrderData);

        return ResponseEntity.status(CREATED).body(responseEntity(frPaymentSubmission, frReadRefundAccount(xReadRefundAccount)));
    }

    @Override
    public ResponseEntity getInternationalStandingOrdersInternationalStandingOrderPaymentId(
            String internationalStandingOrderPaymentId,
            String authorization,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String xReadRefundAccount,
            HttpServletRequest request,
            Principal principal) {
        Optional<FRInternationalStandingOrderPaymentSubmission> isPaymentSubmission = standingOrderPaymentSubmissionRepository.findById(internationalStandingOrderPaymentId);
        if (!isPaymentSubmission.isPresent()) {
            return ResponseEntity.status(BAD_REQUEST).body("Payment submission '" + internationalStandingOrderPaymentId + "' can't be found");
        }

        FRInternationalStandingOrderPaymentSubmission frPaymentSubmission = isPaymentSubmission.get();
        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!isAccessToResourceAllowed(apiVersion, frPaymentSubmission.getObVersion())) {
            return resourceConflictResponse(frPaymentSubmission, apiVersion);
        }
        return ResponseEntity.ok(responseEntity(frPaymentSubmission, frReadRefundAccount(xReadRefundAccount)));
    }

    @Override
    public ResponseEntity<OBWritePaymentDetailsResponse1> getInternationalStandingOrdersInternationalStandingOrderPaymentIdPaymentDetails(
            String internationalStandingOrderPaymentId,
            String authorization,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal
    ) {
        // Optional endpoint - not implemented
        return new ResponseEntity<>(NOT_IMPLEMENTED);
    }

    private OBWriteInternationalStandingOrderResponse6 responseEntity(FRInternationalStandingOrderPaymentSubmission frPaymentSubmission,
                                                                      FRReadRefundAccount readRefundAccount) {
        FRWriteInternationalStandingOrderData data = frPaymentSubmission.getStandingOrder().getData();
        Optional<FRInternationalResponseDataRefund> refund = frInternationalResponseDataRefund(readRefundAccount, data.getInitiation());
        return new OBWriteInternationalStandingOrderResponse6()
                .data(new OBWriteInternationalStandingOrderResponse6Data()
                        .internationalStandingOrderId(frPaymentSubmission.getId())
                        .initiation(toOBWriteInternationalStandingOrder4DataInitiation(data.getInitiation()))
                        .creationDateTime(frPaymentSubmission.getCreated())
                        .statusUpdateDateTime(frPaymentSubmission.getUpdated())
                        .status(toOBWriteInternationalStandingOrderResponse6DataStatus(frPaymentSubmission.getStatus()))
                        .consentId(data.getConsentId())
                        .refund(refund.map(FRResponseDataRefundConverter::toOBWriteInternationalResponse4DataRefund).orElse(null)))
                .links(createInternationalStandingOrderPaymentLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }
}
