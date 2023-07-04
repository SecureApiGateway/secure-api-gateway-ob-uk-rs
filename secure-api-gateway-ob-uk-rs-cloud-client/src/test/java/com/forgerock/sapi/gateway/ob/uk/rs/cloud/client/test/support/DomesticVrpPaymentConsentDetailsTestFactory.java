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
package com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.test.support;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAccountIdentifier;
import com.forgerock.sapi.gateway.ob.uk.rs.cloud.client.ConsentStatusCode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import uk.org.openbanking.datamodel.common.OBVRPAuthenticationMethods;
import uk.org.openbanking.datamodel.common.OBVRPConsentType;
import uk.org.openbanking.datamodel.payment.OBReadRefundAccountEnum;

import java.util.TimeZone;
import java.util.UUID;

import static java.util.UUID.randomUUID;

/**
 * Test data factory for Domestic Vrp Payment Consent Details
 */
public class DomesticVrpPaymentConsentDetailsTestFactory {

    public static final String DEFAULT_ACCOUNT_ID = "01233243245676";
    public static final Gson gson = new Gson();

    public static JsonObject aValidDomesticVrpPaymentConsentDetails() {
        return aValidDomesticVrpPaymentConsentDetailsBuilder(randomUUID().toString(), OBVRPConsentType.SWEEPING);
    }

    public static JsonObject aValidDomesticVrpPaymentConsentDetails(String consentId) {
        return aValidDomesticVrpPaymentConsentDetailsBuilder(consentId, OBReadRefundAccountEnum.YES);
    }

    public static JsonObject aValidDomesticVrpPaymentConsentDetails(String consentId, OBReadRefundAccountEnum obReadRefundAccountEnum) {
        return aValidDomesticVrpPaymentConsentDetailsBuilder(consentId, obReadRefundAccountEnum);
    }

    public static JsonObject aValidDomesticVrpPaymentConsentDetails(String consentId, FRAccountIdentifier accountIdentifier) {
        return aValidDomesticVrpPaymentConsentDetailsBuilder(consentId, accountIdentifier);
    }

    public static JsonObject aValidDomesticVrpPaymentConsentDetails(String consentId, OBVRPConsentType vrpType) {
        return aValidDomesticVrpPaymentConsentDetailsBuilder(consentId, vrpType);
    }

    public static JsonObject aValidDomesticVrpPaymentConsentDetails(String consentId, String clientId) {
        return aValidDomesticVrpPaymentConsentDetailsBuilder(consentId, clientId);
    }

    public static JsonObject aValidDomesticVrpPaymentConsentDetails(String consentId, String clientId, OBVRPConsentType vrpType) {
        return aValidDomesticVrpPaymentConsentDetailsBuilder(consentId, clientId, vrpType);
    }

    public static JsonObject aValidDomesticVrpPaymentConsentDetailsBuilder(String consentId, OBReadRefundAccountEnum obReadRefundAccountEnum) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", UUID.randomUUID().toString());
        final JsonObject obIntent = new JsonObject();
        obIntent.add("Data", aValidDomesticVrpPaymentConsentDataDetailsBuilder(consentId, obReadRefundAccountEnum));
        consent.add("OBIntentObject", obIntent);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", randomUUID().toString());
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountId", DEFAULT_ACCOUNT_ID);
        return consent;
    }

    public static JsonObject aValidDomesticVrpPaymentConsentDetailsBuilder(String consentId, FRAccountIdentifier accountIdentifier) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", UUID.randomUUID().toString());
        final JsonObject obIntent = new JsonObject();
        obIntent.add("Data", aValidDomesticVrpPaymentConsentDataDetailsBuilder(consentId, accountIdentifier));
        consent.add("OBIntentObject", obIntent);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", randomUUID().toString());
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountId", accountIdentifier.getAccountId());
        return consent;
    }

    public static JsonObject aValidDomesticVrpPaymentConsentDetailsBuilder(String consentId, OBVRPConsentType vrpType) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", UUID.randomUUID().toString());
        final JsonObject obIntent = new JsonObject();
        obIntent.add("Data", aValidDomesticVrpPaymentConsentDataDetailsBuilder(consentId, vrpType));
        consent.add("OBIntentObject", obIntent);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", randomUUID().toString());
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountId", DEFAULT_ACCOUNT_ID);
        return consent;
    }

    public static JsonObject aValidDomesticVrpPaymentConsentDetailsBuilder(String consentId, String clientId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", UUID.randomUUID().toString());
        final JsonObject obIntent = new JsonObject();
        obIntent.add("Data", aValidDomesticVrpPaymentConsentDataDetailsBuilder(consentId, OBReadRefundAccountEnum.YES));
        consent.add("OBIntentObject", obIntent);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", clientId);
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountId", DEFAULT_ACCOUNT_ID);

        return consent;
    }

    public static JsonObject aValidDomesticVrpPaymentConsentDetailsBuilder(String consentId, String clientId, OBVRPConsentType vrpType) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", UUID.randomUUID().toString());
        final JsonObject obIntent = new JsonObject();
        obIntent.add("Data", aValidDomesticVrpPaymentConsentDataDetailsBuilder(consentId, vrpType));
        consent.add("OBIntentObject", obIntent);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", clientId);
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountId", DEFAULT_ACCOUNT_ID);

        return consent;
    }

    public static JsonObject aValidDomesticVrpPaymentConsentDataDetailsBuilder(String consentId, OBReadRefundAccountEnum obReadRefundAccountEnum) {
        JsonObject data = new JsonObject();
        data.addProperty("ConsentId", consentId);
        data.addProperty("ReadRefundAccount", obReadRefundAccountEnum.getValue());
        data.addProperty("CreationDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("StatusUpdateDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("Status", ConsentStatusCode.AWAITINGAUTHORISATION.toString());
        data.add("ControlParameters", aValidDomesticVrpPaymentControlParametersBuilder(OBVRPConsentType.SWEEPING));
        data.add("Initiation", aValidFRWriteDomesticVrpDataInitiationBuilder());
        return data;
    }

    public static JsonObject aValidDomesticVrpPaymentConsentDataDetailsBuilder(String consentId, FRAccountIdentifier accountIdentifier) {
        JsonObject data = new JsonObject();
        data.addProperty("ConsentId", consentId);
        data.addProperty("ReadRefundAccount", OBReadRefundAccountEnum.YES.getValue());
        data.addProperty("CreationDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("StatusUpdateDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("Status", ConsentStatusCode.AWAITINGAUTHORISATION.toString());
        data.add("ControlParameters", aValidDomesticVrpPaymentControlParametersBuilder(OBVRPConsentType.SWEEPING));
        data.add("Initiation", aValidFRWriteDomesticVrpDataInitiationBuilder(accountIdentifier));
        return data;
    }

    public static JsonObject aValidDomesticVrpPaymentConsentDataDetailsBuilder(String consentId, OBVRPConsentType vrpType) {
        JsonObject data = new JsonObject();
        data.addProperty("ConsentId", consentId);
        data.addProperty("CreationDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("StatusUpdateDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("Status", ConsentStatusCode.AWAITINGAUTHORISATION.toString());
        data.add("ControlParameters", aValidDomesticVrpPaymentControlParametersBuilder(vrpType));
        data.add("Initiation", aValidFRWriteDomesticVrpDataInitiationBuilder());
        return data;
    }

    public static JsonObject aValidDomesticVrpPaymentControlParametersBuilder(OBVRPConsentType vrpType) {
        JsonObject controlParameters = new JsonObject();
        controlParameters.addProperty("ValidFromDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        controlParameters.addProperty("ValidToDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        controlParameters.add(
                "MaximumIndividualAmount",
                JsonParser.parseString("{ \"Amount\": \"10.01\", \"Currency\": \"GBP\" }")
        );
        controlParameters.add(
                "PeriodicLimits",
                JsonParser.parseString("[{\n" +
                        "                        \"PeriodType\": \"Month\",\n" +
                        "                        \"PeriodAlignment\": \"Calendar\",\n" +
                        "                        \"Amount\": \"10.01\",\n" +
                        "                        \"Currency\": \"GBP\"\n" +
                        "                    }]")
        );
        controlParameters.add("VRPType", JsonParser.parseString("[" + vrpType.getValue() + "]"));
        controlParameters.add("PSUAuthenticationMethods", JsonParser.parseString("[" + OBVRPAuthenticationMethods.SCA_NOT_REQUIRED.getValue() + "]"));
        controlParameters.add("PSUInteractionTypes", JsonParser.parseString("[InSession]"));
        controlParameters.add("SupplementaryData", null);
        return controlParameters;
    }

    public static JsonObject aValidFRWriteDomesticVrpDataInitiationBuilder() {
        JsonObject data = new JsonObject();
        data.add("DebtorAccount", JsonParser.parseString("{" +
                "        'SchemeName': 'UK.OBIE.SortCodeAccountNumber'," +
                "        'Identification': '08080021325698'," +
                "        'Name': 'ACME Inc'," +
                "        'SecondaryIdentification': '0002'" +
                "      }"));
        data.add("CreditorAccount", JsonParser.parseString("{" +
                "        'SchemeName': 'UK.OBIE.SortCodeAccountNumber'," +
                "        'Identification': '08080021325698'," +
                "        'Name': 'ACME Inc'," +
                "        'SecondaryIdentification': '0002'" +
                "      }"));
        data.add("CreditorPostalAddress", null);
        data.add("RemittanceInformation", JsonParser.parseString("{" +
                "   'Unstructured':'Internal ops code 5120101'," +
                "   'Reference':'FRESCO-101'" +
                "}"));
        return data;
    }

    public static JsonObject aValidFRWriteDomesticVrpDataInitiationBuilder(FRAccountIdentifier accountIdentifier) {
        JsonObject data = new JsonObject();
        String debtorAccount = "{'SchemeName': '" + accountIdentifier.getSchemeName() + "',"
                + "'Identification': '" + accountIdentifier.getIdentification() + "',"
                + "'Name': '" + accountIdentifier.getName() + "',"
                + "'SecondaryIdentification': '0002'"
                + "}";
        data.add("DebtorAccount", JsonParser.parseString(debtorAccount));
        data.add("CreditorAccount", JsonParser.parseString("{" +
                "        'SchemeName': 'UK.OBIE.SortCodeAccountNumber'," +
                "        'Identification': '08080021325698'," +
                "        'Name': 'ACME Inc'," +
                "        'SecondaryIdentification': '0002'" +
                "      }"));
        data.add("CreditorPostalAddress", null);
        data.add("RemittanceInformation", JsonParser.parseString("{" +
                "   'Unstructured':'Internal ops code 5120101'," +
                "   'Reference':'FRESCO-101'" +
                "}"));
        return data;
    }
}
