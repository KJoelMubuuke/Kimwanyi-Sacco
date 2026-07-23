package org.pahappa.kimwanyi.bean;

import org.pahappa.kimwanyi.service.AccountService;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;

@ManagedBean(name = "savingsBean")
@ViewScoped
public class SavingsBean implements Serializable {

    @ManagedProperty(value = "#{loginBean}")
    private LoginBean loginBean;

    private final AccountService accountService = new AccountService();

    private BigDecimal depositAmount;
    private String depositDescription;

    private BigDecimal withdrawAmount;
    private String withdrawDescription;

    public String deposit() {
        try {
            Long memberId = loginBean.getCurrentUser().getUserId();
            accountService.deposit(memberId, depositAmount, depositDescription);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Deposit of UGX " + depositAmount + " successful.", null));
            depositAmount = null;
            depositDescription = null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
        return null; // stay on same page
    }

    public String withdraw() {
        try {
            Long memberId = loginBean.getCurrentUser().getUserId();
            accountService.withdraw(memberId, withdrawAmount, withdrawDescription);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Withdrawal of UGX " + withdrawAmount + " successful.", null));
            withdrawAmount = null;
            withdrawDescription = null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
        return null;
    }

    // Getters & Setters
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }

    public String getDepositDescription() { return depositDescription; }
    public void setDepositDescription(String depositDescription) { this.depositDescription = depositDescription; }

    public BigDecimal getWithdrawAmount() { return withdrawAmount; }
    public void setWithdrawAmount(BigDecimal withdrawAmount) { this.withdrawAmount = withdrawAmount; }

    public String getWithdrawDescription() { return withdrawDescription; }
    public void setWithdrawDescription(String withdrawDescription) { this.withdrawDescription = withdrawDescription; }

    public void setLoginBean(LoginBean loginBean) { this.loginBean = loginBean; }
}
