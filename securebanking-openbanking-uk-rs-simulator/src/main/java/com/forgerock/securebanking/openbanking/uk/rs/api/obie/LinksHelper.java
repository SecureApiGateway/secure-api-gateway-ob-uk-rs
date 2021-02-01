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
package com.forgerock.securebanking.openbanking.uk.rs.api.obie;

import org.springframework.hateoas.Link;
import uk.org.openbanking.datamodel.account.Links;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * Helper class for creating the OB {@link Links} class in an HTTP response.
 */
public class LinksHelper {

    private static final String DOMESTIC_PAYMENTS = "domestic-payments";
    private static final String DOMESTIC_SCHEDULED_PAYMENTS = "domestic-scheduled-payments";
    private static final String DOMESTIC_STANDING_ORDER = "domestic-standing-orders";
    private static final String FILE_PAYMENTS = "file-payments";
    private static final String INTERNATIONAL_PAYMENTS = "international-payments";
    private static final String INTERNATIONAL_SCHEDULED_PAYMENTS = "international-scheduled-payments";
    private static final String INTERNATIONAL_STANDING_ORDER = "international-standing-orders";

    /**
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for a domestic payment.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createDomesticPaymentLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, DOMESTIC_PAYMENTS, id);
    }

    /**
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for a domestic scheduled
     * payment.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createDomesticScheduledPaymentLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, DOMESTIC_SCHEDULED_PAYMENTS, id);
    }

    /**
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for a domestic standing
     * order.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createDomesticStandingOrderPaymentLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, DOMESTIC_STANDING_ORDER, id);
    }

    /**
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for a payments file.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createFilePaymentsLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, FILE_PAYMENTS, id);
    }

    /**
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for an international
     * payment.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createInternationalPaymentLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, INTERNATIONAL_PAYMENTS, id);
    }

    /**
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for an international
     * scheduled payment.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createInternationalScheduledPaymentLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, INTERNATIONAL_SCHEDULED_PAYMENTS, id);
    }

    /**
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for a domestic standing
     * order.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createInternationalStandingOrderPaymentLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, INTERNATIONAL_STANDING_ORDER, id);
    }

    /**
     * Uses Spring HATEOAS to create an instance of the OB {@link Links} class with only the 'self' link populated.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param resourcePath The relative path of the resource to retrieve
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    private static Links createSelfLink(Class<?> controllerClass, String resourcePath, String id) {
        Link link = linkTo(controllerClass).slash(resourcePath).slash(id).withSelfRel();
        return new Links().self(link.getHref());
    }
}
