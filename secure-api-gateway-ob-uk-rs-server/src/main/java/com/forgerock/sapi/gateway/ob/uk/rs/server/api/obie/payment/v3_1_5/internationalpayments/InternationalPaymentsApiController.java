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
/**
 * NOTE: This class is auto generated by the swagger code generator program (2.3.1).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_5.internationalpayments;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRSubmissionStatus.PENDING;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRAccountIdentifierConverter.toOBCashAccountDebtor4;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRChargeConverter.toOBWriteDomesticConsentResponse5DataCharges;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRSubmissionStatusConverter.toOBWriteInternationalResponse5DataStatus;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalConsentConverter.toOBWriteInternational3DataInitiation;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalConsentConverter.toOBWriteInternationalConsent5;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRWriteInternationalConverter.toFRWriteInternational;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.joda.time.DateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRResponseDataRefundConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.payment.FRExchangeRateConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRInternationalResponseDataRefund;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternational;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.payment.FRWriteInternationalData;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_5.internationalpayments.InternationalPaymentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.RefundAccountService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaymentApiResponseUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.VersionPathExtractor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.PaymentSubmissionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.ResourceVersionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteInternational3Validator.OBWriteInternational3ValidationContext;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.international.v3_1_10.InternationalPaymentConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.ConsumePaymentConsentRequest;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.international.v3_1_10.InternationalPaymentConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.FRInternationalPaymentSubmission;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.InternationalPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;

import lombok.extern.slf4j.Slf4j;
import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteInternational3;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalResponse5;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalResponse5Data;
import uk.org.openbanking.datamodel.payment.OBWritePaymentDetailsResponse1;
import uk.org.openbanking.datamodel.payment.OBWritePaymentDetailsResponse1Data;
import uk.org.openbanking.datamodel.payment.OBWritePaymentDetailsResponse1DataPaymentStatus;
import uk.org.openbanking.datamodel.payment.OBWritePaymentDetailsResponse1DataStatusDetail;

@Controller("InternationalPaymentsApiV3.1.5")
@Slf4j
public class InternationalPaymentsApiController implements InternationalPaymentsApi {

    private final InternationalPaymentSubmissionRepository paymentSubmissionRepository;
    private final PaymentSubmissionValidator paymentSubmissionValidator;

    private final InternationalPaymentConsentStoreClient consentStoreClient;

    private final OBValidationService<OBWriteInternational3ValidationContext> paymentValidator;

    private final RefundAccountService refundAccountService;

    public InternationalPaymentsApiController(
            InternationalPaymentSubmissionRepository paymentSubmissionRepository,
            PaymentSubmissionValidator paymentSubmissionValidator,
            InternationalPaymentConsentStoreClient consentStoreClient,
            OBValidationService<OBWriteInternational3ValidationContext> paymentValidator,
            RefundAccountService refundAccountService) {
        this.paymentSubmissionRepository = paymentSubmissionRepository;
        this.paymentSubmissionValidator = paymentSubmissionValidator;
        this.consentStoreClient = consentStoreClient;
        this.paymentValidator = paymentValidator;
        this.refundAccountService = refundAccountService;
    }

    @Override
    public ResponseEntity<OBWriteInternationalResponse5> createInternationalPayments(
            @Valid OBWriteInternational3 obWriteInternational3,
            String authorization,
            String xIdempotencyKey,
            String xJwsSignature,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException {
        log.debug("Received payment submission: '{}'", obWriteInternational3);

        paymentSubmissionValidator.validateIdempotencyKeyAndRisk(xIdempotencyKey, obWriteInternational3.getRisk());

        String consentId = obWriteInternational3.getData().getConsentId();

        log.debug("Attempting to get consent: {}, clientId: {}", consentId, apiClientId);
        final InternationalPaymentConsent consent = consentStoreClient.getConsent(consentId, apiClientId);
        log.debug("Got consent from store: {}", consent);

        FRWriteInternational frInternationalPayment = toFRWriteInternational(obWriteInternational3);
        log.trace("Converted to: '{}'", frInternationalPayment);

        // validate the consent against the request
        log.debug("Validating International Payment submission");
        paymentValidator.validate(new OBWriteInternational3ValidationContext(obWriteInternational3,
                toOBWriteInternationalConsent5(consent.getRequestObj()), consent.getStatus()));
        log.debug("International Payment validation successful");

        FRInternationalPaymentSubmission frPaymentSubmission = FRInternationalPaymentSubmission.builder()
                .id(obWriteInternational3.getData().getConsentId())
                .payment(frInternationalPayment)
                .status(PENDING)
                .created(new DateTime())
                .updated(new DateTime())
                .idempotencyKey(xIdempotencyKey)
                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                .build();

        // Save the international payment
        frPaymentSubmission = paymentSubmissionRepository.save(frPaymentSubmission);

        final ConsumePaymentConsentRequest consumePaymentRequest = new ConsumePaymentConsentRequest();
        consumePaymentRequest.setConsentId(consentId);
        consumePaymentRequest.setApiClientId(apiClientId);
        consentStoreClient.consumeConsent(consumePaymentRequest);

        return ResponseEntity.status(CREATED).body(responseEntity(consent, frPaymentSubmission));
    }

    @Override
    public ResponseEntity getInternationalPaymentsInternationalPaymentId(
            String internationalPaymentId,
            String authorization,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal
    ) {
        Optional<FRInternationalPaymentSubmission> isPaymentSubmission = paymentSubmissionRepository.findById(internationalPaymentId);
        if (!isPaymentSubmission.isPresent()) {
            return ResponseEntity.status(BAD_REQUEST).body("Payment submission '" + internationalPaymentId + "' can't be found");
        }

        FRInternationalPaymentSubmission frPaymentSubmission = isPaymentSubmission.get();
        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!ResourceVersionValidator.isAccessToResourceAllowed(apiVersion, frPaymentSubmission.getObVersion())) {
            return PaymentApiResponseUtil.resourceConflictResponse(frPaymentSubmission, apiVersion);
        }
        //get the consent
        final InternationalPaymentConsent consent = consentStoreClient.getConsent(frPaymentSubmission.getConsentId(), apiClientId);
        log.debug("Got consent from store: {}", consent);

        return ResponseEntity.ok(responseEntity(consent, frPaymentSubmission));
    }

    @Override
    public ResponseEntity getInternationalPaymentsInternationalPaymentIdPaymentDetails(
            String internationalPaymentId,
            String authorization,
            DateTime xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal
    ) {
        Optional<FRInternationalPaymentSubmission> isInternationalPaymentSubmission = paymentSubmissionRepository.findById(internationalPaymentId);
        if (!isInternationalPaymentSubmission.isPresent()) {
            return ResponseEntity.status(BAD_REQUEST).body("International payment submission '" + internationalPaymentId + "' can't be found");
        }

        FRInternationalPaymentSubmission frInternationalPaymentSubmission = isInternationalPaymentSubmission.get();
        log.debug("Found The International Payment '{}' to get details.", internationalPaymentId);
        OBVersion apiVersion = VersionPathExtractor.getVersionFromPath(request);
        if (!ResourceVersionValidator.isAccessToResourceAllowed(apiVersion, frInternationalPaymentSubmission.getObVersion())) {
            return PaymentApiResponseUtil.resourceConflictResponse(frInternationalPaymentSubmission, apiVersion);
        }
        return ResponseEntity.ok(responseEntityDetails(frInternationalPaymentSubmission));
    }

    private OBWriteInternationalResponse5 responseEntity(
            InternationalPaymentConsent consent,
            FRInternationalPaymentSubmission frPaymentSubmission
    ) {
        FRWriteInternationalData data = frPaymentSubmission.getPayment().getData();

        final Optional<FRInternationalResponseDataRefund> refundAccountData = refundAccountService.getInternationalPaymentRefundData(
                consent.getRequestObj().getData().getReadRefundAccount(),
                consent.getRequestObj().getData().getInitiation().getCreditor(),
                consent.getRequestObj().getData().getInitiation().getCreditorAgent(),
                consent);

        return new OBWriteInternationalResponse5()
                .data(new OBWriteInternationalResponse5Data()
                        .charges(toOBWriteDomesticConsentResponse5DataCharges(consent.getCharges()))
                        .internationalPaymentId(frPaymentSubmission.getId())
                        .initiation(toOBWriteInternational3DataInitiation(data.getInitiation()))
                        .creationDateTime(frPaymentSubmission.getCreated())
                        .statusUpdateDateTime(frPaymentSubmission.getUpdated())
                        .status(toOBWriteInternationalResponse5DataStatus(frPaymentSubmission.getStatus()))
                        .consentId(data.getConsentId())
                        .debtor(toOBCashAccountDebtor4(data.getInitiation().getDebtorAccount()))
                        .refund(refundAccountData.map(FRResponseDataRefundConverter::toOBWriteInternationalResponse5DataRefund).orElse(null))
                        .exchangeRateInformation(FRExchangeRateConverter.toOBWriteInternationalConsentResponse6DataExchangeRateInformation(consent.getExchangeRateInformation())))
                .links(LinksHelper.createInternationalPaymentLink(this.getClass(), frPaymentSubmission.getId()))
                .meta(new Meta());
    }

    private OBWritePaymentDetailsResponse1 responseEntityDetails(FRInternationalPaymentSubmission frInternationalPaymentSubmission) {
        OBWritePaymentDetailsResponse1DataPaymentStatus.StatusEnum status = OBWritePaymentDetailsResponse1DataPaymentStatus.StatusEnum.fromValue(
                frInternationalPaymentSubmission.getStatus().getValue()
        );

        // Build the response object with data to meet the expected data defined by the spec
        OBWritePaymentDetailsResponse1DataStatusDetail.StatusReasonEnum statusReasonEnum = OBWritePaymentDetailsResponse1DataStatusDetail.StatusReasonEnum.PENDINGSETTLEMENT;
        return new OBWritePaymentDetailsResponse1()
                .data(
                        new OBWritePaymentDetailsResponse1Data()
                                .addPaymentStatusItem(
                                        new OBWritePaymentDetailsResponse1DataPaymentStatus()
                                                .status(status)
                                                .paymentTransactionId(UUID.randomUUID().toString())
                                                .statusUpdateDateTime(new DateTime(frInternationalPaymentSubmission.getUpdated()))
                                                .statusDetail(
                                                        new OBWritePaymentDetailsResponse1DataStatusDetail()
                                                                .status(status.getValue())
                                                                .statusReason(statusReasonEnum)
                                                                .statusReasonDescription(statusReasonEnum.getValue())
                                                )
                                )
                )
                .links(LinksHelper.createInternationalPaymentDetailsLink(this.getClass(), frInternationalPaymentSubmission.getId()))
                .meta(new Meta());
    }
}
