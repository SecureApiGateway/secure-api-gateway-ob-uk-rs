/*
 * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
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

import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.consent.OBWriteDomesticConsent4Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.CurrencyCodeValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteDomestic2DataInitiationInstructedAmountValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.BaseOBValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.OBValidationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.file.PaymentFileProcessorService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation.FilePaymentFileContentValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation.FilePaymentFileContentValidator.FilePaymentFileContentValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.Currencies;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.funds.FundsConfirmationValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.funds.FundsConfirmationValidator.FundsConfirmationValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBDomesticVRPRequestValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBDomesticVRPRequestValidator.OBDomesticVRPRequestValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBRisk1Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteDomestic2Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteDomestic2Validator.OBWriteDomestic2ValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteDomesticScheduled2Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteDomesticScheduled2Validator.OBWriteDomesticScheduled2ValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteDomesticStandingOrder3Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteDomesticStandingOrder3Validator.OBWriteDomesticStandingOrder3ValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteFile2Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteFile2Validator.OBWriteFile2ValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteInternational3DataInitiationExchangeRateInformationValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteInternational3Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteInternational3Validator.OBWriteInternational3ValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteInternationalScheduled3Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteInternationalScheduled3Validator.OBWriteInternationalScheduled3ValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteInternationalStandingOrder4Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteInternationalStandingOrder4Validator.OBWriteInternationalStandingOrder4ValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.consent.OBDomesticVRPConsentRequestValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.consent.OBVRPFundsConfirmationRequestValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.consent.OBVRPFundsConfirmationRequestValidator.VRPFundsConfirmationValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.consent.OBWriteDomesticScheduledConsent4Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.consent.OBWriteDomesticStandingOrderConsent5Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.consent.OBWriteFileConsent3Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.consent.OBWriteInternationalConsent5Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.consent.OBWriteInternationalScheduledConsent5Validator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.consent.OBWriteInternationalStandingOrderConsent6Validator;

import org.springframework.stereotype.Component;
import uk.org.openbanking.datamodel.v3.common.OBRisk1;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomestic2DataInitiationInstructedAmount;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticConsent4;

import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticScheduledConsent4;
import uk.org.openbanking.datamodel.v3.payment.OBWriteDomesticStandingOrderConsent5;
import uk.org.openbanking.datamodel.v3.payment.OBWriteFileConsent3;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalConsent5;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalScheduledConsent5;
import uk.org.openbanking.datamodel.v3.payment.OBWriteInternationalStandingOrderConsent6;
import uk.org.openbanking.datamodel.v3.vrp.OBDomesticVRPConsentRequest;

/**
 * Spring Boot configuration for the Open Banking validation module with the default set of rules.
 * <p>
 * Creates Beans that can be used by services and controllers in this simulator for validation purposes.
 * <p>
 * This can be enabled by setting the following property:
 * rs.obie.validation.module: default
 * <p>
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
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteDomestic2ValidationContext> domesticPaymentValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteDomestic2Validator());
    }

    /*@Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteDomesticConsent4> domesticPaymentConsentValidator(@Qualifier("v3.1.10OBRisk1Validator") com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.BaseOBValidator<OBRisk1> riskValidator) {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.consent.OBWriteDomesticConsent4Validator(instructedAmountValidator(), riskValidator));
    }*/

   /* @Bean
    public OBValidationService<uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticConsent4> domesticPaymentConsentValidatorV4(@Qualifier("v4.0.0OBRisk1Validator") BaseOBValidator<uk.org.openbanking.datamodel.v4.common.OBRisk1> riskValidator) {
        return new OBValidationService<>(new OBWriteDomesticConsent4Validator(instructedAmountValidatorV4(), riskValidator));
    }*/

    /*@Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.BaseOBValidator<OBWriteDomestic2DataInitiationInstructedAmount> instructedAmountValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteDomestic2DataInitiationInstructedAmountValidator(currencyCodeValidator());
    }*/

   /* @Bean
    public BaseOBValidator<uk.org.openbanking.datamodel.v4.payment.OBWriteDomestic2DataInitiationInstructedAmount> instructedAmountValidatorV4() {
        return new OBWriteDomestic2DataInitiationInstructedAmountValidator(currencyCodeValidatorV4());
    }*/

    /*@Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteDomesticScheduledConsent4> domesticScheduledPaymentConsentValidator(@Qualifier("v3.1.10OBRisk1Validator") com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.BaseOBValidator<OBRisk1> riskValidator) {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteDomesticScheduledConsent4Validator(instructedAmountValidator(), riskValidator));
    }*/

    @Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteDomesticScheduled2ValidationContext> domesticScheduledPaymentValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteDomesticScheduled2Validator());
    }

    /*@Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteDomesticStandingOrderConsent5> domesticStandingOrderConsentValidator(@Qualifier("v3.1.10OBRisk1Validator") com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.BaseOBValidator<OBRisk1> riskValidator) {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteDomesticStandingOrderConsent5Validator(currencyCodeValidator(), riskValidator));
    }*/

    @Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteDomesticStandingOrder3ValidationContext> domesticStandingOrderValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteDomesticStandingOrder3Validator());
    }

    @Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBDomesticVRPConsentRequest> domesticVRPConsentValidator(@Qualifier("v3.1.10OBRisk1Validator") com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.BaseOBValidator<OBRisk1> riskValidator) {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBDomesticVRPConsentRequestValidator(riskValidator));
    }

    @Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<VRPFundsConfirmationValidationContext> domesticVRPFundsConfirmationValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBVRPFundsConfirmationRequestValidator());
    }

    @Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBDomesticVRPRequestValidationContext> domesticVRPPaymentRequestValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBDomesticVRPRequestValidator());
    }

    /*@Bean
    public BaseOBValidator<String> currencyCodeValidatorV4() {
        return new CurrencyCodeValidator(Arrays.stream(Currencies.values()).map(Currencies::getCode).collect(Collectors.toSet()));
    }*/

    /*@Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.BaseOBValidator<String> currencyCodeValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.CurrencyCodeValidator(Arrays.stream(Currencies.values()).map(Currencies::getCode).collect(Collectors.toSet()));
    }*/

    @Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteFileConsent3> filePaymentConsentValidator(PaymentFileProcessorService paymentFileProcessorService) {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteFileConsent3Validator(paymentFileProcessorService.getSupportedFileTypes()));
    }

    @Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<FilePaymentFileContentValidationContext> filePaymentFileContentValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new FilePaymentFileContentValidator());
    }

    @Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteFile2ValidationContext> filePaymentRequestValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteFile2Validator());
    }

   /* @Bean
    public OBWriteInternational3DataInitiationExchangeRateInformationValidator exchangeRateInformationValidator2() {
        return new OBWriteInternational3DataInitiationExchangeRateInformationValidator(currencyCodeValidator());
    }*/

    /*@Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteInternationalConsent5> internationalPaymentConsentValidator(@Qualifier("v3.1.10OBRisk1Validator") com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.BaseOBValidator<OBRisk1> riskValidator) {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteInternationalConsent5Validator(instructedAmountValidator(),
                currencyCodeValidator(), exchangeRateInformationValidator2(), riskValidator));
    }*/

    @Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteInternational3ValidationContext> internationalPaymentValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteInternational3Validator());
    }

    /*@Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteInternationalScheduledConsent5> internationalScheduledPaymentConsentValidator(@Qualifier("v3.1.10OBRisk1Validator") com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.BaseOBValidator<OBRisk1> riskValidator) {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteInternationalScheduledConsent5Validator(instructedAmountValidator(),
                currencyCodeValidator(), exchangeRateInformationValidator2(), riskValidator));
    }*/

    @Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteInternationalScheduled3ValidationContext> internationalScheduledPaymentValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteInternationalScheduled3Validator());
    }

    /*@Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteInternationalStandingOrderConsent6> internationalStandingOrderConsentValidator(@Qualifier("v3.1.10OBRisk1Validator")
            com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.BaseOBValidator<OBRisk1> riskValidator) {

        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteInternationalStandingOrderConsent6Validator(instructedAmountValidator(),
                currencyCodeValidator(), riskValidator));
    }*/

    @Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteInternationalStandingOrder4ValidationContext> internationalStandingOrderValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteInternationalStandingOrder4Validator());
    }

    /*@Bean
    public OBRisk1Validator paymentRiskValidator(@Value("${rs.obie.validation.config.payments.requirePaymentContextCode:false}")
                                                 boolean requirePaymentContextCode) {
        return new OBRisk1Validator(requirePaymentContextCode);
    }*/

    @Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<FundsConfirmationValidationContext> fundsConfirmationValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new FundsConfirmationValidator());
    }
}
