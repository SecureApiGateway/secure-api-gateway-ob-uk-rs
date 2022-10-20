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
package com.forgerock.securebanking.rs.platform.client.test.support;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

import static java.util.UUID.randomUUID;

public class FilePaymentPlatformIntentTestFactory {

    public static final Gson gson = new Gson();

    public static JsonObject aValidFilePaymentPlatformIntent(FilePaymentIntentTestModel model) {
        return aValidFilePaymentPlatformIntentBuilder(model);
    }

    public static JsonObject aValidFilePaymentPlatformIntentBuilder(FilePaymentIntentTestModel model) {
        JsonObject consent = new JsonObject();
        consent.addProperty("id", model.getConsentId());
        consent.add(
                "OBIntentObject",
                aValidFilePaymentPlatformIntentDataBuilder(model)
        );
        consent.addProperty("resourceOwnerUsername", model.getResourceOwnerUsername());
        consent.addProperty(
                "oauth2ClientId",
                model.getOauth2ClientId() != null ? model.getOauth2ClientId() : randomUUID().toString()
        );
        consent.addProperty("oauth2ClientName", "PISP Name");
        consent.addProperty(
                "AccountId",
                Objects.isNull(model.getAccountId()) ? gson.toJson(List.of(UUID.randomUUID().toString())) : model.getAccountId()
        );

        return consent;
    }

    public static JsonObject aValidFilePaymentPlatformIntentDataBuilder(FilePaymentIntentTestModel model) {
        JsonObject data = new JsonObject();
        data.addProperty("ConsentId", model.getConsentId());
        data.addProperty("CreationDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("CutOffDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("StatusUpdateDateTime", DateTime.now(DateTimeZone.forTimeZone(TimeZone.getDefault())).toString());
        data.addProperty("Status", model.getStatus().toString());
        data.add("Initiation", aValidOBWriteFile2DataInitiationBuilder(model));
        data.add("Charges", aValidChargesBuilder());
        JsonObject dataRoot = new JsonObject();
        dataRoot.add("Data", data);
        return dataRoot;
    }

    public static JsonObject aValidOBWriteFile2DataInitiationBuilder(FilePaymentIntentTestModel model) {
        JsonObject data = new JsonObject();
        data.addProperty("FileType", model.getFileType());
        data.addProperty("FileHash", model.getFileHash());
        data.addProperty("FileReference", Objects.isNull(model.getFileReference()) ? "XmlExample" : model.getFileReference());
        data.addProperty("NumberOfTransactions", model.getNumberOfTransactions());
        data.addProperty("ControlSum", model.getControlSum());
        data.add("RequestedExecutionDateTime", null);
        data.add("LocalInstrument", null);
        data.add("DebtorAccount", JsonParser.parseString("{\n" +
                "        \"SchemeName\": \"UK.OBIE.SortCodeAccountNumber\",\n" +
                "        \"Identification\": \"08080021325698\",\n" +
                "        \"Name\": \"ACME Inc\",\n" +
                "        \"SecondaryIdentification\": \"0002\"\n" +
                "      }"));
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

}
