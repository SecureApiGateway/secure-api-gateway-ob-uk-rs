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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie.event.v3_1_4.aggregatedpolling;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.event.FREventPolling;
import com.forgerock.securebanking.openbanking.uk.error.OBErrorResponseException;
import com.forgerock.securebanking.openbanking.uk.rs.service.event.EventPollingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import uk.org.openbanking.datamodel.event.OBEventPolling1;
import uk.org.openbanking.datamodel.event.OBEventPollingResponse1;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.Map;

import static com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.converter.event.FREventPollingConverter.toFREventPolling;

@Controller("AggregatedPollingApi3.1.4")
@Slf4j
public class AggregatedPollingApiController implements AggregatedPollingApi {

    private final EventPollingService eventPollingService;

    public AggregatedPollingApiController(EventPollingService eventPollingService) {
        this.eventPollingService = eventPollingService;
    }

    @Override
    public ResponseEntity<OBEventPollingResponse1> pollEvents(@Valid OBEventPolling1 obEventPolling,
                                                              String authorization,
                                                              String xFapiInteractionId,
                                                              String tppId,
                                                              HttpServletRequest request,
                                                              Principal principal
    ) throws OBErrorResponseException {

        FREventPolling frEventPolling = toFREventPolling(obEventPolling);
        log.debug("TPP '{}' sent aggregated polling request: {}", tppId, obEventPolling);
        eventPollingService.acknowledgeEvents(frEventPolling, tppId);
        eventPollingService.recordTppEventErrors(frEventPolling, tppId);
        Map<String, String> allEventNotifications = eventPollingService.fetchNewEvents(frEventPolling, tppId);

        // Apply limit on returned events
        Map<String, String> truncatedEventNotifications = eventPollingService.truncateEvents(obEventPolling.getMaxEvents(), allEventNotifications, tppId);
        boolean moreAvailable = truncatedEventNotifications.size() < allEventNotifications.size();

        ResponseEntity<OBEventPollingResponse1> response = ResponseEntity.ok(new OBEventPollingResponse1()
                .sets(truncatedEventNotifications)
                .moreAvailable((truncatedEventNotifications.isEmpty()) ? null : moreAvailable));
        log.debug("TPP '{}' aggregated polling response: {}", tppId, response.getBody());
        return response;
    }
}
