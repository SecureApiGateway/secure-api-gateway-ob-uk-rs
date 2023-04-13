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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation;

import com.forgerock.sapi.gateway.ob.uk.common.error.ErrorCode;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import uk.org.openbanking.datamodel.common.OBExternalPaymentContext1Code;
import uk.org.openbanking.datamodel.common.OBRisk1;
import uk.org.openbanking.datamodel.common.OBRisk1DeliveryAddress;
import uk.org.openbanking.datamodel.payment.OBExternalExtendedAccountType1Code;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static uk.org.openbanking.testsupport.payment.OBRisk1TestDataFactory.aValidOBRisk1;

/**
 * Unit test for {@link RiskValidationService}
 */
public class RiskValidationServiceTest {

    private final RiskValidationService riskValidationService = new RiskValidationService();

    private final OBRisk1 consentRisk = aValidOBRisk1();

    @Test
    public void shouldVerifyValidRisk() throws OBErrorException {
        // Given
        OBRisk1 requestRisk = aValidOBRisk1();
        // when
        riskValidationService.validate(consentRisk, requestRisk);
    }

    private static Stream<Arguments> argumentsProvider() {
        String random = UUID.randomUUID().toString().replace("-","");
        return Stream.of(
                Arguments.arguments(
                        "request Risk Null",
                        null
                ),
                Arguments.arguments(
                        "beneficiaryAccountType Null",
                        aValidOBRisk1().beneficiaryAccountType(null)
                ),
                Arguments.arguments(
                        "beneficiaryAccountType Diff",
                        aValidOBRisk1().beneficiaryAccountType(OBExternalExtendedAccountType1Code.CHARITY)
                ),
                Arguments.arguments(
                        "contractPresentIndicator Null",
                        aValidOBRisk1().contractPresentInidicator(null)
                ),
                Arguments.arguments(
                        "contractPresentIndicator Diff",
                        aValidOBRisk1().contractPresentInidicator(true)
                ),
                Arguments.arguments(
                        "paymentContextCode Null",
                        aValidOBRisk1().paymentContextCode(null)
                ),
                Arguments.arguments(
                        "paymentContextCode Diff",
                        aValidOBRisk1().paymentContextCode(OBExternalPaymentContext1Code.BILLPAYMENT)
                ),
                Arguments.arguments(
                        "merchantCategoryCode Diff",
                        aValidOBRisk1().merchantCategoryCode("merchant cat code" + random)
                ),
                Arguments.arguments(
                        "merchantCustomerIdentification Null",
                        aValidOBRisk1().merchantCustomerIdentification(null)
                ),
                Arguments.arguments(
                        "merchantCustomerIdentification Diff",
                        aValidOBRisk1().merchantCustomerIdentification("merchant customer ID " + random)
                ),
                Arguments.arguments(
                        "paymentPurposeCode Null",
                        aValidOBRisk1().paymentPurposeCode(null)
                ),
                Arguments.arguments(
                        "paymentPurposeCode Diff",
                        aValidOBRisk1().paymentPurposeCode(random)
                ),
                Arguments.arguments(
                        "deliveryAddress Null",
                        aValidOBRisk1().deliveryAddress(null)
                ),
                Arguments.arguments(
                        "deliveryAddress Diff",
                        aValidOBRisk1().deliveryAddress(
                                new OBRisk1DeliveryAddress()
                                        .addAddressLineItem(random)
                                        .streetName(random)
                        )
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("argumentsProvider")
    public void shouldThrowErrorInvalidInitiation(String testTitle, OBRisk1 requestRisk) {
        // Given parameters
        // When
        OBErrorException e = catchThrowableOfType(() -> riskValidationService.validate(consentRisk, requestRisk), OBErrorException.class);

        // Then
        assertThat(e.getObriErrorType()).isEqualTo(OBRIErrorType.PAYMENT_INVALID_RISK);
        assertThat(e.getObriErrorType().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(e.getObriErrorType().getCode()).isEqualTo(ErrorCode.OBRI_PAYMENT_INVALID);
        assertThat(e.getObriErrorType().getMessage()).isEqualTo(OBRIErrorType.PAYMENT_INVALID_RISK.getMessage());
    }
}
