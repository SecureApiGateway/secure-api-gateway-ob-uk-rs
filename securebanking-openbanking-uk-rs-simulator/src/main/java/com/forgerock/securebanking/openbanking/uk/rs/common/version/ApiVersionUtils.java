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
package com.forgerock.securebanking.openbanking.uk.rs.common.version;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Versioning Util
 */
@Slf4j
public abstract class ApiVersionUtils {
    private static final Pattern VERSION_PATTERN = Pattern.compile("v[0-9][0-9]?\\.[0-9][0-9]?\\.?[0-9]?[0-9]?");

    /**
     * Find and Provides the version of the API supported by this instance if it contained in the passed parameter.
     * @param s
     *         parameter passed to find the version supported.
     * @return The {@link OBVersion} matching.
     */
    public static OBVersion getOBVersion(String s) {
        Matcher matcher = VERSION_PATTERN.matcher(format(s));
        if (!matcher.find()) {
            throw new IllegalArgumentException("Unable to determine version from passed parameter: " + s);
        }
        OBVersion version = OBVersion.fromString(matcher.group());
        if (version == null) {
            log.debug("Unknown version value from: {}", s);
            throw new IllegalArgumentException("Unknown version value from: " + s);
        }
        return version;
    }

    /**
     * Format the value to normalize it the format (vX.X.X)
     * @param s
     * @return param formated
     */
    private static String format(String s){
        return s.startsWith("http") || s.startsWith("v") ? s : "v".concat(s);
    }
}
