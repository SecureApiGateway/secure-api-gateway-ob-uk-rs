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
package com.forgerock.securebanking.openbanking.uk.rs.converter.event;

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.event.FREventSubscriptionData;
import uk.org.openbanking.datamodel.event.OBEventSubscription1;

public class FREventSubscriptionConverter {

    // OB to FR
    public static FREventSubscriptionData toFREventSubscriptionData(OBEventSubscription1 obEventSubscription) {
        return obEventSubscription == null ? null : FREventSubscriptionData.builder()
                .callbackUrl(obEventSubscription.getData().getCallbackUrl())
                .version(obEventSubscription.getData().getVersion())
                .eventTypes(obEventSubscription.getData().getEventTypes())
                .build();
    }
}
