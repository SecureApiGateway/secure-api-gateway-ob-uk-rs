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

import com.forgerock.securebanking.common.openbanking.uk.forgerock.datamodel.event.FRCallbackUrlData;
import uk.org.openbanking.datamodel.event.OBCallbackUrl1;

public class FRCallbackUrlConverter {

    // OB TO FR
    public static FRCallbackUrlData toFRCallbackUrlData(OBCallbackUrl1 obCallbackUrl1) {
        return obCallbackUrl1 == null ? null : FRCallbackUrlData.builder()
                .url(obCallbackUrl1.getData().getUrl())
                .version(obCallbackUrl1.getData().getVersion())
                .build();
    }
}
