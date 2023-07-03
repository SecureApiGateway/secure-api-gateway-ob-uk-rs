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
import com.forgerock.sapi.gateway.uk.common.shared.api.meta.forgerock.FRFrequencyType;
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
 * Test data factory for Domestic StandingOrders Consent Details
 */
public class DomesticStandingOrdersPlatformIntentTestFactory {

    public static final Gson gson = new Gson();

    public static JsonObject aValidDomesticStandingOrdersPlatformIntent() {
        return aValidDomesticStandingOrdersPlatformIntentBuilder(randomUUID().toString());
    }

    public static JsonObject aValidDomesticStandingOrdersPlatformIntent(String consentId) {
        return aValidDomesticStandingOrdersPlatformIntentBuilder(consentId);
    }

    public static JsonObject aValidDomesticStandingOrdersPlatformIntent(String consentId, OBReadRefundAccountEnum obReadRefundAccountEnum) {
        return aValidOBDomesticStandingOrdersPlatformIntentBuilder(consentId, obReadRefundAccountEnum);
    }

    public static JsonObject aValidDomesticStandingOrdersPlatformIntent(String consentId, String clientId) {
        return aValidDomesticStandingOrdersPlatformIntentBuilder(consentId, clientId);
    }

    public static JsonObject aValidOBDomesticStandingOrdersPlatformIntent(String consentId, String clientId) {
        return aValidOBDomesticStandingOrdersPlatformIntentBuilder(consentId, clientId);
    }

    public static JsonObject aValidDomesticStandingOrdersPlatformIntentBuilder(String consentId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", consentId);
        final JsonObject obIntentObject = new JsonObject();
        obIntentObject.add("Data", aValidDomesticStandingOrdersPlatformIntentDataBuilder(consentId, OBReadRefundAccountEnum.YES));
        consent.add("OBIntentObject", obIntentObject);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", randomUUID().toString());
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountId", UUID.randomUUID().toString());

        return consent;
    }

    public static JsonObject aValidOBDomesticStandingOrdersPlatformIntentBuilder(String consentId, OBReadRefundAccountEnum obReadRefundAccountEnum) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", consentId);
        final JsonObject obIntentObject = new JsonObject();
        obIntentObject.add("Data", aValidDomesticStandingOrdersPlatformIntentDataBuilder(consentId, obReadRefundAccountEnum));
        consent.add("OBIntentObject", obIntentObject);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", randomUUID().toString());
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountId", UUID.randomUUID().toString());

        return consent;
    }

    public static JsonObject aValidDomesticStandingOrdersPlatformIntentBuilder(String consentId, String clientId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", consentId);
        final JsonObject obIntentObject = new JsonObject();
        obIntentObject.add("Data", aValidDomesticStandingOrdersPlatformIntentDataBuilder(consentId, OBReadRefundAccountEnum.YES));
        consent.add("OBIntentObject", obIntentObject);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", clientId);
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountId", UUID.randomUUID().toString());
        return consent;
    }

    public static JsonObject aValidOBDomesticStandingOrdersPlatformIntentBuilder(String consentId, String clientId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", consentId);
        final JsonObject obIntentObject = new JsonObject();
        obIntentObject.add("Data", aValidDomesticStandingOrdersPlatformIntentDataBuilder(consentId, OBReadRefundAccountEnum.YES));
        consent.add("OBIntentObject", obIntentObject);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", clientId);
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountId", UUID.randomUUID().toString());
        return consent;
    }

    public static JsonObject aValidDomesticStandingOrdersPlatformIntentDataBuilder(String consentId, OBReadRefundAccountEnum obReadRefundAccountEnum) {
        JsonObject data = new JsonObject();
        String dateTimeNow = DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString();
        data.addProperty("ConsentId", consentId);
        data.addProperty("ReadRefundAccount", obReadRefundAccountEnum.getValue());
        data.addProperty("CreationDateTime", dateTimeNow);
        data.addProperty("StatusUpdateDateTime", dateTimeNow);
        data.addProperty("CutOffDateTime", dateTimeNow);
        data.addProperty("Status", FRExternalRequestStatusCode.AWAITINGAUTHORISATION.toString());
        data.add("Initiation", aValidFRWriteDomesticStandingOrderDataInitiationBuilder());
        data.add("Charges", aValidChargesBuilder());
        return data;
    }

    public static JsonObject aValidFRWriteDomesticStandingOrderDataInitiationBuilder() {
        JsonObject data = new JsonObject();
        String dateTimeNow = DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString();
        data.addProperty("FinalPaymentDateTime", dateTimeNow);
        data.addProperty("FirstPaymentDateTime", dateTimeNow);
        data.addProperty("Frequency", FRFrequencyType.EVERYDAY.getFrequencyStr());
        data.addProperty("NumberOfPayments", "10");
        data.addProperty("RecurringPaymentDateTime", dateTimeNow);
        data.addProperty("Reference", "PaymentREF");
        data.add("CreditorAccount", JsonParser.parseString("{'SchemeName': 'UK.OBIE.SortCodeAccountNumber'," +
                "'Identification': '08080021325698','Name': 'ACME Inc','SecondaryIdentification': '0002'}"));
        data.add("DebtorAccount", null);
        data.add("FinalPaymentAmount", JsonParser.parseString("{'Amount': '15.91', 'Currency': 'GBP'}"));
        data.add("FirstPaymentAmount", JsonParser.parseString("{'Amount': '10.91', 'Currency': 'GBP'}"));
        data.add("RecurringPaymentAmount", JsonParser.parseString("{'Amount': '12.91', 'Currency': 'GBP'}"));
        data.add("SupplementaryData", null);
        return data;
    }

    public static JsonArray aValidChargesBuilder() {
        JsonArray charges = new JsonArray();
        charges.add(JsonParser.parseString("{'ChargeBearer': 'BorneByDebtor'," +
                "'Type': 'UK.OBIE.CHAPSOut','Amount': { 'Amount': '12.91', 'Currency': 'GBP' }}"));
        charges.add(JsonParser.parseString("{'ChargeBearer': 'BorneByDebtor'," +
                "'Type': 'UK.OBIE.CHAPSOut','Amount': { 'Amount': '8.2', 'Currency': 'USD' }}"));
        return charges;
    }
}
