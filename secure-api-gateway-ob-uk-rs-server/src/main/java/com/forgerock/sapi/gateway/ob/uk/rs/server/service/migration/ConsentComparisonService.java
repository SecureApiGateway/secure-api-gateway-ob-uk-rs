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
package com.forgerock.sapi.gateway.ob.uk.rs.server.service.migration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRPeriodicLimits;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRPeriodicLimits.PeriodAlignmentEnum;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRPeriodicLimits.PeriodTypeEnum;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.vrp.FRVRPInteractionType;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.payment.vrp.v3_1_10.DomesticVRPConsent;

import lombok.extern.slf4j.Slf4j;
import uk.org.openbanking.datamodel.v4.vrp.OBDomesticVRPConsentRequest;
import uk.org.openbanking.datamodel.v4.vrp.OBDomesticVRPControlParametersPeriodicLimitsInner;
import uk.org.openbanking.datamodel.v4.vrp.OBPeriodAlignment;
import uk.org.openbanking.datamodel.v4.vrp.OBPeriodType;
import uk.org.openbanking.datamodel.v4.vrp.OBVRPInteractionTypes;

@Service
@Slf4j
public class ConsentComparisonService {

    /**
     * Determines if values originally supplied in the consent such as account information, control parameters, dates or monetary values match the ones provided on the request.
     *
     * @return {@code true} if objects match.
     * {@code @params} request, consent
     */

    public boolean doFieldsMatch(OBDomesticVRPConsentRequest request, DomesticVRPConsent consent) {

  /*      if (request == null || consent == null || consent.getRequestObj() == null) {
            return false;
        }*/

        List<String> requestPSUAuthenticationMethods = extractPsuAuthMethods(request);
        List<String> consentPSUAuthenticationMethods = extractPsuAuthMethods(consent);

        List<String> requestVRPType = extractVrpType(request);
        List<String> consentVRPType = extractVrpType(consent);

        String requestValidFromDateTime = extractValidFromDateTime(request);
        String consentValidFromDateTime = extractValidFromDateTime(consent);

        String requestValidToDateTime = extractValidToDateTime(request);
        String consentValidToDateTime = extractValidToDateTime(consent);

        List<OBVRPInteractionTypes> requestPSUInteractionTypes = extractPSUInteractionTypes(request);
        List<FRVRPInteractionType> consentPSUInteractionTypes = extractPSUInteractionTypes(consent);

        String requestAmount = extractMaximumIndividualAmountAmount(request);
        String consentAmount = extractMaximumIndividualAmountAmount(consent);

        String requestCurrency = extractMaximumIndividualAmountCurrency(request);
        String consentCurrency = extractMaximumIndividualAmountCurrency(consent);

        List<String> requestPeriodicLimitsAmount = extractPeriodicLimitsAmount(request);
        String consentPeriodicLimitsAmount = extractPeriodicLimitsAmount(consent);

        List<String> requestPeriodicLimitsCurrency = extractPeriodicLimitsCurrency(request);
        String consentPeriodicLimitsCurrency = extractPeriodicLimitsCurrency(consent);

        List<OBPeriodAlignment> requestPeriodicLimitsPeriodAlignment = extractPeriodicLimitsPeriodAlignment(request);
        String consentPeriodicLimitsPeriodAlignment = extractPeriodicLimitsPeriodAlignment(consent);

        List<OBPeriodType> requestPeriodicLimitsPeriodType = extractPeriodicLimitsPeriodType(request);
        String consentPeriodicLimitsPeriodType = extractPeriodicLimitsPeriodType(consent);

        String requestCreditorAccountIdentification = extractCreditorAccountIdentification(request);
        String consentCreditorAccountIdentification = extractCreditorAccountIdentification(consent);

        String requestCreditorAccountName = extractCreditorAccountName(request);
        String consentCreditorAccountName = extractCreditorAccountName(consent);

        String requestCreditorAccountSchemeName = extractCreditorAccountSchemeName(request);
        String consentCreditorAccountSchemeName = extractCreditorAccountSchemeName(consent);

        String requestCreditorAccountSecondaryIdentification = extractCreditorAccountSecondaryIdentification(request);
        String consentCreditorAccountSecondaryIdentification = extractCreditorAccountSecondaryIdentification(consent);

        String requestDebtorAccountIdentification = extractDebtorAccountIdentification(request);
        String consentDebtorAccountIdentification = extractDebtorAccountIdentification(consent);

        String requestDebtorAccountName = extractDebtorAccountName(request);
        String consentDebtorAccountName = extractDebtorAccountName(consent);

        String requestDebtorAccountSchemeName = extractDebtorAccountSchemeName(request);
        String consentDebtorAccountSchemeName = extractDebtorAccountSchemeName(consent);

        String requestDebtorAccountSecondaryIdentification = extractDebtorAccountSecondaryIdentification(request);
        String consentDebtorAccountSecondaryIdentification = extractDebtorAccountSecondaryIdentification(consent);

        return doListsMatch(requestPSUAuthenticationMethods, consentPSUAuthenticationMethods) &&
                doListsMatch(requestVRPType, consentVRPType) &&
                doPSUInteractionTypesMatch(requestPSUInteractionTypes, consentPSUInteractionTypes) &&
                doStringsMatch(requestValidFromDateTime, consentValidFromDateTime) &&
                doStringsMatch(requestValidToDateTime, consentValidToDateTime) &&
                doStringsMatch(requestAmount, consentAmount) &&
                doStringsMatch(requestCurrency, consentCurrency) &&
                doPeriodicLimitsAmountsMatch(requestPeriodicLimitsAmount, consentPeriodicLimitsAmount) &&
                doPeriodicLimitsCurrenciesMatch(requestPeriodicLimitsCurrency, consentPeriodicLimitsCurrency) &&
                doPeriodicLimitsPeriodAlignmentsMatch(requestPeriodicLimitsPeriodAlignment, consentPeriodicLimitsPeriodAlignment) &&
                doPeriodicLimitsPeriodTypesMatch(requestPeriodicLimitsPeriodType, consentPeriodicLimitsPeriodType) &&
                doStringsMatch(requestCreditorAccountIdentification, consentCreditorAccountIdentification) &&
                doStringsMatch(requestCreditorAccountName, consentCreditorAccountName) &&
                doStringsMatch(requestCreditorAccountSchemeName, consentCreditorAccountSchemeName) &&
                doStringsMatch(requestCreditorAccountSecondaryIdentification, consentCreditorAccountSecondaryIdentification) &&
                doStringsMatch(requestDebtorAccountIdentification, consentDebtorAccountIdentification) &&
                doStringsMatch(requestDebtorAccountName, consentDebtorAccountName) &&
                doStringsMatch(requestDebtorAccountSchemeName, consentDebtorAccountSchemeName) &&
                doStringsMatch(requestDebtorAccountSecondaryIdentification, consentDebtorAccountSecondaryIdentification);
    }

    // ControlParameters.PSUAuthenticationMethods
    private List<String> extractPsuAuthMethods(OBDomesticVRPConsentRequest request) {
        if (request == null || request.getData() == null || request.getData().getControlParameters() == null) {
            return null;
        }
        return request.getData().getControlParameters().getPsUAuthenticationMethods();
    }

    private List<String> extractPsuAuthMethods(DomesticVRPConsent consent) {
        if (consent == null || consent.getRequestObj() == null ||
                consent.getRequestObj().getData() == null ||
                consent.getRequestObj().getData().getControlParameters() == null) {
            return null;
        }
        return consent.getRequestObj().getData().getControlParameters().getPsuAuthenticationMethods();
    }

    // ControlParameters.PSUInteractionTypes
    private List<OBVRPInteractionTypes> extractPSUInteractionTypes(OBDomesticVRPConsentRequest request) {
        if (request == null || request.getData() == null || request.getData().getControlParameters() == null) {
            return null;
        }
        return request.getData().getControlParameters().getPsUInteractionTypes();
    }

    private List<FRVRPInteractionType> extractPSUInteractionTypes(DomesticVRPConsent consent) {
        if (consent == null || consent.getRequestObj() == null ||
                consent.getRequestObj().getData() == null ||
                consent.getRequestObj().getData().getControlParameters() == null) {
            return null;
        }
        return consent.getRequestObj().getData().getControlParameters().getPsUInteractionTypes();
    }

    // ControlParameters.VRPType
    private List<String> extractVrpType(OBDomesticVRPConsentRequest request) {
        if (request == null || request.getData() == null || request.getData().getControlParameters() == null) {
            return null;
        }
        return request.getData().getControlParameters().getVrPType();
    }

    private List<String> extractVrpType(DomesticVRPConsent consent) {
        if (consent == null || consent.getRequestObj() == null ||
                consent.getRequestObj().getData() == null ||
                consent.getRequestObj().getData().getControlParameters() == null) {
            return null;
        }
        return consent.getRequestObj().getData().getControlParameters().getVrpType();
    }

    // ControlParameters.ValidFromDateTime
    private String extractValidFromDateTime(OBDomesticVRPConsentRequest request) {
        if (request == null || request.getData() == null || request.getData().getControlParameters() == null) {
            return null;
        }
        return String.valueOf(request.getData().getControlParameters().getValidFromDateTime());
    }

    private String extractValidFromDateTime(DomesticVRPConsent consent) {
        if (consent == null || consent.getRequestObj() == null ||
                consent.getRequestObj().getData() == null ||
                consent.getRequestObj().getData().getControlParameters() == null) {
            return null;
        }
        return String.valueOf(consent.getRequestObj().getData().getControlParameters().getValidFromDateTime());
    }

    // ControlParameters.ValidToDateTime
    private String extractValidToDateTime(OBDomesticVRPConsentRequest request) {
        if (request == null || request.getData() == null || request.getData().getControlParameters() == null) {
            return null;
        }
        return String.valueOf(request.getData().getControlParameters().getValidToDateTime());
    }

    private String extractValidToDateTime(DomesticVRPConsent consent) {
        if (consent == null || consent.getRequestObj() == null ||
                consent.getRequestObj().getData() == null ||
                consent.getRequestObj().getData().getControlParameters() == null) {
            return null;
        }
        return String.valueOf(consent.getRequestObj().getData().getControlParameters().getValidToDateTime());
    }

    // MaximumIndividualAmount.Amount
    private String extractMaximumIndividualAmountAmount(OBDomesticVRPConsentRequest request) {
        if (request == null || request.getData() == null ||
                request.getData().getControlParameters() == null ||
                request.getData().getControlParameters().getMaximumIndividualAmount() == null) {
            return null;
        }
        return request.getData().getControlParameters().getMaximumIndividualAmount().getAmount();
    }

    private String extractMaximumIndividualAmountAmount(DomesticVRPConsent consent) {
        if (consent == null || consent.getRequestObj() == null ||
                consent.getRequestObj().getData() == null ||
                consent.getRequestObj().getData().getControlParameters() == null ||
                consent.getRequestObj().getData().getControlParameters().getMaximumIndividualAmount() == null) {
            return null;
        }
        return consent.getRequestObj().getData().getControlParameters().getMaximumIndividualAmount().getAmount();
    }

    // MaximumIndividualAmount.Currency
    private String extractMaximumIndividualAmountCurrency(OBDomesticVRPConsentRequest request) {
        if (request == null || request.getData() == null ||
                request.getData().getControlParameters() == null ||
                request.getData().getControlParameters().getMaximumIndividualAmount() == null) {
            return null;
        }
        return request.getData().getControlParameters().getMaximumIndividualAmount().getCurrency();
    }

    private String extractMaximumIndividualAmountCurrency(DomesticVRPConsent consent) {
        if (consent == null || consent.getRequestObj() == null ||
                consent.getRequestObj().getData() == null ||
                consent.getRequestObj().getData().getControlParameters() == null ||
                consent.getRequestObj().getData().getControlParameters().getMaximumIndividualAmount() == null) {
            return null;
        }
        return consent.getRequestObj().getData().getControlParameters().getMaximumIndividualAmount().getCurrency();
    }

    // PeriodicLimits.Amount
    private List<String> extractPeriodicLimitsAmount(OBDomesticVRPConsentRequest request) {
        List<String> amounts = new ArrayList<>();

        if (request != null &&
                request.getData() != null &&
                request.getData().getControlParameters() != null &&
                request.getData().getControlParameters().getPeriodicLimits() != null) {
            for (OBDomesticVRPControlParametersPeriodicLimitsInner limit :
                    request.getData().getControlParameters().getPeriodicLimits()) {
                if (limit != null && limit.getAmount() != null) {
                    amounts.add(limit.getAmount());
                }
            }
        }
        log.debug("Amount: {}", amounts);
        return amounts;
    }

    private String extractPeriodicLimitsAmount(DomesticVRPConsent consent) {
        List<String> amounts = new ArrayList<>();

        if (consent != null &&
                consent.getRequestObj().getData() != null &&
                consent.getRequestObj().getData().getControlParameters() != null &&
                consent.getRequestObj().getData().getControlParameters().getPeriodicLimits() != null) {
            for (FRPeriodicLimits limit :
                    consent.getRequestObj().getData().getControlParameters().getPeriodicLimits()) {
                if (limit != null && limit.getAmount() != null) {
                    amounts.add(limit.getAmount());
                }
            }
        }
        log.debug("Amount: {}", amounts);
        return String.valueOf(amounts);
    }

    // PeriodicLimits.Currency
    private List<String> extractPeriodicLimitsCurrency(OBDomesticVRPConsentRequest request) {
        List<String> currencies = new ArrayList<>();

        if (request != null &&
                request.getData() != null &&
                request.getData().getControlParameters() != null &&
                request.getData().getControlParameters().getPeriodicLimits() != null) {
            for (OBDomesticVRPControlParametersPeriodicLimitsInner limit :
                    request.getData().getControlParameters().getPeriodicLimits()) {
                if (limit != null && limit.getCurrency() != null) {
                    currencies.add(limit.getCurrency());
                }
            }
        }
        log.debug("Currency: {}", currencies);
        return currencies;
    }

    private String extractPeriodicLimitsCurrency(DomesticVRPConsent consent) {
        List<String> currencies = new ArrayList<>();

        if (consent != null &&
                consent.getRequestObj().getData() != null &&
                consent.getRequestObj().getData().getControlParameters() != null &&
                consent.getRequestObj().getData().getControlParameters().getPeriodicLimits() != null) {
            for (FRPeriodicLimits limit :
                    consent.getRequestObj().getData().getControlParameters().getPeriodicLimits()) {
                if (limit != null && limit.getCurrency() != null) {
                    currencies.add(limit.getCurrency());
                }
            }
        }
        log.debug("Currency: {}", currencies);
        return String.valueOf(currencies);
    }

    // PeriodicLimits.PeriodAlignment
    private List<OBPeriodAlignment> extractPeriodicLimitsPeriodAlignment(OBDomesticVRPConsentRequest request) {
        List<OBPeriodAlignment> periodAlignments = new ArrayList<>();

        if (request != null &&
                request.getData() != null &&
                request.getData().getControlParameters() != null &&
                request.getData().getControlParameters().getPeriodicLimits() != null) {
            for (OBDomesticVRPControlParametersPeriodicLimitsInner limit :
                    request.getData().getControlParameters().getPeriodicLimits()) {
                if (limit != null && limit.getPeriodAlignment() != null) {
                    periodAlignments.add(limit.getPeriodAlignment());
                }
            }
        }
        log.debug("PeriodAlignment: {}", periodAlignments);
        return periodAlignments;
    }

    private String extractPeriodicLimitsPeriodAlignment(DomesticVRPConsent consent) {
        List<PeriodAlignmentEnum> periodAlignments = new ArrayList<>();

        if (consent != null &&
                consent.getRequestObj().getData() != null &&
                consent.getRequestObj().getData().getControlParameters() != null &&
                consent.getRequestObj().getData().getControlParameters().getPeriodicLimits() != null) {
            for (FRPeriodicLimits limit :
                    consent.getRequestObj().getData().getControlParameters().getPeriodicLimits()) {
                if (limit != null && limit.getPeriodAlignment() != null) {
                    periodAlignments.add(limit.getPeriodAlignment());
                }
            }
        }
        log.debug("PeriodAlignment: {}", periodAlignments);
        return String.valueOf(periodAlignments);
    }

    // PeriodicLimits.PeriodType
    private List<OBPeriodType> extractPeriodicLimitsPeriodType(OBDomesticVRPConsentRequest request) {
        List<OBPeriodType> periodTypes = new ArrayList<>();

        if (request != null &&
                request.getData() != null &&
                request.getData().getControlParameters() != null &&
                request.getData().getControlParameters().getPeriodicLimits() != null) {
            for (OBDomesticVRPControlParametersPeriodicLimitsInner limit :
                    request.getData().getControlParameters().getPeriodicLimits()) {
                if (limit != null && limit.getPeriodType() != null) {
                    periodTypes.add(limit.getPeriodType());
                }
            }
        }
        log.debug("PeriodType: {}", periodTypes);
        return periodTypes;
    }

    private String extractPeriodicLimitsPeriodType(DomesticVRPConsent consent) {
        List<PeriodTypeEnum> periodTypes = new ArrayList<>();

        if (consent != null &&
                consent.getRequestObj().getData() != null &&
                consent.getRequestObj().getData().getControlParameters() != null &&
                consent.getRequestObj().getData().getControlParameters().getPeriodicLimits() != null) {
            for (FRPeriodicLimits limit :
                    consent.getRequestObj().getData().getControlParameters().getPeriodicLimits()) {
                if (limit != null && limit.getPeriodType() != null) {
                    periodTypes.add(limit.getPeriodType());
                }
            }
        }
        log.debug("PeriodType: {}", periodTypes);
        return String.valueOf(periodTypes);
    }

    // CreditorAccount.Identification
    private String extractCreditorAccountIdentification(OBDomesticVRPConsentRequest request) {
        if (request == null || request.getData() == null ||
                request.getData().getInitiation() == null ||
                request.getData().getInitiation().getCreditorAccount() == null) {
            return null;
        }
        return request.getData().getInitiation().getCreditorAccount().getIdentification();
    }

    private String extractCreditorAccountIdentification(DomesticVRPConsent consent) {
        if (consent == null || consent.getRequestObj() == null ||
                consent.getRequestObj().getData() == null ||
                consent.getRequestObj().getData().getInitiation().getCreditorAccount() == null ||
                consent.getRequestObj().getData().getInitiation().getCreditorAccount().getIdentification() == null) {
            return null;
        }
        return consent.getRequestObj().getData().getInitiation().getCreditorAccount().getIdentification();
    }

    // CreditorAccount.Name
    private String extractCreditorAccountName(OBDomesticVRPConsentRequest request) {
        if (request == null || request.getData() == null ||
                request.getData().getInitiation() == null ||
                request.getData().getInitiation().getCreditorAccount() == null) {
            return null;
        }
        return request.getData().getInitiation().getCreditorAccount().getName();
    }

    private String extractCreditorAccountName(DomesticVRPConsent consent) {
        if (consent == null || consent.getRequestObj() == null ||
                consent.getRequestObj().getData() == null ||
                consent.getRequestObj().getData().getInitiation().getCreditorAccount() == null ||
                consent.getRequestObj().getData().getInitiation().getCreditorAccount().getName() == null) {
            return null;
        }
        return consent.getRequestObj().getData().getInitiation().getCreditorAccount().getName();
    }

    // CreditorAccount.SchemeName
    private String extractCreditorAccountSchemeName(OBDomesticVRPConsentRequest request) {
        if (request == null || request.getData() == null ||
                request.getData().getInitiation() == null ||
                request.getData().getInitiation().getCreditorAccount() == null) {
            return null;
        }
        return request.getData().getInitiation().getCreditorAccount().getSchemeName();
    }

    private String extractCreditorAccountSchemeName(DomesticVRPConsent consent) {
        if (consent == null || consent.getRequestObj() == null ||
                consent.getRequestObj().getData() == null ||
                consent.getRequestObj().getData().getInitiation().getCreditorAccount() == null ||
                consent.getRequestObj().getData().getInitiation().getCreditorAccount().getSchemeName() == null) {
            return null;
        }
        return consent.getRequestObj().getData().getInitiation().getCreditorAccount().getSchemeName();
    }

    // CreditorAccount.SecondaryIdentification
    private String extractCreditorAccountSecondaryIdentification(OBDomesticVRPConsentRequest request) {
        if (request == null || request.getData() == null ||
                request.getData().getInitiation() == null ||
                request.getData().getInitiation().getCreditorAccount() == null) {
            return null;
        }
        return request.getData().getInitiation().getCreditorAccount().getSecondaryIdentification();
    }

    private String extractCreditorAccountSecondaryIdentification(DomesticVRPConsent consent) {
        if (consent == null || consent.getRequestObj() == null ||
                consent.getRequestObj().getData() == null ||
                consent.getRequestObj().getData().getInitiation().getCreditorAccount() == null ||
                consent.getRequestObj().getData().getInitiation().getCreditorAccount().getSecondaryIdentification() == null) {
            return null;
        }
        return consent.getRequestObj().getData().getInitiation().getCreditorAccount().getSecondaryIdentification();
    }

    // DebtorAccount.Identification
    private String extractDebtorAccountIdentification(OBDomesticVRPConsentRequest request) {
        if (request == null || request.getData() == null ||
                request.getData().getInitiation() == null ||
                request.getData().getInitiation().getDebtorAccount() == null) {
            return null;
        }
        return request.getData().getInitiation().getDebtorAccount().getIdentification();
    }

    private String extractDebtorAccountIdentification(DomesticVRPConsent consent) {
        if (consent == null || consent.getRequestObj() == null ||
                consent.getRequestObj().getData() == null ||
                consent.getRequestObj().getData().getInitiation().getDebtorAccount() == null ||
                consent.getRequestObj().getData().getInitiation().getDebtorAccount().getIdentification() == null) {
            return null;
        }
        return consent.getRequestObj().getData().getInitiation().getDebtorAccount().getIdentification();
    }

    // DebtorAccount.Name
    private String extractDebtorAccountName(OBDomesticVRPConsentRequest request) {
        if (request == null || request.getData() == null ||
                request.getData().getInitiation() == null ||
                request.getData().getInitiation().getDebtorAccount() == null) {
            return null;
        }
        return request.getData().getInitiation().getDebtorAccount().getName();
    }

    private String extractDebtorAccountName(DomesticVRPConsent consent) {
        if (consent == null || consent.getRequestObj() == null ||
                consent.getRequestObj().getData() == null ||
                consent.getRequestObj().getData().getInitiation().getDebtorAccount() == null ||
                consent.getRequestObj().getData().getInitiation().getDebtorAccount().getName() == null) {
            return null;
        }
        return consent.getRequestObj().getData().getInitiation().getDebtorAccount().getName();
    }

    // DebtorAccount.SchemeName
    private String extractDebtorAccountSchemeName(OBDomesticVRPConsentRequest request) {
        if (request == null || request.getData() == null ||
                request.getData().getInitiation() == null ||
                request.getData().getInitiation().getDebtorAccount() == null) {
            return null;
        }
        return request.getData().getInitiation().getDebtorAccount().getSchemeName();
    }

    private String extractDebtorAccountSchemeName(DomesticVRPConsent consent) {
        if (consent == null || consent.getRequestObj() == null ||
                consent.getRequestObj().getData() == null ||
                consent.getRequestObj().getData().getInitiation().getDebtorAccount() == null ||
                consent.getRequestObj().getData().getInitiation().getDebtorAccount().getSchemeName() == null) {
            return null;
        }
        return consent.getRequestObj().getData().getInitiation().getDebtorAccount().getSchemeName();
    }

    // DebtorAccount.SecondaryIdentification
    private String extractDebtorAccountSecondaryIdentification(OBDomesticVRPConsentRequest request) {
        if (request == null || request.getData() == null ||
                request.getData().getInitiation() == null ||
                request.getData().getInitiation().getDebtorAccount() == null) {
            return null;
        }
        return request.getData().getInitiation().getDebtorAccount().getSecondaryIdentification();
    }

    private String extractDebtorAccountSecondaryIdentification(DomesticVRPConsent consent) {
        if (consent == null || consent.getRequestObj() == null ||
                consent.getRequestObj().getData() == null ||
                consent.getRequestObj().getData().getInitiation().getDebtorAccount() == null ||
                consent.getRequestObj().getData().getInitiation().getDebtorAccount().getSecondaryIdentification() == null) {
            return null;
        }
        return consent.getRequestObj().getData().getInitiation().getDebtorAccount().getSecondaryIdentification();
    }

    private boolean doStringsMatch(String s1, String s2) {
        if (Objects.equals(s1, s2)) {
            log.debug("s1: {}", s1);
            log.debug("s2: {}", s2);
            log.debug("Strings match.");
            return true;
        } else {
            log.debug("Strings do not match.");
            return false;
        }
    }

    private boolean doListsMatch(List<String> list1, List<String> list2) {
        if (list1 == null || list2 == null) {
            log.debug("list1: {}", list1);
            log.debug("list2: {}", list2);
            log.debug("One or both lists are null.");
            return false;
        }

        if (list1.isEmpty() && list2.isEmpty()) {
            log.debug("Lists match.");
            return true;
        }

        if (!list1.equals(list2)) {
            log.debug("Lists do not match.");
            log.debug("list1: {}", list1);
            log.debug("list2: {}", list2);
            return false;
        }

        log.debug("Lists match.");
        return true;
    }

    private boolean doPSUInteractionTypesMatch(List<OBVRPInteractionTypes> requestPSUInteractionTypes, List<FRVRPInteractionType> consentPSUInteractionTypes) {

        if (requestPSUInteractionTypes == null || consentPSUInteractionTypes == null) {
            return false;
        }

        // Compare lists
        if (requestPSUInteractionTypes.size() != consentPSUInteractionTypes.size()) {
            return false;
        }

        // Compare elements
        for (OBVRPInteractionTypes request : requestPSUInteractionTypes) {
            boolean foundMatch = false;
            log.debug("PSUInteractionTypes do not match.");

            for (FRVRPInteractionType consent : consentPSUInteractionTypes) {
                if (request.name().equals(consent.name())) {
                    foundMatch = true;
                    log.debug("PSUInteractionTypes match.");
                    break;
                }
            }

            if (!foundMatch) {
                return false;
            }
        }
        return true;
    }

    private boolean doPeriodicLimitsAmountsMatch(List<String> requestPeriodicLimitsAmount, String consentPeriodicLimitsAmount) {

        List<String> consentPeriodicLimitsCurrencies = new ArrayList<>();
        if (consentPeriodicLimitsAmount != null && !consentPeriodicLimitsAmount.isEmpty()) {
            consentPeriodicLimitsCurrencies = Arrays.asList(consentPeriodicLimitsAmount.substring(1, consentPeriodicLimitsAmount.length() - 1).split(", "));
        }

        if (requestPeriodicLimitsAmount.size() != consentPeriodicLimitsCurrencies.size()) {
            return false;
        }

        // Compare elements
        for (int i = 0; i < requestPeriodicLimitsAmount.size(); i++) {
            if (!requestPeriodicLimitsAmount.get(i).equals(consentPeriodicLimitsCurrencies.get(i))) {
                log.debug("Amounts do not match.");
                return false;
            }
        }
        log.debug("Amounts match.");
        return true;
    }

    private boolean doPeriodicLimitsCurrenciesMatch(List<String> requestPeriodicLimitsCurrency, String consentPeriodicLimitsCurrency) {

        List<String> consentPeriodicLimitsCurrencies = new ArrayList<>();
        if (consentPeriodicLimitsCurrency != null && !consentPeriodicLimitsCurrency.isEmpty()) {
            consentPeriodicLimitsCurrencies = Arrays.asList(consentPeriodicLimitsCurrency.substring(1, consentPeriodicLimitsCurrency.length() - 1).split(", "));
        }

        if (requestPeriodicLimitsCurrency.size() != consentPeriodicLimitsCurrencies.size()) {
            return false;
        }

        // Compare elements
        for (int i = 0; i < requestPeriodicLimitsCurrency.size(); i++) {
            if (!requestPeriodicLimitsCurrency.get(i).equals(consentPeriodicLimitsCurrencies.get(i))) {
                log.debug("Currencies do not match.");
                return false;
            }
        }
        log.debug("Currencies match.");
        return true;
    }

    private boolean doPeriodicLimitsPeriodAlignmentsMatch(List<OBPeriodAlignment> requestPeriodicLimitsPeriodAlignment, String consentPeriodicLimitsPeriodAlignment) {

        List<String> consentPeriodicLimitsPeriodAlignments = new ArrayList<>();

        if (consentPeriodicLimitsPeriodAlignment != null && !consentPeriodicLimitsPeriodAlignment.isEmpty()) {

            consentPeriodicLimitsPeriodAlignments = Arrays.asList(
                    consentPeriodicLimitsPeriodAlignment
                            .substring(1, consentPeriodicLimitsPeriodAlignment.length() - 1)
                            // Split by ","
                            .split(",")
            );

            for (int i = 0; i < consentPeriodicLimitsPeriodAlignments.size(); i++) {
                consentPeriodicLimitsPeriodAlignments.set(i, consentPeriodicLimitsPeriodAlignments.get(i).trim());
            }
        }

        if (requestPeriodicLimitsPeriodAlignment.size() != consentPeriodicLimitsPeriodAlignments.size()) {
            return false;
        }

        // Compare elements
        for (int i = 0; i < requestPeriodicLimitsPeriodAlignment.size(); i++) {
            if (!Objects.equals(requestPeriodicLimitsPeriodAlignment.get(i).toString(), consentPeriodicLimitsPeriodAlignments.get(i))) {
                log.debug("PeriodAlignments do not match.");
                return false;
            }
        }

        log.debug("PeriodAlignments match.");
        return true;
    }

    private boolean doPeriodicLimitsPeriodTypesMatch(List<OBPeriodType> requestPeriodicLimitsPeriodType, String consentPeriodicLimitsPeriodType) {

        List<String> consentPeriodicLimitsPeriodTypes = new ArrayList<>();

        if (consentPeriodicLimitsPeriodType != null && !consentPeriodicLimitsPeriodType.isEmpty()) {

            consentPeriodicLimitsPeriodTypes = Arrays.asList(
                    consentPeriodicLimitsPeriodType
                            .substring(1, consentPeriodicLimitsPeriodType.length() - 1)
                            // Split by ","
                            .split(",")
            );

            for (int i = 0; i < consentPeriodicLimitsPeriodTypes.size(); i++) {
                consentPeriodicLimitsPeriodTypes.set(i, consentPeriodicLimitsPeriodTypes.get(i).trim());
            }
        }

        if (requestPeriodicLimitsPeriodType.size() != consentPeriodicLimitsPeriodTypes.size()) {
            return false;
        }

        // Compare elements
        for (int i = 0; i < requestPeriodicLimitsPeriodType.size(); i++) {
            if (!Objects.equals(requestPeriodicLimitsPeriodType.get(i).toString(), consentPeriodicLimitsPeriodTypes.get(i))) {
                log.debug("PeriodTypes do not match.");
                return false;
            }
        }

        log.debug("PeriodTypes match.");
        return true;
    }
}
