package com.ywl.study.axon.account.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.commandhandling.TargetAggregateIdentifier;
import org.springframework.stereotype.Component;

/**
 * 账户创建command
 * 需要关联到某个聚合对象上
 */
public class AccountCreateCommand {
    /*关联的聚合对象ID*/
    @TargetAggregateIdentifier
    private String accountId;

    public AccountCreateCommand(String accountId) {
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
