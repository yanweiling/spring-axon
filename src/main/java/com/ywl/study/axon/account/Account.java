package com.ywl.study.axon.account;

import com.ywl.study.axon.account.command.AccountCreateCommand;
import com.ywl.study.axon.account.command.AccountDepositCommand;
import com.ywl.study.axon.account.command.AccountWithdrawCommand;
import com.ywl.study.axon.account.event.AccountCreatedEvent;
import com.ywl.study.axon.account.event.AccountDepositedEvent;
import com.ywl.study.axon.account.event.AccountWithdrewEvent;

import com.ywl.study.axon.account.query.AccountProjector;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.Id;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

/**
 * 聚合对象
 */
@Aggregate
//@Entity(name = "tb_account")
public class Account {
    private static final Logger LOG= LoggerFactory.getLogger(Account.class);
    /*聚合对象的ID,用@AggregateIdentifier来标志，如果有@Id,则axon框架会将标注有@Id的字段作为聚合对象ID*/
//    @Id
    @AggregateIdentifier
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
        LOG.info("处理存款commandhandler操作:{}",command);
        apply(new AccountDepositedEvent(command.getAccountId(),command.getAmount()));
    }

    @CommandHandler
    public void handle(AccountWithdrawCommand command){
        LOG.info("处理取款commandhandler操作:{}",command);
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
        LOG.info("处理存款EventSourcingHandler操作:{}",event);
        this.deposit+=event.getAmount();
    }

    @EventSourcingHandler
    public void on(AccountWithdrewEvent event){
        LOG.info("处理取款EventSourcingHandler操作:{}",event);
        this.deposit-=event.getAmount();
    }

    public Account(){}


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
