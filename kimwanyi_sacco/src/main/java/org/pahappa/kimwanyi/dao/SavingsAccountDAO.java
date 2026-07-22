package org.pahappa.kimwanyi.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pahappa.kimwanyi.model.SavingsAccount;
import org.pahappa.kimwanyi.util.HibernateUtil;

public class SavingsAccountDAO {

    public void save(SavingsAccount account) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(account);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public SavingsAccount findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(SavingsAccount.class, id);
        }
    }

    public SavingsAccount findByMemberId(Long memberId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM SavingsAccount WHERE member.id = :memberId", SavingsAccount.class)
                    .setParameter("memberId", memberId)
                    .uniqueResult();
        }
    }

    public void update(SavingsAccount account) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(account);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}