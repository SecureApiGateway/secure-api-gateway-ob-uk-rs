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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment;

import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteDomestic2Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteDomestic2Validator.OBWriteDomestic2ValidationContext;
import org.junit.jupiter.api.Test;
import uk.org.openbanking.datamodel.v4.common.OBRisk1;
import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v4.common.OBRisk1PaymentContextCode;
import uk.org.openbanking.datamodel.v4.payment.*;
import uk.org.openbanking.datamodel.v3.payment.OBPaymentConsentStatus;

import java.util.List;

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateSuccessResult;

class OBWriteDomestic2ValidatorTest {

    private static final String AUTHORISED_STATUS = "Authorised";

    private final OBWriteDomestic2Validator obWriteDomestic2Validator = new OBWriteDomestic2Validator();

    @Test
    public void validationSuccessWhenInitiationAndRiskMatchConsent() {
        final OBWriteDomesticConsent4Data consentData = new OBWriteDomesticConsent4Data().initiation(createInitiation());
        final OBWriteDomesticConsent4 consent = new OBWriteDomesticConsent4().data(consentData).risk(createRisk());

        final OBWriteDomestic2Data paymentData = new OBWriteDomestic2Data().initiation(createInitiation());
        final OBWriteDomestic2 paymentRequest = new OBWriteDomestic2().data(paymentData).risk(createRisk());

        final OBWriteDomestic2ValidationContext validatorContext = new OBWriteDomestic2ValidationContext(paymentRequest, consent, AUTHORISED_STATUS);

        validateSuccessResult(obWriteDomestic2Validator.validate(validatorContext));
    }

    @Test
    public void validationFailsWhenInitiationDoesNotMatchConsent() {
        final OBWriteDomesticConsent4Data consentData = new OBWriteDomesticConsent4Data().initiation(createInitiation());
        final OBWriteDomesticConsent4 consent = new OBWriteDomesticConsent4().data(consentData).risk(createRisk());

        final OBWriteDomestic2Data paymentData = new OBWriteDomestic2Data().initiation(createInitiation().endToEndIdentification("value different from consent"));
        final OBWriteDomestic2 paymentRequest = new OBWriteDomestic2().data(paymentData).risk(createRisk());

        final OBWriteDomestic2ValidationContext validatorContext = new OBWriteDomestic2ValidationContext(paymentRequest, consent, AUTHORISED_STATUS);

        validateErrorResult(obWriteDomestic2Validator.validate(validatorContext),
                List.of(new OBError1().errorCode("OBRI.Payment.Invalid")
                                      .message("Payment invalid. Payment initiation received doesn't match the initial payment request: 'The Initiation field in the request does not match with the consent'")));
    }

    @Test
    public void validationFailsWhenRiskDoesNotMatchConsent() {
        final OBWriteDomesticConsent4Data consentData = new OBWriteDomesticConsent4Data().initiation(createInitiation());
        final OBWriteDomesticConsent4 consent = new OBWriteDomesticConsent4().data(consentData).risk(createRisk());

        final OBWriteDomestic2Data paymentData = new OBWriteDomestic2Data().initiation(createInitiation());
        final OBWriteDomestic2 paymentRequest = new OBWriteDomestic2().data(paymentData).risk(createRisk().contractPresentIndicator((Boolean.TRUE)));

        final OBWriteDomestic2ValidationContext validatorContext = new OBWriteDomestic2ValidationContext(paymentRequest, consent, AUTHORISED_STATUS);

        validateErrorResult(obWriteDomestic2Validator.validate(validatorContext),
                List.of(new OBError1().errorCode("OBRI.Payment.Invalid")
                                      .message("Payment invalid. Payment risk received doesn't match the risk payment request: 'The Risk field in the request does not match with the consent'")));
    }

    @Test
    public void validationFailsWhenConsentIsNotAuthorised() {
        final OBPaymentConsentStatus[] invalidStatuses = new OBPaymentConsentStatus[] {
                OBPaymentConsentStatus.CONSUMED, OBPaymentConsentStatus.REJECTED, OBPaymentConsentStatus.AWAITINGAUTHORISATION
        };

        for (OBPaymentConsentStatus invalidStatus : invalidStatuses) {
            final OBWriteDomesticConsent4Data consentData = new OBWriteDomesticConsent4Data().initiation(createInitiation());
            final OBWriteDomesticConsent4 consent = new OBWriteDomesticConsent4().data(consentData).risk(createRisk());

            final OBWriteDomestic2Data paymentData = new OBWriteDomestic2Data().initiation(createInitiation());
            final OBWriteDomestic2 paymentRequest = new OBWriteDomestic2().data(paymentData).risk(createRisk());

            final OBWriteDomestic2ValidationContext validatorContext = new OBWriteDomestic2ValidationContext(paymentRequest, consent, invalidStatus.toString());
            validateErrorResult(obWriteDomestic2Validator.validate(validatorContext), List.of(new OBError1().errorCode("UK.OBIE.Resource.InvalidConsentStatus")
                    .message("Action can only be performed on consents with status: Authorised. Currently, the consent is: " + invalidStatus)));
        }
    }

    private static OBRisk1 createRisk() {
        return new OBRisk1().paymentContextCode(OBRisk1PaymentContextCode.BILLINGGOODSANDSERVICESINADVANCE);
    }

    private static OBWriteDomestic2DataInitiation createInitiation() {
        return new OBWriteDomestic2DataInitiation().localInstrument("INST1")
                .instructedAmount(new OBWriteDomestic2DataInitiationInstructedAmount()
                        .amount("123.45")
                        .currency("GBP"));
    }

}