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
package com.forgerock.securebanking.openbanking.uk.rs.service;

/**
 * Interface for various metrics to be captured (e.g. for TPP requests to be recorded so that they can be analysed).
 */
public interface AnalyticsService {

    /**
     * Records the occurrence of an action/event (which should be clearly described by the provided 'text'
     * {@link String} parameter). Any additional arguments should have '{}' placeholders within the provided
     * {@link String}, so that they are included within the resulting output (i.e. as per the slf4j format).
     *
     * @param text A {@link String} describing the activity, with optional '{}' placeholders for additional arguments.
     * @param arguments One or more optional arguments that are included in the resulting output.
     */
    void recordActivity(String text, Object... arguments);
}
