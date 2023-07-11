package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.consent;

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateSuccessResult;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;

import uk.org.openbanking.datamodel.common.OBRisk1;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticStandingOrder3DataInitiation;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticStandingOrder3DataInitiationFinalPaymentAmount;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticStandingOrder3DataInitiationFirstPaymentAmount;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticStandingOrder3DataInitiationRecurringPaymentAmount;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticStandingOrderConsent5;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticStandingOrderConsent5Data;

class OBWriteDomesticStandingOrderConsent5ValidatorTest {

    private final OBWriteDomesticStandingOrderConsent5Validator validator = new OBWriteDomesticStandingOrderConsent5Validator(Set.of("GBP", "EUR"));

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
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1("Field: firstPaymentAmount - the currency ZZZ provided is not supported")));
    }

    @Test
    void invalidRecurringPaymentAmount() {
        final OBWriteDomesticStandingOrderConsent5 invalidConsent = createValidConsent();
        invalidConsent.getData().getInitiation().getRecurringPaymentAmount().amount("0.0").currency("ZZZ");
        validateErrorResult(validator.validate(invalidConsent), List.of(
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1("Field: recurringPaymentAmount - the amount 0.0 provided must be greater than 0"),
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1("Field: recurringPaymentAmount - the currency ZZZ provided is not supported")));
    }

    @Test
    void invalidFinalPaymentAmount() {
        final OBWriteDomesticStandingOrderConsent5 invalidConsent = createValidConsent();
        invalidConsent.getData().getInitiation().getFinalPaymentAmount().amount("0.0").currency("ZZZ");
        validateErrorResult(validator.validate(invalidConsent), List.of(
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1("Field: finalPaymentAmount - the amount 0.0 provided must be greater than 0"),
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1("Field: finalPaymentAmount - the currency ZZZ provided is not supported")));
    }

}