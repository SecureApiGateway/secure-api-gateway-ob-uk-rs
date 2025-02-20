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
package com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.consent;

import java.util.Objects;
import java.util.Set;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.ValidationResult;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator;

import uk.org.openbanking.datamodel.v3.error.OBError1;
import uk.org.openbanking.datamodel.v3.payment.OBWriteFileConsent3;

/**
 * Validator of OBWriteFileConsent3 objects (OBIE File Payment Consents)
 */
public class OBWriteFileConsent3Validator extends BaseOBValidator<OBWriteFileConsent3> {

    private final Set<String> supportedFileTypes;

    public OBWriteFileConsent3Validator(Set<String> supportedFileTypes) {
        this.supportedFileTypes = Objects.requireNonNull(supportedFileTypes);
    }

    @Override
    protected void validate(OBWriteFileConsent3 obj, ValidationResult<OBError1> validationResult) {
        final String fileType = obj.getData().getInitiation().getFileType();
        if (!supportedFileTypes.contains(fileType)) {
            validationResult.addError(OBRIErrorType.REQUEST_FILE_TYPE_NOT_SUPPORTED.toOBError1(fileType));
        }
    }
}
