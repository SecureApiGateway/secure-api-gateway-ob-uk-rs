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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.consent.v4_0_0;

public class OBWriteDomesticStandingOrderConsent5ValidatorTest {
/*
    private final OBWriteDomesticStandingOrderConsent5Validator validator = new OBWriteDomesticStandingOrderConsent5Validator(
            createCurrencyCodeValidator("GBP", "EUR"), createDefaultRiskValidator());

    private static OBWriteDomesticStandingOrderConsent5 createValidConsent() {
        return new OBWriteDomesticStandingOrderConsent5().data(new OBWriteDomesticStandingOrderConsent5Data().initiation(
                                new OBWriteDomesticStandingOrder3DataInitiation()
                                        .firstPaymentAmount(new OBWriteDomesticStandingOrder3DataInitiationFirstPaymentAmount().amount("36.99").currency("GBP"))
                                        .recurringPaymentAmount(new OBWriteDomesticStandingOrder3DataInitiationRecurringPaymentAmount().amount("40.01").currency("GBP"))
                                        .finalPaymentAmount(new OBWriteDomesticStandingOrder3DataInitiationFinalPaymentAmount().amount("39.98").currency("GBP"))
                        )
                )
                .risk(new OBRisk1());
    }

    @Test
    void validConsent() {
        validateSuccessResult(validator.validate(createValidConsent()));
    }

    @Test
    void invalidFirstPaymentAmount() {
        final OBWriteDomesticStandingOrderConsent5 invalidConsent = createValidConsent();
        invalidConsent.getData().getInitiation().getFirstPaymentAmount().amount("0.0").currency("ZZZ");
        validateErrorResult(validator.validate(invalidConsent), List.of(
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1("Field: firstPaymentAmount - the amount 0.0 provided must be greater than 0"),
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1("The currency ZZZ provided is not supported")));
    }

    @Test
    void invalidRecurringPaymentAmount() {
        final OBWriteDomesticStandingOrderConsent5 invalidConsent = createValidConsent();
        invalidConsent.getData().getInitiation().getRecurringPaymentAmount().amount("0.0").currency("ZZZ");
        validateErrorResult(validator.validate(invalidConsent), List.of(
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1("Field: recurringPaymentAmount - the amount 0.0 provided must be greater than 0"),
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1("The currency ZZZ provided is not supported")));
    }

    @Test
    void invalidFinalPaymentAmount() {
        final OBWriteDomesticStandingOrderConsent5 invalidConsent = createValidConsent();
        invalidConsent.getData().getInitiation().getFinalPaymentAmount().amount("0.0").currency("ZZZ");
        validateErrorResult(validator.validate(invalidConsent), List.of(
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1("Field: finalPaymentAmount - the amount 0.0 provided must be greater than 0"),
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1("The currency ZZZ provided is not supported")));
    }

    @Test
    public void failsRiskValidation() {
        final OBWriteDomesticStandingOrderConsent5 consent = createValidConsent();
        consent.getRisk().setPaymentContextCode(null);
        final ValidationResult<OBError1> validationResult = new OBWriteDomesticStandingOrderConsent5Validator(
                createCurrencyCodeValidator("GBP"), createPaymentContextCodeRiskValidator()).validate(consent);

        validateErrorResult(validationResult, List.of(OBRIErrorType.PAYMENT_CODE_CONTEXT_INVALID.toOBError1()));
    }*/
}
