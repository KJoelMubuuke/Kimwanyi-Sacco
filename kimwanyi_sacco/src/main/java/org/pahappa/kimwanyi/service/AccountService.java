package org.pahappa.kimwanyi.service;

import org.hibernate.Session;
import org.pahappa.kimwanyi.model.SavingsAccount;
import org.pahappa.kimwanyi.model.Transaction;
import org.pahappa.kimwanyi.service.AuditLogService;
import org.pahappa.kimwanyi.util.HibernateUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

/**
 * Deposit and withdrawal logic for a member's savings account.
 *
 * Both operations run inside a SINGLE Hibernate session/transaction that
 * updates SavingsAccount AND inserts the Transaction row together.
 * This is deliberate, not incidental: SavingsAccountDAO.update() and
 * TransactionDAO.save() each open and commit their own separate session,
 * so calling them back-to-back from here would NOT be atomic - a failure
 * between the two calls could leave the balance changed with no ledger
 * entry explaining why. Using one session here guarantees both writes
 * commit together or neither does.
 *
 * The minimum balance is a business rule, not a DB constraint - the
 * savings_accounts table has no minimum_balance column - so it's kept
 * here as a constant.
 */
public class AccountService {

    private static final BigDecimal MINIMUM_BALANCE = new BigDecimal("20000.00");
    private static final BigDecimal ANNUAL_INTEREST_RATE = new BigDecimal("0.05");

    /**
     * @throws IllegalArgumentException if amount is not positive
     * @throws IllegalStateException if no savings account exists for the member
     */
    public Transaction deposit(Long memberId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        org.hibernate.Transaction tx = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            tx = session.beginTransaction();

            SavingsAccount account = session.createQuery(
                            "FROM SavingsAccount s WHERE s.member.id = :memberId", SavingsAccount.class)
                    .setParameter("memberId", memberId)
                    .uniqueResult();

            if (account == null) {
                throw new IllegalStateException("No savings account found for member " + memberId);
            }

            BigDecimal newBalance = account.getBalance().add(amount);
            account.setBalance(newBalance);
            session.merge(account);

            Transaction transaction = new Transaction();
            transaction.setSavingsAccount(account);
            transaction.setType("DEPOSIT");
            transaction.setAmount(amount);
            transaction.setBalanceAfter(newBalance);
            transaction.setDescription(description);
            session.persist(transaction);

            tx.commit();
            AuditLogService.log("Deposit",
                    "UGX " + amount + " to account " + account.getId(),
                    account.getMember() != null ? account.getMember().getFullName() : "Member",
                    "M", "SUCCESSFUL");
            return transaction;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    /**
     * @throws IllegalArgumentException if amount is not positive, or if the
     *         withdrawal would take the balance below the 20,000 minimum
     * @throws IllegalStateException if no savings account exists for the member
     */
    public Transaction withdraw(Long memberId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }

        org.hibernate.Transaction tx = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            tx = session.beginTransaction();

            SavingsAccount account = session.createQuery(
                            "FROM SavingsAccount s WHERE s.member.id = :memberId", SavingsAccount.class)
                    .setParameter("memberId", memberId)
                    .uniqueResult();

            if (account == null) {
                throw new IllegalStateException("No savings account found for member " + memberId);
            }

            BigDecimal newBalance = account.getBalance().subtract(amount);
            if (newBalance.compareTo(MINIMUM_BALANCE) < 0) {
                throw new IllegalArgumentException(
                        "Withdrawal would take balance below the required minimum of " + MINIMUM_BALANCE);
            }

            account.setBalance(newBalance);
            session.merge(account);

            Transaction transaction = new Transaction();
            transaction.setSavingsAccount(account);
            transaction.setType("WITHDRAWAL");
            transaction.setAmount(amount);
            transaction.setBalanceAfter(newBalance);
            transaction.setDescription(description);
            session.persist(transaction);

            tx.commit();
            AuditLogService.log("Withdrawal",
                    "UGX " + amount + " from account " + account.getId(),
                    account.getMember() != null ? account.getMember().getFullName() : "Member",
                    "M", "SUCCESSFUL");
            return transaction;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public BigDecimal getBalance(Long memberId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            SavingsAccount account = session.createQuery(
                            "FROM SavingsAccount s WHERE s.member.id = :memberId", SavingsAccount.class)
                    .setParameter("memberId", memberId)
                    .uniqueResult();
            if (account == null) {
                throw new IllegalStateException("No savings account found for member " + memberId);
            }
            return account.getBalance();
        }
    }

    /**
     * Savings interest per the brief: "5% per annum applied monthly."
     * LIMITATION: the schema has no column tracking when interest was last
     * posted, so this checks the transactions table for an existing interest
     * posting this calendar month as a workaround, not a schema-level guarantee.
     *
     * @throws IllegalStateException if no account exists, or interest was
     *         already posted to it this month
     */
    public Transaction applyMonthlyInterest(Long memberId) {
        org.hibernate.Transaction tx = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            tx = session.beginTransaction();

            SavingsAccount account = session.createQuery(
                            "FROM SavingsAccount s WHERE s.member.id = :memberId", SavingsAccount.class)
                    .setParameter("memberId", memberId)
                    .uniqueResult();
            if (account == null) {
                throw new IllegalStateException("No savings account found for member " + memberId);
            }

            YearMonth thisMonth = YearMonth.now();
            LocalDateTime monthStart = thisMonth.atDay(1).atStartOfDay();
            LocalDateTime monthEnd = thisMonth.plusMonths(1).atDay(1).atStartOfDay();
            String interestDescription = "Monthly savings interest";

            Long alreadyPosted = session.createQuery(
                            "SELECT COUNT(t) FROM Transaction t WHERE t.savingsAccount.id = :accountId " +
                                    "AND t.description = :desc AND t.createdAt >= :start AND t.createdAt < :end",
                            Long.class)
                    .setParameter("accountId", account.getId())
                    .setParameter("desc", interestDescription)
                    .setParameter("start", monthStart)
                    .setParameter("end", monthEnd)
                    .uniqueResult();
            if (alreadyPosted != null && alreadyPosted > 0) {
                throw new IllegalStateException("Interest already posted for " + thisMonth);
            }

            BigDecimal monthlyRate = ANNUAL_INTEREST_RATE.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
            BigDecimal interestAmount = account.getBalance().multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal newBalance = account.getBalance().add(interestAmount);

            account.setBalance(newBalance);
            session.merge(account);

            Transaction interestTxn = new Transaction();
            interestTxn.setSavingsAccount(account);
            interestTxn.setType("DEPOSIT");
            interestTxn.setAmount(interestAmount);
            interestTxn.setBalanceAfter(newBalance);
            interestTxn.setDescription(interestDescription);
            session.persist(interestTxn);

            tx.commit();
            return interestTxn;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    /**
     * Account statement: full transaction history for a member, newest first.
     * Reuses TransactionDAO's existing query rather than duplicating it.
     */
    public List<Transaction> getStatement(Long memberId) {
        return new org.pahappa.kimwanyi.dao.TransactionDAO().findByMemberId(memberId);
    }
}