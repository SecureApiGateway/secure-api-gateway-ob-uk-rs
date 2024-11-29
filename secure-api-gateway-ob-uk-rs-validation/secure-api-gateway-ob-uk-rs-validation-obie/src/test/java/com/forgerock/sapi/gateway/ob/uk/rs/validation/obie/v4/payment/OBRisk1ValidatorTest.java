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

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;

import uk.org.openbanking.datamodel.v4.common.OBRisk1;
import uk.org.openbanking.datamodel.v4.common.OBRisk1PaymentContextCode;

public class OBRisk1ValidatorTest {

    /**
     * @return OBRisk1Validator with the default validation rules, currently no rules applied
     */
    public static OBRisk1ValidatorV4 createDefaultRiskValidator() {
        return new OBRisk1ValidatorV4(false);
    }

    /**
     * @return OBRisk1Validator with risk.paymentContextCode validation rule enabled
     */
    public static OBRisk1ValidatorV4 createPaymentContextCodeRiskValidator() {
        return new OBRisk1ValidatorV4(true);
    }

    @Test
    void shouldAllowEmptyPaymentContextCodeBasedOnConfig() {
        final OBRisk1ValidatorV4 obRisk1Validator = createDefaultRiskValidator();
        validateSuccessResult(obRisk1Validator.validate(new OBRisk1()));
        validateSuccessResult(obRisk1Validator.validate(new OBRisk1().paymentContextCode(OBRisk1PaymentContextCode.BILLINGGOODSANDSERVICESINADVANCE)));
    }

    @Test
    void shouldRequirePaymentContextCodeBasedOnConfig() {
        final OBRisk1ValidatorV4 obRisk1Validator = createPaymentContextCodeRiskValidator();
        validateSuccessResult(obRisk1Validator.validate(new OBRisk1().paymentContextCode(OBRisk1PaymentContextCode.BILLINGGOODSANDSERVICESINADVANCE)));
        validateErrorResult(obRisk1Validator.validate(new OBRisk1()), List.of(OBRIErrorType.PAYMENT_CODE_CONTEXT_INVALID.toOBError1()));
    }
}
