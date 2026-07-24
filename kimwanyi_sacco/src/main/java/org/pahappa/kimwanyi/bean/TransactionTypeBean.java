package org.pahappa.kimwanyi.bean;

import org.pahappa.kimwanyi.dao.TransactionTypeDAO;
import org.pahappa.kimwanyi.model.TransactionType;
import org.pahappa.kimwanyi.service.AuditLogService;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "transactionTypeBean")
@ViewScoped
public class TransactionTypeBean implements Serializable {

    private final TransactionTypeDAO transactionTypeDAO = new TransactionTypeDAO();
    private List<TransactionType> transactionTypes;
    private TransactionType newType = new TransactionType();

    public TransactionTypeBean() {
        loadTypes();
    }

    private void loadTypes() {
        transactionTypes = transactionTypeDAO.findAll();
    }

    public void addType() {
        try {
            transactionTypeDAO.save(newType);
            AuditLogService.log("Transaction Type Added", newType.getName(), "Admin", "A", "SUCCESSFUL");
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Transaction type added successfully.", null));
            newType = new TransactionType(); // reset
            loadTypes();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error adding transaction type: " + e.getMessage(), null));
        }
    }

    public void deleteType(Long id) {
        try {
            transactionTypeDAO.delete(id);
            AuditLogService.log("Transaction Type Deleted", "ID: " + id, "Admin", "A", "INFO");
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Transaction type deleted successfully.", null));
            loadTypes();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error deleting transaction type.", null));
        }
    }

    public List<TransactionType> getTransactionTypes() { return transactionTypes; }
    public TransactionType getNewType() { return newType; }
    public void setNewType(TransactionType newType) { this.newType = newType; }
}
