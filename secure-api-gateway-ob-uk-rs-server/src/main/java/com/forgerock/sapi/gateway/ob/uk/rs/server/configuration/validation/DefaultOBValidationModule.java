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

import uk.org.openbanking.datamodel.v3.common.OBRisk1;
import uk.org.openbanking.datamodel.v3.payment.*;

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
    @Bean("v3.1.10domesticPaymentValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteDomestic2ValidationContext> domesticPaymentValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteDomestic2Validator());
    }

    @Bean("v3.1.10domesticPaymentConsentValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteDomesticConsent4> domesticPaymentConsentValidator(@Qualifier("v3.1.10OBRisk1Validator") com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.BaseOBValidator<OBRisk1> riskValidator) {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.consent.OBWriteDomesticConsent4Validator(instructedAmountValidator(), riskValidator));
    }

    @Bean("v4.0.0domesticPaymentConsentValidator")
    public OBValidationService<uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticConsent4> domesticPaymentConsentValidatorV4(@Qualifier("v4.0.0OBRisk1Validator") BaseOBValidator<uk.org.openbanking.datamodel.v4.common.OBRisk1> riskValidator) {
        return new OBValidationService<>(new OBWriteDomesticConsent4Validator(instructedAmountValidatorV4(), riskValidator));
    }

    @Bean("v3.1.10instructedAmountValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.BaseOBValidator<OBWriteDomestic2DataInitiationInstructedAmount> instructedAmountValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteDomestic2DataInitiationInstructedAmountValidator(currencyCodeValidator());
    }

    @Bean("v4.0.0instructedAmountValidator")
    public BaseOBValidator<uk.org.openbanking.datamodel.v4.payment.OBWriteDomestic2DataInitiationInstructedAmount> instructedAmountValidatorV4() {
        return new OBWriteDomestic2DataInitiationInstructedAmountValidator(currencyCodeValidatorV4());
    }

    @Bean("v3.1.10domesticScheduledPaymentConsentValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteDomesticScheduledConsent4> domesticScheduledPaymentConsentValidator(@Qualifier("v3.1.10OBRisk1Validator") com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.BaseOBValidator<OBRisk1> riskValidator) {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteDomesticScheduledConsent4Validator(instructedAmountValidator(), riskValidator));
    }

    @Bean("v3.1.10domesticScheduledPaymentValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteDomesticScheduled2ValidationContext> domesticScheduledPaymentValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteDomesticScheduled2Validator());
    }

    @Bean("v3.1.10domesticStandingOrderConsentValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteDomesticStandingOrderConsent5> domesticStandingOrderConsentValidator(@Qualifier("v3.1.10OBRisk1Validator") com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.BaseOBValidator<OBRisk1> riskValidator) {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteDomesticStandingOrderConsent5Validator(currencyCodeValidator(), riskValidator));
    }

    @Bean("v3.1.10domesticStandingOrderValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteDomesticStandingOrder3ValidationContext> domesticStandingOrderValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteDomesticStandingOrder3Validator());
    }

    @Bean("v3.1.10domesticVRPConsentValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBDomesticVRPConsentRequest> domesticVRPConsentValidator(@Qualifier("v3.1.10OBRisk1Validator") com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.BaseOBValidator<OBRisk1> riskValidator) {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBDomesticVRPConsentRequestValidator(riskValidator));
    }

    @Bean("v3.1.10domesticVRPFundsConfirmationValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<VRPFundsConfirmationValidationContext> domesticVRPFundsConfirmationValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBVRPFundsConfirmationRequestValidator());
    }

    @Bean("v3.1.10domesticVRPPaymentRequestValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBDomesticVRPRequestValidationContext> domesticVRPPaymentRequestValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBDomesticVRPRequestValidator());
    }

    @Bean("v4.0.0currencyCodeValidator")
    public BaseOBValidator<String> currencyCodeValidatorV4() {
        return new CurrencyCodeValidator(Arrays.stream(Currencies.values()).map(Currencies::getCode).collect(Collectors.toSet()));
    }

    @Bean("v3.1.10currencyCodeValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.CurrencyCodeValidator currencyCodeValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.CurrencyCodeValidator(Arrays.stream(Currencies.values()).map(Currencies::getCode).collect(Collectors.toSet()));
    }

    @Bean("v3.1.10filePaymentConsentValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteFileConsent3> filePaymentConsentValidator(PaymentFileProcessorService paymentFileProcessorService) {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteFileConsent3Validator(paymentFileProcessorService.getSupportedFileTypes()));
    }

    @Bean("v3.1.10filePaymentFileContentValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<FilePaymentFileContentValidationContext> filePaymentFileContentValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new FilePaymentFileContentValidator());
    }

    @Bean("v3.1.10filePaymentRequestValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteFile2ValidationContext> filePaymentRequestValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteFile2Validator());
    }

    @Bean("v3.1.10OexchangeRateInformationValidator")
    public OBWriteInternational3DataInitiationExchangeRateInformationValidator exchangeRateInformationValidatorV3(@Qualifier("v3.1.10currencyCodeValidator") com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.CurrencyCodeValidator currencyCodeValidator) {
        return new OBWriteInternational3DataInitiationExchangeRateInformationValidator(currencyCodeValidator);
    }

    @Bean("exchangeRateInformationValidator2")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteInternational3DataInitiationExchangeRateInformation> exchangeRateService(@Qualifier("v3.1.10exchangeRateInformationValidator") OBWriteInternational3DataInitiationExchangeRateInformationValidator exchRateValidator) {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(exchRateValidator);
    }

    @Bean("v3.1.10internationalPaymentConsentValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteInternationalConsent5> internationalPaymentConsentValidator(@Qualifier("v3.1.10OBRisk1Validator") com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.BaseOBValidator<OBRisk1> riskValidator,
                                                                                                                                                         @Qualifier("v3.1.10OexchangeRateInformationValidator") OBWriteInternational3DataInitiationExchangeRateInformationValidator exchRateValidator) {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteInternationalConsent5Validator(instructedAmountValidator(),
                currencyCodeValidator(), exchRateValidator, riskValidator));
    }

    @Bean("v3.1.10internationalPaymentValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteInternational3ValidationContext> internationalPaymentValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteInternational3Validator());
    }

    @Bean("v3.1.10internationalScheduledPaymentConsentValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteInternationalScheduledConsent5> internationalScheduledPaymentConsentValidator(@Qualifier("v3.1.10OBRisk1Validator") com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.BaseOBValidator<OBRisk1> riskValidator,
                                                                                                                                                                           @Qualifier("v3.1.10OexchangeRateInformationValidator") OBWriteInternational3DataInitiationExchangeRateInformationValidator exchRateValidator) {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteInternationalScheduledConsent5Validator(instructedAmountValidator(),
                currencyCodeValidator(), exchRateValidator, riskValidator));
    }

    @Bean("v3.1.10internationalScheduledPaymentValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteInternationalScheduled3ValidationContext> internationalScheduledPaymentValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteInternationalScheduled3Validator());
    }

    @Bean("v3.1.10internationalStandingOrderConsentValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteInternationalStandingOrderConsent6> internationalStandingOrderConsentValidator(
            @Qualifier("v3.1.10OBRisk1Validator") com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.BaseOBValidator<OBRisk1> riskValidator) {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteInternationalStandingOrderConsent6Validator(instructedAmountValidator(),
                currencyCodeValidator(), riskValidator));
    }

    @Bean("v3.1.10internationalStandingOrderValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<OBWriteInternationalStandingOrder4ValidationContext> internationalStandingOrderValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new OBWriteInternationalStandingOrder4Validator());
    }

   /*@Bean
    public OBRisk1Validator paymentRiskValidator(@Value("${rs.obie.validation.config.payments.requirePaymentContextCode:false}")
                                                 boolean requirePaymentContextCode) {
        return new OBRisk1Validator(requirePaymentContextCode);
    }*/

    @Bean("v3.1.10fundsConfirmationValidator")
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<FundsConfirmationValidationContext> fundsConfirmationValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.OBValidationService<>(new FundsConfirmationValidator());
    }

}
