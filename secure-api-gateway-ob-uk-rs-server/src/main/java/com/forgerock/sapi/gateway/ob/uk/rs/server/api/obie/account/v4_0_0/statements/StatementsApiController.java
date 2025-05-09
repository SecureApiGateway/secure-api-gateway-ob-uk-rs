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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.account.v4_0_0.statements;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalPermissionsCode;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.account.FRTransactionConverter;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorResponseCategory;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBRIErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.account.v4_0_0.statements.StatementsApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.v4.common.util.AccountDataInternalIdFilter;
import com.forgerock.sapi.gateway.ob.uk.rs.server.v4.common.util.PaginationUtil;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.account.consent.AccountResourceAccessService;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.statement.StatementPDFService;
import com.forgerock.sapi.gateway.rcs.consent.store.datamodel.account.v3_1_10.AccountAccessConsent;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRStatement;
import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account.FRTransaction;
import com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.statements.FRStatementRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;

import uk.org.openbanking.datamodel.v4.account.OBReadDataTransaction6;
import uk.org.openbanking.datamodel.v4.account.OBTransaction6;
import uk.org.openbanking.datamodel.v4.account.OBReadDataStatement2;
import uk.org.openbanking.datamodel.v4.account.OBReadStatement2;
import uk.org.openbanking.datamodel.v4.account.OBReadTransaction6;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.account.FRStatementConverter.toOBStatement2;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller("StatementsApiV4.0.0")
public class StatementsApiController implements StatementsApi {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final int pageLimitStatements;

    private final FRStatementRepository frStatementRepository;

    private final com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.transactions.FRTransactionRepository FRTransactionRepository;

    private final AccountDataInternalIdFilter accountDataInternalIdFilter;

    private final StatementPDFService statementPDFService;

    private final AccountResourceAccessService accountResourceAccessService;

    private final Set<FRExternalPermissionsCode> nonFilePermissions = Set.of(FRExternalPermissionsCode.READSTATEMENTSBASIC, FRExternalPermissionsCode.READSTATEMENTSDETAIL);

    private final Set<FRExternalPermissionsCode> filePermission = Set.of(FRExternalPermissionsCode.READSTATEMENTSDETAIL);

    public StatementsApiController(@Value("${rs.page.default.statement.size:10}") int pageLimitStatements,
                                   FRStatementRepository frStatementRepository,
                                   final com.forgerock.sapi.gateway.rs.resource.store.repo.mongo.accounts.transactions.FRTransactionRepository repository,
                                   AccountDataInternalIdFilter accountDataInternalIdFilter,
                                   StatementPDFService statementPDFService, @Qualifier("v4.0.0DefaultAccountResourceAccessService") AccountResourceAccessService accountResourceAccessService) {
        this.pageLimitStatements = pageLimitStatements;
        this.frStatementRepository = frStatementRepository;
        this.FRTransactionRepository = repository;
        this.accountDataInternalIdFilter = accountDataInternalIdFilter;
        this.statementPDFService = statementPDFService;
        this.accountResourceAccessService = accountResourceAccessService;
    }

    @Override
    public ResponseEntity<OBReadStatement2> getStatements(String authorization, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, LocalDateTime fromStatementDateTime, LocalDateTime toStatementDateTime, String xCustomerUserAgent, String apiClientId, String consentId, int page) throws OBErrorException {
        logger.info("getStatements for consentId: {}, apiClientId: {}", consentId, apiClientId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId);
        checkPermissions(consent, nonFilePermissions);

        Page<FRStatement> statements = frStatementRepository.findByAccountIdIn(consent.getAuthorisedAccountIds(), PageRequest.of(page, pageLimitStatements, Sort.Direction.ASC, "startDateTime"));
        int totalPages = statements.getTotalPages();
        return packageResponse(page, buildGetStatementsUri(), statements.getContent(), totalPages);
    }

    @Override
    public ResponseEntity<OBReadStatement2> getAccountsAccountIdStatements(String accountId, String authorization, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, LocalDateTime fromStatementDateTime, LocalDateTime toStatementDateTime, String apiClientId, String consentId, int page) throws OBErrorException {
        logger.info("getAccountStatements for accountId: {}, consentId: {}, apiClientId: {}", accountId, consentId, apiClientId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId, accountId);
        checkPermissions(consent, nonFilePermissions);

        Page<FRStatement> statements = frStatementRepository.byAccountIdWithPermissions(accountId,
                fromStatementDateTime == null ? null : new Date(fromStatementDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()),
                toStatementDateTime == null ? null : new Date(toStatementDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()),
                consent.getRequestObj().getData().getPermissions(),
                PageRequest.of(page, pageLimitStatements, Sort.Direction.ASC, "startDateTime"));

        int totalPages = statements.getTotalPages();
        return packageResponse(page, buildGetAccountStatementsUri(accountId), statements.getContent(), totalPages);
    }

    @Override
    public ResponseEntity<OBReadStatement2> getAccountsAccountIdStatementsStatementId(String statementId, String accountId, String authorization, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId, String consentId, int page) throws OBErrorException {
        logger.info("getAccountStatement for accountId: {}, statementId: {}, consentId: {}, apiClientId: {}", accountId, statementId, consentId, apiClientId);
        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId, accountId);
        checkPermissions(consent, nonFilePermissions);

        List<FRStatement> statements = frStatementRepository.byAccountIdAndStatementIdWithPermissions(accountId, statementId, consent.getRequestObj().getData().getPermissions());
        int totalPages = 1;
        return packageResponse(page, buildGetAccountStatementUri(accountId, statementId), statements, totalPages);
    }

    @Override
    public ResponseEntity<Object> getAccountsAccountIdStatementsStatementIdFile(String statementId, String accountId, String authorization, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId, String consentId, String accept, int page) throws OBErrorResponseException, OBErrorException {
        logger.info("Received a statement file download request for account: {} (Accept: {}). Interaction Id: {}", accountId, accept, xFapiInteractionId);

        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId, accountId);
        checkPermissions(consent, filePermission);
        /*
         * Issue related: https://github.com/SecureBankingAccessToolkit/securebanking-openbanking-uk-functional-tests/issues/17
         * The RS endpoint '/statements/{statementId}/file' has been implemented to return a fixed PDF file for all statement file requests.
         * A PDF file will only be returned if the "Accept: application/pdf" header is supplied in the request
         * The pdf resource lives in 'resources/accounts/statements/${profile}/statement.pdf (profiles: default and docker)
         */
        if (!accept.contains(MediaType.APPLICATION_PDF_VALUE)) {
            // No other file type is implemented apart from PDF
            throw new OBErrorResponseException(
                    HttpStatus.BAD_REQUEST,
                    OBRIErrorResponseCategory.REQUEST_INVALID,
                    OBRIErrorType.REQUEST_INVALID_HEADER
                            .toOBError1("Invalid header 'Accept' the only supported value for this operation is '" +
                                    MediaType.APPLICATION_PDF_VALUE + "'"));
        }

        // Check if this customer has a statement file
        Optional<Resource> statement = statementPDFService.getPdfStatement();
        if (statement.isPresent()) {
            return ResponseEntity.ok()
                    .contentLength(getContentLength(statement.get()))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(statement.get());
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<OBReadTransaction6> getAccountsAccountIdStatementsStatementIdTransactions(String statementId, String accountId, String authorization, String xFapiAuthDate, LocalDateTime fromBookingDateTime, LocalDateTime toBookingDateTime, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId, String consentId, int page)
            throws OBErrorException {
        logger.info("getAccountStatementTransactions from account id {}, statement id {}, consentId: {}, apiClientId: {} fromBookingDate {} toBookingDate {} pageNumber {} ",
                    accountId, statementId, consentId, apiClientId, fromBookingDateTime, toBookingDateTime, page);

        if (toBookingDateTime == null) {
            toBookingDateTime = LocalDateTime.now();
        }
        if (fromBookingDateTime == null) {
            fromBookingDateTime = toBookingDateTime.minus(Period.ofYears(100));
        }

        final AccountAccessConsent consent = accountResourceAccessService.getConsentForResourceAccess(consentId, apiClientId, accountId);
        checkPermissions(consent, nonFilePermissions);

        Page<FRTransaction> response = FRTransactionRepository.byAccountIdAndStatementIdAndBookingDateTimeBetweenWithPermissions(accountId, statementId,
                                                                                                                                 new Date(fromBookingDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()), new Date(toBookingDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()), consent.getRequestObj().getData().getPermissions(),
                                                                                                                                 PageRequest.of(page, pageLimitStatements, Sort.Direction.ASC, "bookingDateTime"));

        List<OBTransaction6> transactions = response.getContent()
                                                    .stream()
                                                    .map(FRTransaction::getTransaction)
                                                    .map(FRTransactionConverter::toOBTransaction6)
                                                    .map(t -> accountDataInternalIdFilter.apply(t))
                                                    .collect(Collectors.toList());

        //Package the answer
        int totalPages = response.getTotalPages();

        return ResponseEntity.ok(new OBReadTransaction6().data(new OBReadDataTransaction6().transaction(transactions))
                                                         .links(PaginationUtil.generateLinks(buildGetAccountStatementTransactionUri(accountId, statementId), page, totalPages))
                                                         .meta(PaginationUtil.generateMetaData(totalPages)));
    }

    private String buildGetAccountStatementsUri(String accountId) {
        return linkTo(getClass()).slash("accounts").slash(accountId).slash("statements").toString();
    }

    private String buildGetAccountStatementUri(String accountId, String statementId) {
        return linkTo(getClass()).slash("accounts").slash(accountId).slash("statements").slash(statementId).toString();
    }

    private String buildGetStatementsUri() {
        return linkTo(getClass()).slash("statements").toString();
    }

    private Integer getContentLength(Resource resource) {
        String data = "";
        try {
            byte[] bdata = FileCopyUtils.copyToByteArray(resource.getInputStream());
            return bdata.length;
        } catch (IOException e) {
            logger.warn("We found a statement PDF file '{}' for ASPSP but could no get content-length with error", resource.getFilename(), e);
            return null;
        }
    }

    private ResponseEntity<OBReadStatement2> packageResponse(int page, String httpUrl, List<FRStatement> statements, int totalPages) {
        return ResponseEntity.ok(new OBReadStatement2().data(new OBReadDataStatement2().statement(
                        statements
                                .stream()
                                .map(st -> toOBStatement2(st.getStatement()))
                                .map(accountDataInternalIdFilter::apply)
                                .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }

    void checkPermissions(AccountAccessConsent consent, Set<FRExternalPermissionsCode> statementPermissions) throws OBErrorException {
        final List<FRExternalPermissionsCode> permissions = consent.getRequestObj().getData().getPermissions();
        if (statementPermissions.stream().noneMatch(permissions::contains)) {
            throw new OBErrorException(OBRIErrorType.PERMISSIONS_INVALID, "at least one of: " + statementPermissions);
        }
    }

    private String buildGetAccountStatementTransactionUri(String accountId, String statementId) {
        return linkTo(getClass()).slash("accounts").slash(accountId).slash("statements").slash(statementId).slash("transactions").toString();
    }
}
