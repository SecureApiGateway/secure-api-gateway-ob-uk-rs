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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment;

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateSuccessResult;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteInternationalStandingOrder4Validator.OBWriteInternationalStandingOrder4ValidationContext;

import uk.org.openbanking.datamodel.common.OBExternalPaymentContext1Code;
import uk.org.openbanking.datamodel.common.OBRisk1;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.payment.OBPaymentConsentStatus;
import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiationInstructedAmount;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrder4;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrder4Data;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrder4DataInitiation;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderConsent6;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderConsent6Data;

class OBWriteInternationalStandingOrder4ValidatorTest {

    private static final String AUTHORISED_STATUS = "Authorised";

    private final OBWriteInternationalStandingOrder4Validator validator = new OBWriteInternationalStandingOrder4Validator();

    @Test
    public void validationSuccessWhenInitiationAndRiskMatchConsent() {
        final OBWriteInternationalStandingOrderConsent6Data consentData = new OBWriteInternationalStandingOrderConsent6Data().initiation(createInitiation());
        final OBWriteInternationalStandingOrderConsent6 consent = new OBWriteInternationalStandingOrderConsent6().data(consentData).risk(createRisk());

        final OBWriteInternationalStandingOrder4Data paymentData = new OBWriteInternationalStandingOrder4Data().initiation(createInitiation());
        final OBWriteInternationalStandingOrder4 paymentRequest = new OBWriteInternationalStandingOrder4().data(paymentData).risk(createRisk());

        final OBWriteInternationalStandingOrder4ValidationContext validatorContext = new OBWriteInternationalStandingOrder4ValidationContext(paymentRequest, consent, AUTHORISED_STATUS);

        validateSuccessResult(validator.validate(validatorContext));
    }

    @Test
    public void validationFailsWhenInitiationDoesNotMatchConsent() {
        final OBWriteInternationalStandingOrderConsent6Data consentData = new OBWriteInternationalStandingOrderConsent6Data().initiation(createInitiation());
        final OBWriteInternationalStandingOrderConsent6 consent = new OBWriteInternationalStandingOrderConsent6().data(consentData).risk(createRisk());

        final OBWriteInternationalStandingOrder4Data paymentData = new OBWriteInternationalStandingOrder4Data().initiation(createInitiation().extendedPurpose("different value"));
        final OBWriteInternationalStandingOrder4 paymentRequest = new OBWriteInternationalStandingOrder4().data(paymentData).risk(createRisk());

        final OBWriteInternationalStandingOrder4ValidationContext validatorContext = new OBWriteInternationalStandingOrder4ValidationContext(paymentRequest, consent, AUTHORISED_STATUS);

        validateErrorResult(validator.validate(validatorContext),
                List.of(new OBError1().errorCode("OBRI.Payment.Invalid")
                        .message("Payment invalid. Payment initiation received doesn't match the initial payment request: 'The Initiation field in the request does not match with the consent'")));
    }

    @Test
    public void validationFailsWhenRiskDoesNotMatchConsent() {
        final OBWriteInternationalStandingOrderConsent6Data consentData = new OBWriteInternationalStandingOrderConsent6Data().initiation(createInitiation());
        final OBWriteInternationalStandingOrderConsent6 consent = new OBWriteInternationalStandingOrderConsent6().data(consentData).risk(createRisk());

        final OBWriteInternationalStandingOrder4Data paymentData = new OBWriteInternationalStandingOrder4Data().initiation(createInitiation());
        final OBWriteInternationalStandingOrder4 paymentRequest = new OBWriteInternationalStandingOrder4().data(paymentData).risk(createRisk().contractPresentInidicator(Boolean.TRUE));

        final OBWriteInternationalStandingOrder4ValidationContext validatorContext = new OBWriteInternationalStandingOrder4ValidationContext(paymentRequest, consent, AUTHORISED_STATUS);

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
            final OBWriteInternationalStandingOrderConsent6Data consentData = new OBWriteInternationalStandingOrderConsent6Data().initiation(createInitiation());
            final OBWriteInternationalStandingOrderConsent6 consent = new OBWriteInternationalStandingOrderConsent6().data(consentData).risk(createRisk());

            final OBWriteInternationalStandingOrder4Data paymentData = new OBWriteInternationalStandingOrder4Data().initiation(createInitiation());
            final OBWriteInternationalStandingOrder4 paymentRequest = new OBWriteInternationalStandingOrder4().data(paymentData).risk(createRisk());

            final OBWriteInternationalStandingOrder4ValidationContext validatorContext = new OBWriteInternationalStandingOrder4ValidationContext(paymentRequest, consent, invalidStatus.toString());
            validateErrorResult(validator.validate(validatorContext), List.of(new OBError1().errorCode("UK.OBIE.Resource.InvalidConsentStatus")
                    .message("Action can only be performed on consents with status: Authorised. Currently, the consent is: " + invalidStatus)));
        }
    }

    private static OBRisk1 createRisk() {
        return new OBRisk1().paymentContextCode(OBExternalPaymentContext1Code.BILLPAYMENT);
    }

    private static OBWriteInternationalStandingOrder4DataInitiation createInitiation() {
        return new OBWriteInternationalStandingOrder4DataInitiation()
                .instructedAmount(new OBWriteDomestic2DataInitiationInstructedAmount()
                        .amount("123.45")
                        .currency("GBP"));
    }

}