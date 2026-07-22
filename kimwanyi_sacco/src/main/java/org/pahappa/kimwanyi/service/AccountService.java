package org.pahappa.kimwanyi.service;

import org.hibernate.Session;
import org.pahappa.kimwanyi.model.SavingsAccount;
import org.pahappa.kimwanyi.model.Transaction;
import org.pahappa.kimwanyi.util.HibernateUtil;

import java.math.BigDecimal;

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

    /**
     * @throws IllegalArgumentException if amount is not positive
     * @throws IllegalStateException if no savings account exists for the member
     */
    public Transaction deposit(Long memberId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            SavingsAccount account = session.createQuery(
                            "FROM SavingsAccount WHERE member.id = :memberId", SavingsAccount.class)
                    .setParameter("memberId", memberId)
                    .uniqueResult();

            if (account == null) {
                throw new IllegalStateException("No savings account found for member " + memberId);
            }

            BigDecimal newBalance = account.getBalance().add(amount);
            account.setBalance(newBalance);
            // account.getVersion() is bumped automatically by Hibernate on
            // this UPDATE via @Version - if another transaction changed
            // this same row since it was read, this commit throws
            // OptimisticLockException instead of silently overwriting it.
            session.merge(account);

            Transaction transaction = new Transaction();
            transaction.setSavingsAccount(account);
            transaction.setType("DEPOSIT");
            transaction.setAmount(amount);
            transaction.setBalanceAfter(newBalance);
            transaction.setDescription(description);
            session.persist(transaction);

            tx.commit();
            return transaction;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
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
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            SavingsAccount account = session.createQuery(
                            "FROM SavingsAccount WHERE member.id = :memberId", SavingsAccount.class)
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
            return transaction;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public BigDecimal getBalance(Long memberId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            SavingsAccount account = session.createQuery(
                            "FROM SavingsAccount WHERE member.id = :memberId", SavingsAccount.class)
                    .setParameter("memberId", memberId)
                    .uniqueResult();
            if (account == null) {
                throw new IllegalStateException("No savings account found for member " + memberId);
            }
            return account.getBalance();
        }
    }
}