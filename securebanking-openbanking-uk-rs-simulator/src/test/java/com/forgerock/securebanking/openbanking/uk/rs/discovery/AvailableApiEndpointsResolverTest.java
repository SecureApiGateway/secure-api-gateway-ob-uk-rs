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
package com.forgerock.securebanking.openbanking.uk.rs.discovery;

import com.forgerock.securebanking.openbanking.uk.rs.api.obie.payment.v3_1_5.domesticpayments.DomesticPaymentsApiController;
import com.forgerock.securebanking.openbanking.uk.rs.common.OBApiReference;
import com.forgerock.securebanking.openbanking.uk.common.api.meta.OBGroupName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.forgerock.securebanking.openbanking.uk.rs.common.OBApiReference.CREATE_DOMESTIC_PAYMENT_CONSENT;
import static com.forgerock.securebanking.openbanking.uk.rs.common.OBApiReference.GET_DOMESTIC_PAYMENT_CONSENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Unit test for {@link AvailableApiEndpointsResolver}.
 */
public class AvailableApiEndpointsResolverTest {

    private static final String BASE_URL = "http://rs";
    private static final String VERSION = "v3.1.5";
    private static final String CREATE_CONSENT_URI = "/open-banking/" + VERSION + "/pisp/domestic-payment-consents";
    private static final String GET_CONSENT_URI = "/open-banking/" + VERSION + "/pisp/domestic-payment-consents/{ConsentId}";

    @Test
    public void shouldGetAvailableApiEndpoints() {
        // Given
        RequestMappingHandlerMapping requestHandlerMapping = requestHandlerMapping();
        AvailableApiEndpointsResolver endpointsResolver = new AvailableApiEndpointsResolver(requestHandlerMapping, BASE_URL);

        // When
        List<AvailableApiEndpoint> availableApiEndpoints = endpointsResolver.getAvailableApiEndpoints();

        // Then
        assertThat(availableApiEndpoints.size()).isEqualTo(2);
        AvailableApiEndpoint getConsentEndpoint = getEndpoint(availableApiEndpoints, GET_DOMESTIC_PAYMENT_CONSENT);
        assertThat(getConsentEndpoint.getVersion().equals(VERSION));
        assertThat(getConsentEndpoint.getGroupName().equals(OBGroupName.AISP));
        assertThat(getConsentEndpoint.getUrl().equals(BASE_URL + CREATE_CONSENT_URI));
        assertThat(getConsentEndpoint.getControllerMethod()).isNotNull();

        AvailableApiEndpoint createConsentEndpoint = getEndpoint(availableApiEndpoints, CREATE_DOMESTIC_PAYMENT_CONSENT);
        assertThat(createConsentEndpoint.getVersion().equals(VERSION));
        assertThat(createConsentEndpoint.getGroupName().equals(OBGroupName.AISP));
        assertThat(createConsentEndpoint.getUrl().equals(BASE_URL + GET_CONSENT_URI));
        assertThat(createConsentEndpoint.getControllerMethod()).isNotNull();
    }

    private RequestMappingHandlerMapping requestHandlerMapping() {
        RequestMappingHandlerMapping requestHandlerMapping = mock(RequestMappingHandlerMapping.class);
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = Map.of(
                createConsentMappingInfo(handlerMethod), handlerMethod,
                getConsentMappingInfo(handlerMethod), handlerMethod
        );
        given(requestHandlerMapping.getHandlerMethods()).willReturn(handlerMethods);
        return requestHandlerMapping;
    }

    private RequestMappingInfo createConsentMappingInfo(HandlerMethod handlerMethod) {
        Class<?> controllerClass = DomesticPaymentsApiController.class;
        BDDMockito.<Class<?>>given(handlerMethod.getBeanType()).willReturn(controllerClass);
        Method controllerMethod = Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(m -> m.getName().equals("createDomesticPayments"))
                .findFirst()
                .get();
        given(handlerMethod.getMethod()).willReturn(controllerMethod);
        return RequestMappingInfo
                .paths(CREATE_CONSENT_URI)
                .methods(RequestMethod.POST)
                .build();
    }

    private RequestMappingInfo getConsentMappingInfo(HandlerMethod handlerMethod) {
        Class<?> controllerClass = DomesticPaymentsApiController.class;
        BDDMockito.<Class<?>>given(handlerMethod.getBeanType()).willReturn(controllerClass);
        Method controllerMethod = Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(m -> m.getName().equals("getDomesticPaymentsDomesticPaymentId"))
                .findFirst()
                .get();
        given(handlerMethod.getMethod()).willReturn(controllerMethod);
        return RequestMappingInfo
                .paths(GET_CONSENT_URI)
                .methods(RequestMethod.GET)
                .build();
    }

    private AvailableApiEndpoint getEndpoint(List<AvailableApiEndpoint> apiEndpoints, OBApiReference apiReference) {
        return apiEndpoints.stream()
                .filter(a -> a.getApiReference().equals(apiReference))
                .findFirst()
                .get();
    }

}