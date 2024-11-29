/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment;

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateSuccessResult;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteInternational3Validator.OBWriteInternational3ValidationContext;

import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;
import uk.org.openbanking.datamodel.v4.common.OBRisk1;
import uk.org.openbanking.datamodel.v4.common.OBRisk1PaymentContextCode;
import uk.org.openbanking.datamodel.v4.payment.OBWriteDomestic2DataInitiationInstructedAmount;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternational3;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternational3Data;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternational3DataInitiation;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalConsent5;
import uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalConsent5Data;

class OBWriteInternational3ValidatorTest {

    private static final String AUTHORISED_STATUS = "Authorised";

    private final OBWriteInternational3Validator validator = new OBWriteInternational3Validator();

    @Test
    public void validationSuccessWhenInitiationAndRiskMatchConsent() {
        final OBWriteInternationalConsent5Data consentData = new OBWriteInternationalConsent5Data().initiation(createInitiation());
        final OBWriteInternationalConsent5 consent = new OBWriteInternationalConsent5().data(consentData).risk(createRisk());

        final OBWriteInternational3Data paymentData = new OBWriteInternational3Data().initiation(createInitiation());
        final OBWriteInternational3 paymentRequest = new OBWriteInternational3().data(paymentData).risk(createRisk());

        final OBWriteInternational3ValidationContext validatorContext = new OBWriteInternational3ValidationContext(paymentRequest, consent, AUTHORISED_STATUS);

        validateSuccessResult(validator.validate(validatorContext));
    }

    @Test
    public void validationFailsWhenInitiationDoesNotMatchConsent() {
        final OBWriteInternationalConsent5Data consentData = new OBWriteInternationalConsent5Data().initiation(createInitiation());
        final OBWriteInternationalConsent5 consent = new OBWriteInternationalConsent5().data(consentData).risk(createRisk());

        final OBWriteInternational3Data paymentData = new OBWriteInternational3Data().initiation(createInitiation().endToEndIdentification("different value"));
        final OBWriteInternational3 paymentRequest = new OBWriteInternational3().data(paymentData).risk(createRisk());

        final OBWriteInternational3ValidationContext validatorContext = new OBWriteInternational3ValidationContext(paymentRequest, consent, AUTHORISED_STATUS);

        validateErrorResult(validator.validate(validatorContext),
                List.of(new OBError1().errorCode("OBRI.Payment.Invalid")
                        .message("Payment invalid. Payment initiation received doesn't match the initial payment request: 'The Initiation field in the request does not match with the consent'")));
    }

    @Test
    public void validationFailsWhenRiskDoesNotMatchConsent() {
        final OBWriteInternationalConsent5Data consentData = new OBWriteInternationalConsent5Data().initiation(createInitiation());
        final OBWriteInternationalConsent5 consent = new OBWriteInternationalConsent5().data(consentData).risk(createRisk());

        final OBWriteInternational3Data paymentData = new OBWriteInternational3Data().initiation(createInitiation());
        final OBWriteInternational3 paymentRequest = new OBWriteInternational3().data(paymentData).risk(createRisk().contractPresentIndicator(Boolean.TRUE));

        final OBWriteInternational3ValidationContext validatorContext = new OBWriteInternational3ValidationContext(paymentRequest, consent, AUTHORISED_STATUS);

        validateErrorResult(validator.validate(validatorContext),
                List.of(new OBError1().errorCode("OBRI.Payment.Invalid")
                        .message("Payment invalid. Payment risk received doesn't match the risk payment request: 'The Risk field in the request does not match with the consent'")));
    }

    @Test
    public void validationFailsWhenConsentIsNotAuthorised() {
        final OBPaymentConsentStatus[] invalidStatuses = new OBPaymentConsentStatus[]{
                OBPaymentConsentStatus.CONSUMED, OBPaymentConsentStatus.REJECTED, OBPaymentConsentStatus.AWAITINGAUTHORISATION
        };

        for (OBPaymentConsentStatus invalidStatus : invalidStatuses) {
            final OBWriteInternationalConsent5Data consentData = new OBWriteInternationalConsent5Data().initiation(createInitiation());
            final OBWriteInternationalConsent5 consent = new OBWriteInternationalConsent5().data(consentData).risk(createRisk());

            final OBWriteInternational3Data paymentData = new OBWriteInternational3Data().initiation(createInitiation());
            final OBWriteInternational3 paymentRequest = new OBWriteInternational3().data(paymentData).risk(createRisk());

            final OBWriteInternational3ValidationContext validatorContext = new OBWriteInternational3ValidationContext(paymentRequest, consent, invalidStatus.toString());
            validateErrorResult(validator.validate(validatorContext), List.of(new OBError1().errorCode("UK.OBIE.Resource.InvalidConsentStatus")
                    .message("Action can only be performed on consents with status: Authorised. Currently, the consent is: " + invalidStatus)));
        }
    }

    private static OBRisk1 createRisk() {
        return new OBRisk1().paymentContextCode(OBRisk1PaymentContextCode.BILLINGGOODSANDSERVICESINADVANCE);
    }

    private static OBWriteInternational3DataInitiation createInitiation() {
        return new OBWriteInternational3DataInitiation().localInstrument("INST1")
                .instructedAmount(new OBWriteDomestic2DataInitiationInstructedAmount()
                        .amount("123.45")
                        .currency("GBP"));
    }
}
