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
            Principal principal) {


        log.error("PaymentFundsConfirmationApiController - request consent: '{}'",
                requestBody);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode consent = null;
        try {
            consent = objectMapper.readTree(requestBody);
        } catch (JsonProcessingException e) {
            log.error("PaymentFundsConfirmationApiController - JsonProcessingException: '{}'", e);
            //TODO - Error handling
        }


        Double amount = 0.0;
        try {
            amount = consent.get("Data").get("Initiation").get("InstructedAmount").get("Amount").asDouble();
        } catch (Exception e) {
            log.error("PaymentFundsConfirmationApiController - Error while getting amount from consent: '{}'", e);
            //TODO - Error handling
        }

        IntentType intentType = IntentType.identify(consent.get("Data").get("ConsentId").asText());
        log.error("PaymentFundsConfirmationApiController - intentType: '{}'", intentType);

        Double charges = 0.0;
        Double exchangeRate = 0.0;
        if (intentType.equals(IntentType.PAYMENT_DOMESTIC_CONSENT)) {
            charges = calculateDomesticPaymentCharges((ArrayNode) consent.get("Data").get("Charges"));
        } else if (intentType.equals(IntentType.PAYMENT_INTERNATIONAL_CONSENT)) {
            String instructedAmountCurrency = consent.get("Data").get("Initiation").get("InstructedAmount").get("Currency").asText();
            exchangeRate = consent.get("Data").get("ExchangeRateInformation").get("ExchangeRate").asDouble();
            log.error("PaymentFundsConfirmationApiController - exchangeRate: {}", exchangeRate);
            charges = calculateInternationalPaymentCharges((ArrayNode) consent.get("Data").get("Charges"), exchangeRate, instructedAmountCurrency);
            amount = amount * exchangeRate;
        } else {
            // TODO - Error handling
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

    private Double calculateDomesticPaymentCharges(ArrayNode charges) {
        log.error("PaymentFundsConfirmationApiController - calculateDomesticPaymentCharges Start");
        if (charges.isNull() || charges.isEmpty()) {
            log.error("PaymentFundsConfirmationApiController - calculateDomesticPaymentCharges. No charges found.");
            return 0.0;
        } else {
            Double amount = 0.0;
            for (JsonNode charge : charges) {
                Double chargeAmount = charge.get("Amount").get("Amount").asDouble();
                log.error("PaymentFundsConfirmationApiController - calculateDomesticPaymentCharges. chargeAmount: {}", chargeAmount);
                amount += chargeAmount;
            }
            return amount;
        }
    }

    private Double calculateInternationalPaymentCharges(ArrayNode charges, Double exchangeRate, String instructedAmountCurrency) {
        log.error("PaymentFundsConfirmationApiController - calculateInternationalPaymentCharges Start");
        if (charges.isNull() || charges.isEmpty()) {
            log.error("PaymentFundsConfirmationApiController - calculateInternationalPaymentCharges. No charges found.");
            return 0.0;
        } else {
            Double amount = 0.0;
            for (JsonNode charge : charges) {
                String currency = charge.get("Amount").get("Currency").asText();
                if (currency.equals(instructedAmountCurrency)) {
                    Double chargeAmount = charge.get("Amount").get("Amount").asDouble();
                    log.error("PaymentFundsConfirmationApiController - calculateInternationalPaymentCharges. chargeAmount: {}", chargeAmount);
                    amount += chargeAmount;
                } else {
                    Double chargeAmount = charge.get("Amount").get("Amount").asDouble();
                    amount += chargeAmount * exchangeRate;
                }
            }
            return amount;
        }
    }
}
