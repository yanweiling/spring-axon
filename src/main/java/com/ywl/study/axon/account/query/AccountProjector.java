package com.ywl.study.axon.account.query;

import com.ywl.study.axon.account.event.AccountCreatedEvent;
import com.ywl.study.axon.account.event.AccountDepositedEvent;
import com.ywl.study.axon.account.event.AccountWithdrewEvent;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 聚合对象向物化视图的投影
 * 聚合对象：Account
 * 物化视图：AccountEntity
 */
@Component
public class AccountProjector {
    private static final Logger LOG= LoggerFactory.getLogger(AccountProjector.class);
    @Autowired
    private AccountEntityRepository accountEntityRepository;

    /*新建的操作*/
    @EventHandler
    public void on(AccountCreatedEvent event){
        AccountEntity account=new AccountEntity();
        account.setAccountId(event.getAccountId());
        accountEntityRepository.save(account);

    }
    /*存款的操作*/
    @EventHandler
    public void on(AccountDepositedEvent event){
        LOG.info("处理存款操作:{}",event);
        AccountEntity account=accountEntityRepository.getOne(event.getAccountId());
        if(account!=null){
            account.setDeposit((account.getDeposit()==null?0:account.getDeposit())+event.getAmount());
            accountEntityRepository.save(account);
        }

    }

    /*取款的操作*/
    @EventHandler
    public void on(AccountWithdrewEvent event){
        LOG.info("处理取款操作:{}",event);
        AccountEntity account=accountEntityRepository.getOne(event.getAccountId());
        account.setDeposit(account.getDeposit()-event.getAmount());
        accountEntityRepository.save(account);


    }
}
