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
package com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.calculation;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.obie.OBVersion;
import com.forgerock.securebanking.openbanking.uk.rs.api.backoffice.payment.utils.DefaultData;
import lombok.extern.slf4j.Slf4j;
import uk.org.openbanking.datamodel.common.OBChargeBearerType1Code;
import uk.org.openbanking.datamodel.error.OBError1;
import uk.org.openbanking.datamodel.payment.*;

import java.util.List;

/**
 * Validation class for Domestic Payment Consent response
 * <ul>
 *     <li>
 *         Consent response {@link OBWriteInternationalConsentResponse3} for v3.1.2
 *     </li>
 *     <li>
 *         Consent response {@link OBWriteInternationalConsentResponse4} for v3.1.3
 *     </li>
 *     <li>
 *         Consent response {@link OBWriteInternationalConsentResponse5} for v3.1.4
 *     </li>
 *     <li>
 *         Consent response {@link OBWriteInternationalConsentResponse6} from v3.1.5 to v3.1.10
 *     </li>
 * </ul>
 */
@SuppressWarnings("unchecked")
@Slf4j
public class InternationalPaymentConsentResponseCalculation extends PaymentConsentResponseCalculation {

    public static final String TYPE = "UK.OBIE.CHAPSOut";

    @Override
    public Class getResponseClass(OBVersion version) {
        log.debug("{} is the version to calculate response elements", version.getCanonicalName());
        if (version.equals(OBVersion.v3_1_2)) {
            return OBWriteInternationalConsentResponse3.class;
        } else if (version.equals(OBVersion.v3_1_3)) {
            return OBWriteInternationalConsentResponse4.class;
        } else if (version.equals(OBVersion.v3_1_4)) {
            return OBWriteInternationalConsentResponse5.class;
        }
        return OBWriteInternationalConsentResponse6.class;
    }

    @Override
    public <T, R> R calculate(T consentRequest, R consentResponse) {
        errors.clear();

        if (consentResponse instanceof OBWriteInternationalConsentResponse3) {
            log.debug("OBWriteInternationalConsentResponse3 instance");
            ((OBWriteInternationalConsentResponse3) consentResponse)
                    .getData()
                    .addChargesItem(
                            new OBWriteDomesticConsentResponse3DataCharges()
                                    .chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                                    .type(TYPE)
                                    .amount(getDefaultAmount())
                    );
            if (((OBWriteInternationalConsentResponse3) consentResponse).getData().getInitiation().getExchangeRateInformation() == null) {
                log.debug("OBWriteInternationalConsentResponse3 instance uses default exchangeRate");
                ((OBWriteInternationalConsentResponse3) consentResponse)
                        .getData().
                        getInitiation()
                        .setExchangeRateInformation(
                                DefaultData.defaultOBWriteInternational2DataInitiationExchangeRateInformation(
                                        ((OBWriteInternationalConsentResponse3) consentResponse).getData().getInitiation().getInstructedAmount().getCurrency(),
                                        ((OBWriteInternationalConsentResponse3) consentResponse).getData().getInitiation().getCurrencyOfTransfer())
                        );
            }

        } else if (consentResponse instanceof OBWriteInternationalConsentResponse4) {
            log.debug("OBWriteInternationalConsentResponse4 instance");
            ((OBWriteInternationalConsentResponse4) consentResponse)
                    .getData()
                    .addChargesItem(
                            new OBWriteDomesticConsentResponse3DataCharges()
                                    .chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                                    .type(TYPE)
                                    .amount(getDefaultAmount())
                    );
            if (((OBWriteInternationalConsentResponse4) consentResponse).getData().getInitiation().getExchangeRateInformation() == null) {
                log.debug("OBWriteInternationalConsentResponse4 instance uses default exchangeRate");
                ((OBWriteInternationalConsentResponse4) consentResponse)
                        .getData().
                        getInitiation()
                        .setExchangeRateInformation(
                                DefaultData.defaultOBWriteInternational3DataInitiationExchangeRateInformation(
                                        ((OBWriteInternationalConsentResponse4) consentResponse).getData().getInitiation().getInstructedAmount().getCurrency(),
                                        ((OBWriteInternationalConsentResponse4) consentResponse).getData().getInitiation().getCurrencyOfTransfer())
                        );
            }

        } else if (consentResponse instanceof OBWriteInternationalConsentResponse5) {
            log.debug("OBWriteInternationalConsentResponse5 instance");
            ((OBWriteInternationalConsentResponse5) consentResponse)
                    .getData()
                    .addChargesItem(
                            new OBWriteDomesticConsentResponse4DataCharges()
                                    .chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                                    .type(TYPE)
                                    .amount(getDefaultAmount())
                    );

            if (((OBWriteInternationalConsentResponse5) consentResponse).getData().getInitiation().getExchangeRateInformation() == null) {
                log.debug("OBWriteInternationalConsentResponse5 instance uses default exchangeRate");
                ((OBWriteInternationalConsentResponse5) consentResponse)
                        .getData().
                        getInitiation()
                        .setExchangeRateInformation(
                                DefaultData.defaultOBWriteInternational3DataInitiationExchangeRateInformation(
                                        ((OBWriteInternationalConsentResponse5) consentResponse).getData().getInitiation().getInstructedAmount().getCurrency(),
                                        ((OBWriteInternationalConsentResponse5) consentResponse).getData().getInitiation().getCurrencyOfTransfer())
                        );
            }

        } else {
            log.debug("OBWriteInternationalConsentResponse6 instance");
            ((OBWriteInternationalConsentResponse6) consentResponse)
                    .getData()
                    .addChargesItem(
                            new OBWriteDomesticConsentResponse5DataCharges().
                                    chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                                    .type(TYPE)
                                    .amount(getDefaultAmount())
                    );

            if (((OBWriteInternationalConsentResponse6) consentResponse).getData().getInitiation().getExchangeRateInformation() == null) {
                log.debug("OBWriteInternationalConsentResponse6 instance uses default exchangeRate");
                ((OBWriteInternationalConsentResponse6) consentResponse)
                        .getData().
                        getInitiation()
                        .setExchangeRateInformation(
                                DefaultData.defaultOBWriteInternational3DataInitiationExchangeRateInformation(
                                        ((OBWriteInternationalConsentResponse6) consentResponse).getData().getInitiation().getInstructedAmount().getCurrency(),
                                        ((OBWriteInternationalConsentResponse6) consentResponse).getData().getInitiation().getCurrencyOfTransfer())
                        );
            }
        }

        return consentResponse;
    }

    @Override
    public List<OBError1> getErrors() {
        return errors;
    }
}
