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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment;

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateSuccessResult;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;

import uk.org.openbanking.datamodel.common.OBExternalPaymentContext1Code;
import uk.org.openbanking.datamodel.common.OBRisk1;

class OBRisk1ValidatorTest {

    @Test
    void shouldAllowEmptyPaymentContextCodeBasedOnConfig() {
        final OBRisk1Validator obRisk1Validator = new OBRisk1Validator(false);
        validateSuccessResult(obRisk1Validator.validate(new OBRisk1()));
        validateSuccessResult(obRisk1Validator.validate(new OBRisk1().paymentContextCode(OBExternalPaymentContext1Code.BILLPAYMENT)));
    }

    @Test
    void shouldRequirePaymentContextCodeBasedOnConfig() {
        final OBRisk1Validator obRisk1Validator = new OBRisk1Validator(true);
        validateSuccessResult(obRisk1Validator.validate(new OBRisk1().paymentContextCode(OBExternalPaymentContext1Code.BILLPAYMENT)));
        validateErrorResult(obRisk1Validator.validate(new OBRisk1()), List.of(OBRIErrorType.PAYMENT_CODE_CONTEXT_INVALID.toOBError1()));
    }
}