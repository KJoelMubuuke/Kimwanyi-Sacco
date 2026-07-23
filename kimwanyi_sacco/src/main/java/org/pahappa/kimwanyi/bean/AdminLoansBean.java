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
import java.util.List;

@ManagedBean(name = "adminLoansBean")
@ViewScoped
public class AdminLoansBean implements Serializable {

    @ManagedProperty(value = "#{loginBean}")
    private LoginBean loginBean;

    private final LoanService loanService = new LoanService();
    private final LoanApplicationDAO loanApplicationDAO = new LoanApplicationDAO();
    private final LoanDAO loanDAO = new LoanDAO();

    private String rejectionReason;
    // Used to hold selected application id for rejection
    private Long selectedApplicationId;

    public List<LoanApplication> getPendingApplications() {
        return loanApplicationDAO.findPending();
    }

    public List<Loan> getAllLoans() {
        return loanDAO.findAll();
    }

    public String approve(Long applicationId) {
        try {
            Long adminId = loginBean.getCurrentUser().getUserId();
            loanService.approveLoan(applicationId, adminId);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Loan application approved.", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
        return null;
    }

    public String reject() {
        try {
            Long adminId = loginBean.getCurrentUser().getUserId();
            loanService.rejectLoan(selectedApplicationId, adminId, rejectionReason);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Loan application rejected.", null));
            rejectionReason = null;
            selectedApplicationId = null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
        return null;
    }

    // Getters & Setters
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Long getSelectedApplicationId() { return selectedApplicationId; }
    public void setSelectedApplicationId(Long selectedApplicationId) { this.selectedApplicationId = selectedApplicationId; }

    public void setLoginBean(LoginBean loginBean) { this.loginBean = loginBean; }
}
