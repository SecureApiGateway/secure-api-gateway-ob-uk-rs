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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie;

/**
 * Constants used throughout the OB Read/Write API.
 */
public class ApiConstants {

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
    public static final String BOOKED_TIME_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String AVAILABLE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZ";
    public static final String STATEMENT_TIME_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public static class ParametersFieldName {
        public static final String FROM_BOOKING_DATE_TIME = "fromBookingDateTime";
        public static final String TO_BOOKING_DATE_TIME = "toBookingDateTime";
        public static final String FROM_STATEMENT_DATE_TIME = "fromStatementDateTime";
        public static final String TO_STATEMENT_DATE_TIME = "toStatementDateTime";
    }
}
