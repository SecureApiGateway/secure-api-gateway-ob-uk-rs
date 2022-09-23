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
package com.forgerock.securebanking.rs.platform.client.services;

import com.forgerock.securebanking.rs.platform.client.exceptions.ExceptionClient;
import com.forgerock.securebanking.rs.platform.client.model.ClientRequest;
import com.google.gson.JsonObject;

/**
 * Service interface for retrieving the details of consent requests.
 */
interface PlatformClient {

    /**
     * Retrieves the specific consent for the type of the consent from the platform.
     *
     * @param clientRequest  {@link ClientRequest} required information to provide the consent details.
     * @return The underlying Consent, depending on the type of the consent.
     * @throws ExceptionClient if an error occurs.
     */
    JsonObject getIntentAsJsonObject(ClientRequest clientRequest) throws ExceptionClient;
}
