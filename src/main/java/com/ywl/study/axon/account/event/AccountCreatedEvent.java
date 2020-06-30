package com.ywl.study.axon.account.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import org.springframework.stereotype.Component;

public class AccountCreatedEvent {
    /*关联的聚合对象ID*/
    private String accountId;

    public AccountCreatedEvent(String accountId) {
        this.accountId = accountId;
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
}
