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
package com.forgerock.sapi.gateway.ob.uk.rs.server.common.util.link;

import org.springframework.hateoas.Link;
import uk.org.openbanking.datamodel.common.Links;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * Helper class for creating the OB {@link Links} class in an HTTP response.
 */
public class LinksHelper {

    private static final String ACCOUNT_ACCESS_CONSENTS = "account-access-consents";
    private static final String ACCOUNTS = "accounts";
    private static final String DOMESTIC_PAYMENT_CONSENTS = "domestic-payment-consents";
    private static final String DOMESTIC_PAYMENTS = "domestic-payments";
    private static final String DOMESTIC_PAYMENTS_DETAILS = "payment-details";
    private static final String DOMESTIC_SCHEDULED_PAYMENT_CONSENTS = "domestic-scheduled-payment-consents";
    private static final String DOMESTIC_SCHEDULED_PAYMENTS = "domestic-scheduled-payments";
    private static final String DOMESTIC_STANDING_ORDER = "domestic-standing-orders";
    private static final String FILE_PAYMENTS = "file-payments";
    private static final String INTERNATIONAL_PAYMENTS = "international-payments";
    private static final String INTERNATIONAL_SCHEDULED_PAYMENTS = "international-scheduled-payments";
    private static final String INTERNATIONAL_STANDING_ORDER = "international-standing-orders";
    private static final String CALLBACK_URLS = "callback-urls";
    private static final String EVENT_SUBSCRIPTIONS = "event-subscriptions";
    private static final String FUNDS_CONFIRMATION = "funds-confirmation";
    private static final String FUNDS_CONFIRMATIONS = "funds-confirmations";
    private static final String DOMESTIC_VRP_PAYMENTS = "domestic-vrps";
    private static final String DOMESTIC_VRP_CONSENTS = "domestic-vrp-consents";


    /**
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for a domestic payments consent funds confirmation.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createDomesticPaymentConsentsFundsConfirmationLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, DOMESTIC_PAYMENT_CONSENTS, id, FUNDS_CONFIRMATION);
    }

    public static Links createDomesticPaymentConsentsLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, DOMESTIC_PAYMENT_CONSENTS, id);
    }

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
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for a domestic payment details.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createDomesticPaymentDetailsLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, DOMESTIC_PAYMENTS, id, DOMESTIC_PAYMENTS_DETAILS);
    }


    public static Links createDomesticScheduledPaymentConsentsLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, DOMESTIC_SCHEDULED_PAYMENT_CONSENTS, id);
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
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for a domestic payment details.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createDomesticScheduledPaymentDetailsLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, DOMESTIC_SCHEDULED_PAYMENTS, id, DOMESTIC_PAYMENTS_DETAILS);
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
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for a domestic standing
     * order payment details.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createDomesticStandingOrderPaymentDetailsLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, DOMESTIC_STANDING_ORDER, id, DOMESTIC_PAYMENTS_DETAILS);
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
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for an international payment
     * details.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createInternationalPaymentDetailsLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, INTERNATIONAL_PAYMENTS, id, DOMESTIC_PAYMENTS_DETAILS);
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
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for an international scheduled payment
     * details.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createInternationalScheduledPaymentDetailsLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, INTERNATIONAL_SCHEDULED_PAYMENTS, id, DOMESTIC_PAYMENTS_DETAILS);
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
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for an international standing
     * order payment details.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createInternationalStandingOrderPaymentDetailsLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, INTERNATIONAL_STANDING_ORDER, id, DOMESTIC_PAYMENTS_DETAILS);
    }

    /**
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for a domestic VRP payment.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createDomesticVrpPaymentLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, DOMESTIC_VRP_PAYMENTS, id);
    }

    /**
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for a domestic VRP consent.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createDomesticVrpConsentLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, DOMESTIC_VRP_CONSENTS, id);
    }

    /**
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for returning callback URLs.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createCallbackUrlsSelfLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, CALLBACK_URLS, id);
    }

    /**
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for returning callback URLs.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createCallbackUrlsResourcesLink(Class<?> controllerClass) {
        return createResourcesLink(controllerClass, CALLBACK_URLS);
    }

    /**
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for an event subscription.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createEventSubscriptionSelfLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, EVENT_SUBSCRIPTIONS, id);
    }

    /**
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for the event subscription
     * resources.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createEventSubscriptionResourcesLink(Class<?> controllerClass) {
        return createResourcesLink(controllerClass, EVENT_SUBSCRIPTIONS);
    }

    /**
     * Creates an instance of the OB {@link Links} class with only the 'self' link populated for a funds confirmation
     * resource.
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    public static Links createFundsConfirmationSelfLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, FUNDS_CONFIRMATIONS, id);
    }


    public static Links createAccountAccessConsentsSelfLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, ACCOUNT_ACCESS_CONSENTS, id);
    }

    public static Links createGetAccountsSelfLink(Class<?> controllerClass) {
        return createResourcesLink(controllerClass, ACCOUNTS);
    }

    public static Links createGetAccountsSelfLink(Class<?> controllerClass, String id) {
        return createSelfLink(controllerClass, ACCOUNTS, id);
    }

    /**
     * Uses Spring HATEOAS to create an instance of the OB {@link Links} class with the 'self' link pointing to a
     * specific resource (e.g. /callback-urls/{id}).
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param resourcePath The relative path of the resource to retrieve
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    private static Links createSelfLink(Class<?> controllerClass, String resourcePath, String id) {
        Link link = linkTo(controllerClass).slash(resourcePath).slash(id).withSelfRel();
        return new Links().self(link.toUri());
    }

    /**
     * Uses Spring HATEOAS to create an instance of the OB {@link Links} class with the 'self' link pointing to a
     * specific resource (e.g. /callback-urls/{id}).
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param resourcePath The relative path of the resource to retrieve
     * @param id The ID of the resource concerned.
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    private static Links createSelfLink(Class<?> controllerClass, String resourcePath, String id, String type) {
        Link link = linkTo(controllerClass).slash(resourcePath).slash(id).slash(type).withSelfRel();
        return new Links().self(link.toUri());
    }

    /**
     * Uses Spring HATEOAS to create an instance of the OB {@link Links} class with the self link pointing to a
     * resource's base path (e.g. /callback-urls).
     *
     * @param controllerClass The controller class that is responsible for handling the self link.
     * @param resourcePath The relative path of the resource to retrieve
     * @return The {@link Links} instance with the populated 'self' URL.
     */
    private static Links createResourcesLink(Class<?> controllerClass, String resourcePath) {
        Link link = linkTo(controllerClass).slash(resourcePath).withSelfRel();
        return new Links().self(link.toUri());
    }
}
