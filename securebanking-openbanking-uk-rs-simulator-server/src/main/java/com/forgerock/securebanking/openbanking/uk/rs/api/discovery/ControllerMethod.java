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
package com.forgerock.securebanking.openbanking.uk.rs.api.discovery;

import lombok.Value;

import java.lang.reflect.Method;

/**
 * A {@link String} representing an endpoint's controller class and method. The format of this {@link String} is used
 * when building up a blacklist of endpoints to block. A {@link String}, rather than the Class and Method objects, is
 * used for efficiency purposes.
 */
@Value
public class ControllerMethod {

    private final String value;

    /**
     * Private constructor to enforce use of builder method.
     *
     * @param value A {@link String} representing the relevant endpoint's controller class and method.
     */
    private ControllerMethod(String value) {
        this.value = value;
    }

    /**
     * Static builder method to construct a {@link String} representing an endpoint's controller class and
     * method in the expected format.
     *
     * @param clazz The {@link Class} of the endpoint's controller.
     * @param method The {@link Method} within the endpoint's controller.
     * @return A valid {@link ControllerMethod} instance.
     */
    public static ControllerMethod of(Class clazz, Method method) {
        return new ControllerMethod(formatString(clazz, method));
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    private static String formatString(Class clazz, Method method) {
        return String.format("%s.%s", clazz.getName(), method.getName());
    }
}
