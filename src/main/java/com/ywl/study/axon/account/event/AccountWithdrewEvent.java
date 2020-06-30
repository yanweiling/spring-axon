package com.ywl.study.axon.account.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

public class AccountWithdrewEvent {
    private String accountId;

    /*取款金额*/
    private Double amount;

    public AccountWithdrewEvent(String accountId, Double amount) {
        this.accountId = accountId;
        this.amount = amount;
    }

    /**
     * @return the accountId
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * @param accountId to set
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * @return the amount
     */
    public Double getAmount() {
        return amount;
    }

    /**
     * @param amount to set
     */
    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
