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
package com.forgerock.sapi.gateway.ob.uk.rs.server.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRAddressTypeCode;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRCustomerInfo;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.customerinfo.FRCustomerInfoAddress;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest(classes = {RsApplicationConfiguration.class})
public class JacksonMapperTest {

    @Autowired
    private MappingJackson2HttpMessageConverter mappingJacksonHttpMessageConverter;

    private ObjectMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = mappingJacksonHttpMessageConverter.getObjectMapper();
    }

    private static Stream<Arguments> deserializeArgumentsProvider() {
        return Stream.of(
                Arguments.arguments(
                        "Zeros precision[2]",
                        "1.00",
                        new BigDecimal("1.00")
                ),
                Arguments.arguments(
                        "Zeros precision[4]",
                        "156.0000",
                        new BigDecimal("156.0000")
                ),
                Arguments.arguments(
                        "Zeros precision[6]",
                        "43.000000",
                        new BigDecimal("43.000000")
                ),
                Arguments.arguments(
                        "precision[2]",
                        "66.12",
                        new BigDecimal("66.12")
                ),
                Arguments.arguments(
                        "precision[4]",
                        "3.1234",
                        new BigDecimal("3.1234")
                ),
                Arguments.arguments(
                        "precision[6]",
                        "90.123456",
                        new BigDecimal("90.123456")
                ),
                Arguments.arguments(
                        "scientist format",
                        "8.7000E-4",
                        new BigDecimal("0.00087000")
                ),
                Arguments.arguments(
                        "scientist format",
                        "13400000.3456E-0",
                        new BigDecimal("13400000.3456")
                ),
                Arguments.arguments(
                        "scientist format",
                        "134.563456E-0",
                        new BigDecimal("134.563456")
                ),
                Arguments.arguments(
                        "scientist format",
                        "5312.1E-0",
                        new BigDecimal("5312.1")
                ),
                Arguments.arguments(
                        "scientist format",
                        "8.124734E-3",
                        new BigDecimal("0.008124734")
                )
        );
    }

    @ParameterizedTest(name = "{0}=({1})")
    @MethodSource("deserializeArgumentsProvider")
    public void deserialize(String title, String value, BigDecimal expected) throws JsonProcessingException {
        final BigDecimal deserialized = mapper.readValue(value, BigDecimal.class);
        log.info("[deserialized | expected]: {} | {}", deserialized, expected);
        assertEquals(deserialized, expected);
    }

    private static Stream<Arguments> serializeArgumentsProvider() {
        return Stream.of(
                Arguments.arguments(
                        "zero precision[2]",
                        new BigDecimal("112.00"),
                        "112.00"
                ),
                Arguments.arguments(
                        "zero precision[4]",
                        new BigDecimal("112.0000"),
                        "112.0000"
                ),
                Arguments.arguments(
                        "zero precision[6]",
                        new BigDecimal("112.000000"),
                        "112.000000"
                ),
                Arguments.arguments(
                        "precision[2]",
                        new BigDecimal("112.12"),
                        "112.12"
                ),
                Arguments.arguments(
                        "precision[4]",
                        new BigDecimal("145.1234"),
                        "145.1234"
                ),
                Arguments.arguments(
                        "precision[6]",
                        new BigDecimal("145.1235"),
                        "145.1235"
                ),
                Arguments.arguments(
                        "scientist format",
                        new BigDecimal("134.563456E-0"),
                        "134.563456"
                ),
                Arguments.arguments(
                        "scientist format",
                        new BigDecimal("13400000.3456E-0"),
                        "13400000.3456"
                ),
                Arguments.arguments(
                        "scientist format",
                        new BigDecimal("8.124734E-3"),
                        "0.008124734"
                ),
                Arguments.arguments(
                        "scientist format",
                        new BigDecimal("5312.1E-0"),
                        "5312.1"
                )
        );
    }

    @ParameterizedTest(name = "{0}=({1})")
    @MethodSource("serializeArgumentsProvider")
    public void serialize(String title, BigDecimal value, String expected) throws JsonProcessingException {
        final String serialized = mapper.writeValueAsString(value);
        log.info("[serialized | expected]: {} | {}", serialized, expected);
        assertEquals(serialized, expected);
    }

    @Test
    public void serializeCustomerInfo() throws JsonProcessingException {
        LocalDate birthDate = new DateTime().toDateTimeISO().minusYears(20).toLocalDate();
        FRCustomerInfo customerInfo = aValidFRCustomerInfo(UUID.randomUUID().toString(), "userName");
        customerInfo.setBirthdate(birthDate);
        String customerInfoSerialized = mapper.writeValueAsString(customerInfo);
        assertThat(customerInfoSerialized).contains(DateTimeFormat.forPattern("dd/MM/yyyy").print(birthDate));
    }

    @Test
    public void deserializeCustomerInfo() throws JsonProcessingException {
        FRCustomerInfo customerInfo = mapper.readValue(getCustomerInfoString(), FRCustomerInfo.class);
        assertThat(customerInfo.getBirthdate().toString()).isEqualTo("2001-02-16");
    }

    public static FRCustomerInfo aValidFRCustomerInfo(String userId, String userName) {
        return FRCustomerInfo.builder()
                .userID(userId)
                .userName(userName)
                .address(aValidFRCustomerInfoAddress())
                .birthdate(new DateTime().toDateTimeISO().minusYears(19).toLocalDate())
                .email("joe.doe@acme.com")
                .familyName("Joe")
                .givenName("Doe")
                .initials("JD")
                .title("Mr")
                .partyId("party-Id")
                .phoneNumber("+44 7777 777777").build();
    }

    public static FRCustomerInfoAddress aValidFRCustomerInfoAddress() {
        return FRCustomerInfoAddress.builder()
                .streetAddress(List.of("999", "Letsbe Avenue", "Chelmsford", "Essex"))
                .addressType(FRAddressTypeCode.RESIDENTIAL)
                .country("UK")
                .postalCode("ES12 3RR").build();
    }

    private static String getCustomerInfoString() {
        return "{\n" +
                "   \"partyId\": \"32432-3242\",\n" +
                "   \"title\": \"Mr\",\n" +
                "   \"initials\": \"F\",\n" +
                "   \"familyName\": \"Titmus\",\n" +
                "   \"givenName\": \"Fred\",\n" +
                "   \"email\": \"fred.titmus@acme.com\",\n" +
                "   \"phoneNumber\": \"07743 234323\",\n" +
                "   \"birthdate\": \"16/02/2001\",\n" +
                "   \"address\": {\n" +
                "     \"addressType\": \"Residential\",\n" +
                "     \"streetAddress\": [\"999\", \"letsbe avenue\", \"colchester\", \"essex\"],\n" +
                "     \"postalCode\": \"CO8 3JJ\",\n" +
                "     \"country\": \"UK\"\n" +
                "   }\n" +
                " }\n";
    }
}
