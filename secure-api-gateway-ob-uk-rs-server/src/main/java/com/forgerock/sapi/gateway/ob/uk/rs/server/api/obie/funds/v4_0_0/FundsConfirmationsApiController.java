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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.funds.v4_0_0;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.funds.FRFundsConfirmationData;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.funds.v4_0_0.FundsConfirmationsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.VersionPathExtractor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.v4.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.balance.FundsAvailabilityService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.OBValidationService;
import com.forgerock.sapi.gateway.ob.uk.rs.validation.obie.v4.funds.FundsConfirmationValidator;
import com.forgerock.sapi.gateway.rcs.consent.store.client.funds.FundsConfirmationConsentStoreClient;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.funds.v3_1_10.FundsConfirmationConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRAccount;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.funds.FRFundsConfirmation;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.accounts.FRAccountRepository;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.funds.FundsConfirmationRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.v4.common.Meta;
import uk.org.openbanking.datamodel.v4.fund.OBFundsConfirmation1;
import uk.org.openbanking.datamodel.v4.fund.OBFundsConfirmationResponse1;
import uk.org.openbanking.datamodel.v4.fund.OBFundsConfirmationResponse1Data;

import java.util.Date;
import java.util.Optional;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.common.FRAmountConverter.toOBFundsConfirmation1DataInstructedAmount;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.funds.FRFundsConfirmationConverter.toFRFundsConfirmationData;

@Controller("FundsConfirmationsApiV4.0.0")
public class FundsConfirmationsApiController implements FundsConfirmationsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final FundsConfirmationRepository fundsConfirmationRepository;
    private final FundsAvailabilityService fundsAvailabilityService;
    private final FRAccountRepository accountRepository;
    private final OBValidationService<FundsConfirmationValidator.FundsConfirmationValidationContext> fundsConfirmationValidator;
    private final FundsConfirmationConsentStoreClient consentStoreClient;

    public FundsConfirmationsApiController(
            FundsConfirmationRepository fundsConfirmationRepository,
            FundsAvailabilityService fundsAvailabilityService,
            FRAccountRepository accountRepository,
            OBValidationService<FundsConfirmationValidator.FundsConfirmationValidationContext> fundsConfirmationValidator,
            @Qualifier("v4.0.0RestFundsConfirmationConsentStoreClient") FundsConfirmationConsentStoreClient consentStoreClient
    ) {
        this.fundsConfirmationRepository = fundsConfirmationRepository;
        this.fundsAvailabilityService = fundsAvailabilityService;
        this.accountRepository = accountRepository;
        this.fundsConfirmationValidator = fundsConfirmationValidator;
        this.consentStoreClient = consentStoreClient;
    }


    @Override
    public ResponseEntity<OBFundsConfirmationResponse1> createFundsConfirmations(String authorization, OBFundsConfirmation1 obFundsConfirmation1, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId, HttpServletRequest request) throws OBErrorResponseException {
        logger.debug("Create funds confirmation: {}", obFundsConfirmation1);

        String consentId = obFundsConfirmation1.getData().getConsentId();
        logger.debug("Attempting to get consent: {}, clientId: {}", consentId, apiClientId);
        final FundsConfirmationConsent consent = consentStoreClient.getConsent(consentId, apiClientId);
        logger.debug("Got consent from store: {}", consent);

        Optional<FRFundsConfirmation> isSubmission = fundsConfirmationRepository.findById(consentId);
        FRFundsConfirmation frFundsConfirmation = isSubmission
                .orElseGet(() ->
                        FRFundsConfirmation.builder()
                                .id(consentId)
                                .created(new Date())
                                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                                .build()
                );

        // validate funds confirmation
        logger.debug("Validating funds confirmation");
        FRAccount account = accountRepository.byAccountId(consent.getAuthorisedDebtorAccountId());
        if (account == null) {
            logger.warn("Funds confirmation verification failed, Account with ID {} not found", consent.getAuthorisedDebtorAccountId());
            throw new OBErrorResponseException(
                    OBRIErrorType.FUNDS_CONFIRMATION_DEBTOR_ACCOUNT_NOT_FOUND.getHttpStatus(),
                    OBRIErrorResponseCategory.SERVER_INTERNAL_ERROR,
                    OBRIErrorType.FUNDS_CONFIRMATION_DEBTOR_ACCOUNT_NOT_FOUND.toOBError1(consent.getAuthorisedDebtorAccountId())
            );
        }
        final FundsConfirmationValidator.FundsConfirmationValidationContext validationContext = new FundsConfirmationValidator.FundsConfirmationValidationContext(
                obFundsConfirmation1,
                consent.getRequestObj().getData().getExpirationDateTime(),
                consent.getStatus(),
                account.getAccount().getCurrency()
        );

        fundsConfirmationValidator.validate(validationContext);
        logger.debug("Funds Confirmation validation successful");

        // Check if funds are available on the account selected in consent
        boolean areFundsAvailable = fundsAvailabilityService.isFundsAvailable(
                consent.getAuthorisedDebtorAccountId(),
                obFundsConfirmation1.getData().getInstructedAmount().getAmount());
        frFundsConfirmation.setFundsAvailable(areFundsAvailable);
        frFundsConfirmation.setFundsConfirmation(toFRFundsConfirmationData(obFundsConfirmation1));
        frFundsConfirmation = fundsConfirmationRepository.save(frFundsConfirmation);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(packageResponse(frFundsConfirmation, request));
    }

    private OBFundsConfirmationResponse1 packageResponse(FRFundsConfirmation fundsConfirmation, HttpServletRequest request) {
        final FRFundsConfirmationData obFundsConfirmationData = fundsConfirmation.getFundsConfirmation();
        return new OBFundsConfirmationResponse1()
                .data(new OBFundsConfirmationResponse1Data()
                        .instructedAmount(toOBFundsConfirmation1DataInstructedAmount(obFundsConfirmationData.getInstructedAmount()))
                        .creationDateTime(new DateTime(fundsConfirmation.getCreated()))
                        .fundsConfirmationId(fundsConfirmation.getId())
                        .fundsAvailable(fundsConfirmation.isFundsAvailable())
                        .reference(obFundsConfirmationData.getReference())
                        .consentId(fundsConfirmation.getFundsConfirmation().getConsentId()))
                .meta(new Meta())
                .links(LinksHelper.createFundsConfirmationSelfLink(this.getClass(), fundsConfirmation.getId()));
    }
}
