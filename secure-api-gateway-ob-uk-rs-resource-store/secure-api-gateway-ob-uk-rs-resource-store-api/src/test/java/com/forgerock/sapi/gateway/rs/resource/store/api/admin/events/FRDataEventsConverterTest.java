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
package com.forgerock.sapi.gateway.rs.resource.store.api.admin.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.forgerock.sapi.gateway.rs.resource.store.repo.entity.event.FREventMessageEntity;

import uk.org.openbanking.datamodel.event.OBEvent1;
import uk.org.openbanking.datamodel.event.OBEventLink1;
import uk.org.openbanking.datamodel.event.OBEventNotification1;
import uk.org.openbanking.datamodel.event.OBEventResourceUpdate1;
import uk.org.openbanking.datamodel.event.OBEventSubject1;

/**
 * Unit test for {@link FRDataEventsConverter}
 */
public class FRDataEventsConverterTest {

    @Test
    void shouldConvertEntityToOBEventNotification1() {
        // Given
        FREventMessageEntity entity = aValidFREventMessageEntity();
        // When
        OBEventNotification1 obEventNotification1 = FRDataEventsConverter.toOBEventNotification1(entity);
        // Then
        assertThat(obEventNotification1).isNotNull();
        assertThat(obEventNotification1.getJti()).isEqualTo(entity.getJti());
        assertThat(obEventNotification1.getAud()).isEqualTo(entity.getAud());
        assertThat(obEventNotification1.getIat()).isEqualTo(entity.getIat());
        assertThat(obEventNotification1.getIss()).isEqualTo(entity.getIss());
        assertThat(obEventNotification1.getSub()).isEqualTo(entity.getSub());
        assertThat(obEventNotification1.getToe()).isEqualTo(entity.getToe());
        assertThat(obEventNotification1.getTxn()).isEqualTo(entity.getTxn());
        assertThat(obEventNotification1.getEvents()).isEqualTo(entity.getEvents());
    }

    @Test
    void shouldConvertOBEventNotification1ToEntity() {
        // Given
        String apiClientId = UUID.randomUUID().toString();
        OBEventNotification1 obEventNotification1 = obValidEventNotification1();
        // When
        FREventMessageEntity entity = FRDataEventsConverter.toFREventMessageEntity(apiClientId, obEventNotification1);
        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getJti()).isEqualTo(obEventNotification1.getJti());
        assertThat(entity.getAud()).isEqualTo(obEventNotification1.getAud());
        assertThat(entity.getIat()).isEqualTo(obEventNotification1.getIat());
        assertThat(entity.getIss()).isEqualTo(obEventNotification1.getIss());
        assertThat(entity.getSub()).isEqualTo(obEventNotification1.getSub());
        assertThat(entity.getToe()).isEqualTo(obEventNotification1.getToe());
        assertThat(entity.getTxn()).isEqualTo(obEventNotification1.getTxn());
        assertThat(entity.getEvents()).isEqualTo(obEventNotification1.getEvents());
    }

    private OBEventNotification1 obValidEventNotification1() {
        return new OBEventNotification1()
                .aud("7umx5nTR33811QyQfi")
                .iat(1516239022)
                .iss("https://examplebank.com/")
                .jti("b460a07c-4962-43d1-85ee-9dc10fbb8f6c")
                .sub("https://examplebank.com/api/open-banking/v3.0/pisp/domestic-payments/pmt-7290-003")
                .toe(1516239022)
                .txn("dfc51628-3479-4b81-ad60-210b43d02306")
                .events(new OBEvent1().urnukorgopenbankingeventsresourceUpdate(
                                new OBEventResourceUpdate1()
                                        .subject(
                                                new OBEventSubject1()
                                                        .subjectType("http://openbanking.org.uk/rid_http://openbanking.org.uk/rty")
                                                        .httpopenbankingOrgUkrid("pmt-7290-003")
                                                        .httpopenbankingOrgUkrlk(
                                                                List.of(
                                                                        new OBEventLink1()
                                                                                .link("https://examplebank.com/api/open-banking/v3.0/pisp/domestic-payments/pmt-7290-003")
                                                                                .version("v3.1.5"),
                                                                        new OBEventLink1()
                                                                                .link("https://examplebank.com/api/open-banking/v3.0/pisp/domestic-payments/pmt-7290-003")
                                                                                .version("v3.1.10")
                                                                )
                                                        )
                                                        .httpopenbankingOrgUkrty("domestic-payment")
                                        )
                        )
                );
    }

    private FREventMessageEntity aValidFREventMessageEntity() {
        return FREventMessageEntity.builder()
                .id(UUID.randomUUID().toString())
                .jti("b460a07c-4962-43d1-85ee-9dc10fbb8f6c")
                .apiClientId(UUID.randomUUID().toString())
                .iss("https://examplebank.com/")
                .iat(1516239022)
                .sub("https://examplebank.com/api/open-banking/v3.0/pisp/domestic-payments/pmt-7290-003")
                .aud("7umx5nTR33811QyQfi")
                .txn("dfc51628-3479-4b81-ad60-210b43d02306")
                .toe(1516239022)
                .events(new OBEvent1().urnukorgopenbankingeventsresourceUpdate(
                                new OBEventResourceUpdate1()
                                        .subject(
                                                new OBEventSubject1()
                                                        .subjectType("http://openbanking.org.uk/rid_http://openbanking.org.uk/rty")
                                                        .httpopenbankingOrgUkrid("pmt-7290-003")
                                                        .httpopenbankingOrgUkrlk(
                                                                List.of(
                                                                        new OBEventLink1()
                                                                                .link("https://examplebank.com/api/open-banking/v3.0/pisp/domestic-payments/pmt-7290-003")
                                                                                .version("v3.1.5"),
                                                                        new OBEventLink1()
                                                                                .link("https://examplebank.com/api/open-banking/v3.0/pisp/domestic-payments/pmt-7290-003")
                                                                                .version("v3.1.10")
                                                                )
                                                        )
                                                        .httpopenbankingOrgUkrty("domestic-payment")
                                        )
                        )
                )
                .build();
    }
}
