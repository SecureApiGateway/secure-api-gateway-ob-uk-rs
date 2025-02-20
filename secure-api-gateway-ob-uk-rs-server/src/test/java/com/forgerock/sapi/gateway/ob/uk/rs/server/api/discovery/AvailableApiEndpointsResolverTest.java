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
///*
// * Copyright © 2020-2024 ForgeRock AS (obst@forgerock.com)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.forgerock.sapi.gateway.ob.uk.rs.server.api.discovery;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.util.List;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import com.forgerock.sapi.gateway.ob.uk.rs.server.common.OBApiReference;
//import com.forgerock.sapi.gateway.uk.common.shared.api.meta.obie.OBGroupName;
//
///**
// * Unit test for {@link AvailableApiEndpointsResolver}.
// */
//@SpringBootTest
//@ActiveProfiles("test")
//public class AvailableApiEndpointsResolverTest {
//
//    private static final String VERSION = "v3.1.10";
//    private static final String CREATE_PAYMENT_URI = "/open-banking/" + VERSION + "/pisp/domestic-payments";
//    private static final String GET_PAYMENT_URI = "/open-banking/" + VERSION + "/pisp/domestic-payments/{DomesticPaymentId}";
//
//    @Autowired
//    private AvailableApiEndpointsResolver availableApiEndpointsResolver;
//
//    @Test
//    public void shouldGetAvailableApiEndpoints() {
//        // Get all available endpoints
//        List<AvailableApiEndpoint> availableApiEndpoints = availableApiEndpointsResolver.getAvailableApiEndpoints();
//
//        assertThat(availableApiEndpoints.size()).isGreaterThan(0);
//
//        // Validate some example endpoints
//        AvailableApiEndpoint getPaymentEndpoint = getEndpoint(availableApiEndpoints, OBApiReference.GET_DOMESTIC_PAYMENT, VERSION);
//        assertThat(getPaymentEndpoint.getVersion()).isEqualTo(VERSION);
//        assertThat(getPaymentEndpoint.getGroupName()).isEqualTo(OBGroupName.PISP);
//        assertThat(getPaymentEndpoint.getUriPath()).isEqualTo( GET_PAYMENT_URI);
//        assertThat(getPaymentEndpoint.getControllerMethod()).isNotNull();
//
//        AvailableApiEndpoint createPaymentEndpoint = getEndpoint(availableApiEndpoints, OBApiReference.CREATE_DOMESTIC_PAYMENT, VERSION);
//        assertThat(createPaymentEndpoint.getVersion()).isEqualTo(VERSION);
//        assertThat(createPaymentEndpoint.getGroupName()).isEqualTo(OBGroupName.PISP);
//        assertThat(createPaymentEndpoint.getUriPath()).isEqualTo(CREATE_PAYMENT_URI);
//        assertThat(createPaymentEndpoint.getControllerMethod()).isNotNull();
//    }
//
//    private AvailableApiEndpoint getEndpoint(List<AvailableApiEndpoint> apiEndpoints, OBApiReference apiReference, String version) {
//        return apiEndpoints.stream()
//                .filter(a -> a.getApiReference().equals(apiReference) && a.getVersion().equals(version))
//                .findFirst()
//                .get();
//    }
//
//}