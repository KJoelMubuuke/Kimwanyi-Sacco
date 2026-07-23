package org.pahappa.kimwanyi.dao;

import org.hibernate.Session;
import org.pahappa.kimwanyi.model.Transaction;
import org.pahappa.kimwanyi.util.HibernateUtil;

import java.util.List;

public class TransactionDAO {

    public void save(Transaction transaction) {
        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(transaction);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public List<Transaction> findByAccountId(Long accountId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Transaction t WHERE t.savingsAccount.id = :accountId ORDER BY t.createdAt DESC", Transaction.class)
                    .setParameter("accountId", accountId)
                    .list();
        }
    }

    public List<Transaction> findByMemberId(Long memberId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Transaction t WHERE t.savingsAccount.member.id = :memberId ORDER BY t.createdAt DESC", Transaction.class)
                    .setParameter("memberId", memberId)
                    .list();
        }
    }
}