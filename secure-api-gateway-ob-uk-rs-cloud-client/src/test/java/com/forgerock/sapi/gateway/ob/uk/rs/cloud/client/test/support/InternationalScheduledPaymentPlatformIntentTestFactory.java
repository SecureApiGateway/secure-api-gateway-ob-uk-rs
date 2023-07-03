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

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRExternalRequestStatusCode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import uk.org.openbanking.datamodel.payment.OBReadRefundAccountEnum;

import java.util.TimeZone;
import java.util.UUID;

import static java.util.UUID.randomUUID;

/**
 * Test data factory for International Scheduled Payment Consent Details
 */
public class InternationalScheduledPaymentPlatformIntentTestFactory {

    public static final Gson gson = new Gson();

    public static JsonObject aValidScheduledPaymentPlatformIntent() {
        return aValidScheduledPaymentPlatformIntentBuilder(randomUUID().toString());
    }

    public static JsonObject aValidScheduledPaymentPlatformIntent(String consentId) {
        return aValidScheduledPaymentPlatformIntentBuilder(consentId);
    }

    public static JsonObject aValidScheduledPaymentPlatformIntent(String consentId, OBReadRefundAccountEnum obReadRefundAccountEnum) {
        return aValidOBScheduledPaymentPlatformIntentBuilder(consentId, obReadRefundAccountEnum);
    }

    public static JsonObject aValidScheduledPaymentPlatformIntent(String consentId, String clientId) {
        return aValidScheduledPaymentPlatformIntentBuilder(consentId, clientId);
    }

    public static JsonObject aValidOBScheduledPaymentPlatformIntent(String consentId, String clientId) {
        return aValidOBScheduledPaymentPlatformIntentBuilder(consentId, clientId);
    }

    public static JsonObject aValidScheduledPaymentPlatformIntentBuilder(String consentId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", consentId);
        final JsonObject obIntentObject = new JsonObject();
        obIntentObject.add("Data", aValidScheduledPaymentPlatformIntentDataBuilder(consentId, OBReadRefundAccountEnum.YES));
        consent.add("OBIntentObject", obIntentObject);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", randomUUID().toString());
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountId", UUID.randomUUID().toString());

        return consent;
    }

    public static JsonObject aValidOBScheduledPaymentPlatformIntentBuilder(String consentId, OBReadRefundAccountEnum obReadRefundAccountEnum) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", consentId);
        final JsonObject obIntentObject = new JsonObject();
        obIntentObject.add("Data", aValidScheduledPaymentPlatformIntentDataBuilder(consentId, obReadRefundAccountEnum));
        consent.add("OBIntentObject", obIntentObject);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", randomUUID().toString());
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountId", UUID.randomUUID().toString());

        return consent;
    }

    public static JsonObject aValidScheduledPaymentPlatformIntentBuilder(String consentId, String clientId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", consentId);
        final JsonObject obIntentObject = new JsonObject();
        obIntentObject.add("Data", aValidScheduledPaymentPlatformIntentDataBuilder(consentId, OBReadRefundAccountEnum.YES));
        consent.add("OBIntentObject", obIntentObject);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", clientId);
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountId", UUID.randomUUID().toString());
        return consent;
    }

    public static JsonObject aValidOBScheduledPaymentPlatformIntentBuilder(String consentId, String clientId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", consentId);
        final JsonObject obIntentObject = new JsonObject();
        obIntentObject.add("Data", aValidScheduledPaymentPlatformIntentDataBuilder(consentId, OBReadRefundAccountEnum.YES));
        consent.add("OBIntentObject", obIntentObject);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", clientId);
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountId", UUID.randomUUID().toString());
        return consent;
    }

    public static JsonObject aValidScheduledPaymentPlatformIntentDataBuilder(String consentId, OBReadRefundAccountEnum obReadRefundAccountEnum) {
        JsonObject data = new JsonObject();
        data.addProperty("ConsentId", consentId);
        data.addProperty("ReadRefundAccount", obReadRefundAccountEnum.getValue());
        data.addProperty("CreationDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("StatusUpdateDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("Status", FRExternalRequestStatusCode.AWAITINGAUTHORISATION.toString());
        data.add("Initiation", aValidFRWriteInternationalDataInitiationBuilder());
        data.add("Charges", aValidChargesBuilder());
        return data;
    }

    public static JsonObject aValidFRWriteInternationalDataInitiationBuilder() {
        JsonObject data = new JsonObject();
        data.addProperty("InstructionIdentification", "ACME412");
        data.addProperty("EndToEndIdentification", "FRESCO.21302.GFX.20");
        data.addProperty("LocalInstrument", UUID.randomUUID().toString());
        data.add("InstructedAmount", JsonParser.parseString("{ 'Amount': '819.91', 'Currency': 'GBP' }"));
        data.add("ChargeBearer", null);
        data.addProperty("CurrencyOfTransfer", "GBP");
        data.add("InstructedAmount", JsonParser.parseString("{ 'Amount': '819.91', 'Currency': 'GBP' }"));
        data.add("DebtorAccount", null);
        data.add("CreditorAccount", JsonParser.parseString("{" +
                "        'SchemeName': 'UK.OBIE.SortCodeAccountNumber'," +
                "        'Identification': '08080021325698'," +
                "        'Name': 'ACME Inc'," +
                "        'SecondaryIdentification': '0002'" +
                "      }"));
        data.add("Creditor", null);
        data.add("CreditorAgent", null);
        data.add("RemittanceInformation", JsonParser.parseString("{" +
                "        'Reference': 'FRESCO-101'," +
                "        'Unstructured': 'Internal ops code 5120101'" +
                "      }"));
        data.add("SupplementaryData", null);
        data.add("ExchangeRateInformation", null);
        return data;
    }

    public static JsonArray aValidChargesBuilder() {
        JsonArray charges = new JsonArray();
        charges.add(JsonParser.parseString("{" +
                "        'ChargeBearer': 'BorneByDebtor'," +
                "        'Type': 'UK.OBIE.CHAPSOut'," +
                "        'Amount': { 'Amount': '12.91', 'Currency': 'GBP' }" +
                "      }"));
        charges.add(JsonParser.parseString("{" +
                "        'ChargeBearer': 'BorneByDebtor'," +
                "        'Type': 'UK.OBIE.CHAPSOut'," +
                "        'Amount': { 'Amount': '8.2', 'Currency': 'USD' }" +
                "      }"));
        return charges;
    }
}
