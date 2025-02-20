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
package com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.utils.jwt;

import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ErrorType;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.exceptions.ExceptionClient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

public class JwtUtilTest {

    private static final String JWT = "eyJ0eXAiOiJKV1QiLCJraWQiOiJkVVJiaDVOcWtOUTBnY3NmZG91MzJBWDR5T1k9IiwiYWxnIjoiU" +
            "FMyNTYifQ.eyJzdWIiOiI2MzI5MmNjMS1jYmJhLTQyODQtYjAzOC1jNWNiYzYwOTg1NmIiLCJjdHMiOiJPQVVUSDJfU1RBVEVMRVNTX" +
            "0dSQU5UIiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiNGE3ODBjY2QtMmUxYi00NWUwLTg5ZDUtMWIxZjU4MTMwZTU4" +
            "LTEzMjk3NyIsInN1Ym5hbWUiOiI2MzI5MmNjMS1jYmJhLTQyODQtYjAzOC1jNWNiYzYwOTg1NmIiLCJpc3MiOiJodHRwczovL2lhbS5" +
            "kZXYuZm9yZ2Vyb2NrLmZpbmFuY2lhbC9hbS9vYXV0aDIvcmVhbG1zL3Jvb3QvcmVhbG1zL2FscGhhIiwidG9rZW5OYW1lIjoiYWNjZX" +
            "NzX3Rva2VuIiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImF1dGhHcmFudElkIjoiT1lUbWtTMVlzLXNDZHB3NXBHOV9Kei14ejFjIiwib" +
            "m9uY2UiOiIxMGQyNjBiZi1hN2Q5LTQ0NGEtOTJkOS03YjdhNWYwODgyMDgiLCJhdWQiOiIzN2M3MjIwNC1iZWZmLTQwMmMtOWYzOC1i" +
            "MzUzYzBjZmE5MWEiLCJuYmYiOjE2NjM2ODEzMjMsImdyYW50X3R5cGUiOiJhdXRob3JpemF0aW9uX2NvZGUiLCJzY29wZSI6WyJvcGV" +
            "uaWQiLCJwYXltZW50cyJdLCJhdXRoX3RpbWUiOjE2NjM2ODEyODcsImNsYWltcyI6IntcImlkX3Rva2VuXCI6e1wiYWNyXCI6e1widm" +
            "FsdWVcIjpcInVybjpvcGVuYmFua2luZzpwc2QyOmNhXCIsXCJlc3NlbnRpYWxcIjp0cnVlfSxcIm9wZW5iYW5raW5nX2ludGVudF9pZ" +
            "FwiOntcInZhbHVlXCI6XCJQRENfYTU3NmM0ZGItYjVlZS00N2FiLThlOTctZGZlZDM4MGVhZjUxXCIsXCJlc3NlbnRpYWxcIjp0cnVl" +
            "fX19IiwicmVhbG0iOiIvYWxwaGEiLCJjbmYiOnsieDV0I1MyNTYiOiJUczM2M1JQQ2l6LUtMREU0V01FVGlSQm9PZlRQSVItZklsQ2t" +
            "SWEM3TUhzIn0sImV4cCI6MTY2NDA0MTMyMywiaWF0IjoxNjYzNjgxMzIzLCJleHBpcmVzX2luIjozNjAwMDAsImp0aSI6Img3UDRUOX" +
            "kzN2ZvTGktMHVBd0ttdHRIRVFrZyJ9.S5DGry3UIFV_RCFrG7Z850v_HVpJVVh1BG2sd7XBlrTGznuYaERNB0LutZkvrtvS_g_cvFAc" +
            "-sjrtV2f4y8cq3tak-z_z_HqUno105sywWyYT4YP3MX7Cyk35FG78mMiPKCVigFHVmygktM8wuwgG-Sw_HgESk3GgmzQ7qbPMp2t6UJ" +
            "99x9tSsP7klRdXr6Dvn-3vltxbJaGSnLNkX1Edlm3FrI0TZhJRMHgeR1DbiERch-pVmHq4ri9P4ChlTf2olTefBIhnyGclwGBVyXw2N" +
            "sCVuUMPZ507dKFBncs24bazh0tryOVr0HEuIgmsoDJqm1hmb6KcDimhIYisRGkpw";

    private static final String AUD_JWT = "37c72204-beff-402c-9f38-b353c0cfa91a";

    @Test
    public void shouldGetAUDClaim() throws ExceptionClient {
        // When
        List<String> audiences = JwtUtil.getAudiences(JWT);
        // Then
        assertThat(audiences).isNotEmpty().containsExactly(AUD_JWT);
    }

    @Test
    public void shouldGetAParseException() {
        // When
        ExceptionClient exception = catchThrowableOfType(
                () -> JwtUtil.getAudiences("asdfasdfasdgfaefacsecasefasefasef"), ExceptionClient.class
        );

        //Then
        assertThat(exception.getErrorClient().getErrorType()).isEqualTo(ErrorType.JWT_INVALID);
        assertThat(exception.getErrorClient().getErrorType().getInternalCode()).isEqualTo(ErrorType.JWT_INVALID.getInternalCode());
    }

}
