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
package com.forgerock.sapi.gateway.ob.uk.rs.server.api.obie.event;

import java.util.List;
import java.util.UUID;

import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.event.FREventMessageEntity;

import uk.org.openbanking.datamodel.event.OBEvent1;
import uk.org.openbanking.datamodel.event.OBEventLink1;
import uk.org.openbanking.datamodel.event.OBEventPolling1;
import uk.org.openbanking.datamodel.event.OBEventResourceUpdate1;
import uk.org.openbanking.datamodel.event.OBEventSubject1;

public class EventTestHelper {

    public static FREventMessageEntity aValidFREventMessageEntity(){
        return aValidFREventNotificationEntityBuilder(UUID.randomUUID().toString()).build();
    }

    public static FREventMessageEntity aValidFREventMessageEntity(String apiClientId){
        return aValidFREventNotificationEntityBuilder(apiClientId).build();
    }

    public static OBEventPolling1 aValidOBEventPolling1() {
        return new OBEventPolling1()
                .maxEvents(1)
                .returnImmediately(true);
    }

    public static FREventMessageEntity.FREventMessageEntityBuilder aValidFREventNotificationEntityBuilder(String apiClientId) {
        return FREventMessageEntity.builder()
                .id(UUID.randomUUID().toString())
                .apiClientId(apiClientId)
                .iss("https://examplebank.com/")
                .iat(1516239022)
                .jti(UUID.randomUUID().toString())
                .sub("https://examplebank.com/api/open-banking/v3.0/pisp/domestic-payments/pmt-7290-003")
                .aud("7umx5nTR33811QyQfi")
                .txn(UUID.randomUUID().toString())
                .toe(1516239022)
                .events(getOBEvent());
    }

    public static OBEvent1 getOBEvent() {
        return new OBEvent1().urnukorgopenbankingeventsresourceUpdate(
                new OBEventResourceUpdate1()
                        .subject(
                                new OBEventSubject1()
                                        .subjectType("http://openbanking.org.uk/rid_http://openbanking.org.uk/rty")
                                        .httpopenbankingOrgUkrid("pmt-7290-003")
                                        .httpopenbankingOrgUkrlk(
                                                List.of(
                                                        new OBEventLink1()
                                                                .link("https://examplebank.com/api/open-banking/v3.0/pisp/domestic-payments/pmt-7290-003")
                                                                .version("v3.1.5"),
                                                        new OBEventLink1()
                                                                .link("https://examplebank.com/api/open-banking/v3.0/pisp/domestic-payments/pmt-7290-003")
                                                                .version("v3.1.10")
                                                )
                                        )
                                        .httpopenbankingOrgUkrty("domestic-payment")
                        )
        );
    }
}
