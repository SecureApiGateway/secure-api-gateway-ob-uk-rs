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

import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.org.openbanking.datamodel.common.OBExternalPaymentContext1Code;
import uk.org.openbanking.datamodel.common.OBRisk1;
import uk.org.openbanking.datamodel.common.OBRisk1DeliveryAddress;
import uk.org.openbanking.datamodel.payment.OBExternalExtendedAccountType1Code;

import java.util.Objects;

/**
 * Service implementation Class to validate {@link OBRisk1} property between initiation payment and consent <br/>
 * <ul>
 *     <li>The {@link OBRisk1} property provided in the initiation payment must match the {@link OBRisk1} provided in the consent</li>
 *     <li>Validates nullability and equality between {@link OBRisk1} properties</li>
 * </ul>
 */
@Service
@Slf4j
public class RiskValidationService {

    /**
     * Validates the equality of {@link OBRisk1} properties between the initiation Payment request against the consent <br/>
     * From version 3.1.4 to 3.1.10
     * @param consentRisk the consent as saved during the consent creation step
     * @param requestRisk the risk from the current initiation Payment request
     * @throws OBErrorException typified exception
     */
    public void validate(OBRisk1 consentRisk, OBRisk1 requestRisk) throws OBErrorException {
        if (Objects.isNull(requestRisk)) {
            log.error("Request Risk cannot be null");
            throw new OBErrorException(OBRIErrorType.PAYMENT_INVALID_RISK, "Request Risk cannot be null");
        }
        checkEquality(consentRisk.getBeneficiaryAccountType(), requestRisk.getBeneficiaryAccountType());
        checkEquality(
                consentRisk.getBeneficiaryPrepopulatedIndicator(),
                requestRisk.getBeneficiaryPrepopulatedIndicator(),
                "BeneficiaryPrepopulatedIndicator"
        );
        checkEquality(
                consentRisk.getContractPresentInidicator(),
                requestRisk.getContractPresentInidicator(),
                "ContractPresentInidicator"
        );
        checkEquality(
                consentRisk.getMerchantCategoryCode(),
                requestRisk.getMerchantCategoryCode(),
                "MerchantCategoryCode"
        );
        checkEquality(
                consentRisk.getMerchantCustomerIdentification(),
                requestRisk.getMerchantCustomerIdentification(),
                "MerchantCustomerIdentification"
        );
        checkEquality(consentRisk.getPaymentContextCode(), requestRisk.getPaymentContextCode());
        checkEquality(
                consentRisk.getPaymentPurposeCode(),
                requestRisk.getPaymentPurposeCode(),
                "PaymentPurposeCode"
        );
        checkEquality(consentRisk.getDeliveryAddress(), requestRisk.getDeliveryAddress());
    }

    /**
     * Checks nullability and equality between of {@link Boolean} properties between request and consent
     * @param fromConsent Boolean field value from consent
     * @param fromRequest Boolean field value from request
     * @param propertyName property name to check for logging purposes
     * @throws OBErrorException typified exception
     */
    private void checkEquality(Boolean fromConsent, Boolean fromRequest, String propertyName) throws OBErrorException {
        if (Objects.isNull(fromConsent)) {
            propertyMustBeNull(fromRequest, propertyName);
        } else if (Objects.isNull(fromRequest) || Boolean.compare(fromRequest, fromConsent) != 0) {
            throwError("The property '" + propertyName + "' value does not match with the value provided in the consent");
        }
    }

    /**
     * Checks nullability and equality of {@link OBExternalExtendedAccountType1Code} property between request and consent
     * @param fromConsent consent field value
     * @param fromRequest request field value
     * @throws OBErrorException typified exception
     */
    private void checkEquality(OBExternalExtendedAccountType1Code fromConsent, OBExternalExtendedAccountType1Code fromRequest) throws OBErrorException {
        if (Objects.isNull(fromConsent)) {
            propertyMustBeNull(fromRequest, "BeneficiaryAccountType");
        } else if (!fromConsent.equals(fromRequest)) {
            throwError("The property 'BeneficiaryAccountType' value does not match with the value provided in the consent");
        }
    }

    /**
     * Checks nullability and equality of {@link OBExternalPaymentContext1Code} property between request and consent
     * @param fromConsent consent field value
     * @param fromRequest request field value
     * @throws OBErrorException typified exception
     */
    private void checkEquality(OBExternalPaymentContext1Code fromConsent, OBExternalPaymentContext1Code fromRequest) throws OBErrorException {
        if (Objects.isNull(fromConsent)) {
            propertyMustBeNull(fromRequest, "PaymentContextCode");
        } else if (!fromConsent.equals(fromRequest)) {
            throwError("The property 'PaymentContextCode' value does not match with the value provided in the consent");
        }
    }

    /**
     * Checks nullability and equality of {@link String} properties between request field value and consent field value
     * @param fromConsent consent field value as string
     * @param fromRequest request field value as string
     * @param propertyName property name to check for logging purposes
     * @throws OBErrorException typified exception
     */
    private void checkEquality(String fromConsent, String fromRequest, String propertyName) throws OBErrorException {
        if (Objects.isNull(fromConsent)) {
            propertyMustBeNull(fromRequest, propertyName);
        } else if (!fromConsent.equals(fromRequest)) {
            throwError("The property '" + propertyName + "' value does not match with the value provided in the consent");
        }
    }

    /**
     * Checks nullability and equality of {@link  OBRisk1DeliveryAddress} property between request and consent
     * @param fromConsent consent field value
     * @param fromRequest request field value
     * @throws OBErrorException typified exception
     */
    private void checkEquality(OBRisk1DeliveryAddress fromConsent, OBRisk1DeliveryAddress fromRequest) throws OBErrorException {
        if (Objects.isNull(fromConsent)) {
            propertyMustBeNull(fromRequest, "DeliveryAddress");
        } else if (!fromConsent.equals(fromRequest)) {
            throwError("The property 'DeliveryAddress' value does not match with the value provided in the consent");
        }
    }

    /**
     * Checks the {@link Object} property must be null
     * @param property property value to be checked
     * @param propertyName property name to check for logging purposes
     * @throws OBErrorException typified exception
     */
    protected void propertyMustBeNull(Object property, String propertyName) throws OBErrorException {
        log.warn("The consent property '{}' value is null, validating nullability in the request", propertyName);
        if (!Objects.isNull(property)) {
            throwError("The property '" + propertyName + "' It was expected to be null as provided in the consent");
        }
    }

    private void throwError(String reason) throws OBErrorException {
        log.error(reason);
        throw new OBErrorException(OBRIErrorType.PAYMENT_INVALID_RISK, reason);
    }
}
