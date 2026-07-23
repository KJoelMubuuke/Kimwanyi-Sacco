package org.pahappa.kimwanyi.bean;

import org.pahappa.kimwanyi.dao.LoanApplicationDAO;
import org.pahappa.kimwanyi.dao.LoanDAO;
import org.pahappa.kimwanyi.model.Loan;
import org.pahappa.kimwanyi.model.LoanApplication;
import org.pahappa.kimwanyi.service.LoanService;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@ManagedBean(name = "loanBean")
@ViewScoped
public class LoanBean implements Serializable {

    @ManagedProperty(value = "#{loginBean}")
    private LoginBean loginBean;

    private final LoanService loanService = new LoanService();
    private final LoanApplicationDAO loanApplicationDAO = new LoanApplicationDAO();
    private final LoanDAO loanDAO = new LoanDAO();

    private BigDecimal requestedAmount;
    private String reason;
    private BigDecimal repayAmount;

    /** Apply for a loan. */
    public String applyForLoan() {
        try {
            Long memberId = loginBean.getCurrentUser().getUserId();
            loanService.applyForLoan(memberId, requestedAmount, reason);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Loan application submitted successfully. Awaiting admin review.", null));
            requestedAmount = null;
            reason = null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
        return null;
    }

    /** Repay active loan. */
    public String repayLoan() {
        try {
            Long memberId = loginBean.getCurrentUser().getUserId();
            Loan activeLoan = loanDAO.findActiveLoanByMemberId(memberId);
            if (activeLoan == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "You have no active loan to repay.", null));
                return null;
            }
            loanService.repay(activeLoan.getId(), repayAmount);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Repayment of UGX " + repayAmount + " recorded.", null));
            repayAmount = null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
        return null;
    }

    public List<LoanApplication> getMyApplications() {
        return loanApplicationDAO.findByMemberId(loginBean.getCurrentUser().getUserId());
    }

    public Loan getMyActiveLoan() {
        return loanDAO.findActiveLoanByMemberId(loginBean.getCurrentUser().getUserId());
    }

    // Getters & Setters
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public BigDecimal getRepayAmount() { return repayAmount; }
    public void setRepayAmount(BigDecimal repayAmount) { this.repayAmount = repayAmount; }

    public void setLoginBean(LoginBean loginBean) { this.loginBean = loginBean; }
}
