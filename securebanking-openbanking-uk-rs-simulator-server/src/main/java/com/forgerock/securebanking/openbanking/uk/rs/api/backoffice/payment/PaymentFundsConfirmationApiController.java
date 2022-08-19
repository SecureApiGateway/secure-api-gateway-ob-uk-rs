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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.share.IntentType;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorResponseCategory;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import com.forgerock.securebanking.openbanking.uk.rs.service.balance.FundsAvailabilityService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.payment.OBWriteFundsConfirmationResponse1;
import uk.org.openbanking.datamodel.payment.OBWriteFundsConfirmationResponse1Data;
import uk.org.openbanking.datamodel.payment.OBWriteFundsConfirmationResponse1DataFundsAvailableResult;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

import static com.forgerock.securebanking.openbanking.uk.rs.common.util.link.LinksHelper.createDomesticPaymentsConsentFundsConfirmationLink;

@Controller
@Slf4j
public class PaymentFundsConfirmationApiController implements PaymentFundsConfirmationApi {

    private final FundsAvailabilityService fundsAvailabilityService;

    public PaymentFundsConfirmationApiController(FundsAvailabilityService fundsAvailabilityService) {
        this.fundsAvailabilityService = fundsAvailabilityService;
    }

    @Override
    public ResponseEntity<OBWriteFundsConfirmationResponse1> getPaymentFundsConfirmation(
            String requestBody,
            String accountId,
            String version,
            String authorization,
            String xFapiFinancialId,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal) throws OBErrorResponseException {

        log.debug("PaymentFundsConfirmationApiController - request consent: '{}'",
                requestBody);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode consent = null;
        try {
            consent = objectMapper.readTree(requestBody);
        } catch (JsonProcessingException e) {
            log.debug("PaymentFundsConfirmationApiController - JsonProcessingException: '{}'", e);
            throw new OBErrorResponseException(
                    HttpStatus.BAD_REQUEST,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.DATA_INVALID_REQUEST
                            .toOBError1("The request body should have a json format."));
        }

        double amount = 0.0;
        try {
            amount = consent.get("Data").get("Initiation").get("InstructedAmount").get("Amount").asDouble();
        } catch (Exception e) {
            log.debug("PaymentFundsConfirmationApiController - Error while getting amount from consent: '{}'", e);
            throw new OBErrorResponseException(
                    HttpStatus.BAD_REQUEST,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.DATA_INVALID_REQUEST
                            .toOBError1("Couldn't get the InstructedAmount from the Initiation."));
        }

        IntentType intentType = IntentType.identify(consent.get("Data").get("ConsentId").asText());
        log.debug("PaymentFundsConfirmationApiController - intentType: '{}'", intentType);

        double charges = 0.0;
        double exchangeRate;
        switch (intentType) {
            case PAYMENT_DOMESTIC_CONSENT ->
                    charges = calculateDomesticPaymentCharges((ArrayNode) consent.get("Data").get("Charges"));
            case PAYMENT_INTERNATIONAL_CONSENT, PAYMENT_INTERNATIONAL_SCHEDULED_CONSENT -> {
                String instructedAmountCurrency = consent.get("Data").get("Initiation").get("InstructedAmount").get("Currency").asText();
                exchangeRate = consent.get("Data").get("ExchangeRateInformation").get("ExchangeRate").asDouble();
                log.debug("PaymentFundsConfirmationApiController - exchangeRate: {}", exchangeRate);
                charges = calculateInternationalPaymentCharges((ArrayNode) consent.get("Data").get("Charges"), exchangeRate, instructedAmountCurrency);
                amount = amount * exchangeRate;
            }
            default -> throw new OBErrorResponseException(
                    HttpStatus.BAD_REQUEST,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.DATA_INVALID_REQUEST
                            .toOBError1("Request not supported for this intent type."));
        }

        // Check if funds are available on the account
        boolean areFundsAvailable = fundsAvailabilityService.isFundsAvailable(accountId, String.valueOf(amount + charges));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        new OBWriteFundsConfirmationResponse1()
                                .data(
                                        new OBWriteFundsConfirmationResponse1Data()
                                                .fundsAvailableResult(
                                                        new OBWriteFundsConfirmationResponse1DataFundsAvailableResult()
                                                                .fundsAvailable(areFundsAvailable)
                                                                .fundsAvailableDateTime(DateTime.now())
                                                )
                                                .supplementaryData(null)
                                )
                                .links(createDomesticPaymentsConsentFundsConfirmationLink(this.getClass(), version, accountId))
                                .meta(new Meta())
                );
    }

    private double calculateDomesticPaymentCharges(ArrayNode charges) {
        log.debug("PaymentFundsConfirmationApiController - calculateDomesticPaymentCharges Start");
        if (charges.isNull() || charges.isEmpty()) {
            log.debug("PaymentFundsConfirmationApiController - calculateDomesticPaymentCharges. No charges found.");
            return 0.0;
        } else {
            double amount = 0.0;
            for (JsonNode charge : charges) {
                double chargeAmount = charge.get("Amount").get("Amount").asDouble();
                log.debug("PaymentFundsConfirmationApiController - calculateDomesticPaymentCharges. chargeAmount: {}", chargeAmount);
                amount += chargeAmount;
            }
            return amount;
        }
    }

    private double calculateInternationalPaymentCharges(ArrayNode charges, double exchangeRate, String instructedAmountCurrency) {
        log.debug("PaymentFundsConfirmationApiController - calculateInternationalPaymentCharges Start");
        if (charges.isNull() || charges.isEmpty()) {
            log.debug("PaymentFundsConfirmationApiController - calculateInternationalPaymentCharges. No charges found.");
            return 0.0;
        } else {
            double amount = 0.0;
            for (JsonNode charge : charges) {
                String currency = charge.get("Amount").get("Currency").asText();
                double chargeAmount = charge.get("Amount").get("Amount").asDouble();
                if (currency.equals(instructedAmountCurrency)) {
                    log.debug("PaymentFundsConfirmationApiController - calculateInternationalPaymentCharges. chargeAmount: {}", chargeAmount);
                    amount += chargeAmount;
                } else {
                    amount += chargeAmount * exchangeRate;
                }
            }
            return amount;
        }
    }
}
