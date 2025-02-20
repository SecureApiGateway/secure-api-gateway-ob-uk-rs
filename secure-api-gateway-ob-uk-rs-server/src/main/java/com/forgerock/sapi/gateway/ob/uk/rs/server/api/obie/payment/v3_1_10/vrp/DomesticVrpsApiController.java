/*
 * Copyright © 2020-2025 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_10.vrp;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.common.FRSubmissionStatusConverter.toFRSubmissionStatus;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.common.FRSubmissionStatusConverter.toOBDomesticVRPResponseDataStatusEnum;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.vrp.FRDomesticVrpConverters.toFRDomesticVRPRequest;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.vrp.FRDomesticVrpConverters.toOBDomesticVRPRequest;
import static org.springframework.http.HttpStatus.CREATED;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRResponseDataRefund;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.common.FRResponseDataRefundConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.vrp.FRDomesticVRPConsentConverters;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRDomesticVrpRequest;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_10.vrp.DomesticVrpsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.RefundAccountService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.simulations.vrp.PeriodicLimitBreachResponseSimulatorService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.VersionPathExtractor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.idempotency.IdempotentPaymentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.idempotency.VRPIdempotentPaymentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.PaymentSubmissionValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBDomesticVRPRequestValidator.OBDomesticVRPRequestValidationContext;
import com.forgerock.sapi.gateway.rcs.consent.store.client.payment.vrp.DomesticVRPConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.vrp.v3_1_10.DomesticVRPConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.payment.FRDomesticVrpPaymentSubmission;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.payments.DomesticVrpPaymentSubmissionRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import uk.org.openbanking.datamodel.v3.common.Meta;
import uk.org.openbanking.datamodel.v3.common.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.v3.common.OBChargeBearerType1Code;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPDetails;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPDetailsData;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPDetailsDataPaymentStatusInner;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPDetailsDataPaymentStatusInnerStatus;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPDetailsDataPaymentStatusInnerStatusDetail;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPDetailsDataPaymentStatusInnerStatusDetailStatusReason;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPRequest;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPResponse;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPResponseData;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPResponseDataChargesInner;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPResponseDataStatus;
import uk.org.openbanking.datamodel.v3.vrp.OBExternalPaymentChargeType1Code;

@Controller("DomesticVrpsApiV3.1.10")
@Slf4j
public class DomesticVrpsApiController implements DomesticVrpsApi {

    protected static final String DEFAULT_CHARGE_AMOUNT = "1.00";
    protected static final String DEFAULT_CHARGE_CURRENCY = "GBP";

    private final OBValidationService<OBDomesticVRPRequestValidationContext> paymentRequestValidator;
    private final DomesticVrpPaymentSubmissionRepository paymentSubmissionRepository;
    private final PeriodicLimitBreachResponseSimulatorService limitBreachResponseSimulatorService;
    private final PaymentSubmissionValidator paymentSubmissionValidator;
    private final RefundAccountService refundAccountService;
    private final DomesticVRPConsentStoreClient consentStoreClient;
    private final IdempotentPaymentService<FRDomesticVrpPaymentSubmission, FRDomesticVrpRequest> idempotentPaymentService;

    public DomesticVrpsApiController(
            DomesticVrpPaymentSubmissionRepository paymentSubmissionRepository,
            OBValidationService<OBDomesticVRPRequestValidationContext> paymentRequestValidator,
            @Qualifier("v3.1.10RestDomesticVRPConsentStoreClient") DomesticVRPConsentStoreClient consentStoreClient,
            PeriodicLimitBreachResponseSimulatorService limitBreachResponseSimulatorService,
            PaymentSubmissionValidator paymentSubmissionValidator,
            RefundAccountService refundAccountService
    ) {
        this.paymentSubmissionRepository = Objects.requireNonNull(paymentSubmissionRepository, "PaymentSubmissionRepository cannot be null");
        this.paymentRequestValidator = Objects.requireNonNull(paymentRequestValidator, "PaymentRequestValidator cannot be null");
        this.consentStoreClient = Objects.requireNonNull(consentStoreClient, "ConsentStoreClient cannot be null");
        this.limitBreachResponseSimulatorService = Objects.requireNonNull(limitBreachResponseSimulatorService, "LimitBreachResponseSimulatorService cannot be null");
        this.paymentSubmissionValidator = Objects.requireNonNull(paymentSubmissionValidator, "PaymentSubmissionValidator cannot be null");
        this.refundAccountService = Objects.requireNonNull(refundAccountService, "RefundAccountService cannot be null");
        this.idempotentPaymentService = new VRPIdempotentPaymentService(
                Objects.requireNonNull(paymentSubmissionRepository, "PaymentSubmissionRepository cannot be null for IdempotentPaymentService")
        );
    }

    @Override
    public ResponseEntity domesticVrpGet(
            String domesticVRPId,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal
    ) {
        log.debug("Searching for VRP payment by id '{}'", domesticVRPId);
        Optional<FRDomesticVrpPaymentSubmission> optionalVrpPayment = paymentSubmissionRepository.findById(domesticVRPId);

        if (!optionalVrpPayment.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Domestic VRP payment '" + domesticVRPId + "' " +
                    "can't be found");
        }
        log.debug("Found VRP payment '{}'", domesticVRPId);

        final FRDomesticVrpPaymentSubmission paymentSubmission = optionalVrpPayment.get();
        final DomesticVRPConsent consent = consentStoreClient.getConsent(paymentSubmission.getConsentId(), apiClientId);
        OBDomesticVRPResponse entity = responseEntity(consent, paymentSubmission);

        return ResponseEntity.ok(entity);
    }

    @Override
    public ResponseEntity domesticVrpPaymentDetailsGet(
            String domesticVRPId,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String apiClientId,
            HttpServletRequest request,
            Principal principal
    ) {
        Optional<FRDomesticVrpPaymentSubmission> optionalVrpPayment = paymentSubmissionRepository.findById(domesticVRPId);
        if (!optionalVrpPayment.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Domestic VRP payment '" + domesticVRPId + "' " +
                    "can't be found to retrieve the details");
        }
        log.debug("Found VRP payment '{}' to get details.", domesticVRPId);
        // Build the response object with data just to meet the expected data defined by the spec
        FRDomesticVrpPaymentSubmission paymentSubmission = optionalVrpPayment.get();
        OBDomesticVRPDetailsDataPaymentStatusInnerStatus status = OBDomesticVRPDetailsDataPaymentStatusInnerStatus.fromValue(
                paymentSubmission.getStatus().getValue()
        );

        OBDomesticVRPDetailsDataPaymentStatusInnerStatusDetailStatusReason statusReasonEnum = OBDomesticVRPDetailsDataPaymentStatusInnerStatusDetailStatusReason.PENDINGSETTLEMENT;
        String localInstrument = paymentSubmission.getPayment().getData().getInstruction().getLocalInstrument();
        OBDomesticVRPDetails vrpDetails = new OBDomesticVRPDetails()
                .data(
                        new OBDomesticVRPDetailsData()
                                .addPaymentStatusItem(
                                        new OBDomesticVRPDetailsDataPaymentStatusInner()
                                                .status(status)
                                                .paymentTransactionId(paymentSubmission.getTransactionId())
                                                .statusUpdateDateTime(new DateTime(paymentSubmission.getUpdated()))
                                                .statusDetail(
                                                        new OBDomesticVRPDetailsDataPaymentStatusInnerStatusDetail()
                                                                .localInstrument(localInstrument)
                                                                .status(status.getValue())
                                                                .statusReason(statusReasonEnum)
                                                                .statusReasonDescription(statusReasonEnum.getValue())
                                                )
                                )

                );
        return ResponseEntity.ok(vrpDetails);
    }

    @Override
    public ResponseEntity<OBDomesticVRPResponse> domesticVrpPost(
            String authorization,
            String xIdempotencyKey,
            String xJwsSignature,
            OBDomesticVRPRequest obDomesticVRPRequest,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String xVrpLimitBreachResponseSimulation,
            String apiClientId,
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException, OBErrorException {
        log.debug("Received VRP payment submission: '{}'", obDomesticVRPRequest);

        paymentSubmissionValidator.validateIdempotencyKey(xIdempotencyKey);

        String consentId = obDomesticVRPRequest.getData().getConsentId();
        final DomesticVRPConsent consent = consentStoreClient.getConsent(consentId, apiClientId);
        if (xVrpLimitBreachResponseSimulation != null) {
            log.info("Executing Limit breach simulation, value of header: {}", xVrpLimitBreachResponseSimulation);
            limitBreachResponseSimulatorService.processRequest(xVrpLimitBreachResponseSimulation, consent.getRequestObj());
        }

        FRDomesticVrpRequest frDomesticVRPRequest = toFRDomesticVRPRequest(obDomesticVRPRequest);

        final Optional<FRDomesticVrpPaymentSubmission> existingPayment =
                idempotentPaymentService.findExistingPayment(frDomesticVRPRequest, consentId, apiClientId, xIdempotencyKey);
        if (existingPayment.isPresent()) {
            final FRDomesticVrpPaymentSubmission paymentSubmission = existingPayment.get();
            log.info("Payment submission is a replay of a previous payment, returning previously created payment for x-idempotencyKey: {}, FRDomesticVrpPaymentSubmission.id: {}",
                    xIdempotencyKey, paymentSubmission.getId());
            return ResponseEntity.status(CREATED).body(responseEntity(consent, paymentSubmission));
        }

        // validate the consent against the instruction
        log.debug("Validating VRP submission");
        paymentRequestValidator.validate(new OBDomesticVRPRequestValidationContext(obDomesticVRPRequest,
                FRDomesticVRPConsentConverters.toOBDomesticVRPConsentRequest(consent.getRequestObj()), consent.getStatus()));

        log.debug("VRP validation successful! Creating the payment.");

        FRDomesticVrpPaymentSubmission vrpPaymentSubmission = FRDomesticVrpPaymentSubmission.builder()
                .id(UUID.randomUUID().toString())
                .transactionId(UUID.randomUUID().toString())
                .idempotencyKey(xIdempotencyKey)
                .idempotencyKeyExpiration(DateTime.now().plusHours(24))
                .apiClientId(apiClientId)
                .consentId(frDomesticVRPRequest.data.consentId)
                .payment(frDomesticVRPRequest)
                .status(toFRSubmissionStatus(OBDomesticVRPResponseDataStatus.PENDING))
                .created(new Date())
                .updated(new Date())
                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                .build();

        //save the domestic vrp
        idempotentPaymentService.savePayment(vrpPaymentSubmission);

        OBDomesticVRPResponse entity = responseEntity(consent, obDomesticVRPRequest, vrpPaymentSubmission);

        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

    private OBDomesticVRPResponse responseEntity(DomesticVRPConsent consent, FRDomesticVrpPaymentSubmission paymentSubmission) {
        OBDomesticVRPRequest obDomesticVRPRequest = toOBDomesticVRPRequest(paymentSubmission.getPayment());
        return responseEntity(consent, obDomesticVRPRequest, paymentSubmission);
    }

    private OBDomesticVRPResponse responseEntity(DomesticVRPConsent consent, OBDomesticVRPRequest obDomesticVRPRequest,
            FRDomesticVrpPaymentSubmission paymentSubmission) {

        final Optional<FRResponseDataRefund> refundAccountData = refundAccountService.getDomesticPaymentRefundData(
                consent.getRequestObj().getData().getReadRefundAccount(), consent);

        OBDomesticVRPResponse response = new OBDomesticVRPResponse()
                .data(
                        new OBDomesticVRPResponseData()
                                .consentId(paymentSubmission.getConsentId())
                                .domesticVRPId(paymentSubmission.getId())
                                .status(toOBDomesticVRPResponseDataStatusEnum(paymentSubmission.getStatus()))
                                .creationDateTime(new DateTime(paymentSubmission.getCreated().getTime()))
                                .statusUpdateDateTime(new DateTime(paymentSubmission.getUpdated().getTime()))
                                .debtorAccount(obDomesticVRPRequest.getData().getInitiation().getDebtorAccount())
                                .initiation(obDomesticVRPRequest.getData().getInitiation())
                                .instruction(obDomesticVRPRequest.getData().getInstruction())
                                .refund(refundAccountData.map(FRResponseDataRefundConverter::toOBCashAccountDebtorWithName).orElse(null))
                ).links(LinksHelper.createDomesticVrpPaymentLink(this.getClass(), paymentSubmission.getId())
                ).meta(new Meta())
                .risk(obDomesticVRPRequest.getRisk());

        // just to meet the expected data defined by the spec
        response.getData()
                .charges(List.of(
                        new OBDomesticVRPResponseDataChargesInner()
                                .type(OBExternalPaymentChargeType1Code.BALANCETRANSFEROUT)
                                .chargeBearer(OBChargeBearerType1Code.BORNEBYCREDITOR)
                                .amount(
                                        new OBActiveOrHistoricCurrencyAndAmount()
                                                .amount(DEFAULT_CHARGE_AMOUNT)
                                                .currency(DEFAULT_CHARGE_CURRENCY)
                                )
                ));

        return response;
    }

}
