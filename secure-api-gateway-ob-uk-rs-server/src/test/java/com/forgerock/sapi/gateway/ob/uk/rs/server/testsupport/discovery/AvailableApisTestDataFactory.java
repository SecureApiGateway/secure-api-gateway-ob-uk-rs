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
package com.forgerock.sapi.gateway.ob.uk.rs.server.testsupport.discovery;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.forgerock.sapi.gateway.ob.uk.rs.server.api.discovery.AvailableApiEndpoint;
import com.forgerock.sapi.gateway.ob.uk.rs.server.common.OBApiReference;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBGroupName;
import com.google.common.collect.ImmutableList;

/**
 * Test data factory to generate a list of Read/Write APIs that are supported by the application (though not
 * necessarily by the customer).
 */
public class AvailableApisTestDataFactory {

    public static final String BASE_URL = "/rs/open-banking/";
    public static final String VERSION_PREFIX = "v3.1.";
    public static final int PATCHES = 10;

    public static List<AvailableApiEndpoint> getAvailableApiEndpoints() {
        List<AvailableApiEndpoint> availableApis = new ArrayList<>();
        availableApis.addAll(generateAccountApis());
        availableApis.addAll(generatePaymentApis());
        availableApis.addAll(generateEventApis());
        availableApis.addAll(generateFundApis());
        return availableApis;
    }

    public static List<AvailableApiEndpoint> generateAccountApis() {
        List<Pair<OBApiReference, String>> content = ImmutableList.of(
                Pair.of(OBApiReference.GET_ACCOUNT, "/aisp/accounts/{AccountId}"),
                Pair.of(OBApiReference.GET_ACCOUNTS, "/aisp/accounts")
        );
        return generateApi(OBGroupName.AISP, content);
    }

    public static List<AvailableApiEndpoint> generatePaymentApis() {
        List<Pair<OBApiReference, String>> content = ImmutableList.of(
                Pair.of(OBApiReference.CREATE_DOMESTIC_PAYMENT, "/pisp/domestic-payment"),
                Pair.of(OBApiReference.GET_DOMESTIC_PAYMENT, "/pisp/domestic-payment/{PaymentId}")
        );
        return generateApi(OBGroupName.PISP, content);
    }

    public static List<AvailableApiEndpoint> generateEventApis() {
        List<Pair<OBApiReference, String>> content = ImmutableList.of(
                Pair.of(OBApiReference.CREATE_CALLBACK_URL, "/callback-urls"),
                Pair.of(OBApiReference.GET_CALLBACK_URLS, "/callback-urls/{CallbackUrlId}"),
                Pair.of(OBApiReference.AMEND_CALLBACK_URL, "/callback-urls/{CallbackUrlId}"),
                Pair.of(OBApiReference.DELETE_CALLBACK_URL, "/callback-urls/{CallbackUrlId}"),
                Pair.of(OBApiReference.CREATE_EVENT_SUBSCRIPTION, "/event-subscriptions"),
                Pair.of(OBApiReference.GET_EVENT_SUBSCRIPTION, "/event-subscriptions"),
                Pair.of(OBApiReference.AMEND_EVENT_SUBSCRIPTION, "/event-subscriptions/{EventSubscriptionId}"),
                Pair.of(OBApiReference.DELETE_EVENT_SUBSCRIPTION, "/event-subscriptions/{EventSubscriptionId}"),
                Pair.of(OBApiReference.EVENT_AGGREGATED_POLLING, "/events")
        );
        return generateApi(OBGroupName.EVENT, content);
    }

    public static List<AvailableApiEndpoint> generateFundApis() {
        List<Pair<OBApiReference, String>> content = ImmutableList.of(
                Pair.of(OBApiReference.CREATE_FUNDS_CONFIRMATION_CONSENT, "/cbpii/funds-confirmation-consent"),
                Pair.of(OBApiReference.GET_FUNDS_CONFIRMATION_CONSENT, "/cbpii/funds-confirmation-consent/{ConsentId}"),
                Pair.of(OBApiReference.DELETE_FUNDS_CONFIRMATION_CONSENT, "/cbpii/funds-confirmation-consent/{ConsentId}"),
                Pair.of(OBApiReference.CREATE_FUNDS_CONFIRMATION, "/cbpii/funds-confirmations")
        );
        return generateApi(OBGroupName.CBPII, content);
    }

    private static List<AvailableApiEndpoint> generateApi(OBGroupName groupName, List<Pair<OBApiReference, String>> content) {
        List<AvailableApiEndpoint> apiVersions = new ArrayList<>();
        for (int patch = 1; patch <= PATCHES; patch++) {
            for (Pair<OBApiReference, String> refAndPath : content) {
                String url = BASE_URL + VERSION_PREFIX + patch + refAndPath.getValue();
                AvailableApiEndpoint apiEndpoint = AvailableApiEndpoint.builder()
                        .groupName(groupName)
                        .version(VERSION_PREFIX + patch)
                        .apiReference(refAndPath.getKey())
                        .uriPath(url)
                        .build();
                apiVersions.add(apiEndpoint);
            }
        }
        return apiVersions;
    }
}
