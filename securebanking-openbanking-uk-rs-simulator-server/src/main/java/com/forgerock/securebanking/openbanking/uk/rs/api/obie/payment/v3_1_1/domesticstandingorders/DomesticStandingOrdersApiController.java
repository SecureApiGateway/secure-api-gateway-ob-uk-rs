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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_1_1.domesticstandingorders;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.account.FRStandingOrderData;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDataDomesticStandingOrder;
import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.payment.FRWriteDomesticStandingOrder;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.obie.OBVersion;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.VersionPathExtractor;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.payment.FRDomesticStandingOrderPaymentSubmission;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.IdempotentRepositoryAdapter;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.payments.DomesticStandingOrderPaymentSubmissionRepository;
import com.forgerock.securebanking.openbanking.uk.rs.service.standingorder.StandingOrderService;
import com.forgerock.securebanking.openbanking.uk.rs.validator.PaymentSubmissionValidator;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteDataDomesticStandingOrderResponse3;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticStandingOrder3;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticStandingOrderResponse3;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.common.FRSubmissionStatusConverter.toOBExternalStatus1Code;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRWriteDomesticStandingOrderConsentConverter.toOBDomesticStandingOrder3;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.payment.FRWriteDomesticStandingOrderConverter.toFRWriteDomesticStandingOrder;
import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.common.FRSubmissionStatus.INITIATIONPENDING;
import static com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.factories.FRStandingOrderDataFactory.createFRStandingOrderData;
import static com.forgerock.securebanking.openbanking.uk.rs.common.util.link.LinksHelper.createDomesticStandingOrderPaymentLink;
import static com.forgerock.securebanking.openbanking.uk.rs.common.util.PaymentApiResponseUtil.resourceConflictResponse;
import static com.forgerock.securebanking.openbanking.uk.rs.validator.ResourceVersionValidator.isAccessToResourceAllowed;

@Controller("DomesticStandingOrdersApiV3.1.1")
@Slf4j
public class DomesticStandingOrdersApiController implements DomesticStandingOrdersApi {

    private final DomesticStandingOrderPaymentSubmissionRepository standingOrderPaymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;
    private final StandingOrderService standingOrderService;

    public DomesticStandingOrdersApiController(
            DomesticStandingOrderPaymentSubmissionRepository standingOrderPaymentSubmissionRepository,
            PaymentSubmissionValidator paymentSubmissionValidator,
            StandingOrderService standingOrderService) {
        this.standingOrderPaymentSubmissionRepository = standingOrderPaymentSubmissionRepository;
        this.paymentSubmissionValidator = paymentSubmissionValidator;
        this.standingOrderService = standingOrderService;
    }

    @Override
    public ResponseEntity<OBWriteDomesticStandingOrderResponse3> createDomesticStandingOrders(
            @Valid OBWriteDomesticStandingOrder3 obWriteDomesticStandingOrder3,
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
        log.debug("Received payment submission: '{}'", obWriteDomesticStandingOrder3);

        paymentSubmissionValidator.validateIdempotencyKeyAndRisk(xIdempotencyKey, obWriteDomesticStandingOrder3.getRisk());

        FRWriteDomesticStandingOrder frStandingOrder = toFRWriteDomesticStandingOrder(obWriteDomesticStandingOrder3);
        log.trace("Converted to: '{}'", frStandingOrder);

        FRDomesticStandingOrderPaymentSubmission frPaymentSubmission = FRDomesticStandingOrderPaymentSubmission.builder()
                .id(obWriteDomesticStandingOrder3.getData().getConsentId())
                .standingOrder(frStandingOrder)
                .status(INITIATIONPENDING)
                .created(new DateTime())
                .updated(new DateTime())
                .idempotencyKey(xIdempotencyKey)
                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                .build();

        // Save the standing order
        frPaymentSubmission = new IdempotentRepositoryAdapter<>(standingOrderPaymentSubmissionRepository)
                .idempotentSave(frPaymentSubmission);

        // Save the standing order data for the Accounts API
        FRStandingOrderData standingOrderData = createFRStandingOrderData(frStandingOrder, xAccountId);
        standingOrderService.createStandingOrder(standingOrderData);

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

        FRDomesticStandingOrderPaymentSubmission frPaymentSubmission = isPaymentSubmission.get();
        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!isAccessToResourceAllowed(apiVersion, frPaymentSubmission.getObVersion())) {
            return resourceConflictResponse(frPaymentSubmission, apiVersion);
        }
        return ResponseEntity.ok(responseEntity(frPaymentSubmission));
    }

    private OBWriteDomesticStandingOrderResponse3 responseEntity(FRDomesticStandingOrderPaymentSubmission frPaymentSubmission) {
        FRWriteDataDomesticStandingOrder data = frPaymentSubmission.getStandingOrder().getData();
        return new OBWriteDomesticStandingOrderResponse3()
                .data(new OBWriteDataDomesticStandingOrderResponse3()
                        .domesticStandingOrderId(frPaymentSubmission.getId())
                        .initiation(toOBDomesticStandingOrder3(data.getInitiation()))
                        .creationDateTime(frPaymentSubmission.getCreated())
                        .statusUpdateDateTime(frPaymentSubmission.getUpdated())
                        .status(toOBExternalStatus1Code(frPaymentSubmission.getStatus()))
                        .consentId(data.getConsentId()))
                .links(createDomesticStandingOrderPaymentLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }

}
