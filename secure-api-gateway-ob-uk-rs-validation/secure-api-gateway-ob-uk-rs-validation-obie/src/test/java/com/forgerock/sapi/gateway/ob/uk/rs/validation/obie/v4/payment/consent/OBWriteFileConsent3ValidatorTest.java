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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.consent;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import org.junit.jupiter.api.Test;
import uk.org.openbanking.datamodel.v4.payment.OBWriteFileConsent3;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateErrorResult;
import static com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResultTest.validateSuccessResult;
import static uk.org.openbanking.testsupport.v4.payment.OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsent3;

class OBWriteFileConsent3ValidatorTest {

    private final Set<String> supportedFileTypes = Set.of("UK.OBIE.PaymentInitiation.3.1", "UK.OBIE.pain.001.001.08");
    private final OBWriteFileConsent3Validator validator = new OBWriteFileConsent3Validator(supportedFileTypes);

    private static OBWriteFileConsent3 createConsent(String fileType) {
        return aValidOBWriteFileConsent3(fileType, "hash", "1", BigDecimal.ONE);
    }

    @Test
    void succeedsWhenFileTypeIsSupported() {
        for (final String supportedFileType : supportedFileTypes) {
            validateSuccessResult(validator.validate(createConsent(supportedFileType)));
        }

    }

    @Test
    void failsWhenFileTypeIsNotSupported() {
        final String[] unsupportedFileTypes = new String[] {
                 "", "blah", "UK.OBIE.PaymentInitiation.3.0"
        };
        for (final String unsupportedFileType : unsupportedFileTypes) {
            validateErrorResult(validator.validate(createConsent(unsupportedFileType)),
                    List.of(OBRIErrorType.REQUEST_FILE_TYPE_NOT_SUPPORTED.toOBError1(unsupportedFileType)));
        }
    }

}