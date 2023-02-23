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

package com.forgerock.sapi.gateway.ob.uk.rs.server.api.backoffice.payment.validation;

import com.forgerock.sapi.gateway.ob.uk.rs.server.common.filepayment.PaymentFileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsent3;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.org.openbanking.testsupport.payment.OBWriteFileConsentTestDataFactory.aValidOBWriteFileConsent3;


/**
 * Unit test for {@link FilePaymentConsentValidation}
 */

public class FilePaymentConsentValidationTest {

    private PaymentConsentValidation validation;
    private OBWriteFileConsent3 consent3;

    @BeforeEach
    public void setup() {
        validation = new FilePaymentConsentValidation();
        consent3 = aValidOBWriteFileConsent3(PaymentFileType.UK_OBIE_PAIN_001.getFileType(), "fileHash", "3", BigDecimal.ONE);
    }

    @Test
    public void shouldBeValidate() {
        validation.clearErrors().validate(consent3);
        assertThat(validation.getErrors()).isEmpty();
    }
}

