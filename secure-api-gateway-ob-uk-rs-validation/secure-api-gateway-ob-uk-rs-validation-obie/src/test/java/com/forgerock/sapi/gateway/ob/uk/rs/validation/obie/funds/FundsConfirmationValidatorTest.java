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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.funds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.org.openbanking.datamodel.fund.OBFundsConfirmationConsentResponse1Data.StatusEnum.AUTHORISED;
import static uk.org.openbanking.datamodel.fund.OBFundsConfirmationConsentResponse1Data.StatusEnum.REJECTED;
import static uk.org.openbanking.datamodel.fund.OBFundsConfirmationConsentResponse1Data.StatusEnum.REVOKED;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;

import uk.org.openbanking.datamodel.common.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.common.OBCashAccount3;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmation1;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationConsent1;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationConsentData1;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationData1;

public class FundsConfirmationValidatorTest {

    private static final String ACCOUNT_CURRENCY = "GBP";
    private final FundsConfirmationValidator fundsConfirmationValidator = new FundsConfirmationValidator();

    @Test
    void fundsConfirmationMeetsValidationRequirements() {
        OBFundsConfirmationConsent1 obConsent = createValidOBFundsConfirmationConsent1();
        FundsConfirmationValidator.FundsConfirmationValidationContext confirmationValidationContext = new FundsConfirmationValidator.FundsConfirmationValidationContext(
                aValidOBFundsConfirmation1(), obConsent.getData().getExpirationDateTime(), AUTHORISED.toString(), ACCOUNT_CURRENCY
        );
        ValidationResult<?> result = fundsConfirmationValidator.validate(confirmationValidationContext);
        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
    }

    @Test
    void fundsConfirmationConsentExpired() {
        DateTime dateTime = DateTime.now().minusMinutes(1).withZone(DateTimeZone.UTC);
        OBFundsConfirmation1 fundsConfirmation1Request = aValidOBFundsConfirmation1();
        OBFundsConfirmationConsent1 obConsent = createValidOBFundsConfirmationConsent1(dateTime);
        FundsConfirmationValidator.FundsConfirmationValidationContext confirmationValidationContext = new FundsConfirmationValidator.FundsConfirmationValidationContext(
                fundsConfirmation1Request, obConsent.getData().getExpirationDateTime(), AUTHORISED.toString(), ACCOUNT_CURRENCY
        );
        List<OBError1> expectedErrors = List.of(
                OBRIErrorType.FUNDS_CONFIRMATION_EXPIRED.toOBError1(obConsent.getData().getExpirationDateTime().toString())
        );
        ValidationResult<?> result = fundsConfirmationValidator.validate(confirmationValidationContext);
        assertFalse(result.isValid());
        assertEquals(expectedErrors, result.getErrors());
    }

    @Test
    void fundsConfirmationCurrencyMismatch() {
        OBFundsConfirmation1 fundsConfirmation1Request = aValidOBFundsConfirmation1();
        final String currency = "EUR";
        fundsConfirmation1Request.getData().getInstructedAmount().setCurrency(currency);
        OBFundsConfirmationConsent1 obConsent = createValidOBFundsConfirmationConsent1();
        FundsConfirmationValidator.FundsConfirmationValidationContext confirmationValidationContext = new FundsConfirmationValidator.FundsConfirmationValidationContext(
                fundsConfirmation1Request, obConsent.getData().getExpirationDateTime(), AUTHORISED.toString(), ACCOUNT_CURRENCY
        );
        List<OBError1> expectedErrors = List.of(
                OBRIErrorType.FUNDS_CONFIRMATION_CURRENCY_MISMATCH.toOBError1(currency, ACCOUNT_CURRENCY)
        );
        ValidationResult<?> result = fundsConfirmationValidator.validate(confirmationValidationContext);
        assertFalse(result.isValid());
        assertEquals(expectedErrors, result.getErrors());
    }

    @Test
    void fundsConfirmationConsentStatusNotAuthorised() {
        OBFundsConfirmation1 fundsConfirmation1Request = aValidOBFundsConfirmation1();
        OBFundsConfirmationConsent1 obConsent = createValidOBFundsConfirmationConsent1();
        FundsConfirmationValidator.FundsConfirmationValidationContext confirmationValidationContext = new FundsConfirmationValidator.FundsConfirmationValidationContext(
                fundsConfirmation1Request, obConsent.getData().getExpirationDateTime(), REVOKED.toString(), ACCOUNT_CURRENCY
        );
        List<OBError1> expectedErrors = List.of(
                OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(REVOKED.toString())
        );
        ValidationResult<?> result = fundsConfirmationValidator.validate(confirmationValidationContext);
        assertFalse(result.isValid());
        assertEquals(expectedErrors, result.getErrors());
    }

    @Test
    void fundsConfirmationAllErrors() {
        DateTime dateTime = DateTime.now().minusMinutes(1).withZone(DateTimeZone.UTC);
        OBFundsConfirmation1 fundsConfirmation1Request = aValidOBFundsConfirmation1();
        final String currency = "EUR";
        fundsConfirmation1Request.getData().getInstructedAmount().setCurrency(currency);
        OBFundsConfirmationConsent1 obConsent = createValidOBFundsConfirmationConsent1(dateTime);
        FundsConfirmationValidator.FundsConfirmationValidationContext confirmationValidationContext = new FundsConfirmationValidator.FundsConfirmationValidationContext(
                fundsConfirmation1Request, obConsent.getData().getExpirationDateTime(), REJECTED.toString(), ACCOUNT_CURRENCY
        );
        List<OBError1> expectedErrors = List.of(
                OBRIErrorType.FUNDS_CONFIRMATION_CURRENCY_MISMATCH.toOBError1(currency, ACCOUNT_CURRENCY),
                OBRIErrorType.FUNDS_CONFIRMATION_EXPIRED.toOBError1(obConsent.getData().getExpirationDateTime().toString()),
                OBRIErrorType.CONSENT_STATUS_NOT_AUTHORISED.toOBError1(REJECTED.toString())
        );
        ValidationResult<?> result = fundsConfirmationValidator.validate(confirmationValidationContext);
        assertFalse(result.isValid());
        assertEquals(expectedErrors, result.getErrors());
    }

    private OBFundsConfirmation1 aValidOBFundsConfirmation1() {
        return new OBFundsConfirmation1()
                .data(new OBFundsConfirmationData1()
                        .consentId(UUID.randomUUID().toString())
                        .reference("Funds confirmation ref")
                        .instructedAmount(new OBActiveOrHistoricCurrencyAndAmount()
                                .currency(ACCOUNT_CURRENCY)
                                .amount("20.00")
                        ));
    }

    private OBFundsConfirmationConsent1 createValidOBFundsConfirmationConsent1() {
        return createValidOBFundsConfirmationConsent1(DateTime.now().plusMonths(3).withZone(DateTimeZone.UTC));
    }

    private OBFundsConfirmationConsent1 createValidOBFundsConfirmationConsent1(DateTime dateTime) {
        return new OBFundsConfirmationConsent1()
                .data(
                        new OBFundsConfirmationConsentData1()
                                .expirationDateTime(dateTime)
                                .debtorAccount(
                                        new OBCashAccount3()
                                                .schemeName("UK.OBIE.SortCodeAccountNumber")
                                                .identification("40400422390112")
                                                .name("Mrs B Smith")
                                )
                );
    }
}
