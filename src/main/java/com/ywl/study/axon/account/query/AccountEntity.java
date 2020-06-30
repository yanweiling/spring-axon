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
