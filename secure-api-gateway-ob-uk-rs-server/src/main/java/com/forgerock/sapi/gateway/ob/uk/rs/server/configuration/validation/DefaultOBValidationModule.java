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
package com.forgerock.sapi.gateway.ob.uk.rs.server.configuration.validation;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation.FilePaymentFileContentValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation.FilePaymentFileContentValidator.FilePaymentFileContentValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.Currencies;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.CurrencyCodeValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBDomesticVRPRequestValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBDomesticVRPRequestValidator.OBDomesticVRPRequestValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteDomestic2DataInitiationInstructedAmountValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteDomestic2Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteDomestic2Validator.OBWriteDomestic2ValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteDomesticScheduled2Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteDomesticScheduled2Validator.OBWriteDomesticScheduled2ValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteDomesticStandingOrder3Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteDomesticStandingOrder3Validator.OBWriteDomesticStandingOrder3ValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteFile2Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteFile2Validator.OBWriteFile2ValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteInternational3DataInitiationExchangeRateInformationValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteInternational3Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteInternational3Validator.OBWriteInternational3ValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteInternationalScheduled3Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteInternationalScheduled3Validator.OBWriteInternationalScheduled3ValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteInternationalStandingOrder4Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.OBWriteInternationalStandingOrder4Validator.OBWriteInternationalStandingOrder4ValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.consent.OBDomesticVRPConsentRequestValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.consent.OBWriteDomesticConsent4Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.consent.OBWriteDomesticScheduledConsent4Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.consent.OBWriteDomesticStandingOrderConsent5Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.consent.OBWriteFileConsent3Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.consent.OBWriteInternationalConsent5Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.consent.OBWriteInternationalScheduledConsent5Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.payment.consent.OBWriteInternationalStandingOrderConsent6Validator;

import uk.org.openbanking.datamodel.payment.OBWriteDomestic2DataInitiationInstructedAmount;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsent4;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticScheduledConsent4;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticStandingOrderConsent5;
import uk.org.openbanking.datamodel.payment.OBWriteFileConsent3;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalConsent5;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalScheduledConsent5;
import uk.org.openbanking.datamodel.payment.OBWriteInternationalStandingOrderConsent6;
import uk.org.openbanking.datamodel.vrp.OBDomesticVRPConsentRequest;

/**
 * Spring Boot configuration for the Open Banking validation module with the default set of rules.
 *
 * Creates Beans that can be used by services and controllers in this simulator for validation purposes.
 *
 * This can be enabled by setting the following property:
 * rs.obie.validation.module: default
 *
 * Different validation rules may be implemented by creating a new module that creates the same set of beans, such a
 * module should include the @ConditionalOnProperty annotation as below but with a different havingValue.
 */
@Configuration
@ConditionalOnProperty(prefix = "rs.obie.validation", name = "module", havingValue = "default")
public class DefaultOBValidationModule {

    /**
     * Bean to validate OBWriteDomestic2 objects (Domestic Payment Requests)
     */
    @Bean
    public OBValidationService<OBWriteDomestic2ValidationContext> domesticPaymentValidator() {
        return new OBValidationService<>(new OBWriteDomestic2Validator());
    }

    @Bean
    public OBValidationService<OBWriteDomesticConsent4> domesticPaymentConsentValidator() {
        return new OBValidationService<>(new OBWriteDomesticConsent4Validator(instructedAmountValidator()));
    }

    @Bean
    public BaseOBValidator<OBWriteDomestic2DataInitiationInstructedAmount> instructedAmountValidator() {
        return new OBWriteDomestic2DataInitiationInstructedAmountValidator(currencyCodeValidator());
    }

    @Bean
    public OBValidationService<OBWriteDomesticScheduledConsent4> domesticScheduledPaymentConsentValidator() {
        return new OBValidationService<>(new OBWriteDomesticScheduledConsent4Validator(instructedAmountValidator()));
    }

    @Bean
    public OBValidationService<OBWriteDomesticScheduled2ValidationContext> domesticScheduledPaymentValidator() {
        return new OBValidationService<>(new OBWriteDomesticScheduled2Validator());
    }

    @Bean
    public OBValidationService<OBWriteDomesticStandingOrderConsent5> domesticStandingOrderConsentValidator() {
        return new OBValidationService<>(new OBWriteDomesticStandingOrderConsent5Validator(currencyCodeValidator()));
    }

    @Bean
    public OBValidationService<OBWriteDomesticStandingOrder3ValidationContext> domesticStandingOrderValidator() {
        return new OBValidationService<>(new OBWriteDomesticStandingOrder3Validator());
    }

    @Bean
    public OBValidationService<OBDomesticVRPConsentRequest> domesticVRPConsentValidator() {
        return new OBValidationService<>(new OBDomesticVRPConsentRequestValidator());
    }

    @Bean
    public OBValidationService<OBDomesticVRPRequestValidationContext> domesticVRPPaymentRequestValidator() {
        return new OBValidationService<>(new OBDomesticVRPRequestValidator());
    }

    @Bean
    public BaseOBValidator<String> currencyCodeValidator() {
        return new CurrencyCodeValidator(Arrays.stream(Currencies.values()).map(Currencies::getCode).collect(Collectors.toSet()));
    }

    @Bean
    public OBValidationService<OBWriteFileConsent3> filePaymentConsentValidator() {
        return new OBValidationService<>(new OBWriteFileConsent3Validator());
    }

    @Bean
    public OBValidationService<FilePaymentFileContentValidationContext> filePaymentFileContentValidator() {
        return new OBValidationService<>(new FilePaymentFileContentValidator());
    }

    @Bean
    public OBValidationService<OBWriteFile2ValidationContext> filePaymentRequestValidator() {
        return new OBValidationService<>(new OBWriteFile2Validator());
    }

    @Bean
    public OBWriteInternational3DataInitiationExchangeRateInformationValidator exchangeRateInformationValidator() {
        return new OBWriteInternational3DataInitiationExchangeRateInformationValidator(currencyCodeValidator());
    }

    @Bean
    public OBValidationService<OBWriteInternationalConsent5> internationalPaymentConsentValidator() {
        return new OBValidationService<>(new OBWriteInternationalConsent5Validator(instructedAmountValidator(),
                currencyCodeValidator(), exchangeRateInformationValidator()));
    }

    @Bean
    public OBValidationService<OBWriteInternational3ValidationContext> internationalPaymentValidator() {
        return new OBValidationService<>(new OBWriteInternational3Validator());
    }

    @Bean
    public OBValidationService<OBWriteInternationalScheduledConsent5> internationalScheduledPaymentConsentValidator() {
        return new OBValidationService<>(new OBWriteInternationalScheduledConsent5Validator(instructedAmountValidator(),
                currencyCodeValidator(), exchangeRateInformationValidator()));
    }

    @Bean
    public OBValidationService<OBWriteInternationalScheduled3ValidationContext> internationalScheduledPaymentValidator() {
        return new OBValidationService<>(new OBWriteInternationalScheduled3Validator());
    }

    @Bean
    public OBValidationService<OBWriteInternationalStandingOrderConsent6> internationalStandingOrderConsentValidator() {
        return new OBValidationService<>(new OBWriteInternationalStandingOrderConsent6Validator(instructedAmountValidator(),
                                                                                                currencyCodeValidator()));
    }

    @Bean
    public OBValidationService<OBWriteInternationalStandingOrder4ValidationContext> internationalStandingOrderValidator() {
        return new OBValidationService<>(new OBWriteInternationalStandingOrder4Validator());
    }
}
