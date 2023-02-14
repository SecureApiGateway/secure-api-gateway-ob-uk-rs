package com.forgerock.securebanking.openbanking.uk.rs.configuration;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
