/**
 * Copyright © 2020-2021 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.common;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.OBGroupName;
import org.springframework.http.HttpMethod;

import java.util.Arrays;

import static com.forgerock.securebanking.openbanking.uk.common.api.meta.OBGroupName.*;
import static org.springframework.http.HttpMethod.*;

/**
 * A version agnostic list of Open Banking API endpoints, along with their HTTP methods. This is used to help build up
 * a list of endpoints that are supported by the application and returned in the Discovery endpoint. It is also used to
 * disable specific controller methods that are not supported by the customer.
 */
public enum OBApiReference {

    /** Account and Transactions Api **/

    GET_ACCOUNTS(AISP, "GetAccounts", GET, "/aisp/accounts"),
    GET_ACCOUNT(AISP, "GetAccount", GET, "/aisp/accounts/{AccountId}"),
    GET_ACCOUNT_TRANSACTIONS(AISP, "GetAccountTransactions", GET, "/aisp/accounts/{AccountId}/transactions"),
    GET_ACCOUNT_BENEFICIARIES(AISP, "GetAccountBeneficiaries", GET, "/aisp/accounts/{AccountId}/beneficiaries"),
    GET_ACCOUNT_BALANCES(AISP, "GetAccountBalances", GET, "/aisp/accounts/{AccountId}/balances"),
    GET_ACCOUNT_DIRECT_DEBITS(AISP, "GetAccountDirectDebits", GET, "/aisp/accounts/{AccountId}/direct-debits"),
    GET_ACCOUNT_STANDING_ORDERS(AISP, "GetAccountStandingOrders", GET, "/aisp/accounts/{AccountId}/standing-orders"),
    GET_ACCOUNT_PRODUCT(AISP, "GetAccountProduct", GET, "/aisp/accounts/{AccountId}/product"),

    GET_STANDING_ORDERS(AISP, "GetStandingOrders", GET, "/aisp/standing-orders"),
    GET_DIRECT_DEBITS(AISP, "GetDirectDebits", GET, "/aisp/direct-debits"),
    GET_BENEFICIARIES(AISP, "GetBeneficiaries", GET, "/aisp/beneficiaries"),
    GET_TRANSACTIONS(AISP, "GetTransactions", GET, "/aisp/transactions"),
    GET_BALANCES(AISP, "GetBalances", GET, "/aisp/balances"),
    GET_PRODUCTS(AISP, "GetProducts", GET, "/aisp/products"),

    GET_ACCOUNT_OFFERS(AISP, "GetAccountOffers", GET, "/aisp/accounts/{AccountId}/offers"),
    GET_ACCOUNT_PARTY(AISP, "GetAccountParty", GET, "/aisp/accounts/{AccountId}/party"),
    GET_ACCOUNT_PARTIES(AISP, "GetAccountParties", GET, "/aisp/accounts/{AccountId}/parties"),
    GET_ACCOUNT_SCHEDULED_PAYMENTS(AISP, "GetAccountScheduledPayments", GET, "/aisp/accounts/{AccountId}/scheduled-payments"),
    GET_ACCOUNT_STATEMENTS(AISP, "GetAccountStatements", GET, "/aisp/accounts/{AccountId}/statements"),
    GET_ACCOUNT_STATEMENT(AISP, "GetAccountStatement", GET, "/aisp/accounts/{AccountId}/statements/{StatementId}"),
    GET_ACCOUNT_STATEMENT_FILE(AISP, "GetAccountStatementFile", GET, "/aisp/accounts/{AccountId}/statements/{StatementId}/file"),
    GET_ACCOUNT_STATEMENT_TRANSACTIONS(AISP, "GetAccountStatementTransactions", GET, "/aisp/accounts/{AccountId}/statements/{StatementId}/transactions"),

    GET_OFFERS(AISP, "GetOffers", GET, "/aisp/offers"),
    GET_PARTY(AISP, "GetParty", GET, "/aisp/party"),
    GET_SCHEDULED_PAYMENTS(AISP, "GetScheduledPayments", GET, "/aisp/scheduled-payments"),
    GET_STATEMENTS(AISP, "GetStatements", GET, "/aisp/statements"),

    /** Funds Api **/
    CREATE_FUNDS_CONFIRMATION(CBPII, "CreateFundsConfirmation", POST, "/cbpii/funds-confirmations"),
    GET_FUNDS_CONFIRMATION(CBPII, "GetFundsConfirmation", GET, "/cbpii/funds-confirmations/{FundsConfirmationId}"),

    /** Callback Api **/
    CREATE_CALLBACK_URL(EVENT, "CreateCallbackUrl", POST, "/callback-urls"),
    GET_CALLBACK_URLS(EVENT, "GetCallbackUrls", GET, "/callback-urls"),
    AMEND_CALLBACK_URL(EVENT, "AmendCallbackUrl", PUT, "/callback-urls/{CallbackUrlId}"),
    DELETE_CALLBACK_URL(EVENT, "DeleteCallbackUrl", DELETE, "/callback-urls/{CallbackUrlId}"),

    /** Events Api **/
    CREATE_EVENT_SUBSCRIPTION(EVENT, "CreateEventSubscription", POST, "/event-subscriptions"),
    GET_EVENT_SUBSCRIPTION(EVENT, "GetEventSubscription", GET, "/event-subscriptions"),
    AMEND_EVENT_SUBSCRIPTION(EVENT, "AmendEventSubscription", PUT, "/event-subscriptions/{EventSubscriptionId}"),
    DELETE_EVENT_SUBSCRIPTION(EVENT, "DeleteEventSubscription", DELETE, "/event-subscriptions/{EventSubscriptionId}"),

    EVENT_AGGREGATED_POLLING(EVENT, "EventAggregatedPolling", GET, "/events"),

    /** Payments Api **/
    CREATE_DOMESTIC_PAYMENT(PISP, "CreateDomesticPayment", POST, "/pisp/domestic-payments"),
    GET_DOMESTIC_PAYMENT(PISP, "GetDomesticPayment", GET, "/pisp/domestic-payments/{DomesticPaymentId}"),

    CREATE_DOMESTIC_SCHEDULED_PAYMENT(PISP, "CreateDomesticScheduledPayment", POST, "/pisp/domestic-scheduled-payment-consents"),
    GET_DOMESTIC_SCHEDULED_PAYMENT(PISP, "GetDomesticScheduledPayment", GET, "/pisp/domestic-scheduled-payments/{DomesticScheduledPaymentId}"),

    CREATE_DOMESTIC_STANDING_ORDER(PISP, "CreateDomesticStandingOrder", POST, "/pisp/domestic-standing-orders"),
    GET_DOMESTIC_STANDING_ORDER(PISP, "GetDomesticStandingOrder", GET, "/pisp/domestic-standing-orders/{DomesticStandingOrderId}"),

    CREATE_INTERNATIONAL_PAYMENT(PISP, "CreateInternationalPayment", POST, "/pisp/international-payments"),
    GET_INTERNATIONAL_PAYMENT(PISP, "GetInternationalPayment", GET, "/pisp/international-payments/{InternationalPaymentId}"),

    CREATE_INTERNATIONAL_SCHEDULED_PAYMENT(PISP, "CreateInternationalScheduledPayment", POST, "/pisp/international-scheduled-payments"),
    GET_INTERNATIONAL_SCHEDULED_PAYMENT(PISP, "GetInternationalScheduledPayment", GET, "/pisp/international-scheduled-payments/{InternationalScheduledPaymentId}"),

    CREATE_INTERNATIONAL_STANDING_ORDER(PISP, "CreateInternationalStandingOrder", POST, "/pisp/international-standing-orders"),
    GET_INTERNATIONAL_STANDING_ORDER(PISP, "GetInternationalStandingOrder", GET, "/pisp/international-standing-orders/{InternationalStandingOrderId}"),


    CREATE_FILE_PAYMENT_FILE(PISP, "CreateFilePaymentFile", POST, "/pisp/file-payment-consents/{ConsentId}/file"),
    GET_FILE_PAYMENT_FILE(PISP, "GetFilePaymentFile", GET, "/pisp/file-payment-consents/{ConsentId}/file"),

    CREATE_FILE_PAYMENT(PISP, "CreateFilePayment", POST, "/pisp/file-payments"),
    GET_FILE_PAYMENT(PISP, "GetFilePayment", GET, "/pisp/file-payments/{FilePaymentId}"),
    GET_FILE_PAYMENT_REPORT(PISP, "GetFilePaymentReport", GET, "/pisp/file-payments/{ConsentId}/report-file");

    private static final OBApiReference[] VALUES = OBApiReference.values();

    /**
     * A reference to the endpoint which is returned in the Discovery endpoint.
     */
    private final String reference;

    /**
     * The HTTP method (e.g. GET). This is used to help map an API URL to a specific endpoint (e.g. GET
     * and DELETE share the same URL).
     */
    private final HttpMethod httpMethod;

    /**
     * The API's "group" or area of functionality - e.g. AISP, PISP.
     */
    private final OBGroupName groupName;

    /**
     * The relative path of the API endpoint (e.g. /pisp/domestic-payments/{DomesticPaymentId}).
     */
    private final String relativePath;

    /**
     * Constructor used by enum declarations above.
     * @param groupName The "group" of the API (e.g. AISP, PISP).
     * @param reference A reference for the API endpoint.
     * @param httpMethod The HTTP method (e.g. GET) for the endpoint.
     * @param relativePath The relative path of the API endpoint.
     */
    OBApiReference(OBGroupName groupName, String reference, HttpMethod httpMethod, String relativePath) {
        this.reference = reference;
        this.httpMethod = httpMethod;
        this.groupName = groupName;
        this.relativePath = relativePath;
    }

    public OBGroupName getGroupName() {
        return groupName;
    }

    public String getReference() {
        return reference;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public static OBApiReference fromReference(String reference) {
        return Arrays.stream(VALUES)
                .filter(r -> r.reference.equals(reference))
                .findFirst()
                .orElse(null);
    }

    public static OBApiReference fromMethodAndPath(HttpMethod httpMethod, String relativePath) {
        return Arrays.stream(VALUES)
                .filter(r -> r.httpMethod.equals(httpMethod) && r.relativePath.equals(relativePath))
                .findFirst()
                .orElse(null);
    }
}
