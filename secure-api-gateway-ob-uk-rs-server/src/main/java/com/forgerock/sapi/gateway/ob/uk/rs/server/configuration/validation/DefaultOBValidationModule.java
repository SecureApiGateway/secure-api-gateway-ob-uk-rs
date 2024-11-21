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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.file.PaymentFileProcessorService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation.FilePaymentFileContentValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.payment.services.validation.FilePaymentFileContentValidator.FilePaymentFileContentValidationContext;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.Currencies;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.BaseOBValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
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
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBRisk1ValidatorV4;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteDomestic2DataInitiationInstructedAmountValidator;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.consent.OBWriteDomesticConsent4Validator;

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
 * This can be enabled by setting the following property: rs.obie.validation.module: default
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
    public OBValidationService<OBWriteDomestic2ValidationContext> domesticPaymentValidator() {
        return new OBValidationService<>(new OBWriteDomestic2Validator());
    }

    @Bean
    public OBValidationService<com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteDomestic2Validator.OBWriteDomestic2ValidationContext> domesticPaymentValidatorV4() {
        return new OBValidationService<>(new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteDomestic2Validator());
    }

    @Bean
    public OBValidationService<OBWriteDomesticConsent4> domesticPaymentConsentValidator(
            BaseOBValidator<OBRisk1> riskValidator) {
        return new OBValidationService<>(new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.consent.OBWriteDomesticConsent4Validator(
                instructedAmountValidator(),
                riskValidator));
    }

    @Bean
    public OBValidationService<uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticConsent4> domesticPaymentConsentValidatorV4(
            BaseOBValidator<uk.org.openbanking.datamodel.v4.common.OBRisk1> riskValidator) {
        return new OBValidationService<>(new OBWriteDomesticConsent4Validator(instructedAmountValidatorV4(),
                                                                              riskValidator));
    }

    @Bean
    public BaseOBValidator<OBWriteDomestic2DataInitiationInstructedAmount> instructedAmountValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.OBWriteDomestic2DataInitiationInstructedAmountValidator(
                currencyCodeValidator());
    }

    @Bean
    public BaseOBValidator<uk.org.openbanking.datamodel.v4.payment.OBWriteDomestic2DataInitiationInstructedAmount> instructedAmountValidatorV4() {
        return new OBWriteDomestic2DataInitiationInstructedAmountValidator(currencyCodeValidator());
    }

    @Bean
    public OBValidationService<OBWriteDomesticScheduledConsent4> domesticScheduledPaymentConsentValidator(
            BaseOBValidator<OBRisk1> riskValidator) {
        return new OBValidationService<>(new OBWriteDomesticScheduledConsent4Validator(
                instructedAmountValidator(),
                riskValidator));
    }

    @Bean
    public OBValidationService<uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticScheduledConsent4> domesticScheduledPaymentConsentValidatorV4(
            BaseOBValidator<uk.org.openbanking.datamodel.v4.common.OBRisk1> riskValidator) {
        return new OBValidationService<>(new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.consent.OBWriteDomesticScheduledConsent4Validator(
                instructedAmountValidatorV4(),
                riskValidator));
    }

    @Bean
    public OBValidationService<OBWriteDomesticScheduled2ValidationContext> domesticScheduledPaymentValidator() {
        return new OBValidationService<>(new OBWriteDomesticScheduled2Validator());
    }

    @Bean
    public OBValidationService<com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteDomesticScheduled2Validator.OBWriteDomesticScheduled2ValidationContext> domesticScheduledPaymentValidatorV4() {
        return new OBValidationService<>(new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteDomesticScheduled2Validator());
    }

    @Bean
    public OBValidationService<OBWriteDomesticStandingOrderConsent5> domesticStandingOrderConsentValidator(
            BaseOBValidator<OBRisk1> riskValidator) {
        return new OBValidationService<>(new OBWriteDomesticStandingOrderConsent5Validator(
                currencyCodeValidator(),
                riskValidator));
    }

    @Bean
    public OBValidationService<uk.org.openbanking.datamodel.v4.payment.OBWriteDomesticStandingOrderConsent5> domesticStandingOrderConsentValidatorV4(
            BaseOBValidator<uk.org.openbanking.datamodel.v4.common.OBRisk1> riskValidator) {
        return new OBValidationService<>(new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.consent.OBWriteDomesticStandingOrderConsent5Validator(
                currencyCodeValidator(),
                riskValidator));
    }

    @Bean
    public OBValidationService<OBWriteDomesticStandingOrder3ValidationContext> domesticStandingOrderValidator() {
        return new OBValidationService<>(new OBWriteDomesticStandingOrder3Validator());
    }

    @Bean
    public OBValidationService<com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteDomesticStandingOrder3Validator.OBWriteDomesticStandingOrder3ValidationContext> domesticStandingOrderValidatorV4() {
        return new OBValidationService<>(new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteDomesticStandingOrder3Validator());
    }

    @Bean
    public OBValidationService<OBDomesticVRPConsentRequest> domesticVRPConsentValidator(
            BaseOBValidator<OBRisk1> riskValidator) {
        return new OBValidationService<>(new OBDomesticVRPConsentRequestValidator(
                riskValidator));
    }

    @Bean
    public OBValidationService<uk.org.openbanking.datamodel.v4.vrp.OBDomesticVRPConsentRequest> domesticVRPConsentValidatorV4(
            BaseOBValidator<uk.org.openbanking.datamodel.v4.common.OBRisk1> riskValidator) {
        return new OBValidationService<>(new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.consent.OBDomesticVRPConsentRequestValidator(
                riskValidator));
    }

    @Bean
    public OBValidationService<VRPFundsConfirmationValidationContext> domesticVRPFundsConfirmationValidator() {
        return new OBValidationService<>(new OBVRPFundsConfirmationRequestValidator());
    }

    @Bean
    public OBValidationService<com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.consent.OBVRPFundsConfirmationRequestValidator.VRPFundsConfirmationValidationContext> domesticVRPFundsConfirmationValidatorV4() {
        return new OBValidationService<>(new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.consent.OBVRPFundsConfirmationRequestValidator());
    }

    @Bean
    public OBValidationService<OBDomesticVRPRequestValidationContext> domesticVRPPaymentRequestValidator() {
        return new OBValidationService<>(new OBDomesticVRPRequestValidator());
    }

    @Bean
    public OBValidationService<com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBDomesticVRPRequestValidator.OBDomesticVRPRequestValidationContext> domesticVRPPaymentRequestValidatorV4() {
        return new OBValidationService<>(new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBDomesticVRPRequestValidator());
    }

    @Bean
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.CurrencyCodeValidator currencyCodeValidator() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v3.payment.CurrencyCodeValidator(Arrays.stream(
                Currencies.values()).map(Currencies::getCode).collect(Collectors.toSet()));
    }

    @Bean
    public OBValidationService<OBWriteFileConsent3> filePaymentConsentValidator(
            PaymentFileProcessorService paymentFileProcessorService) {
        return new OBValidationService<>(new OBWriteFileConsent3Validator(
                paymentFileProcessorService.getSupportedFileTypes()));
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
    public com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteInternational3DataInitiationExchangeRateInformationValidator exchangeRateInformationValidatorV4() {
        return new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteInternational3DataInitiationExchangeRateInformationValidator(currencyCodeValidator());
    }

    @Bean
    public OBValidationService<OBWriteInternationalConsent5> internationalPaymentConsentValidator(
            BaseOBValidator<OBRisk1> riskValidator) {
        return new OBValidationService<>(new OBWriteInternationalConsent5Validator(
                instructedAmountValidator(),
                currencyCodeValidator(),
                exchangeRateInformationValidator(),
                riskValidator));
    }

    @Bean
    public OBValidationService<OBWriteInternational3ValidationContext> internationalPaymentValidator() {
        return new OBValidationService<>(new OBWriteInternational3Validator());
    }

    @Bean
    public OBValidationService<OBWriteInternationalScheduledConsent5> internationalScheduledPaymentConsentValidator(
            BaseOBValidator<OBRisk1> riskValidator) {
        return new OBValidationService<>(new OBWriteInternationalScheduledConsent5Validator(
                instructedAmountValidator(),
                currencyCodeValidator(),
                exchangeRateInformationValidator(),
                riskValidator));
    }

    @Bean
    public OBValidationService<uk.org.openbanking.datamodel.v4.payment.OBWriteInternationalScheduledConsent5> internationalScheduledPaymentConsentValidatorV4(
            BaseOBValidator<uk.org.openbanking.datamodel.v4.common.OBRisk1> riskValidator) {
        return new OBValidationService<>(new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.consent.OBWriteInternationalScheduledConsent5Validator(
                instructedAmountValidatorV4(),
                currencyCodeValidator(),
                exchangeRateInformationValidatorV4(),
                riskValidator));
    }

    @Bean
    public OBValidationService<OBWriteInternationalScheduled3ValidationContext> internationalScheduledPaymentValidator() {
        return new OBValidationService<>(new OBWriteInternationalScheduled3Validator());
    }

    @Bean
    public OBValidationService<com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteInternationalScheduled3Validator.OBWriteInternationalScheduled3ValidationContext> internationalScheduledPaymentValidatorV4() {
        return new OBValidationService<>(new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.payment.OBWriteInternationalScheduled3Validator());
    }

    @Bean
    public OBValidationService<OBWriteInternationalStandingOrderConsent6> internationalStandingOrderConsentValidator(
            BaseOBValidator<OBRisk1> riskValidator) {
        return new OBValidationService<>(new OBWriteInternationalStandingOrderConsent6Validator(
                instructedAmountValidator(),
                currencyCodeValidator(),
                riskValidator));
    }

    @Bean
    public OBValidationService<OBWriteInternationalStandingOrder4ValidationContext> internationalStandingOrderValidator() {
        return new OBValidationService<>(new OBWriteInternationalStandingOrder4Validator());
    }

   @Bean
    public OBRisk1Validator paymentRiskValidator(@Value("${rs.obie.validation.config.payments.requirePaymentContextCode:false}")
                                                 boolean requirePaymentContextCode) {
        return new OBRisk1Validator(requirePaymentContextCode);
    }

    @Bean
    public OBRisk1ValidatorV4 paymentRiskValidatorV4(@Value("${rs.obie.validation.config.payments.requirePaymentContextCode:false}")
                                                 boolean requirePaymentContextCode) {
        return new OBRisk1ValidatorV4(requirePaymentContextCode);
    }

    @Bean
    public OBValidationService<FundsConfirmationValidationContext> fundsConfirmationValidator() {
        return new OBValidationService<>(new FundsConfirmationValidator());
    }

    @Bean
    public OBValidationService<com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.funds.FundsConfirmationValidator.FundsConfirmationValidationContext> fundsConfirmationValidatorV4() {
        return new OBValidationService<>(new com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.funds.FundsConfirmationValidator());
    }

}
