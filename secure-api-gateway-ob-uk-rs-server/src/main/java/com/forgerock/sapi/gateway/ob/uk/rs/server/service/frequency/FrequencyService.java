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
package com.forgerock.sapi.gateway.ob.uk.rs.server.service.frequency;

import com.forgerock.sapi.gateway.uk.common.shared.api.meta.forgerock.FRFrequencyType;
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.forgerock.FRQuarterType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.regex.Matcher;

public class FrequencyService {

    public static DateTime getNextDateTime(DateTime previous, String frequency) {

        String[] parts = frequency.split(":", 2);

        FRFrequencyType frequencyType = FRFrequencyType.fromFrequencyString(parts[0]);
        Matcher matcher = frequencyType.getPattern().matcher(frequency);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Frequency '" + frequency + "' doesn't match regex '" + frequencyType.getPattern().pattern() + "'");
        }
        switch (frequencyType) {
            case INTERVALWEEKDAY:
                return nextIntervalWeekDay(previous, matcher);
            case WEEKINMONTHDAY: {
                return nextIntervalWeekDay(previous
                        .plusMonths(1)
                        .withDayOfMonth(1), matcher);
            }
            case INTERVALMONTHDAY:
                return nextIntervalMonthDay(previous, matcher);
            case QUARTERDAY:
                return nextQuarterDay(previous, matcher);
            case EVERYDAY:
                return nextDay(previous);
            case EVERYWORKINGDAY:
                return nextWorkingDay(previous);
            case INTERVALDAY:
                return nextIntervalDay(previous, matcher);
            default:
                throw new IllegalStateException("Frequency type should be defined at this state");
        }
    }

    private static DateTime nextWorkingDay(DateTime previous) {
        if (previous.dayOfWeek().get() >= DateTimeConstants.FRIDAY) {
            return previous.plusDays(7 - previous.dayOfWeek().get() + 1);
        } else {
            return nextDay(previous);
        }
    }

    private static DateTime nextDay(DateTime previous) {
        return previous.plusDays(1);
    }


    private static DateTime nextQuarterDay(DateTime previous, Matcher matcher) {
        String quarterStr = matcher.group(1);
        FRQuarterType quarterType = FRQuarterType.fromQuarterTypeString(quarterStr);

        DateTime candidateDate = previous;
        do {
            candidateDate = candidateDate.plusDays(1);
            //termination
            if (candidateDate.isAfter(previous.plusYears(1))) {
                throw new IllegalStateException("We should always find a quarter matching for date '" + previous + "'");
            }
        } while(!quarterType.matchOneQuarter(candidateDate));
        return candidateDate;
    }

    private static DateTime nextIntervalMonthDay(DateTime previous, Matcher matcher) {
        int monthIntv = Integer.valueOf(matcher.group(1));
        int daysIntv = Integer.valueOf(matcher.group(2));
        if (daysIntv < 0) {
            return previous
                    .plusMonths(monthIntv + 1)
                    .withDayOfMonth(1)
                    .plusDays(daysIntv);
        } else {
            return previous
                    .plusMonths(monthIntv)
                    .withDayOfMonth(daysIntv);
        }
    }

    private static DateTime nextIntervalWeekDay(DateTime previous, Matcher matcher) {
        int weekIntv = Integer.valueOf(matcher.group(1));
        int dayOfWeekIntv = Integer.valueOf(matcher.group(2));
        return previous
                .plusWeeks(weekIntv)
                .withDayOfWeek(dayOfWeekIntv);
    }

    private static DateTime nextIntervalDay(DateTime previous, Matcher matcher) {
        int dayIntv = Integer.valueOf(matcher.group(1));
        return previous.plusDays(dayIntv);
    }
}
