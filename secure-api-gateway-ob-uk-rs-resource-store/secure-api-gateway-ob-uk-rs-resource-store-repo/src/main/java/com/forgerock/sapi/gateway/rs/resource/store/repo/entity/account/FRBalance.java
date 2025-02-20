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
package com.forgerock.sapi.gateway.rs.resource.store.repo.entity.account;

import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRCashBalance;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.account.FRCreditDebitIndicator;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRAmount;
import com.forgerock.sapi.gateway.ob.uk.common.datamodel.common.FRTotalValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Representation of an account's balance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class FRBalance {
    private final static NumberFormat FORMAT_AMOUNT = new DecimalFormat("#0.00");

    @Id
    @Indexed
    private String id;
    @Indexed
    private String accountId;
    private FRCashBalance balance;
    private FRTotalValue totalValue;
    @CreatedDate
    private Date created;
    @LastModifiedDate
    private Date updated;

    public FRAmount getCurrencyAndAmount() {
        return getBalance().getAmount();
    }

    public BigDecimal getAmount() {
        return new BigDecimal(getBalance().getAmount().getAmount());
    }

    public String getCurrency() {
        return getBalance().getAmount().getCurrency();
    }

    public FRCreditDebitIndicator getCreditDebitIndicator() {
        return getBalance().getCreditDebitIndicator();
    }

    public void setAmount(BigDecimal amount) {
        getBalance().getAmount().setAmount(FORMAT_AMOUNT.format(amount));
    }

    public void setCreditDebitIndicator(FRCreditDebitIndicator code) {
        getBalance().setCreditDebitIndicator(code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FRBalance that = (FRBalance) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(balance, that.balance) &&
                Objects.equals(created, that.created) &&
                Objects.equals(updated, that.updated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountId, balance, created, updated);
    }
}
