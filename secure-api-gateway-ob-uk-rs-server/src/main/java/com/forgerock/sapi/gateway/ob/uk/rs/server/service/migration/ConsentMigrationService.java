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
package com.forgerock.sapi.gateway.ob.uk.rs.server.service.migration;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConsentMigrationService {

    // latest major version
    private final String LATEST_VERSION = "4.0.0";

    /**
     * Determines if migration is needed based on the version number. Migration is required if the current version is older than the latest version available.
     *
     * @param currentVersion
     * @return {@code true} if migration is needed.
     */
    public boolean isConsentMigrationNeeded(String currentVersion) {
        // Validate input
        if (currentVersion == null || currentVersion.isEmpty()) {
            throw new IllegalArgumentException("Consent version cannot be null.");
        }

        // Compare the currentVersion to the latestVersion
        return currentVersion.compareTo(LATEST_VERSION) < 0;
    }

}
