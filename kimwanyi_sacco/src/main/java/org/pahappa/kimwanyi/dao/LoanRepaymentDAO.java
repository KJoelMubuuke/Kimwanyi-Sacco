package org.pahappa.kimwanyi.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pahappa.kimwanyi.model.Loan;
import org.pahappa.kimwanyi.model.LoanRepayment;
import org.pahappa.kimwanyi.util.HibernateUtil;

import java.math.BigDecimal;
import java.util.List;

public class LoanRepaymentDAO {

    public void save(LoanRepayment repayment) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(repayment);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public LoanRepayment recordPayment(Long loanId, BigDecimal amount) {
        if (loanId == null) {
            throw new IllegalArgumentException("loanId is required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Repayment amount must be greater than zero");
        }

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Loan loan = session.get(Loan.class, loanId);
            if (loan == null) {
                throw new IllegalArgumentException("Loan not found for id " + loanId);
            }
            if (!"ACTIVE".equals(loan.getStatus())) {
                throw new IllegalStateException("Only active loans can receive repayments");
            }
            if (amount.compareTo(loan.getRemainingBalance()) > 0) {
                throw new IllegalArgumentException("Repayment amount cannot exceed the remaining loan balance");
            }

            BigDecimal remainingBalance = loan.getRemainingBalance().subtract(amount);
            loan.setRemainingBalance(remainingBalance);
            if (remainingBalance.compareTo(BigDecimal.ZERO) == 0) {
                loan.setStatus("REPAID");
            }

            LoanRepayment repayment = new LoanRepayment();
            repayment.setLoan(loan);
            repayment.setAmount(amount);

            session.persist(repayment);
            session.merge(loan);
            tx.commit();
            return repayment;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public LoanRepayment findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(LoanRepayment.class, id);
        }
    }

    public List<LoanRepayment> findByLoanId(Long loanId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM LoanRepayment r WHERE r.loan.id = :loanId ORDER BY r.paidAt DESC", LoanRepayment.class)
                    .setParameter("loanId", loanId)
                    .list();
        }
    }
}
