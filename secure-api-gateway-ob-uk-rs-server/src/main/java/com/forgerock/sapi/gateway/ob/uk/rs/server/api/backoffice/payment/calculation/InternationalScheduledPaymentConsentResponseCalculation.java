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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.backoffice.payment.calculation;

import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBVersion;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import uk.org.openbanking.datamodel.common.OBChargeBearerType1Code;
import uk.org.openbanking.datamodel.payment.*;

import java.util.Objects;

/**
 * Validation class for Domestic Payment Consent response
 * <ul>
 *     <li>
 *         Consent response {@link OBWriteInternationalScheduledConsentResponse3} for v3.1.2
 *     </li>
 *     <li>
 *         Consent response {@link OBWriteInternationalScheduledConsentResponse4} for v3.1.3
 *     </li>
 *     <li>
 *         Consent response {@link OBWriteInternationalScheduledConsentResponse5} for v3.1.4
 *     </li>
 *     <li>
 *         Consent response {@link OBWriteInternationalScheduledConsentResponse6} from v3.1.5 to v3.1.10
 *     </li>
 * </ul>
 */
@SuppressWarnings("unchecked")
@Slf4j
public class InternationalScheduledPaymentConsentResponseCalculation extends PaymentConsentResponseCalculation {

    public static final String TYPE = "UK.OBIE.CHAPSOut";

    @Override
    public Class getResponseClass(OBVersion version) {
        log.debug("{} is the version to calculate response elements", version.getCanonicalName());
        if (version.equals(OBVersion.v3_1_2)) {
            return OBWriteInternationalScheduledConsentResponse3.class;
        } else if (version.equals(OBVersion.v3_1_3)) {
            return OBWriteInternationalScheduledConsentResponse4.class;
        } else if (version.equals(OBVersion.v3_1_4)) {
            return OBWriteInternationalScheduledConsentResponse5.class;
        }
        return OBWriteInternationalScheduledConsentResponse6.class;
    }

    @Override
    public <T, R> R calculate(T consentRequest, R consentResponse) {
        if (consentResponse instanceof OBWriteInternationalScheduledConsentResponse3) {
            log.debug("OBWriteInternationalScheduledConsentResponse3 instance");
            ((OBWriteInternationalScheduledConsentResponse3) consentResponse)
                    .getData()
                    .addChargesItem(
                            new OBWriteDomesticConsentResponse3DataCharges()
                                    .chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                                    .type(TYPE)
                                    .amount(getDefaultAmount())
                    );

            if (Objects.nonNull(((OBWriteInternationalScheduledConsent5) consentRequest).getData().getInitiation().getExchangeRateInformation())) {
                OBExchangeRateType2Code rateType = ((OBWriteInternationalScheduledConsentResponse3) consentResponse).getData().getInitiation().getExchangeRateInformation().getRateType();
                switch (rateType) {
                    case ACTUAL -> {
                        ((OBWriteInternationalScheduledConsentResponse3) consentResponse).getData().setExchangeRateInformation(
                                new OBWriteInternationalConsentResponse3DataExchangeRateInformation()
                                        .exchangeRate(EXCHANGE_RATE)
                                        .rateType(rateType)
                                        .unitCurrency(((OBWriteInternationalScheduledConsentResponse3) consentResponse).getData().getInitiation().getExchangeRateInformation().getUnitCurrency())
                                        .expirationDateTime(DateTime.now().plusMinutes(10))
                        );
                    }
                    case INDICATIVE -> {
                        ((OBWriteInternationalScheduledConsentResponse3) consentResponse).getData().setExchangeRateInformation(
                                new OBWriteInternationalConsentResponse3DataExchangeRateInformation()
                                        .exchangeRate(EXCHANGE_RATE)
                                        .rateType(rateType)
                                        .unitCurrency(((OBWriteInternationalScheduledConsentResponse3) consentResponse).getData().getInitiation().getExchangeRateInformation().getUnitCurrency())
                        );

                    }
                    case AGREED -> {
                        ((OBWriteInternationalScheduledConsentResponse3) consentResponse).getData().setExchangeRateInformation(
                                new OBWriteInternationalConsentResponse3DataExchangeRateInformation()
                                        .exchangeRate(((OBWriteInternationalScheduledConsentResponse3) consentResponse).getData().getInitiation().getExchangeRateInformation().getExchangeRate())
                                        .rateType(rateType)
                                        .unitCurrency(((OBWriteInternationalScheduledConsentResponse3) consentResponse).getData().getInitiation().getExchangeRateInformation().getUnitCurrency())
                                        .contractIdentification(((OBWriteInternationalScheduledConsentResponse3) consentResponse).getData().getInitiation().getExchangeRateInformation().getContractIdentification())
                        );
                    }
                    default -> errors.add(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                            String.format("The rate type %s provided isn't valid", rateType)
                    ));
                }
            } else {
                // TPP did not supply ExchangeRateInformation in the request, generate an indicative quote using the InstructedAmount.currency as the UnitCurrency
                ((OBWriteInternationalScheduledConsentResponse3) consentResponse).getData().setExchangeRateInformation(
                        new OBWriteInternationalConsentResponse3DataExchangeRateInformation()
                                .exchangeRate(EXCHANGE_RATE)
                                .rateType(OBExchangeRateType2Code.INDICATIVE)
                                .unitCurrency(((OBWriteInternationalScheduledConsentResponse3) consentResponse).getData().getInitiation().getInstructedAmount().getCurrency()));
            }
        } else if (consentResponse instanceof OBWriteInternationalScheduledConsentResponse4) {
            log.debug("OBWriteInternationalScheduledConsentResponse4 instance");
            ((OBWriteInternationalScheduledConsentResponse4) consentResponse)
                    .getData()
                    .addChargesItem(
                            new OBWriteDomesticConsentResponse3DataCharges()
                                    .chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                                    .type(TYPE)
                                    .amount(getDefaultAmount())
                    );

            if (Objects.nonNull(((OBWriteInternationalScheduledConsent5) consentRequest).getData().getInitiation().getExchangeRateInformation())) {
                OBExchangeRateType2Code rateType = ((OBWriteInternationalScheduledConsentResponse4) consentResponse).getData().getInitiation().getExchangeRateInformation().getRateType();
                switch (rateType) {
                    case ACTUAL -> {
                        ((OBWriteInternationalScheduledConsentResponse4) consentResponse).getData().setExchangeRateInformation(
                                new OBWriteInternationalConsentResponse4DataExchangeRateInformation()
                                        .exchangeRate(EXCHANGE_RATE)
                                        .rateType(rateType)
                                        .unitCurrency(((OBWriteInternationalScheduledConsentResponse4) consentResponse).getData().getInitiation().getExchangeRateInformation().getUnitCurrency())
                                        .expirationDateTime(DateTime.now().plusMinutes(10))
                        );
                    }
                    case INDICATIVE -> {
                        ((OBWriteInternationalScheduledConsentResponse4) consentResponse).getData().setExchangeRateInformation(
                                new OBWriteInternationalConsentResponse4DataExchangeRateInformation()
                                        .exchangeRate(EXCHANGE_RATE)
                                        .rateType(rateType)
                                        .unitCurrency(((OBWriteInternationalScheduledConsentResponse4) consentResponse).getData().getInitiation().getExchangeRateInformation().getUnitCurrency())
                        );

                    }
                    case AGREED -> {
                        ((OBWriteInternationalScheduledConsentResponse4) consentResponse).getData().setExchangeRateInformation(
                                new OBWriteInternationalConsentResponse4DataExchangeRateInformation()
                                        .exchangeRate(((OBWriteInternationalScheduledConsentResponse4) consentResponse).getData().getInitiation().getExchangeRateInformation().getExchangeRate())
                                        .rateType(rateType)
                                        .unitCurrency(((OBWriteInternationalScheduledConsentResponse4) consentResponse).getData().getInitiation().getExchangeRateInformation().getUnitCurrency())
                                        .contractIdentification(((OBWriteInternationalScheduledConsentResponse4) consentResponse).getData().getInitiation().getExchangeRateInformation().getContractIdentification())
                        );
                    }
                    default -> errors.add(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                            String.format("The rate type %s provided isn't valid", rateType)
                    ));
                }
            } else {
                // TPP did not supply ExchangeRateInformation in the request, generate an indicative quote using the InstructedAmount.currency as the UnitCurrency
                ((OBWriteInternationalScheduledConsentResponse4) consentResponse).getData().setExchangeRateInformation(
                        new OBWriteInternationalConsentResponse4DataExchangeRateInformation()
                                .exchangeRate(EXCHANGE_RATE)
                                .rateType(OBExchangeRateType2Code.INDICATIVE)
                                .unitCurrency(((OBWriteInternationalScheduledConsentResponse4) consentResponse).getData().getInitiation().getInstructedAmount().getCurrency()));
            }
        } else if (consentResponse instanceof OBWriteInternationalScheduledConsentResponse5) {
            log.debug("OBWriteInternationalScheduledConsentResponse5 instance");
            ((OBWriteInternationalScheduledConsentResponse5) consentResponse)
                    .getData()
                    .addChargesItem(
                            new OBWriteDomesticConsentResponse4DataCharges()
                                    .chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                                    .type(TYPE)
                                    .amount(getDefaultAmount())
                    );

            if (Objects.nonNull(((OBWriteInternationalScheduledConsent5) consentRequest).getData().getInitiation().getExchangeRateInformation())) {
                OBExchangeRateType2Code rateType = ((OBWriteInternationalScheduledConsentResponse5) consentResponse).getData().getInitiation().getExchangeRateInformation().getRateType();
                switch (rateType) {
                    case ACTUAL -> {
                        ((OBWriteInternationalScheduledConsentResponse5) consentResponse).getData().setExchangeRateInformation(
                                new OBWriteInternationalConsentResponse5DataExchangeRateInformation()
                                        .exchangeRate(EXCHANGE_RATE)
                                        .rateType(rateType)
                                        .unitCurrency(((OBWriteInternationalScheduledConsentResponse5) consentResponse).getData().getInitiation().getExchangeRateInformation().getUnitCurrency())
                                        .expirationDateTime(DateTime.now().plusMinutes(10))
                        );
                    }
                    case INDICATIVE -> {
                        ((OBWriteInternationalScheduledConsentResponse5) consentResponse).getData().setExchangeRateInformation(
                                new OBWriteInternationalConsentResponse5DataExchangeRateInformation()
                                        .exchangeRate(EXCHANGE_RATE)
                                        .rateType(rateType)
                                        .unitCurrency(((OBWriteInternationalScheduledConsentResponse5) consentResponse).getData().getInitiation().getExchangeRateInformation().getUnitCurrency())
                        );

                    }
                    case AGREED -> {
                        ((OBWriteInternationalScheduledConsentResponse5) consentResponse).getData().setExchangeRateInformation(
                                new OBWriteInternationalConsentResponse5DataExchangeRateInformation()
                                        .exchangeRate(((OBWriteInternationalScheduledConsentResponse5) consentResponse).getData().getInitiation().getExchangeRateInformation().getExchangeRate())
                                        .rateType(rateType)
                                        .unitCurrency(((OBWriteInternationalScheduledConsentResponse5) consentResponse).getData().getInitiation().getExchangeRateInformation().getUnitCurrency())
                                        .contractIdentification(((OBWriteInternationalScheduledConsentResponse5) consentResponse).getData().getInitiation().getExchangeRateInformation().getContractIdentification())
                        );
                    }
                    default -> errors.add(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                            String.format("The rate type %s provided isn't valid", rateType)
                    ));
                }
            } else {
                // TPP did not supply ExchangeRateInformation in the request, generate an indicative quote using the InstructedAmount.currency as the UnitCurrency
                ((OBWriteInternationalScheduledConsentResponse5) consentResponse).getData().setExchangeRateInformation(
                        new OBWriteInternationalConsentResponse5DataExchangeRateInformation()
                                .exchangeRate(EXCHANGE_RATE)
                                .rateType(OBExchangeRateType2Code.INDICATIVE)
                                .unitCurrency(((OBWriteInternationalScheduledConsentResponse5) consentResponse).getData().getInitiation().getInstructedAmount().getCurrency()));
            }
        } else {
            log.debug("OBWriteInternationalScheduledConsentResponse6 instance");
            ((OBWriteInternationalScheduledConsentResponse6) consentResponse)
                    .getData()
                    .addChargesItem(
                            new OBWriteDomesticConsentResponse5DataCharges().
                                    chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                                    .type(TYPE)
                                    .amount(getDefaultAmount())
                    );

            if (Objects.nonNull(((OBWriteInternationalScheduledConsent5) consentRequest).getData().getInitiation().getExchangeRateInformation())) {
                OBExchangeRateType2Code rateType = ((OBWriteInternationalScheduledConsentResponse6) consentResponse).getData().getInitiation().getExchangeRateInformation().getRateType();
                switch (rateType) {
                    case ACTUAL -> {
                        ((OBWriteInternationalScheduledConsentResponse6) consentResponse).getData().setExchangeRateInformation(
                                new OBWriteInternationalConsentResponse6DataExchangeRateInformation()
                                        .exchangeRate(EXCHANGE_RATE)
                                        .rateType(rateType)
                                        .unitCurrency(((OBWriteInternationalScheduledConsentResponse6) consentResponse).getData().getInitiation().getExchangeRateInformation().getUnitCurrency())
                                        .expirationDateTime(DateTime.now().plusMinutes(10))
                        );
                    }
                    case INDICATIVE -> {
                        ((OBWriteInternationalScheduledConsentResponse6) consentResponse).getData().setExchangeRateInformation(
                                new OBWriteInternationalConsentResponse6DataExchangeRateInformation()
                                        .exchangeRate(EXCHANGE_RATE)
                                        .rateType(rateType)
                                        .unitCurrency(((OBWriteInternationalScheduledConsentResponse6) consentResponse).getData().getInitiation().getExchangeRateInformation().getUnitCurrency())
                        );

                    }
                    case AGREED -> {
                        ((OBWriteInternationalScheduledConsentResponse6) consentResponse).getData().setExchangeRateInformation(
                                new OBWriteInternationalConsentResponse6DataExchangeRateInformation()
                                        .exchangeRate(((OBWriteInternationalScheduledConsentResponse6) consentResponse).getData().getInitiation().getExchangeRateInformation().getExchangeRate())
                                        .rateType(rateType)
                                        .unitCurrency(((OBWriteInternationalScheduledConsentResponse6) consentResponse).getData().getInitiation().getExchangeRateInformation().getUnitCurrency())
                                        .contractIdentification(((OBWriteInternationalScheduledConsentResponse6) consentResponse).getData().getInitiation().getExchangeRateInformation().getContractIdentification())
                        );
                    }
                    default -> errors.add(OBRIErrorType.DATA_INVALID_REQUEST.toOBError1(
                            String.format("The rate type %s provided isn't valid", rateType)
                    ));
                }
            } else {
                // TPP did not supply ExchangeRateInformation in the request, generate an indicative quote using the InstructedAmount.currency as the UnitCurrency
                ((OBWriteInternationalScheduledConsentResponse6) consentResponse).getData().setExchangeRateInformation(
                        new OBWriteInternationalConsentResponse6DataExchangeRateInformation()
                                .exchangeRate(EXCHANGE_RATE)
                                .rateType(OBExchangeRateType2Code.INDICATIVE)
                                .unitCurrency(((OBWriteInternationalScheduledConsentResponse6) consentResponse).getData().getInitiation().getInstructedAmount().getCurrency()));
            }
        }
        return consentResponse;
    }
}
