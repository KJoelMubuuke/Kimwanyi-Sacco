package org.pahappa.kimwanyi.bean;

import org.pahappa.kimwanyi.dao.MemberDAO;
import org.pahappa.kimwanyi.dao.SavingsAccountDAO;
import org.pahappa.kimwanyi.dao.TransactionDAO;
import org.pahappa.kimwanyi.dao.TransactionTypeDAO;
import org.pahappa.kimwanyi.model.Member;
import org.pahappa.kimwanyi.model.SavingsAccount;
import org.pahappa.kimwanyi.model.Transaction;
import org.pahappa.kimwanyi.model.TransactionType;
import org.pahappa.kimwanyi.service.AuditLogService;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@ManagedBean(name = "creditAccountBean")
@ViewScoped
public class CreditAccountBean implements Serializable {

    private final MemberDAO memberDAO = new MemberDAO();
    private final SavingsAccountDAO savingsAccountDAO = new SavingsAccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final TransactionTypeDAO transactionTypeDAO = new TransactionTypeDAO();

    private String searchQuery;
    private List<Member> searchResults;
    private Member selectedMember;
    private SavingsAccount selectedAccount;

    private List<TransactionType> availableTransactionTypes;
    private String selectedTransactionTypeName;

    private BigDecimal amount;
    private BigDecimal charge;
    private String receiptNo;

    private List<Transaction> recentTransactions;

    public CreditAccountBean() {
        availableTransactionTypes = transactionTypeDAO.findAll();
    }

    public void searchMember() {
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            searchResults = memberDAO.searchByName(searchQuery.trim());
        } else {
            searchResults = null;
        }
    }

    public void selectMember(Member member) {
        this.selectedMember = member;
        this.searchResults = null;
        this.searchQuery = member.getFullName();
        // Load account separately to avoid LazyInitializationException
        this.selectedAccount = savingsAccountDAO.findByMemberId(member.getId());
        loadRecentTransactions();
    }

    private void loadRecentTransactions() {
        if (selectedAccount != null) {
            recentTransactions = transactionDAO.findByAccountId(selectedAccount.getId());
        }
    }

    public void processTransaction() {
        if (selectedMember == null) {
            addError("Please select a member first.");
            return;
        }
        if (selectedAccount == null) {
            addError("No savings account found for this member.");
            return;
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            addError("Amount must be greater than zero.");
            return;
        }

        TransactionType type = availableTransactionTypes.stream()
                .filter(tt -> tt.getName().equals(selectedTransactionTypeName))
                .findFirst().orElse(null);

        boolean isWithdrawal = (type != null && "Reduction".equalsIgnoreCase(type.getAccountEffect()))
                || "Withdraw".equalsIgnoreCase(selectedTransactionTypeName)
                || "Withdrawal".equalsIgnoreCase(selectedTransactionTypeName);

        org.hibernate.Transaction tx = null;
        org.hibernate.Session session = org.pahappa.kimwanyi.util.HibernateUtil.getSessionFactory().openSession();
        try {
            tx = session.beginTransaction();

            SavingsAccount acc = session.get(SavingsAccount.class, selectedAccount.getId());
            BigDecimal chargeVal = charge != null ? charge : BigDecimal.ZERO;

            if (isWithdrawal) {
                BigDecimal totalDeduction = amount.add(chargeVal);
                if (acc.getBalance().compareTo(totalDeduction) < 0) {
                    throw new IllegalArgumentException("Insufficient balance for withdrawal + charge.");
                }
                acc.setBalance(acc.getBalance().subtract(totalDeduction));
            } else {
                acc.setBalance(acc.getBalance().add(amount).subtract(chargeVal));
            }

            Transaction t = new Transaction();
            t.setSavingsAccount(acc);
            t.setType(selectedTransactionTypeName);
            t.setAmount(amount);
            t.setCharge(chargeVal);
            t.setReceiptNo(receiptNo);
            t.setManager("Admin");
            t.setBalanceAfter(acc.getBalance());
            t.setDescription(selectedTransactionTypeName + " processed by admin.");

            session.merge(acc);
            session.persist(t);
            tx.commit();

            AuditLogService.log("Admin Transaction: " + selectedTransactionTypeName,
                    "UGX " + amount + " for " + selectedMember.getFullName(),
                    "Admin", "A", "SUCCESSFUL");

            addInfo("Transaction processed successfully.");
            amount = null;
            charge = null;
            receiptNo = null;
            selectedMember = memberDAO.findById(selectedMember.getId());
            selectedAccount = savingsAccountDAO.findByMemberId(selectedMember.getId());
            loadRecentTransactions();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            addError("Transaction failed: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    private void addInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }

    public List<Member> getSearchResults() { return searchResults; }
    public Member getSelectedMember() { return selectedMember; }
    public SavingsAccount getSelectedAccount() { return selectedAccount; }
    public List<TransactionType> getAvailableTransactionTypes() { return availableTransactionTypes; }

    public String getSelectedTransactionTypeName() { return selectedTransactionTypeName; }
    public void setSelectedTransactionTypeName(String v) { this.selectedTransactionTypeName = v; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getCharge() { return charge; }
    public void setCharge(BigDecimal charge) { this.charge = charge; }

    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }

    public List<Transaction> getRecentTransactions() { return recentTransactions; }
}
