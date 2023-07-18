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

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteFile2Validator.OBWriteFile2ValidationContext;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.share.IntentType;

import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.payment.OBWriteFile2;
import uk.org.openbanking.datamodel.payment.OBWriteFile2Data;
import uk.org.openbanking.datamodel.payment.OBWriteFile2DataInitiation;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsent3;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsentResponse4Data.StatusEnum;
import uk.org.openbanking.testsupport.payment.OBWriteFileConsentTestDataFactory;

class OBWriteFile2ValidatorTest {

    private final OBWriteFile2Validator validator = new OBWriteFile2Validator();

    private OBWriteFile2ValidationContext createValidationContext() {
        final StatusEnum status = StatusEnum.AUTHORISED;
        return createValidationContext(status);
    }

    private static OBWriteFile2ValidationContext createValidationContext(StatusEnum status) {
        final OBWriteFileConsent3 consentRequest = createConsentRequest();
        final OBWriteFile2 paymentRequest = new OBWriteFile2().data(new OBWriteFile2Data().consentId(IntentType.PAYMENT_FILE_CONSENT.generateIntentId())
                .initiation(consentRequest.getData().getInitiation()));
        return new OBWriteFile2ValidationContext(paymentRequest, consentRequest, status.toString());
    }

    private static OBWriteFileConsent3 createConsentRequest() {
        return OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsent3("a", "b", "c", BigDecimal.ONE);
    }

    @Test
    void validationSuccess() {
        validateSuccessResult(validator.validate(createValidationContext()));
    }

    @Test
    void failsWhenInitiationDoesNotMatch() {
        final OBWriteFileConsent3 consentRequest = createConsentRequest();
        final OBWriteFile2 paymentRequest = new OBWriteFile2().data(new OBWriteFile2Data().consentId(IntentType.PAYMENT_FILE_CONSENT.generateIntentId()).initiation(new OBWriteFile2DataInitiation()));

        final OBWriteFile2ValidationContext obWriteFile2ValidationContext = new OBWriteFile2ValidationContext(paymentRequest, consentRequest, StatusEnum.AUTHORISED.toString());
        final ValidationResult<OBError1> validationResult = validator.validate(obWriteFile2ValidationContext);

        validateErrorResult(validationResult, List.of(new OBError1().errorCode("OBRI.Payment.Invalid")
                .message("Payment invalid. Payment initiation received doesn't match the initial payment request: 'The Initiation field in the request does not match with the consent'")));

    }

    @Test
    void failsWhenConsentNotAuthorised() {
        final ValidationResult<OBError1> validationResult = validator.validate(createValidationContext(StatusEnum.AWAITINGUPLOAD));

        validateErrorResult(validationResult, List.of(new OBError1().errorCode("UK.OBIE.Resource.InvalidConsentStatus")
                .message("Action can only be performed on consents with status: Authorised. Currently, the consent is: AwaitingUpload")));
    }

}