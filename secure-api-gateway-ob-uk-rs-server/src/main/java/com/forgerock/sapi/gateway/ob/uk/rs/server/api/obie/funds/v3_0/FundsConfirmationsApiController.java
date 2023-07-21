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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.funds.v3_0;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.funds.FRFundsConfirmationData;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.VersionPathExtractor;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link.LinksHelper;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.funds.FRFundsConfirmation;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.funds.FundsConfirmationRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.balance.FundsAvailabilityService;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.funds.v3_0.FundsConfirmationsApi;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.common.Meta;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmation1;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationDataResponse1;
import uk.org.openbanking.datamodel.fund.OBFundsConfirmationResponse1;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.common.FRAmountConverter.toOBActiveOrHistoricCurrencyAndAmount;
import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.fund.FRFundsConfirmationConverter.toFRFundsConfirmationData;

@Controller("FundsConfirmationsApiV3.0")
@Slf4j
public class FundsConfirmationsApiController implements FundsConfirmationsApi {

    private FundsConfirmationRepository fundsConfirmationRepository;
    private FundsAvailabilityService fundsAvailabilityService;

    public FundsConfirmationsApiController(FundsConfirmationRepository fundsConfirmationRepository,
                                           FundsAvailabilityService fundsAvailabilityService) {
        this.fundsConfirmationRepository = fundsConfirmationRepository;
        this.fundsAvailabilityService = fundsAvailabilityService;
    }

    @Override
    public ResponseEntity createFundsConfirmation(
            @Valid OBFundsConfirmation1 obFundsConfirmation1,
            String accountId,
            String authorization,
            DateTime xFapiCustomerLastLoggedTime,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal
    ) {
        log.debug("Create funds confirmation: {}", obFundsConfirmation1);

        String consentId = obFundsConfirmation1.getData().getConsentId();
        Optional<FRFundsConfirmation> isSubmission = fundsConfirmationRepository.findById(consentId);
        FRFundsConfirmation frFundsConfirmation = isSubmission
                .orElseGet(() ->
                        FRFundsConfirmation.builder()
                                .id(consentId)
                                .created(DateTime.now())
                                .obVersion(VersionPathExtractor.getVersionFromPath(request))
                                .build()
                );

        // Check if funds are available on the account selected in consent
        boolean areFundsAvailable = fundsAvailabilityService.isFundsAvailable(
                accountId,
                obFundsConfirmation1.getData().getInstructedAmount().getAmount());
        frFundsConfirmation.setFundsAvailable(areFundsAvailable);
        frFundsConfirmation.setFundsConfirmation(toFRFundsConfirmationData(obFundsConfirmation1));
        frFundsConfirmation = fundsConfirmationRepository.save(frFundsConfirmation);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(packageResponse(frFundsConfirmation, request));
    }

    @Override
    public ResponseEntity getFundsConfirmationId(
            String fundsConfirmationId,
            String authorization,
            DateTime xFapiCustomerLastLoggedTime,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            HttpServletRequest request,
            Principal principal
    ) {
        Optional<FRFundsConfirmation> isFundsConfirmation = fundsConfirmationRepository.findById(fundsConfirmationId);
        if (!isFundsConfirmation.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Payment submission '" + fundsConfirmationId + "' can't be found");
        }
        return ResponseEntity.ok(packageResponse(isFundsConfirmation.get(), request));
    }

    private OBFundsConfirmationResponse1 packageResponse(FRFundsConfirmation fundsConfirmation, HttpServletRequest request) {
        final FRFundsConfirmationData obFundsConfirmationData = fundsConfirmation.getFundsConfirmation();
        return new OBFundsConfirmationResponse1()
                .data(new OBFundsConfirmationDataResponse1()
                        .instructedAmount(toOBActiveOrHistoricCurrencyAndAmount(obFundsConfirmationData.getInstructedAmount()))
                        .creationDateTime(fundsConfirmation.getCreated())
                        .fundsConfirmationId(fundsConfirmation.getId())
                        .fundsAvailable(fundsConfirmation.isFundsAvailable())
                        .reference(obFundsConfirmationData.getReference())
                        .consentId(fundsConfirmation.getFundsConfirmation().getConsentId()))
                .meta(new Meta())
                .links(LinksHelper.createFundsConfirmationSelfLink(this.getClass(), fundsConfirmation.getId()));
    }

}
