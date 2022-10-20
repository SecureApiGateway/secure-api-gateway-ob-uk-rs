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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.account.v3_0.statements;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.account.FRStatementConverter;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorResponseCategory;
import com.forgerock.securebanking.openbanking.uk.error.OBRIErrorType;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.AccountDataInternalIdFilter;
import com.forgerock.securebanking.openbanking.uk.rs.common.util.PaginationUtil;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.document.account.FRStatement;
import com.forgerock.securebanking.openbanking.uk.rs.persistence.repository.accounts.statements.FRStatementRepository;
import com.forgerock.securebanking.openbanking.uk.rs.service.statement.StatementPDFService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
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
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;
import uk.org.openbanking.datamodel.account.OBReadStatement1;
import uk.org.openbanking.datamodel.account.OBReadStatement1Data;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.account.FRExternalPermissionsCodeConverter.toFRExternalPermissionsCodeList;

@Controller("StatementsApiV3.0")
@Slf4j
public class StatementsApiController implements StatementsApi {

    @Value("${rs.page.default.statement.size:10}")
    private int PAGE_LIMIT_STATEMENTS;

    private final FRStatementRepository frStatementRepository;
    private final AccountDataInternalIdFilter accountDataInternalIdFilter;
    private final StatementPDFService statementPDFService;

    public StatementsApiController(FRStatementRepository frStatementRepository,
                                   AccountDataInternalIdFilter accountDataInternalIdFilter,
                                   StatementPDFService statementPDFService) {
        this.frStatementRepository = frStatementRepository;
        this.accountDataInternalIdFilter = accountDataInternalIdFilter;
        this.statementPDFService = statementPDFService;
    }

    @Override
    public ResponseEntity<OBReadStatement1> getAccountStatement(String accountId,
                                                                String statementId,
                                                                int page,
                                                                String xFapiFinancialId,
                                                                String authorization,
                                                                DateTime xFapiCustomerLastLoggedTime,
                                                                String xFapiCustomerIpAddress,
                                                                String xFapiInteractionId,
                                                                String xCustomerUserAgent,
                                                                List<OBExternalPermissions1Code> permissions,
                                                                String httpUrl
    ) {
        log.info("Read statements for account {} with minimumPermissions {}", accountId, permissions);
        List<FRStatement> statements = frStatementRepository.byAccountIdAndStatementIdWithPermissions(accountId, statementId, toFRExternalPermissionsCodeList(permissions));
        int totalPages = 1;

        return ResponseEntity.ok(new OBReadStatement1().data(new OBReadStatement1Data().statement(
                        statements
                                .stream()
                                .map(FRStatement::getStatement)
                                .map(FRStatementConverter::toOBStatement1)
                                .map(so -> accountDataInternalIdFilter.apply(so))
                                .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }

    @Override
    public ResponseEntity<Resource> getAccountStatementFile(String accountId,
                                                            int page,
                                                            String statementId,
                                                            String xFapiFinancialId,
                                                            String authorization,
                                                            DateTime xFapiCustomerLastLoggedTime,
                                                            String xFapiCustomerIpAddress,
                                                            String xFapiInteractionId,
                                                            String accept) throws OBErrorResponseException {
        log.info("Received a statement file download request for account: {} (Accept: {}). Interaction Id: {}", accountId, accept, xFapiInteractionId);
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

        // Check if this cusotmer has a statement file
        Optional<Resource> statement = statementPDFService.getPdfStatement();
        if (statement.isPresent()) {
            return ResponseEntity.ok()
                    .contentLength(getContentLength(statement.get()))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(statement.get());
        }
        return new ResponseEntity<Resource>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<OBReadStatement1> getStatements(String xFapiFinancialId,
                                                          int page,
                                                          DateTime fromStatementDateTime,
                                                          DateTime toStatementDateTime,
                                                          String authorization,
                                                          DateTime xFapiCustomerLastLoggedTime,
                                                          String xFapiCustomerIpAddress,
                                                          String xFapiInteractionId,
                                                          String xCustomerUserAgent,
                                                          List<String> accountIds,
                                                          List<OBExternalPermissions1Code> permissions,
                                                          String httpUrl
    ) {
        log.info("Reading statements from account ids {}", accountIds);
        Page<FRStatement> statements = frStatementRepository.findByAccountIdIn(accountIds,
                PageRequest.of(page, PAGE_LIMIT_STATEMENTS, Sort.Direction.ASC, "startDateTime"));
        int totalPages = statements.getTotalPages();

        return ResponseEntity.ok(new OBReadStatement1().data(new OBReadStatement1Data().statement(
                        statements.getContent()
                                .stream()
                                .map(FRStatement::getStatement)
                                .map(FRStatementConverter::toOBStatement1)
                                .map(so -> accountDataInternalIdFilter.apply(so))
                                .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }

    @Override
    public ResponseEntity<OBReadStatement1> getAccountStatements(String accountId,
                                                                 int page,
                                                                 String xFapiFinancialId,
                                                                 String authorization,
                                                                 DateTime fromStatementDateTime,
                                                                 DateTime toStatementDateTime,
                                                                 DateTime xFapiCustomerLastLoggedTime,
                                                                 String xFapiCustomerIpAddress,
                                                                 String xFapiInteractionId,
                                                                 String xCustomerUserAgent,
                                                                 List<OBExternalPermissions1Code> permissions,
                                                                 String httpUrl
    ) {
        log.info("Read statements for account {} with minimumPermissions {}", accountId, permissions);
        Page<FRStatement> statements = frStatementRepository.byAccountIdWithPermissions(accountId,
                fromStatementDateTime, toStatementDateTime,
                toFRExternalPermissionsCodeList(permissions),
                PageRequest.of(page, PAGE_LIMIT_STATEMENTS, Sort.Direction.ASC, "startDateTime"));

        int totalPages = statements.getTotalPages();

        return ResponseEntity.ok(new OBReadStatement1().data(new OBReadStatement1Data().statement(
                        statements.getContent()
                                .stream()
                                .map(FRStatement::getStatement)
                                .map(FRStatementConverter::toOBStatement1)
                                .map(so -> accountDataInternalIdFilter.apply(so))
                                .collect(Collectors.toList())))
                .links(PaginationUtil.generateLinks(httpUrl, page, totalPages))
                .meta(PaginationUtil.generateMetaData(totalPages)));
    }

    private Integer getContentLength(Resource resource) {
        try {
            byte[] bdata = FileCopyUtils.copyToByteArray(resource.getInputStream());
            return bdata.length;
        } catch (IOException e) {
            log.warn("We found a statement PDF file '{}' for ASPSP but could no get content-length with error", resource.getFilename(), e);
            return null;
        }
    }
}
