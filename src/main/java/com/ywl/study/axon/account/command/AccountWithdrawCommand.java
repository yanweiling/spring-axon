package com.ywl.study.axon.account.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import org.springframework.stereotype.Component;

/**
 * 取款command
 */

public class AccountWithdrawCommand {
    @TargetAggregateIdentifier
    private String accountId;

    /*取款金额*/
    private Double amount;

    public AccountWithdrawCommand(String accountId, Double amount) {
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
