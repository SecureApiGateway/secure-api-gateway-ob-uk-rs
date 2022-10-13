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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.validation;

import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent3;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent3;
import static uk.org.openbanking.testsupport.payment.OBWriteDomesticConsentTestDataFactory.aValidOBWriteDomesticConsent4;

/**
 * Unit test for {@link DomesticPaymentConsentValidation}
 */
public class DomesticPaymentConsentValidationTest {
    private PaymentConsentValidation validation;
    private OBWriteDomesticConsent3 consent3;

    @BeforeEach
    public void setup() {
        validation = new DomesticPaymentConsentValidation();
        consent3 = aValidOBWriteDomesticConsent3();
    }

    private static Stream<Arguments> provideConsent() {
        return Stream.of(
                Arguments.arguments(aValidOBWriteDomesticConsent3()),
                Arguments.arguments(aValidOBWriteDomesticConsent4())
        );
    }

    @ParameterizedTest
    @MethodSource("provideConsent")
    public <T> void shouldBeValidate(T consent) {
        validation.clearErrors().validate(consent);
        assertThat(validation.getErrors()).isEmpty();
    }

    @Test
    public void shouldNotValidateAmountZERO() {
        String amount = "0";
        consent3.getData().getInitiation().getInstructedAmount().setAmount(amount);
        validation.clearErrors().validate(consent3);
        assertThat(validation.getErrors()).isNotEmpty().containsExactly(
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                        String.format("The amount %s provided must be greater than 0", amount)
                )
        );
    }

    @Test
    public void shouldNotValidateCurrency() {
        String currency = "WRONG_CURRENCY";
        consent3.getData().getInitiation().getInstructedAmount().setCurrency(currency);
        validation.clearErrors().validate(consent3);
        assertThat(validation.getErrors()).isNotEmpty().containsExactly(
                OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                        String.format("The currency %s provided is not supported", currency)
                )
        );
    }
}
