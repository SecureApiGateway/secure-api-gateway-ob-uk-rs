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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConsentMigrationServiceTest {

    @InjectMocks
    private ConsentMigrationService consentMigrationService;

    @Test
    public void ShouldBeMigratedGivenOlderVersion() {

        boolean isMigrationNeeded = consentMigrationService.isConsentMigrationNeeded("3.1.5");

        assertThat(isMigrationNeeded).isTrue();

    }

    @Test
    public void ShouldNotBeMigratedGivenNewerVersion() {

        boolean isMigrationNeeded = consentMigrationService.isConsentMigrationNeeded("4.1.5");

        assertThat(isMigrationNeeded).isFalse();
    }

}
