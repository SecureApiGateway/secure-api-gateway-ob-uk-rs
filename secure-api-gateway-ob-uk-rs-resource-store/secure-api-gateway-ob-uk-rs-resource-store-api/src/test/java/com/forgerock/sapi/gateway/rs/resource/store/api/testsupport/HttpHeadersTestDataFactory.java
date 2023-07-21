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
package com.forgerock.sapi.gateway.rs.resource.store.api.testsupport;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.org.openbanking.datamodel.account.OBExternalPermissions1Code;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

/**
 * A test data factory for {@link HttpHeaders} that are sent in each HTTP request.
 */
public class HttpHeadersTestDataFactory {

    /**
     * @return an instance of {@link HttpHeaders} with the minimal set of required headers for resource API.
     */
    public static HttpHeaders requiredResourceHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-fapi-interaction-id", UUID.randomUUID().toString());
        return headers;
    }
}
