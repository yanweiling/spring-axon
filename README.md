**账户管理**

    功能：账户创建，查看，转账
    
**使用Axon框架的设计过程**
    
    领域模型的设计
    业务-Command-Command处理
    数据-Event-Event处理
    将数据保存到数据库：聚合数据-映射到-视图数据
    查询-QUery

**使用Axon框架的设计过程**

    领域模型：账户Account
    业务Command：创建账户、存款、取款
    事件Event：账户创建、存款、取款
    将账户信息保存到数据库中，方便查询
    查询Command：查询账户

---
1.新建项目spirng-axon
https://github.com/yanweiling/spring-axon.git

**pom文件**

```
 <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.axonframework</groupId>
            <artifactId>axon-spring-boot-starter</artifactId>
            <version>3.2</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.16.10</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

    </dependencies>
```
日志文件logback-spring.xml


```
<?xml version="1.0" encoding="UTF-8"?>

<configuration scan="true">
    <include resource="org/springframework/boot/logging/logback/base.xml"/>

<!-- The FILE and ASYNC appenders are here as examples for a production configuration -->
    <logger name="com.ywl.study.axon" level="DEBUG"/>
    <logger name="org.springframework.transaction" level="DEBUG"/>
    <logger name="org.springframework.jms" level="DEBUG"/>
    <logger name="org.springframework.jdbc" level="DEBUG"/>
    <logger name="org.springframework.orm.jpa" level="DEBUG"/>
    <logger name="javax.transaction" level="DEBUG"/>
    <logger name="javax.jms" level="DEBUG"/>
    <logger name="org.hibernate.jpa" level="DEBUG"/>
    <logger name="org.hibernate.SQL" level="DEBUG"/>
    <logger name="org.axonframework" level="DEBUG"/>
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>

</configuration>

```




      --| account
             Account.java
             --|command 
                    --|AccountCreateCommand.java
                    --|AccountDepositCommand.java
                    --|AccountWithdrawCommand.java
              --|controller
                     ---| AccountController.java
              --| event
                     --|AccountCreatedEvent.java
                     --|AccountDepositedEvent.java
                     --|AccountWithdrewEvent.java
             
                      
  

```
public class AccountCreateCommand {
    /*关联的聚合对象ID*/
    @TargetAggregateIdentifier
    private String accountId;
    //get set ..
    //构造函数
    }
```
```

/**
 * 存款command
 */
public class AccountDepositCommand {
    @TargetAggregateIdentifier
    private String accountId;

    /*存款金额*/
    private Double amount;
    //get set ..
    //构造函数
    }
```
```
/**
 * 取款command
 */

public class AccountWithdrawCommand {
    @TargetAggregateIdentifier
    private String accountId;

    /*取款金额*/
    private Double amount;
    //get set ..
    //构造函数
    }
```

```
public class AccountCreatedEvent {
    /*关联的聚合对象ID*/
    private String accountId;
    //get set ..
    //构造函数
    }
```
```
public class AccountDepositedEvent {
    private String accountId;
    /*存款金额*/
    private Double amount;
    //get set ..
    //构造函数
    }
```
```
public class AccountWithdrewEvent {
    private String accountId;

    /*取款金额*/
    private Double amount;
    //get set ..
    //构造函数
    }
```
**Account**

```
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

    public Account(String accountId, Double deposit) {
        this.accountId = accountId;
        this.deposit = deposit;
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


```


```

@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private CommandGateway commandGateway;

    @PostMapping("")
    public CompletableFuture<Object> create(){
        UUID accountId= UUID.randomUUID();
        AccountCreateCommand command=new AccountCreateCommand(accountId.toString());
        return commandGateway.send(command);
    }

    @PutMapping("/{accountId}/deposit/{amount}")
    public void deposit(@PathVariable String accountId,@PathVariable Double amount){
        commandGateway.send(new AccountDepositCommand(accountId,amount));
    }


    @PutMapping("/{accountId}/withdraw/{amount}")
    public void withdraw(@PathVariable String accountId,@PathVariable Double amount){
        commandGateway.send(new AccountWithdrawCommand(accountId,amount));
    }

}

```


启动类

```
@SpringBootApplication
public class SpringAxonApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAxonApplication.class, args);
    }

}
```

**配置文件**

```
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ywl_axon?useSSL=false&serverTimezone=PRC
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true


```
**项目启动后，自动生成表：**

    association_value_entry--axon生成
    domain_event_entry     --axon生成 保存event数据，event的参数序列化后保存
    hibernate_sequence     --axon生成
    saga_entry             --axon生成
    snapshot_event_entr    --axon生成  用来做镜像的 ，将domain-event-entry中的event生成的聚合对象镜像保存起来
    token_entry            --axon生成  暂时不需要
    tb_account--jpa自动生成


先读镜像，然后根据镜像的event 读取domain-event-entry中的command 和event对象，进行执行--这个说法可能不对

**测试**


```
E:\DCITS-YWL\project\spring-axon>curl -X POST http://localhost:8080/account
0946d980-a1d5-4223-81ff-d46a3b755e7a  --返回account的id

```
日志


```
2020-06-30 16:04:01.174  INFO 23756 --- [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2020-06-30 16:04:01.174  INFO 23756 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2020-06-30 16:04:01.182  INFO 23756 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 8 ms
2020-06-30 16:04:01.196 DEBUG 23756 --- [nio-8080-exec-1] o.j.s.OpenEntityManagerInViewInterceptor : Opening JPA EntityManager in OpenEntityManagerInViewInterceptor
2020-06-30 16:04:01.206 DEBUG 23756 --- [nio-8080-exec-1] o.a.common.IdentifierFactory             : Looking for IdentifierFactory implementation using the context class loader
2020-06-30 16:04:01.207 DEBUG 23756 --- [nio-8080-exec-1] o.a.common.IdentifierFactory             : Looking for IdentifierFactory implementation using the IdentifierFactory class loader.
2020-06-30 16:04:01.207 DEBUG 23756 --- [nio-8080-exec-1] o.a.common.IdentifierFactory             : Using default UUID-based IdentifierFactory
2020-06-30 16:04:01.209 DEBUG 23756 --- [nio-8080-exec-1] o.a.commandhandling.SimpleCommandBus     : Handling command [com.ywl.study.axon.account.command.AccountCreateCommand]
2020-06-30 16:04:01.212 DEBUG 23756 --- [nio-8080-exec-1] o.a.m.unitofwork.AbstractUnitOfWork      : Starting Unit Of Work
2020-06-30 16:04:01.213 DEBUG 23756 --- [nio-8080-exec-1] o.a.m.u.MessageProcessingContext         : Adding handler org.axonframework.messaging.unitofwork.AbstractUnitOfWork$$Lambda$753/264213514 for phase ROLLBACK
2020-06-30 16:04:01.213 DEBUG 23756 --- [nio-8080-exec-1] o.a.m.u.MessageProcessingContext         : Notifying handlers for phase STARTED
2020-06-30 16:04:01.213 DEBUG 23756 --- [nio-8080-exec-1] o.s.orm.jpa.JpaTransactionManager        : Found thread-bound EntityManager [SessionImpl(1691286694<open>)] for JPA transaction
2020-06-30 16:04:01.214 DEBUG 23756 --- [nio-8080-exec-1] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2020-06-30 16:04:01.219 DEBUG 23756 --- [nio-8080-exec-1] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@678d8b80]
2020-06-30 16:04:01.220 DEBUG 23756 --- [nio-8080-exec-1] o.a.m.u.MessageProcessingContext         : Adding handler org.axonframework.commandhandling.SimpleCommandBus$$Lambda$757/144365739 for phase COMMIT
2020-06-30 16:04:01.221 DEBUG 23756 --- [nio-8080-exec-1] o.a.m.u.MessageProcessingContext         : Adding handler org.axonframework.commandhandling.SimpleCommandBus$$Lambda$758/1745469236 for phase ROLLBACK
2020-06-30 16:04:01.229 DEBUG 23756 --- [nio-8080-exec-1] o.a.m.u.MessageProcessingContext         : Adding handler org.axonframework.eventhandling.AbstractEventBus$$Lambda$777/1201167294 for phase PREPARE_COMMIT
2020-06-30 16:04:01.229 DEBUG 23756 --- [nio-8080-exec-1] o.a.m.u.MessageProcessingContext         : Adding handler org.axonframework.eventhandling.AbstractEventBus$$Lambda$778/600152020 for phase COMMIT
2020-06-30 16:04:01.230 DEBUG 23756 --- [nio-8080-exec-1] o.a.m.u.MessageProcessingContext         : Adding handler org.axonframework.eventhandling.AbstractEventBus$$Lambda$779/95255565 for phase AFTER_COMMIT
2020-06-30 16:04:01.230 DEBUG 23756 --- [nio-8080-exec-1] o.a.m.u.MessageProcessingContext         : Adding handler org.axonframework.eventhandling.AbstractEventBus$$Lambda$780/1627598436 for phase CLEANUP
2020-06-30 16:04:01.230 DEBUG 23756 --- [nio-8080-exec-1] o.a.m.u.MessageProcessingContext         : Adding handler org.axonframework.commandhandling.model.LockingRepository$$Lambda$781/1682168913 for phase CLEANUP
2020-06-30 16:04:01.232 DEBUG 23756 --- [nio-8080-exec-1] o.a.m.u.MessageProcessingContext         : Adding handler org.axonframework.commandhandling.model.AbstractRepository$$Lambda$785/1803780797 for phase ROLLBACK
2020-06-30 16:04:01.232 DEBUG 23756 --- [nio-8080-exec-1] o.a.m.u.MessageProcessingContext         : Adding handler org.axonframework.commandhandling.model.AbstractRepository$$Lambda$787/881437434 for phase PREPARE_COMMIT
2020-06-30 16:04:01.233 DEBUG 23756 --- [nio-8080-exec-1] o.a.m.unitofwork.AbstractUnitOfWork      : Committing Unit Of Work
2020-06-30 16:04:01.233 DEBUG 23756 --- [nio-8080-exec-1] o.a.m.u.MessageProcessingContext         : Notifying handlers for phase PREPARE_COMMIT
2020-06-30 16:04:01.264 DEBUG 23756 --- [nio-8080-exec-1] org.hibernate.SQL                        : select next_val as id_val from hibernate_sequence for update
Hibernate: select next_val as id_val from hibernate_sequence for update
2020-06-30 16:04:01.267 DEBUG 23756 --- [nio-8080-exec-1] org.hibernate.SQL                        : update hibernate_sequence set next_val= ? where next_val=?
Hibernate: update hibernate_sequence set next_val= ? where next_val=?
2020-06-30 16:04:01.450 DEBUG 23756 --- [nio-8080-exec-1] org.hibernate.SQL                        : insert into domain_event_entry (event_identifier, meta_data, payload, payload_revision, payload_type, time_stamp, aggregate_identifier, sequence_number, type, global_index) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
Hibernate: insert into domain_event_entry (event_identifier, meta_data, payload, payload_revision, payload_type, time_stamp, aggregate_identifier, sequence_number, type, global_index) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
2020-06-30 16:04:01.458 DEBUG 23756 --- [nio-8080-exec-1] org.hibernate.SQL                        : insert into tb_account (deposit, account_id) values (?, ?)
Hibernate: insert into tb_account (deposit, account_id) values (?, ?)
2020-06-30 16:04:01.460 DEBUG 23756 --- [nio-8080-exec-1] o.a.m.u.MessageProcessingContext         : Notifying handlers for phase COMMIT
2020-06-30 16:04:01.460 DEBUG 23756 --- [nio-8080-exec-1] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
2020-06-30 16:04:01.460 DEBUG 23756 --- [nio-8080-exec-1] o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(1691286694<open>)]
2020-06-30 16:04:01.533 DEBUG 23756 --- [nio-8080-exec-1] o.s.orm.jpa.JpaTransactionManager        : Not closing pre-bound JPA EntityManager after transaction
2020-06-30 16:04:01.534 DEBUG 23756 --- [nio-8080-exec-1] o.a.m.u.MessageProcessingContext         : Notifying handlers for phase AFTER_COMMIT
2020-06-30 16:04:01.535 DEBUG 23756 --- [nio-8080-exec-1] o.a.m.u.MessageProcessingContext         : Notifying handlers for phase CLEANUP
2020-06-30 16:04:01.535 DEBUG 23756 --- [nio-8080-exec-1] o.a.m.u.MessageProcessingContext         : Notifying handlers for phase CLOSED
2020-06-30 16:04:01.623 DEBUG 23756 --- [nio-8080-exec-1] o.j.s.OpenEntityManagerInViewInterceptor : Closing JPA EntityManager in OpenEntityManagerInViewInterceptor


```

解析

     @PostMapping("")
    public CompletableFuture<Object> create(){
        UUID accountId= UUID.randomUUID();
        AccountCreateCommand command=new AccountCreateCommand(accountId.toString());
        return commandGateway.send(command);
    }
    
    1.发起AccountCreate的command请求，由commandBus去处理
    2.启动Unit of Work （所有的command，handler处理都是在unit of work的执行单元中执行的，unit of work又是在事务中执行的）
    3.关联到JPA的jdbc事务中
    4.检查现有的handler
    5.command执行完进行commit
    6.将event保存到数据库中，然后把event发送给对应的eventHandler中
    7.处理event---创建账户account，把账户的聚合对象写入到物化视图中（我们把聚合对象和Account 实体是一个类，那么就把Account做物化视图处理）
    8.然后提交事务
    
**测试充值**

    curl -X PUT http://localhost:8080/account/1253ab17-76db-4bf6-8c85-d5b09ffc1ab5/deposit/100

**备注：聚合对象上必须要有无参的构造函数**


测试取款

```
curl -X PUT http://localhost:8080/account/1253ab17-76db-4bf6-8c85-d5b09ffc1ab5/withdraw/10


```


```
Handling command [com.ywl.study.axon.account.command.AccountWithdrawCommand]
Starting Unit Of Work
Exposing JPA transaction as JDBC --关联事务
select account ...where account_id=? for update---锁定聚合对象中的记录
select max(domaineven0_.sequence_number) as col_0_0_ from domain_event_entry domaineven0_ where domaineven0_.aggregate_identifier=?--获得下一步sequence number值
```

新建query文件夹，将物化视图都放在这个包下

    --| account
             Account.java
             --|command 
                    --|AccountCreateCommand.java
                    --|AccountDepositCommand.java
                    --|AccountWithdrawCommand.java
              --|controller
                     ---| AccountController.java
              --| event
                     --|AccountCreatedEvent.java
                     --|AccountDepositedEvent.java
                     --|AccountWithdrewEvent.java
              --| query
                    ---|AccountEntity.java
                    ---|AccountEntityRepository.java
                    ---|AccountProjector.java
 
 
 
```
package com.ywl.study.axon.account.query;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
@Data
@Entity(name="tb_account")
public class AccountEntity {
    @Id
    private String accountId;

    /*余额*/
    private Double deposit;

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

```

```
public interface AccountEntityRepository extends JpaRepository<AccountEntity,String> {
}
```

 
```
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

```

 
                
**更改聚合对象Account**

```
@Aggregate
//@Entity(name = "tb_account")
public class Account {
    private static final Logger LOG= LoggerFactory.getLogger(Account.class);
    /*聚合对象的ID,用@AggregateIdentifier来标志，如果有@Id,则axon框架会将标注有@Id的字段作为聚合对象ID*/
//    @Id
    @AggregateIdentifier
    private String accountId;
    //其他地方和之前一样
```

启动项目后
    
    
    curl -X PUT http://localhost:8080/account/90278353-0a37-4c17-9d60-4598ad839db1/withdraw/100


```
Account       : 处理取款commandhandler操作
Account       : 处理取款EventSourcingHandler操作
AccountProjector   : 处理取款操作
```



