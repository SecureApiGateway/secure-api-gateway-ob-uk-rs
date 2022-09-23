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
package com.forgerock.securebanking.rs.platform.client.utils.url;

import com.forgerock.securebanking.openbanking.uk.common.api.meta.share.IntentType;
import com.forgerock.securebanking.rs.platform.client.exceptions.ErrorType;
import com.forgerock.securebanking.rs.platform.client.exceptions.ExceptionClient;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.forgerock.securebanking.rs.platform.client.utils.url.UrlContext.INTENT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

/**
 * Unit test for {@link UrlContext}
 */
public class UrlContextTest {

    private static final String CONTEXT_PATH = "/repo/context";
    @Test
    public void shouldReplaceIntentId() throws ExceptionClient {
        // Given
        String testContext = CONTEXT_PATH + INTENT_ID;
        String intentId = IntentType.PAYMENT_DOMESTIC_CONSENT.generateIntentId();

        // When
        String replaced = UrlContext.replaceParameterContextIntentId(testContext, intentId);

        //Then
        assertThat(replaced).isEqualTo(CONTEXT_PATH + intentId);
    }

    @Test
    public void shouldGetUnknownIntentType() {
        // Given
        String testContext = CONTEXT_PATH + INTENT_ID;
        // No intentTypeClient prefix
        String intentId = UUID.randomUUID().toString();

        // When
        ExceptionClient exception = catchThrowableOfType(() -> UrlContext.replaceParameterContextIntentId(testContext, intentId), ExceptionClient.class);

        //Then
        assertThat(exception.getErrorClient().getErrorType()).isEqualTo(ErrorType.UNKNOWN_INTENT_TYPE);
        assertThat(exception.getErrorClient().getErrorType().getInternalCode()).isEqualTo(ErrorType.UNKNOWN_INTENT_TYPE.getInternalCode());
    }

    @Test
    public void shouldGetParameterErrorIntentContext() {
        // When
        ExceptionClient exception = catchThrowableOfType(() -> UrlContext.replaceParameterContextIntentId(null, null), ExceptionClient.class);

        //Then
        assertThat(exception.getErrorClient().getErrorType()).isEqualTo(ErrorType.PARAMETER_ERROR);
        assertThat(exception.getErrorClient().getErrorType().getInternalCode()).isEqualTo(ErrorType.PARAMETER_ERROR.getInternalCode());
    }

    @Test
    public void shouldGetParameterErrorContext() {
        // When
        ExceptionClient exception = catchThrowableOfType(() -> UrlContext.replaceParameterContextValue(null, null, null), ExceptionClient.class);

        //Then
        assertThat(exception.getErrorClient().getErrorType()).isEqualTo(ErrorType.PARAMETER_ERROR);
    }

}
