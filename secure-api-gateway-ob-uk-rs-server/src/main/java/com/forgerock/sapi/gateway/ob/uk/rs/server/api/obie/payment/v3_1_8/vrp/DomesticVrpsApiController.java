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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.v3_1_8.vrp;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRResponseDataRefund;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRResponseDataRefundConverter;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRDomesticVrpRequest;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.payment.v3_1_8.vrp.DomesticVrpsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.ConsentService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation.DomesticVrpValidationService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.simulations.vrp.PeriodicLimitBreachResponseSimulatorService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.refund.FRResponseDataRefundFactory;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.VersionPathExtractor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.account.FRAccount;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.document.payment.FRDomesticVrpPaymentSubmission;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.IdempotentRepositoryAdapter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.persistence.repository.payments.DomesticVrpPaymentSubmissionRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.validator.PaymentSubmissionValidator;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.common.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.common.OBChargeBearerType1Code;
import uk.org.openbanking.datamodel.payment.OBReadRefundAccountEnum;
import uk.org.openbanking.datamodel.vrp.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRSubmissionStatusConverter.toFRSubmissionStatus;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRSubmissionStatusConverter.toOBDomesticVRPResponseDataStatusEnum;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.vrp.FRDomesticVrpConverters.toFRDomesticVRPRequest;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.vrp.FRDomesticVrpConverters.toOBDomesticVRPRequest;

@Controller("DomesticVrpsApiV3.1.8")
@Slf4j
public class DomesticVrpsApiController implements DomesticVrpsApi {

    protected static final String DEFAULT_CHARGE_AMOUNT = "1.00";
    protected static final String DEFAULT_CHARGE_CURRENCY = "GBP";

    private final DomesticVrpPaymentSubmissionRepository paymentSubmissionRepository;
    private final DomesticVrpValidationService domesticVrpValidationService;
    private final ConsentService consentService;
    private final PeriodicLimitBreachResponseSimulatorService limitBreachResponseSimulatorService;
    private final PaymentSubmissionValidator paymentSubmissionValidator;
    private final FRAccountRepository frAccountRepository;

    public DomesticVrpsApiController(
            DomesticVrpPaymentSubmissionRepository paymentSubmissionRepository,
            DomesticVrpValidationService domesticVrpValidationService,
            ConsentService consentService,
            PeriodicLimitBreachResponseSimulatorService limitBreachResponseSimulatorService,
            PaymentSubmissionValidator paymentSubmissionValidator,
            FRAccountRepository frAccountRepository
    ) {
        this.paymentSubmissionRepository = paymentSubmissionRepository;
        this.domesticVrpValidationService = domesticVrpValidationService;
        this.consentService = consentService;
        this.limitBreachResponseSimulatorService = limitBreachResponseSimulatorService;
        this.paymentSubmissionValidator = paymentSubmissionValidator;
        this.frAccountRepository = frAccountRepository;
    }

    @Override
    public ResponseEntity domesticVrpGet(
            String domesticVRPId,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
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

        //get the consent
        JsonObject intent = consentService.getIDMIntent(authorization, optionalVrpPayment.get().getConsentId());
        log.debug("Retrieved consent from IDM");

        //deserialize the intent to ob response object
        OBDomesticVRPConsentResponse obConsentResponse = consentService.deserialize(
                OBDomesticVRPConsentResponse.class,
                intent.getAsJsonObject("OBIntentObject"),
                optionalVrpPayment.get().getConsentId()
        );

        OBDomesticVRPResponse entity = responseEntity(optionalVrpPayment.get());

        // update entity with refund
        setRefund(
                obConsentResponse.getData().getReadRefundAccount(),
                intent,
                entity
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

    @Override
    public ResponseEntity domesticVrpPaymentDetailsGet(
            String domesticVRPId,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
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
        OBDomesticVRPDetailsDataPaymentStatus.StatusEnum status = OBDomesticVRPDetailsDataPaymentStatus.StatusEnum.fromValue(
                paymentSubmission.getStatus().getValue()
        );

        OBDomesticVRPDetailsDataStatusDetail.StatusReasonEnum statusReasonEnum = OBDomesticVRPDetailsDataStatusDetail.StatusReasonEnum.PENDINGSETTLEMENT;
        String localInstrument = paymentSubmission.getPayment().getData().getInstruction().getLocalInstrument();
        OBDomesticVRPDetails vrpDetails = new OBDomesticVRPDetails()
                .data(
                        new OBDomesticVRPDetailsData()
                                .addPaymentStatusItem(
                                        new OBDomesticVRPDetailsDataPaymentStatus()
                                                .status(status)
                                                .paymentTransactionId(paymentSubmission.getTransactionId())
                                                .statusUpdateDateTime(new DateTime(paymentSubmission.getUpdated()))
                                                .statusDetail(
                                                        new OBDomesticVRPDetailsDataStatusDetail()
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
            HttpServletRequest request,
            Principal principal
    ) throws OBErrorResponseException, OBErrorException {
        log.debug("Received VRP payment submission: '{}'", obDomesticVRPRequest);

        paymentSubmissionValidator.validateIdempotencyKeyAndRisk(xIdempotencyKey, obDomesticVRPRequest.getRisk());

        String consentId = obDomesticVRPRequest.getData().getConsentId();
        //get the consent
        JsonObject intent = consentService.getIDMIntent(authorization, consentId);
        log.debug("Retrieved consent from IDM");

        //deserialize the intent to ob response object
        OBDomesticVRPConsentResponse obConsentResponse = consentService.deserialize(
                OBDomesticVRPConsentResponse.class,
                intent.getAsJsonObject("OBIntentObject"),
                consentId
        );

        log.debug("Deserialized consent from IDM");

        if (xVrpLimitBreachResponseSimulation != null) {
            log.info("Executing Limit breach simulation, value of header: {}", xVrpLimitBreachResponseSimulation);
            limitBreachResponseSimulatorService.processRequest(xVrpLimitBreachResponseSimulation, obConsentResponse);
        }

        FRDomesticVrpRequest frDomesticVRPRequest = toFRDomesticVRPRequest(obDomesticVRPRequest);

        // validate the consent against the instruction
        log.debug("Validating VRP submission");
        domesticVrpValidationService.validate(obConsentResponse, frDomesticVRPRequest);
        log.debug("VRP validation successful! Creating the payment.");

        FRDomesticVrpPaymentSubmission vrpPaymentSubmission = FRDomesticVrpPaymentSubmission.builder()
                .id(UUID.randomUUID().toString())
                .transactionId(UUID.randomUUID().toString())
                .consentId(frDomesticVRPRequest.data.consentId)
                .payment(frDomesticVRPRequest)
                .idempotencyKey(xIdempotencyKey)
                .status(toFRSubmissionStatus(OBDomesticVRPResponseData.StatusEnum.PENDING))
                .created(new DateTime())
                .updated(new DateTime())
                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                .build();

        //save the domestic vrp
        vrpPaymentSubmission = new IdempotentRepositoryAdapter<>(paymentSubmissionRepository)
                .idempotentSave(vrpPaymentSubmission);

        OBDomesticVRPResponse entity = responseEntity(obDomesticVRPRequest, vrpPaymentSubmission);

        // update entity with refund
        setRefund(
                obConsentResponse.getData().getReadRefundAccount(),
                intent,
                entity
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

    private OBDomesticVRPResponse responseEntity(
            FRDomesticVrpPaymentSubmission paymentSubmission
    ) {
        OBDomesticVRPRequest obDomesticVRPRequest = toOBDomesticVRPRequest(paymentSubmission.getPayment());
        return responseEntity(obDomesticVRPRequest, paymentSubmission);
    }

    private OBDomesticVRPResponse responseEntity(
            OBDomesticVRPRequest obDomesticVRPRequest,
            FRDomesticVrpPaymentSubmission paymentSubmission
    ) {
        OBDomesticVRPResponse response = new OBDomesticVRPResponse()
                .data(
                        new OBDomesticVRPResponseData()
                                .consentId(paymentSubmission.getConsentId())
                                .domesticVRPId(paymentSubmission.getId())
                                .status(toOBDomesticVRPResponseDataStatusEnum(paymentSubmission.getStatus()))
                                .creationDateTime(paymentSubmission.getCreated())
                                .debtorAccount(obDomesticVRPRequest.getData().getInitiation().getDebtorAccount())
                                .initiation(obDomesticVRPRequest.getData().getInitiation())
                                .instruction(obDomesticVRPRequest.getData().getInstruction())
                ).links(LinksHelper.createDomesticVrpPaymentLink(this.getClass(), paymentSubmission.getId())
                ).meta(new Meta())
                .risk(obDomesticVRPRequest.getRisk());

        // just to meet the expected data defined by the spec
        response.getData().expectedExecutionDateTime(DateTime.now())
                .expectedSettlementDateTime(DateTime.now())
                .charges(List.of(
                        new OBDomesticVRPResponseDataCharges()
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

    private void setRefund(
            OBReadRefundAccountEnum obReadRefundAccountEnum,
            JsonObject intent,
            OBDomesticVRPResponse entity
    ) {
        if (Objects.nonNull(obReadRefundAccountEnum) && obReadRefundAccountEnum.equals(OBReadRefundAccountEnum.YES)) {
            String accountId = Objects.nonNull(intent.get("accountId")) ? intent.get("accountId").getAsString() : null;
            log.debug("Account Id from consent '{}'", accountId);
            if (Objects.nonNull(accountId)) {
                FRAccount frAccount = Objects.nonNull(accountId) ? frAccountRepository.byAccountId(accountId) : null;
                FRAccountIdentifier frAccountIdentifier = Objects.nonNull(frAccount) ?
                        frAccount.getAccount().getFirstAccount() :
                        null;
                Optional<FRResponseDataRefund> refund = FRResponseDataRefundFactory.frResponseDataRefund(frAccountIdentifier);
                if(Objects.nonNull(refund)) {
                    entity.getData().setRefund(
                            refund.map(FRResponseDataRefundConverter::toOBCashAccountDebtorWithName).orElse(null)
                    );
                }
            }
        }
    }
}
