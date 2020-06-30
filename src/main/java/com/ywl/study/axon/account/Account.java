package com.ywl.study.axon.account;

import com.ywl.study.axon.account.command.AccountCreateCommand;
import com.ywl.study.axon.account.command.AccountDepositCommand;
import com.ywl.study.axon.account.command.AccountWithdrawCommand;
import com.ywl.study.axon.account.event.AccountCreatedEvent;
import com.ywl.study.axon.account.event.AccountDepositedEvent;
import com.ywl.study.axon.account.event.AccountWithdrewEvent;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;

import javax.persistence.Entity;
import javax.persistence.Id;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

/**
 * 聚合对象
 */
@Aggregate
@Entity(name = "tb_account")
public class Account {
    /*聚合对象的ID,用@AggregateIdentifier来标志，如果有@Id,则axon框架会将标注有@Id的字段作为聚合对象ID*/
    @Id
    private String accountId;

    /*余额*/
    private Double deposit;

    /**
     * command 是用来触发event
     * @param command
     */
    @CommandHandler
    public Account(AccountCreateCommand command) {
     apply(new AccountCreatedEvent(command.getAccountId()));
    }

    @CommandHandler
    public void handle(AccountDepositCommand command){
        apply(new AccountDepositedEvent(command.getAccountId(),command.getAmount()));
    }

    @CommandHandler
    public void handle(AccountWithdrawCommand command){
        //取款时要先检查
        if(command.getAmount()<=this.deposit){
            apply(new AccountWithdrewEvent(command.getAccountId(),command.getAmount()));
        }else{
            throw new IllegalArgumentException("余额不足");
        }

    }

    /**
     * @return the accountId
     */
    @EventSourcingHandler
    public void on(AccountCreatedEvent event){
        this.accountId=event.getAccountId();
        this.deposit=0d;
    }

    @EventSourcingHandler
    public void on(AccountDepositedEvent event){
        this.deposit+=event.getAmount();
    }

    @EventSourcingHandler
    public void on(AccountWithdrewEvent event){
        this.deposit-=event.getAmount();
    }


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
     * @return the deposit
     */
    public Double getDeposit() {
        return deposit;
    }

    /**
     * @param deposit to set
     */
    public void setDeposit(Double deposit) {
        this.deposit = deposit;
    }
}
