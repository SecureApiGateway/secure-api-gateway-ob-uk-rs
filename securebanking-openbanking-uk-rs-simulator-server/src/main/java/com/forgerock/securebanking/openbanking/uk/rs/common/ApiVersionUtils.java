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
package com.forgerock.securebanking.openbanking.uk.rs.common;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.obie.OBVersion;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Open Banking API version utility.
 */
@Slf4j
public abstract class ApiVersionUtils {

    private static final Pattern VERSION_PATTERN = Pattern.compile("v[0-9][0-9]?\\.[0-9][0-9]?\\.?[0-9]?[0-9]?");

    /**
     * Provides the matching version of the API if it is contained within the provided String.
     *
     * @param value a {@link String} containing the supported version, which may or may not be prefixed by 'v'
     *              (e.g. v3.1.3). The value may be an OB URL, which should contain the version in the expected format.
     * @return The matching {@link OBVersion}.
     */
    public static OBVersion getOBVersion(String value) {
        Matcher matcher = VERSION_PATTERN.matcher(format(value));
        if (!matcher.find()) {
            throw new IllegalArgumentException("Unable to determine version from value: " + value);
        }
        OBVersion version = OBVersion.fromString(matcher.group());
        if (version == null) {
            log.debug("Unknown version value from: {}", value);
            throw new IllegalArgumentException("Unknown version value from: " + value);
        }
        return version;
    }

    /**
     * Ensures the provided String starts with 'v' (e.g. v3.1.3), unless it is a URL in which case it should already
     * contain the version in the correct format.
     *
     * @param value a String that should contain the version, with or without the 'v' prefix.
     * @return The value prefixed with v (unless it is a URL).
     */
    private static String format(String value) {
        return value.startsWith("http") || value.startsWith("v") ? value : "v".concat(value);
    }
}
