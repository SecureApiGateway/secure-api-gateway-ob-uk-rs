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
package com.forgerock.sapi.gateway.ob.uk.rs.server.validator;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.org.openbanking.datamodel.common.OBRisk1;

/**
 * Provides validation for OBRisk1 objects passed to payment interfaces.
 *
 * This class became necessary when an implementor needed to be able to deviate from the OB specification and enforce
 * that the optional field {@code PaymentContextCode} within the {@code OBRisk1} class is provided.
 *
 * @see <a href="https://openbankinguk.github.io/read-write-api-site3/v3.1.5/profiles/payment-initiation-api-profile.html#obrisk1">OB specifications</a>
 */
@Component
@Slf4j
public class OBRisk1Validator {

    private boolean requirePaymentContextCode;

    /**
     * Constructor
     * @param requirePaymentContextCode boolean value. Annotated so that this validator can be enabled in spring
     *                                  config using the property
     *                                  {@code rs.api.payment.validate.risk.require-payment-context-code}
     */
    public OBRisk1Validator(@Value("${rs.api.payment.validate.risk.require-payment-context-code:false}") boolean requirePaymentContextCode) {
        this.requirePaymentContextCode = requirePaymentContextCode;
    }

    /**
     * If the object was constructed with {@code requirePaymentContextCode = true}, this method will throw an
     * OBErrorException if the consent object passed in contains either a null risk object, or a risk object with a
     * null PaymentContextCode.
     *
     * @param risk - the risk object to be validated.
     * @throws OBErrorException if the risk is null or does not contain a PaymentContextCode
     */
    public void validate(final OBRisk1 risk) throws OBErrorException {
        if (requirePaymentContextCode) {
            if (risk == null || risk.getPaymentContextCode() == null) {
                log.debug("'Risk.PaymentContextCode' failed validation as it was not specified. Risk:" + " {}", risk);
                throw new OBErrorException(OBRIErrorType.PAYMENT_CODE_CONTEXT_INVALID);
            }
        }
    }
}
