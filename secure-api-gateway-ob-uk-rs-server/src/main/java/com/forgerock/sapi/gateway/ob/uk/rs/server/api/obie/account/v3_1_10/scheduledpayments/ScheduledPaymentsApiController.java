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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v3_1_10.scheduledpayments;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v3.payment.FRScheduledPaymentConverter.toOBScheduledPayment3;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.util.List;
import java.util.stream.Collectors;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.AccountDataInternalIdFilter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRScheduledPayment;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.scheduledpayments.FRScheduledPaymentRepository;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v3_1_10.scheduledpayments.ScheduledPaymentsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.org.openbanking.datamodel.v3.account.OBReadScheduledPayment3;
import uk.org.openbanking.datamodel.v3.account.OBReadScheduledPayment3Data;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller("ScheduledPaymentsApiV3.1.10")
@Slf4j
public class ScheduledPaymentsApiController implements ScheduledPaymentsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int pageLimitSchedulePayments;

    private final FRScheduledPaymentRepository frScheduledPaymentRepository;

    private final AccountDataInternalIdFilter accountDataInternalIdFilter;

    private final AccountResourceAccessService accountResourceAccessService;

    public ScheduledPaymentsApiController(@Value("${rs.page.default.scheduled-payments.size:10}") int pageLimitSchedulePayments,
            FRScheduledPaymentRepository frScheduledPaymentRepository,
            AccountDataInternalIdFilter accountDataInternalIdFilter, @Qualifier("v3.1.10DefaultAccountResourceAccessService") AccountResourceAccessService accountResourceAccessService) {
        this.pageLimitSchedulePayments = pageLimitSchedulePayments;
        this.frScheduledPaymentRepository = frScheduledPaymentRepository;
        this.accountDataInternalIdFilter = accountDataInternalIdFilter;
        this.accountResourceAccessService = accountResourceAccessService;
    }

    @Override
    public ResponseEntity<OBReadScheduledPayment3> getAccountScheduledPayments(String accountId,
            int page,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String consentId,
            String apiClientId) throws OBErrorException {
        logger.info("getAccountScheduledPayments for accountId: {}, consentId: {}, apiClientId: {}", accountId, consentId, apiClientId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId, accountId);
        checkPermissions(consent);
        Page<FRScheduledPayment> scheduledPayments = frScheduledPaymentRepository.byAccountIdWithPermissions(accountId, consent.getRequestObj().getData().getPermissions(),
                PageRequest.of(page, pageLimitSchedulePayments));
        return packageResponse(page, buildGetAccountScheduledPaymentsUri(accountId), scheduledPayments);
    }

    @Override
    public ResponseEntity<OBReadScheduledPayment3> getScheduledPayments(int page,
            String authorization,
            String xFapiAuthDate,
            String xFapiCustomerIpAddress,
            String xFapiInteractionId,
            String xCustomerUserAgent,
            String consentId,
            String apiClientId) throws OBErrorException {
        logger.info("getScheduledPayments for consentId: {}, apiClientIdd: {}", consentId, apiClientId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId);
        checkPermissions(consent);
        Page<FRScheduledPayment> scheduledPayments = frScheduledPaymentRepository.byAccountIdInWithPermissions(consent.getAuthorisedAccountIds(), consent.getRequestObj().getData().getPermissions(),
                PageRequest.of(page, pageLimitSchedulePayments));
        return packageResponse(page, buildGetScheduledPaymentsUri(), scheduledPayments);
    }

    private ResponseEntity<OBReadScheduledPayment3> packageResponse(int page, String httpUrl, Page<FRScheduledPayment> scheduledPayments) {
        int totalPages = scheduledPayments.getTotalPages();

        return ResponseEntity.ok(new OBReadScheduledPayment3().data(new OBReadScheduledPayment3Data().scheduledPayment(
                        scheduledPayments.getContent()
                                .stream()
                                .map(sp -> toOBScheduledPayment3(sp.getScheduledPayment()))
                                .map(sp -> accountDataInternalIdFilter.apply(sp))
                                .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }

    private String buildGetAccountScheduledPaymentsUri(String accountId) {
        return linkTo(getClass()).slash("accounts").slash(accountId).slash("scheduled-payments").toString();
    }

    private String buildGetScheduledPaymentsUri() {
        return linkTo(getClass()).slash("scheduled-payments").toString();
    }

    private static void checkPermissions(AccountAccessConsent consent) throws OBErrorException {
        final List<FRExternalPermissionsCode> permissions = consent.getRequestObj().getData().getPermissions();
        if (!permissions.contains(FRExternalPermissionsCode.READSCHEDULEDPAYMENTSBASIC) && !permissions.contains(FRExternalPermissionsCode.READSCHEDULEDPAYMENTSDETAIL)) {
            throw new OBErrorException(OBRIErrorType.PERMISSIONS_INVALID, FRExternalPermissionsCode.READSCHEDULEDPAYMENTSBASIC + " or " + FRExternalPermissionsCode.READSCHEDULEDPAYMENTSDETAIL);
        }
    }


}
