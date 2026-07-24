package org.pahappa.kimwanyi.service;

import org.hibernate.Session;
import org.pahappa.kimwanyi.dao.AdminDAO;
import org.pahappa.kimwanyi.dao.LoanApplicationDAO;
import org.pahappa.kimwanyi.dao.LoanRepaymentDAO;
import org.pahappa.kimwanyi.model.Admin;
import org.pahappa.kimwanyi.model.Loan;
import org.pahappa.kimwanyi.model.LoanApplication;
import org.pahappa.kimwanyi.model.LoanRepayment;
import org.pahappa.kimwanyi.model.Member;
import org.pahappa.kimwanyi.service.AuditLogService;
import org.pahappa.kimwanyi.util.HibernateUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Loan application, approval, rejection and repayment.
 *
 * Same reasoning as AccountService: approveLoan() touches two tables
 * (loan_applications AND loans) and must do it in one Hibernate session,
 * not by chaining LoanApplicationDAO.update() + a separate save of Loan -
 * otherwise a failure between the two could approve an application with
 * no loan actually created, or the reverse.
 *
 * repayLoan() is NOT reimplemented here - LoanRepaymentDAO.recordPayment()
 * already does the active-check, remaining-balance check, and auto-flip
 * to REPAID atomically in one session. This just calls it.
 */
public class LoanService {

    private static final BigDecimal INTEREST_RATE = new BigDecimal("0.10"); // flat 10%
    private static final BigDecimal MAX_LOAN_MULTIPLIER = new BigDecimal("3");

    private final LoanApplicationDAO loanApplicationDAO = new LoanApplicationDAO();
    private final LoanRepaymentDAO loanRepaymentDAO = new LoanRepaymentDAO();
    private final AdminDAO adminDAO = new AdminDAO();
    private final AccountService accountService = new AccountService();

    /**
     * @throws IllegalArgumentException if amount is not positive, or exceeds
     *         3x the member's current savings balance
     * @throws IllegalStateException if the member already has a pending
     *         application or an active loan
     */
    public LoanApplication applyForLoan(Long memberId, BigDecimal requestedAmount, String reason) {
        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Requested amount must be greater than zero");
        }

        if (loanApplicationDAO.hasPendingOrActive(memberId)) {
            throw new IllegalStateException(
                    "Member already has a pending application or an active loan - must be resolved first");
        }

        BigDecimal savingsBalance = accountService.getBalance(memberId);
        BigDecimal maxAllowed = savingsBalance.multiply(MAX_LOAN_MULTIPLIER);
        if (requestedAmount.compareTo(maxAllowed) > 0) {
            throw new IllegalArgumentException(
                    "Requested amount exceeds the maximum allowed (3x savings balance = " + maxAllowed + ")");
        }

        LoanApplication application = new LoanApplication();
        // LoanApplication needs a Member reference, not just an id, since
        // it's a @ManyToOne. A detached Member with only the id set is
        // enough for Hibernate to persist the FK correctly.
        Member memberRef = new Member();
        memberRef.setId(memberId);
        application.setMember(memberRef);
        application.setRequestedAmount(requestedAmount);
        application.setReason(reason);
        application.setStatus("PENDING");

        loanApplicationDAO.save(application);
        return application;
    }

    /**
     * Approves a PENDING application and creates the corresponding Loan
     * in the same transaction.
     *
     * @throws IllegalStateException if the application doesn't exist or
     *         isn't PENDING
     */
    public Loan approveLoan(Long applicationId, Long adminId) {
        org.hibernate.Transaction tx = null;
        Admin admin = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            tx = session.beginTransaction();

            LoanApplication application = session.get(LoanApplication.class, applicationId);
            if (application == null) {
                throw new IllegalStateException("Loan application not found: " + applicationId);
            }
            if (!"PENDING".equals(application.getStatus())) {
                throw new IllegalStateException("Only PENDING applications can be approved");
            }

            admin = session.get(Admin.class, adminId);
            if (admin == null) {
                throw new IllegalStateException("Admin not found: " + adminId);
            }

            LocalDateTime now = LocalDateTime.now();
            application.setStatus("APPROVED");
            application.setReviewedAt(now);
            application.setReviewedBy(admin);
            session.merge(application);

            BigDecimal principal = application.getRequestedAmount();
            BigDecimal interestAmount = principal.multiply(INTEREST_RATE);
            BigDecimal totalRepayable = principal.add(interestAmount);

            Loan loan = new Loan();
            loan.setLoanApplication(application);
            loan.setMember(application.getMember());
            loan.setPrincipal(principal);
            loan.setInterestAmount(interestAmount);
            loan.setTotalRepayable(totalRepayable);
            loan.setRemainingBalance(totalRepayable);
            loan.setStatus("ACTIVE");
            loan.setApprovedBy(admin);
            loan.setApprovedAt(now);
            session.persist(loan);

            tx.commit();
            AuditLogService.log("Loan Approved",
                    "Application #" + applicationId + " | Amount: UGX " + principal,
                    admin.getFullName(), "A", "SUCCESSFUL");
            return loan;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            AuditLogService.log("Loan Approval FAILED", "Application #" + applicationId,
                    admin != null ? admin.getFullName() : "Admin", "A", "FAILED");
            throw e;
        } finally {
            session.close();
        }
    }

    /**
     * @throws IllegalStateException if the application doesn't exist or
     *         isn't PENDING
     */
    public LoanApplication rejectLoan(Long applicationId, Long adminId, String rejectionReason) {
        LoanApplication application = loanApplicationDAO.findById(applicationId);
        if (application == null) {
            throw new IllegalStateException("Loan application not found: " + applicationId);
        }
        if (!"PENDING".equals(application.getStatus())) {
            throw new IllegalStateException("Only PENDING applications can be rejected");
        }

        Admin admin = adminDAO.findById(adminId);
        if (admin == null) {
            throw new IllegalStateException("Admin not found: " + adminId);
        }

        application.setStatus("REJECTED");
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewedBy(admin);
        application.setRejectionReason(rejectionReason);

        loanApplicationDAO.update(application);
        AuditLogService.log("Loan Rejected",
                "Application #" + applicationId + " | Reason: " + rejectionReason,
                admin.getFullName(), "A", "INFO");
        return application;
    }

    /**
     * Delegates straight to LoanRepaymentDAO.recordPayment(), which already
     * handles the active-check, remaining-balance validation, and auto-flip
     * to REPAID atomically - no need to duplicate that logic here.
     */
    public LoanRepayment repay(Long loanId, BigDecimal amount) {
        return loanRepaymentDAO.recordPayment(loanId, amount);
    }
}