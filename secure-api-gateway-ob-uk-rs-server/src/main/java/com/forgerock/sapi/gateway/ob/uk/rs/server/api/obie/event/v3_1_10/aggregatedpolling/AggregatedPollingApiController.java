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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.event.v3_1_10.aggregatedpolling;

import static com.forgerock.sapi.gateway.ob.uk.common.datamodel.converter.event.FREventPollingConverter.toFREventPolling;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.event.FREventPolling;
import com.forgerock.sapi.gateway.ob.uk.common.error.OBErrorResponseException;
import com.forgerock.sapi.gateway.ob.uk.rs.obie.api.event.v3_1_10.aggregatedpolling.AggregatedPollingApi;
import com.forgerock.sapi.gateway.ob.uk.rs.server.service.event.EventPollingService;

import lombok.extern.slf4j.Slf4j;
import uk.org.openbanking.datamodel.event.OBEventPolling1;
import uk.org.openbanking.datamodel.event.OBEventPollingResponse1;

@Controller("AggregatedPollingApiV3.1.10")
@Slf4j
public class AggregatedPollingApiController implements AggregatedPollingApi {
    private final EventPollingService eventPollingService;

    public AggregatedPollingApiController(EventPollingService eventPollingService) {
        this.eventPollingService = eventPollingService;
    }

    @Override
    public ResponseEntity<OBEventPollingResponse1> pollEvents(
            OBEventPolling1 obEventPolling,
            String authorization,
            String xFapiInteractionId,
            String apiClientId,
            HttpServletRequest request
    ) throws OBErrorResponseException {
        FREventPolling frEventPolling = toFREventPolling(obEventPolling);
        log.debug("apiClient '{}' sent aggregated polling request: {}", apiClientId, obEventPolling);
        eventPollingService.acknowledgeEvents(frEventPolling, apiClientId);
        eventPollingService.recordTppEventErrors(frEventPolling, apiClientId);
        Map<String, String> allEventNotifications = eventPollingService.fetchNewEvents(frEventPolling, apiClientId);

        // Apply limit on returned events
        Map<String, String> truncatedEventNotifications = eventPollingService.truncateEvents(obEventPolling.getMaxEvents(), allEventNotifications, apiClientId);
        boolean moreAvailable = truncatedEventNotifications.size() < allEventNotifications.size();

        ResponseEntity<OBEventPollingResponse1> response = ResponseEntity.ok(new OBEventPollingResponse1()
                .sets(truncatedEventNotifications)
                .moreAvailable((truncatedEventNotifications.isEmpty()) ? null : moreAvailable));
        log.debug("apiClient '{}' aggregated polling response: {}", apiClientId, response.getBody());
        return response;
    }
}
