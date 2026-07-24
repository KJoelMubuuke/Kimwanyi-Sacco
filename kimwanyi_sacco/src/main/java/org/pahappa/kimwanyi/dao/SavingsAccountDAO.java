package org.pahappa.kimwanyi.dao;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pahappa.kimwanyi.model.SavingsAccount;
import org.pahappa.kimwanyi.util.HibernateUtil;
import java.util.List;
public class SavingsAccountDAO {
    public void save(SavingsAccount account) {
        Transaction tx = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            tx = session.beginTransaction();
            session.persist(account);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }
    public SavingsAccount findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(SavingsAccount.class, id);
        }
    }
    public SavingsAccount findByMemberId(Long memberId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM SavingsAccount s WHERE s.member.id = :memberId", SavingsAccount.class)
                    .setParameter("memberId", memberId)
                    .uniqueResult();
        }
    }
    public List<SavingsAccount> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM SavingsAccount", SavingsAccount.class).list();
        }
    }
    public void update(SavingsAccount account) {
        Transaction tx = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            tx = session.beginTransaction();
            session.merge(account);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }
}