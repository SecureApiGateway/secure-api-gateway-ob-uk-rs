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
package com.forgerock.sapi.gateway.ob.uk.rs.server.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Configuration class representing Test User AccountId data
 */
@Configuration
@ConfigurationProperties(prefix = "testdata")
public class TestUserAccountIds {

    private Map<String, List<TestAccountId>> userAccountIds;

    public void setUserAccountIds(Map<String, List<TestAccountId>> userAccountIds) {
        this.userAccountIds = userAccountIds;
    }

    public Map<String, List<TestAccountId>> getUserAccountIds() {
        return userAccountIds;
    }

    public static class TestAccountId {
        private String sortCode;
        private String accountNumber;

        public String getAccountNumber() {
            return accountNumber;
        }
        public String getSortCode() {
            return sortCode;
        }

        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }

        public void setSortCode(String sortCode) {
            this.sortCode = sortCode;
        }
    }
}
