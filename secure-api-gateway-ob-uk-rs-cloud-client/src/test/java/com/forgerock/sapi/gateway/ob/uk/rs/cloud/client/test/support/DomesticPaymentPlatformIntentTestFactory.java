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
import uk.org.openbanking.datamodel.common.OBActiveOrHistoricCurrencyAndAmount;
import uk.org.openbanking.datamodel.common.OBChargeBearerType1Code;
import uk.org.openbanking.datamodel.payment.OBReadRefundAccountEnum;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse3DataCharges;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse4DataCharges;
import uk.org.openbanking.datamodel.payment.OBWriteDomesticConsentResponse5DataCharges;

import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static java.util.UUID.randomUUID;

/**
 * Test data factory for Domestic Payment Consent Details
 */
public class DomesticPaymentPlatformIntentTestFactory {

    public static final Gson gson = new Gson();

    public static JsonObject aValidDomesticPaymentPlatformIntent() {
        return aValidDomesticPaymentPlatformIntentBuilder(randomUUID().toString());
    }

    public static JsonObject aValidDomesticPaymentPlatformIntent(String consentId) {
        return aValidDomesticPaymentPlatformIntentBuilder(consentId);
    }

    public static JsonObject aValidDomesticPaymentPlatformIntent(String consentId, OBReadRefundAccountEnum obReadRefundAccountEnum) {
        return aValidOBDomesticPaymentPlatformIntentBuilder(consentId, obReadRefundAccountEnum);
    }

    public static JsonObject aValidDomesticPaymentPlatformIntent(String consentId, String clientId) {
        return aValidDomesticPaymentPlatformIntentBuilder(consentId, clientId);
    }

    public static JsonObject aValidOBDomesticPaymentPlatformIntent(String consentId, String clientId) {
        return aValidOBDomesticPaymentPlatformIntentBuilder(consentId, clientId);
    }

    public static JsonObject aValidDomesticPaymentPlatformIntentBuilder(String consentId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", consentId);
        final JsonObject obIntentObject = new JsonObject();
        obIntentObject.add("Data",  aValidDomesticPaymentPlatformIntentDataBuilder(consentId, OBReadRefundAccountEnum.YES));
        consent.add("OBIntentObject", obIntentObject);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", randomUUID().toString());
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountId", UUID.randomUUID().toString());

        return consent;
    }

    public static JsonObject aValidOBDomesticPaymentPlatformIntentBuilder(String consentId, OBReadRefundAccountEnum obReadRefundAccountEnum) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", consentId);
        final JsonObject obIntentObject = new JsonObject();
        obIntentObject.add("Data",  aValidDomesticPaymentPlatformIntentDataBuilder(consentId, obReadRefundAccountEnum));
        consent.add("OBIntentObject", obIntentObject);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", randomUUID().toString());
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountId", UUID.randomUUID().toString());

        return consent;
    }

    public static JsonObject aValidDomesticPaymentPlatformIntentBuilder(String consentId, String clientId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", consentId);
        final JsonObject obIntentObject = new JsonObject();
        obIntentObject.add("Data",  aValidDomesticPaymentPlatformIntentDataBuilder(consentId, OBReadRefundAccountEnum.YES));
        consent.add("OBIntentObject", obIntentObject);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", clientId);
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountId", UUID.randomUUID().toString());
        return consent;
    }

    public static JsonObject aValidOBDomesticPaymentPlatformIntentBuilder(String consentId, String clientId) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", consentId);
        final JsonObject obIntentObject = new JsonObject();
        obIntentObject.add("Data",  aValidDomesticPaymentPlatformIntentDataBuilder(consentId, OBReadRefundAccountEnum.YES));
        consent.add("OBIntentObject", obIntentObject);
        consent.add("resourceOwnerUsername", null);
        consent.addProperty("oauth2ClientId", clientId);
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty("accountId", UUID.randomUUID().toString());
        return consent;
    }

    public static JsonObject aValidDomesticPaymentPlatformIntentDataBuilder(String consentId, OBReadRefundAccountEnum obReadRefundAccountEnum) {
        JsonObject data = new JsonObject();
        data.addProperty("ConsentId", consentId);
        data.addProperty("ReadRefundAccount", obReadRefundAccountEnum.getValue());
        data.addProperty("CreationDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("StatusUpdateDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("Status", FRExternalRequestStatusCode.AWAITINGAUTHORISATION.toString());
        data.add("Initiation", aValidFRWriteDomesticDataInitiationBuilder());
        data.add("Charges", aValidChargesBuilder());
        return data;
    }

    public static JsonObject aValidFRWriteDomesticDataInitiationBuilder() {
        JsonObject data = new JsonObject();
        data.addProperty("InstructionIdentification", "ACME412");
        data.addProperty("EndToEndIdentification", "FRESCO.21302.GFX.20");
        data.addProperty("LocalInstrument", UUID.randomUUID().toString());
        data.add("InstructedAmount", JsonParser.parseString("{ \"Amount\": '819.91', \"Currency\": 'GBP' }"));
        data.add("DebtorAccount", null);
        data.add("CreditorAccount", JsonParser.parseString("{\n" +
                "        \"SchemeName\": \"UK.OBIE.SortCodeAccountNumber\",\n" +
                "        \"Identification\": \"08080021325698\",\n" +
                "        \"Name\": \"ACME Inc\",\n" +
                "        \"SecondaryIdentification\": \"0002\"\n" +
                "      }"));
        data.add("CreditorPostalAddress", null);
        data.add("RemittanceInformation", JsonParser.parseString("{\n" +
                "        \"Reference\": \"FRESCO-101\",\n" +
                "        \"Unstructured\": \"Internal ops code 5120101\"\n" +
                "      }"));
        data.add("SupplementaryData", null);
        return data;
    }

    public static JsonArray aValidChargesBuilder() {
        JsonArray charges = new JsonArray();
        charges.add(JsonParser.parseString("{\n" +
                "        \"ChargeBearer\": \"BorneByDebtor\",\n" +
                "        \"Type\": \"UK.OBIE.CHAPSOut\",\n" +
                "        \"Amount\": { \"Amount\": '12.91', \"Currency\": 'GBP' }" +
                "      }"));
        charges.add(JsonParser.parseString("{\n" +
                "        \"ChargeBearer\": \"BorneByDebtor\",\n" +
                "        \"Type\": \"UK.OBIE.CHAPSOut\",\n" +
                "        \"Amount\": { \"Amount\": '8.2', \"Currency\": 'USD' }" +
                "      }"));
        return charges;
    }

    public static List<OBWriteDomesticConsentResponse3DataCharges> aValidOBWriteDomesticConsentResponse3DataCharges() {
        return List.of(
                new OBWriteDomesticConsentResponse3DataCharges().
                        chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                        .type("UK.OBIE.CHAPSOut")
                        .amount(
                                new OBActiveOrHistoricCurrencyAndAmount().amount("1.5").currency("GBP")
                        ),
                new OBWriteDomesticConsentResponse3DataCharges().
                        chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                        .type("UK.OBIE.CHAPSOut")
                        .amount(
                                new OBActiveOrHistoricCurrencyAndAmount().amount("1.5").currency("GBP")
                        )
        );
    }

    public static List<OBWriteDomesticConsentResponse4DataCharges> aValidOBWriteDomesticConsentResponse4DataCharges() {
        return List.of(
                new OBWriteDomesticConsentResponse4DataCharges().
                        chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                        .type("UK.OBIE.CHAPSOut")
                        .amount(
                                new OBActiveOrHistoricCurrencyAndAmount().amount("1.5").currency("GBP")
                        ),
                new OBWriteDomesticConsentResponse4DataCharges().
                        chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                        .type("UK.OBIE.CHAPSOut")
                        .amount(
                                new OBActiveOrHistoricCurrencyAndAmount().amount("1.5").currency("GBP")
                        )
        );
    }

    public static List<OBWriteDomesticConsentResponse5DataCharges> aValidOBWriteDomesticConsentResponse5DataCharges() {
        return List.of(
                new OBWriteDomesticConsentResponse5DataCharges().
                        chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                        .type("UK.OBIE.CHAPSOut")
                        .amount(
                                new OBActiveOrHistoricCurrencyAndAmount().amount("1.5").currency("GBP")
                        ),
                new OBWriteDomesticConsentResponse5DataCharges().
                        chargeBearer(OBChargeBearerType1Code.BORNEBYDEBTOR)
                        .type("UK.OBIE.CHAPSOut")
                        .amount(
                                new OBActiveOrHistoricCurrencyAndAmount().amount("1.5").currency("GBP")
                        )
        );
    }
}
