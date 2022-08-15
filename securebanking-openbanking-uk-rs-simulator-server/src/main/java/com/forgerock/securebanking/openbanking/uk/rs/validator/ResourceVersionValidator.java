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
package com.forgerock.securebanking.openbanking.uk.rs.validator;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.obie.OBVersion;

public class ResourceVersionValidator {

    /**
     * Checks if a resource can be accessed from the version of the API that's currently being invoked. The OB spec
     * states that a TPP cannot access a resource from an older API version if the resource was created in a newer
     * version.
     *
     * <p>
     * This method is being used to filter any resources that cannot be accessed via the version of the API version
     * in question.
     *
     * @param apiVersion The version of the API currently being invoked.
     * @param resourceVersion The version of the API that the resource was saved against.
     * @return {@code true} if the resource is allowed to be accessed, otherwise false.
     */
    public static boolean isAccessToResourceAllowed(OBVersion apiVersion, OBVersion resourceVersion) {
        return apiVersion.equals(resourceVersion) ||
                apiVersion.isAfterVersion(resourceVersion);
    }
}
