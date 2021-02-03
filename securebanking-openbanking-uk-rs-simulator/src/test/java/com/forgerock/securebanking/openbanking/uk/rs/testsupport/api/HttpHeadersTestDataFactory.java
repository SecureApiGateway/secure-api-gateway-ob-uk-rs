/**
 * Copyright © 2020 ForgeRock AS (obst@forgerock.com)
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
package com.forgerock.securebanking.openbanking.uk.rs.testsupport.api;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.OBVersion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.UUID;

import static java.util.Collections.singletonList;
import static uk.org.openbanking.datamodel.account.OBExternalPermissions1Code.READACCOUNTSDETAIL;
import static uk.org.openbanking.datamodel.account.OBExternalPermissions1Code.READPAN;

/**
 * A test data factory for {@link HttpHeaders} that are sent in each HTTP request.
 */
public class HttpHeadersTestDataFactory {

    /**
     * Provides an instance of {@link HttpHeaders} with the minimal set of required headers for the Accounts API.
     *
     * @param version The version of the OB Read/Write API being used.
     * @param accountId The ID of the account to be returned.
     * @return the {@link HttpHeaders} instance.
     */
    public static HttpHeaders requiredAccountHttpHeaders(OBVersion version, String accountId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("dummyAuthToken");
        headers.add("x-idempotency-key", UUID.randomUUID().toString());
        headers.add("x-ob-url", "http://open-banking/" + version.getCanonicalVersion() + "/aisp/accounts/" + accountId);
        headers.add("x-ob-permissions", READACCOUNTSDETAIL.name() + "," + READPAN.name());
        headers.add("x-ob-account-ids", accountId);
        return headers;
    }

    /**
     * @return an instance of {@link HttpHeaders} with the minimal set of required headers for the Payments API.
     */
    public static HttpHeaders requiredPaymentHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth("dummyAuthToken");
        headers.add("x-fapi-financial-id", UUID.randomUUID().toString());
        headers.add("x-idempotency-key", UUID.randomUUID().toString());
        headers.add("x-jws-signature", "dummyJwsSignature");
        headers.add("x-ob-tpp-id", "tppId");
        headers.add("x-ob-tpp-name", "tppName");
        return headers;
    }
}
