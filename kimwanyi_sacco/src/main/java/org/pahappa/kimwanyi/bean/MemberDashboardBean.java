package org.pahappa.kimwanyi.bean;

import org.pahappa.kimwanyi.model.Transaction;
import org.pahappa.kimwanyi.service.AccountService;
import org.pahappa.kimwanyi.service.AuthResult;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@ManagedBean(name = "memberDashboardBean")
@ViewScoped
public class MemberDashboardBean implements Serializable {

    @ManagedProperty(value = "#{loginBean}")
    private LoginBean loginBean;

    private final AccountService accountService = new AccountService();

    public BigDecimal getBalance() {
        return accountService.getBalance(loginBean.getCurrentUser().getUserId());
    }

    public List<Transaction> getStatement() {
        return accountService.getStatement(loginBean.getCurrentUser().getUserId());
    }

    public void setLoginBean(LoginBean loginBean) {
        this.loginBean = loginBean;
    }
}