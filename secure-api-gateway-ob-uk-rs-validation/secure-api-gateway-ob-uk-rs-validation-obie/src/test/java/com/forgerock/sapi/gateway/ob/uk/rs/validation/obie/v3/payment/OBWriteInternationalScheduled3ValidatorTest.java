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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment;

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateSuccessResult;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteInternationalScheduled3Validator.OBWriteInternationalScheduled3ValidationContext;

import uk.org.openbanking.datamodel.v3.common.OBExternalPaymentContext1Code;
import uk.org.openbanking.datamodel.v3.common.OBRisk1;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomestic2DataInitiationInstructedAmount;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalScheduled3;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalScheduled3Data;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalScheduled3DataInitiation;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalScheduledConsent5;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalScheduledConsent5Data;

class OBWriteInternationalScheduled3ValidatorTest {

    private static final String AUTHORISED_STATUS = "Authorised";

    private final OBWriteInternationalScheduled3Validator validator = new OBWriteInternationalScheduled3Validator();

    @Test
    public void validationSuccessWhenInitiationAndRiskMatchConsent() {
        final OBWriteInternationalScheduledConsent5Data consentData = new OBWriteInternationalScheduledConsent5Data().initiation(createInitiation());
        final OBWriteInternationalScheduledConsent5 consent = new OBWriteInternationalScheduledConsent5().data(consentData).risk(createRisk());

        final OBWriteInternationalScheduled3Data paymentData = new OBWriteInternationalScheduled3Data().initiation(createInitiation());
        final OBWriteInternationalScheduled3 paymentRequest = new OBWriteInternationalScheduled3().data(paymentData).risk(createRisk());

        final OBWriteInternationalScheduled3ValidationContext validatorContext = new OBWriteInternationalScheduled3ValidationContext(paymentRequest, consent, AUTHORISED_STATUS);

        validateSuccessResult(validator.validate(validatorContext));
    }

    @Test
    public void validationFailsWhenInitiationDoesNotMatchConsent() {
        final OBWriteInternationalScheduledConsent5Data consentData = new OBWriteInternationalScheduledConsent5Data().initiation(createInitiation());
        final OBWriteInternationalScheduledConsent5 consent = new OBWriteInternationalScheduledConsent5().data(consentData).risk(createRisk());

        final OBWriteInternationalScheduled3Data paymentData = new OBWriteInternationalScheduled3Data().initiation(createInitiation().endToEndIdentification("different value"));
        final OBWriteInternationalScheduled3 paymentRequest = new OBWriteInternationalScheduled3().data(paymentData).risk(createRisk());

        final OBWriteInternationalScheduled3ValidationContext validatorContext = new OBWriteInternationalScheduled3ValidationContext(paymentRequest, consent, AUTHORISED_STATUS);

        validateErrorResult(validator.validate(validatorContext),
                List.of(new OBError1().errorCode("OBRI.Payment.Invalid")
                        .message("Payment invalid. Payment initiation received doesn't match the initial payment request: 'The Initiation field in the request does not match with the consent'")));
    }

    @Test
    public void validationFailsWhenRiskDoesNotMatchConsent() {
        final OBWriteInternationalScheduledConsent5Data consentData = new OBWriteInternationalScheduledConsent5Data().initiation(createInitiation());
        final OBWriteInternationalScheduledConsent5 consent = new OBWriteInternationalScheduledConsent5().data(consentData).risk(createRisk());

        final OBWriteInternationalScheduled3Data paymentData = new OBWriteInternationalScheduled3Data().initiation(createInitiation());
        final OBWriteInternationalScheduled3 paymentRequest = new OBWriteInternationalScheduled3().data(paymentData).risk(createRisk().contractPresentInidicator(Boolean.TRUE));

        final OBWriteInternationalScheduled3ValidationContext validatorContext = new OBWriteInternationalScheduled3ValidationContext(paymentRequest, consent, AUTHORISED_STATUS);

        validateErrorResult(validator.validate(validatorContext),
                List.of(new OBError1().errorCode("OBRI.Payment.Invalid")
                        .message("Payment invalid. Payment risk received doesn't match the risk payment request: 'The Risk field in the request does not match with the consent'")));
    }

    @Test
    public void validationFailsWhenConsentIsNotAuthorised() {
        final OBPaymentConsentStatus[] invalidStatuses = new OBPaymentConsentStatus[] {
                OBPaymentConsentStatus.CONSUMED, OBPaymentConsentStatus.REJECTED, OBPaymentConsentStatus.AWAITINGAUTHORISATION
        };

        for (OBPaymentConsentStatus invalidStatus : invalidStatuses) {
            final OBWriteInternationalScheduledConsent5Data consentData = new OBWriteInternationalScheduledConsent5Data().initiation(createInitiation());
            final OBWriteInternationalScheduledConsent5 consent = new OBWriteInternationalScheduledConsent5().data(consentData).risk(createRisk());

            final OBWriteInternationalScheduled3Data paymentData = new OBWriteInternationalScheduled3Data().initiation(createInitiation());
            final OBWriteInternationalScheduled3 paymentRequest = new OBWriteInternationalScheduled3().data(paymentData).risk(createRisk());

            final OBWriteInternationalScheduled3ValidationContext validatorContext = new OBWriteInternationalScheduled3ValidationContext(paymentRequest, consent, invalidStatus.toString());
            validateErrorResult(validator.validate(validatorContext), List.of(new OBError1().errorCode("UK.OBIE.Resource.InvalidConsentStatus")
                    .message("Action can only be performed on consents with status: Authorised. Currently, the consent is: " + invalidStatus)));
        }
    }

    private static OBRisk1 createRisk() {
        return new OBRisk1().paymentContextCode(OBExternalPaymentContext1Code.BILLPAYMENT);
    }

    private static OBWriteInternationalScheduled3DataInitiation createInitiation() {
        return new OBWriteInternationalScheduled3DataInitiation().localInstrument("INST1")
                .instructedAmount(new OBWriteDomestic2DataInitiationInstructedAmount()
                        .amount("123.45")
                        .currency("GBP"));
    }

}