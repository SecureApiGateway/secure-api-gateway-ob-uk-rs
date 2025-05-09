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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.event.v4_0_0.aggregatedpolling;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.event.FREventPolling;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.event.v4_0_0.aggregatedpolling.AggregatedPollingApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.event.EventPollingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.v4.event.OBEventPolling1;
import uk.org.openbanking.datamodel.v4.event.OBEventPollingResponse1;

import java.util.Map;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.v4.event.FREventPollingConverter.toFREventPolling;

@Controller("AggregatedPollingApiV4.0.0")
@Slf4j
public class AggregatedPollingApiController implements AggregatedPollingApi {
    private final EventPollingService eventPollingService;

    public AggregatedPollingApiController(EventPollingService eventPollingService) {
        this.eventPollingService = eventPollingService;
    }

    @Override
    public ResponseEntity<OBEventPollingResponse1> createEvents(String authorization, OBEventPolling1 obEventPolling1, String xFapiAuthDate, String xFapiCustomerIpAddress, String xFapiInteractionId, String xCustomerUserAgent, String apiClientId) throws OBErrorResponseException {
        FREventPolling frEventPolling = toFREventPolling(obEventPolling1);
        log.debug("apiClient '{}' sent aggregated polling request: {}", apiClientId, obEventPolling1);
        eventPollingService.acknowledgeEvents(frEventPolling, apiClientId);
        eventPollingService.recordTppEventErrors(frEventPolling, apiClientId);
        Map<String, String> allEventNotifications = eventPollingService.fetchNewEvents(frEventPolling, apiClientId);

        // Apply limit on returned events
        Map<String, String> truncatedEventNotifications = eventPollingService.truncateEvents(obEventPolling1.getMaxEvents(), allEventNotifications, apiClientId);
        boolean moreAvailable = truncatedEventNotifications.size() < allEventNotifications.size();

        ResponseEntity<OBEventPollingResponse1> response = ResponseEntity.ok(new OBEventPollingResponse1()
                .sets(truncatedEventNotifications)
                .moreAvailable((truncatedEventNotifications.isEmpty()) ? null : moreAvailable));
        log.debug("apiClient '{}' aggregated polling response: {}", apiClientId, response.getBody());
        return response;
    }
}
